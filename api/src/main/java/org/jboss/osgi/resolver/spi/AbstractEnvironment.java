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
package org.jboss.osgi.resolver.spi;

import org.jboss.osgi.resolver.NotImplementedException;
import org.jboss.osgi.resolver.XEnvironment;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The abstract implementation of a {@link XEnvironment}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractEnvironment extends AbstractElement implements XEnvironment {

    private final List<Resource> resources = new ArrayList<Resource>();
    private final Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();

    @Override
    public Comparator<Capability> getComparator() {
        return new ResourceIndexComparator() {
            @Override
            protected long getResourceIndex(Resource res) {
                return resources.indexOf(res);
            }
        };
    }

    public void installResources(Resource... resarr) {
        if (resources == null)
            throw new IllegalArgumentException("Null resource");
        synchronized (resources) {
            for(Resource res : resarr) {
                if (resources.contains(res))
                    throw new IllegalArgumentException("Resource already installed: " + res);
                resources.add(res);
            }
        }
    }

    public void uninstallResources(Resource... resarr) {
        synchronized (resources) {
            for(Resource res : resarr) {
                resources.remove(res);
            }
        }
    }

    public long getResourceIndex(Resource resource) {
        return resources.indexOf(resource);
    }

    @Override
    public SortedSet<Capability> findProviders(Requirement req) {
        SortedSet<Capability> result = new TreeSet<Capability>(getComparator());
        synchronized (resources) {
            for (Resource res : resources) {
                for (Capability cap : res.getCapabilities(req.getNamespace())) {
                    if (req instanceof BundleRequirement) {
                        if (((BundleRequirement)req).matches((BundleCapability) cap)) {
                            result.add(cap);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map<Requirement, SortedSet<Capability>> findProviders(Collection<? extends Requirement> requirements) {
        throw new NotImplementedException();
    }

    @Override
    public Map<Resource, Wiring> applyResolverResults(Map<Resource, List<Wire>> wiremap) {
        Map<Resource, Wiring> result = new HashMap<Resource, Wiring>();
        synchronized (wirings) {
            for (Map.Entry<Resource, List<Wire>> entry : wiremap.entrySet()) {
                Resource res = entry.getKey();
                List<Wire> wires = entry.getValue();
                AbstractWiring reqwiring = (AbstractWiring) getWiring(result, res);
                reqwiring.addRequiredWires(wires);
                for (Wire wire : wires) {
                    Resource provider = wire.getProvider();
                    AbstractWiring provwiring = (AbstractWiring) getWiring(result, provider);
                    provwiring.addProvidedWire(wire);
                }
            }
            for (Map.Entry<Resource, Wiring> entry : result.entrySet()) {
                Resource res = entry.getKey();
                Wiring delta = entry.getValue();
                AbstractWiring wiring = (AbstractWiring) wirings.get(res);
                if (wiring == null) {
                    wirings.put(res, delta);
                } else {
                    for (Wire wire : delta.getProvidedResourceWires(null)) {
                        wiring.addProvidedWire(wire);
                    }
                }
            }
        }
        return result;
    }

    private Wiring getWiring(Map<Resource, Wiring> result, Resource requirer) {
        Wiring wiring = result.get(requirer);
        if (wiring == null) {
            wiring = new AbstractWiring(requirer);
            result.put(requirer, wiring);
        }
        return wiring;
    }

    @Override
    public boolean isEffective(Requirement req) {
        return true;
    }

    @Override
    public Wiring getWiring(Resource resource) {
        synchronized (wirings) {
            return wirings.get(resource);
        }
    }

    @Override
    public Map<Resource, Wiring> getWirings() {
        synchronized (wirings) {
            return Collections.unmodifiableMap(wirings);
        }
    }
}