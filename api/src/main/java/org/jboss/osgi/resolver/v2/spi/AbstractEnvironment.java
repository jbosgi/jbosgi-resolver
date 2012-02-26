/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.resolver.v2.spi;

import org.jboss.logging.Logger;
import org.jboss.osgi.resolver.v2.XEnvironment;
import org.jboss.osgi.resolver.v2.XIdentityCapability;
import org.jboss.osgi.resolver.v2.XResource;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;
import org.osgi.service.resolver.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_ATTRIBUTE;

/**
 * The abstract implementation of a {@link Environment}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractEnvironment implements XEnvironment {

    private static Logger log = Logger.getLogger(AbstractEnvironment.class);
    
    private final List<XResource> resources = new ArrayList<XResource>();
    private final Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();

    protected abstract Comparator<Capability> getComparator();

    @Override
    public synchronized void installResources(Resource... resarr) {
        for (Resource res : resarr) {
            if (resources.contains(res))
                throw new IllegalArgumentException("Resource already installed: " + res);
            
            log.debugf("Install resource: %s", res);
            resources.add((XResource) res);
        }
    }

    @Override
    public synchronized void uninstallResources(Resource... resarr) {
        for (Resource res : resarr) {
            log.debugf("Uninstall resource: %s", res);
            resources.remove(res);
            wirings.remove(res);
        }
    }

    @Override
    public void refreshResources(Resource... resarr) {
        for (Resource res : resarr) {
            wirings.remove(res);
        }
    }

    @Override
    public long getResourceIndex(Resource resource) {
        return resources.indexOf(resource);
    }

    @Override
    public Collection<Resource> getResources(String identityType) {
        Set<Resource> result = new HashSet<Resource>();
        for (XResource res : resources) {
            XIdentityCapability icap = res.getIdentityCapability();
            Object captype = icap.getAttribute(IDENTITY_TYPE_ATTRIBUTE);
            if (identityType.equals(captype)) {
                result.add(res);
            }
        }
        return result;
    }
    
    @Override
    public synchronized SortedSet<Capability> findProviders(Requirement req) {
        log.debugf("Find providers: %s", req);
        SortedSet<Capability> result = new TreeSet<Capability>(getComparator());
        for (Resource res : resources) {
            for (Capability cap : res.getCapabilities(req.getNamespace())) {
                if (req.matches(cap)) {
                    result.add(cap);
                }
            }
        }
        log.debugf("Found providers: %s", result);
        return result;
    }

    @Override
    public synchronized Map<Resource, Wiring> applyResolverResults(Map<Resource, List<Wire>> wiremap) {
        Map<Resource, Wiring> wirings = getWiringMap(wiremap);
        applyWiringMap(wirings);
        return wirings;
    }

    @Override
    public Wiring createWiring(Resource res, List<Wire> wires) {
        return new AbstractWiring(res, wires);
    }

    @Override
    public Wiring applyWiring(Resource res, Wiring wiring) {
        return wiring;
    }

    @Override
    public boolean isEffective(Requirement req) {
        return true;
    }

    @Override
    public synchronized Wiring getWiring(Resource resource) {
        return wirings.get(resource);
    }

    @Override
    public synchronized Map<Resource, Wiring> getWirings() {
        return Collections.unmodifiableMap(wirings);
    }

    private Map<Resource, Wiring> getWiringMap(Map<Resource, List<Wire>> wiremap) {
        Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
        for (Map.Entry<Resource, List<Wire>> entry : wiremap.entrySet()) {
            XResource res = (XResource) entry.getKey();
            List<Wire> wires = entry.getValue();
            AbstractWiring reqwiring = (AbstractWiring) wirings.get(res);
            if (reqwiring == null) {
                reqwiring = (AbstractWiring) createWiring(res, wires);
                wirings.put(res, reqwiring);
            }
            for (Wire wire : wires) {
                XResource provider = (XResource) wire.getProvider();
                AbstractWiring provwiring = (AbstractWiring) wirings.get(provider);
                if (provwiring == null) {
                    provwiring = (AbstractWiring) createWiring(provider, Collections.EMPTY_LIST);
                    wirings.put(provider, provwiring);
                }
                provwiring.addProvidedWire(wire);
            }
        }
        return wirings;
    }

    private void applyWiringMap(Map<Resource, Wiring> wirings) {
        for (Map.Entry<Resource, Wiring> entry : wirings.entrySet()) {
            XResource res = (XResource) entry.getKey();
            AbstractWiring deltaWiring = (AbstractWiring) entry.getValue();
            AbstractWiring wiring = (AbstractWiring) this.wirings.get(res);
            if (wiring == null) {
                this.wirings.put(res, deltaWiring);
                wiring = deltaWiring;
            } else {
                for (Wire wire : deltaWiring.getProvidedResourceWires(null)) {
                    wiring.addProvidedWire(wire);
                }
            }
            applyWiring(res, wiring);
        }
    }
}