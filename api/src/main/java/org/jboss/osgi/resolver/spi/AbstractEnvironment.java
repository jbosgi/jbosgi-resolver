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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.logging.Logger;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XWiring;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;
import org.osgi.service.resolver.Environment;

/**
 * The abstract implementation of a {@link Environment}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractEnvironment implements XEnvironment {

    private static Logger log = Logger.getLogger(AbstractEnvironment.class);

    private final AtomicLong resourceIndex = new AtomicLong();
    private final Map<CacheKey, Set<Capability>> capabilityCache = new ConcurrentHashMap<CacheKey, Set<Capability>>();
    private final Map<String, Set<Resource>> resourceCache = new ConcurrentHashMap<String, Set<Resource>>();
    private final Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();

    protected abstract Comparator<Capability> getComparator();

    @Override
    public synchronized void installResources(Resource... resarr) {
        for (Resource res : resarr) {
            XResource xres = (XResource) res;
            XIdentityCapability icap = xres.getIdentityCapability();
            if (getCachedCapabilities(CacheKey.create(icap)).contains(icap))
                throw new IllegalArgumentException("Resource already installed: " + res);

            log.debugf("Install resource: %s", res);

            // Attach the install index
            xres.addAttachment(Long.class, resourceIndex.getAndIncrement());
            // Add resource by type
            getCachedResources(icap.getType()).add(res);
            // Add resource capabilites
            for (Capability cap : res.getCapabilities(null)) {
                getCachedCapabilities(CacheKey.create(cap)).add(cap);
                log.debugf("   %s", cap);
            }
            if (log.isDebugEnabled()) {
                for (Requirement req : res.getRequirements(null)) {
                    log.debugf("   %s", req);
                }
            }
        }
    }

    @Override
    public synchronized void uninstallResources(Resource... resarr) {
        for (Resource res : resarr) {
            log.debugf("Uninstall resource: %s", res);
            XResource xres = (XResource) res;
            XIdentityCapability icap = xres.getIdentityCapability();
            // Remove resource by type
            getCachedResources(icap.getType()).remove(res);
            // Remove resource capabilities
            for (Capability cap : res.getCapabilities(null)) {
                CacheKey cachekey = CacheKey.create(cap);
                Set<Capability> capset = getCachedCapabilities(cachekey);
                capset.remove(cap);
                if (capset.isEmpty()) {
                    capabilityCache.remove(cachekey);
                }
            }
            // Remove cached wirings
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
    public long getResourceIndex(Resource res) {
        XResource xres = (XResource) res;
        return xres.getAttachment(Long.class);
    }

    @Override
    public Collection<Resource> getResources(String type) {
        return getCachedResources(type);
    }

    @Override
    public synchronized SortedSet<Capability> findProviders(Requirement req) {
        CacheKey cachekey = CacheKey.create(req);
        SortedSet<Capability> result = new TreeSet<Capability>(getComparator());
        for (Capability cap : getCachedCapabilities(cachekey)) {
            if (req.matches(cap)) {
                // Check if the package capability has been substituted
                boolean ignoreCapability = false;
                Wiring wiring = getWirings().get(cap.getResource());
                if (wiring != null && cap instanceof XPackageCapability) {
                    String pkgname = ((XPackageCapability) cap).getPackageName();
                    for (Wire wire : wiring.getRequiredResourceWires(cap.getNamespace())) {
                        XPackageRequirement wirereq = (XPackageRequirement) wire.getRequirement();
                        if (pkgname.equals(wirereq.getPackageName())) {
                            ignoreCapability = true;
                            break;
                        }
                    }
                }
                if (!ignoreCapability) {
                    result.add(cap);
                }
            }
        }
        log.debugf("findProviders: %s => %s", req, result);
        return result;
    }

    @Override
    public synchronized Map<Resource, Wiring> updateWiring(Map<Resource, List<Wire>> wiremap) {
        Map<Resource, Wiring> result = new HashMap<Resource, Wiring>();
        for (Map.Entry<Resource, List<Wire>> entry : wiremap.entrySet()) {
            Resource res = entry.getKey();
            List<Wire> wires = entry.getValue();
            XWiring reswiring = (XWiring) wirings.get(res);
            if (reswiring == null) {
                reswiring = (XWiring) createWiring(res, wires);
                wirings.put(res, reswiring);
            }
            for (Wire wire : wires) {
                XResource provider = (XResource) wire.getProvider();
                XWiring provwiring = (XWiring) wirings.get(provider);
                if (provwiring == null) {
                    provwiring = (XWiring) createWiring(provider, null);
                    wirings.put(provider, provwiring);
                }
                provwiring.addProvidedWire(wire);
            }
            result.put(res, reswiring);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Wiring createWiring(Resource res, List<Wire> wires) {
        return new AbstractWiring(res, wires);
    }

    @Override
    public boolean isEffective(Requirement req) {
        return true;
    }

    @Override
    public synchronized Map<Resource, Wiring> getWirings() {
        return Collections.unmodifiableMap(wirings);
    }

    private Set<Capability> getCachedCapabilities(CacheKey key) {
        Set<Capability> capset = capabilityCache.get(key);
        if (capset == null) {
            capset = new LinkedHashSet<Capability>();
            capabilityCache.put(key, capset);
        }
        return capset;
    }

    private Set<Resource> getCachedResources(String type) {
        Set<Resource> resset = resourceCache.get(type);
        if (resset == null) {
            resset = new LinkedHashSet<Resource>();
            resourceCache.put(type, resset);
        }
        return resset;
    }

    private static class CacheKey {

        private final String key;

        static CacheKey create(Capability cap) {
            String namespace = cap.getNamespace();
            return new CacheKey(namespace, (String) cap.getAttributes().get(namespace));
        }

        static CacheKey create(Requirement req) {
            String namespace = req.getNamespace();
            return new CacheKey(namespace, (String) req.getAttributes().get(namespace));
        }

        private CacheKey(String namespace, String value) {
            key = namespace + ":" + value;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            return key.equals(other.key);
        }

        @Override
        public String toString() {
            return "[" + key + "]";
        }
    }
}