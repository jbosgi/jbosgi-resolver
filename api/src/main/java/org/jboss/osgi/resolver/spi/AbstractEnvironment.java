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

import static org.jboss.osgi.resolver.internal.ResolverLogger.LOGGER;
import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XHostRequirement;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XWiring;
import org.omg.CORBA.Environment;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;

/**
 * The abstract implementation of a {@link Environment}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractEnvironment implements XEnvironment {

    private static final String[] ALL_IDENTITY_TYPES = new String[] { 
        IdentityNamespace.TYPE_BUNDLE, 
        IdentityNamespace.TYPE_FRAGMENT, 
        IdentityNamespace.TYPE_UNKNOWN
    };

    private final AtomicLong resourceIndex = new AtomicLong();
    private final Map<CacheKey, Set<Capability>> capabilityCache = new ConcurrentHashMap<CacheKey, Set<Capability>>();
    private final Map<String, Set<XResource>> resourceTypeCache = new ConcurrentHashMap<String, Set<XResource>>();
    private final Map<Long, XResource> resourceIndexCache = new ConcurrentHashMap<Long, XResource>();
    private final Map<Resource, Wiring> wirings = new HashMap<Resource, Wiring>();
    
    @Override
    public synchronized void installResources(XResource... resources) {
        for (XResource res : resources) {
            XIdentityCapability icap = res.getIdentityCapability();
            if (getCachedCapabilities(CacheKey.create(icap)).contains(icap))
                throw MESSAGES.illegalStateResourceAlreadyInstalled(res);

            LOGGER.debugf("Install resource: %s", res);

            // Add resource to index
            Long index = resourceIndex.getAndIncrement();
            res.addAttachment(Long.class, index);
            resourceIndexCache.put(index, res);

            // Add resource by type
            getCachedResources(icap.getType()).add(res);
            // Add resource capabilites
            for (Capability cap : res.getCapabilities(null)) {
                getCachedCapabilities(CacheKey.create(cap)).add(cap);
                LOGGER.debugf("   %s", cap);
            }
            if (LOGGER.isDebugEnabled()) {
                for (Requirement req : res.getRequirements(null)) {
                    LOGGER.debugf("   %s", req);
                }
            }
        }
    }

    @Override
    public synchronized void uninstallResources(XResource... resources) {
        for (XResource res : resources) {
            
            // Remove resource by index
            Long index = res.getAttachment(Long.class);
            if (index == null || resourceIndexCache.remove(index) == null) {
                LOGGER.debugf("Unknown resource: %s", res);
                continue;
            }

            LOGGER.debugf("Uninstall resource: %s", res);

            // Remove resource by type
            XIdentityCapability icap = res.getIdentityCapability();
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
    public void refreshResources(XResource... resources) {
        for (XResource res : resources) {
            wirings.remove(res);
        }
    }

    @Override
    public synchronized Collection<XResource> getResources(Set<String> types) {
        Set<XResource> result = new HashSet<XResource>();
        for (String type : (types != null ? types : Arrays.asList(ALL_IDENTITY_TYPES))) {
            result.addAll(getCachedResources(type));
        }
        return result;
    }

    @Override
    public synchronized List<Capability> findProviders(Requirement req) {
        CacheKey cachekey = CacheKey.create(req);
        List<Capability> result = new ArrayList<Capability>();
        for (Capability cap : getCachedCapabilities(cachekey)) {
            if (((XRequirement)req).matches((XCapability) cap)) {
                boolean ignoreCapability = false;
                XResource res = (XResource) cap.getResource();

                // Check if the package capability has been substituted
                Wiring wiring = getWirings().get(res);
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

                // A fragment can only provide a capability if it is either already attached
                // or if there is one possible hosts that it can attach to
                // i.e. one of the hosts in the range is not resolved already
                List<Requirement> hostreqs = res.getRequirements(HostNamespace.HOST_NAMESPACE);
                if (wiring == null && !hostreqs.isEmpty()) {
                    XHostRequirement hostreq = (XHostRequirement) hostreqs.get(0);
                    boolean unresolvedHost = false;
                    for (Capability hostcap : capabilityCache.get(CacheKey.create(hostreq))) {
                        if (hostreq.matches((XCapability) hostcap)) {
                            Resource host = hostcap.getResource();
                            if (getWirings().get(host) == null) {
                                unresolvedHost = true;
                                break;
                            }
                        }
                    }
                    ignoreCapability = !unresolvedHost;
                }

                if (!ignoreCapability) {
                    result.add(cap);
                }
            }
        }
        
        LOGGER.debugf("findProviders: %s => %s", req, result);
        return result;
    }

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
                ((AbstractWiring)provwiring).addProvidedWire(wire);
            }
            result.put(res, reswiring);
        }
        return Collections.unmodifiableMap(result);
    }
    
    protected Wiring createWiring(Resource res, List<Wire> wires) {
        return new AbstractWiring(res, wires);
    }

    @Override
    public Long getResourceIndex(XResource res) {
        return res.getAttachment(Long.class);
    }

    @Override
    public synchronized Map<Resource, Wiring> getWirings() {
        return Collections.unmodifiableMap(wirings);
    }

    private synchronized Set<Capability> getCachedCapabilities(CacheKey key) {
        Set<Capability> capset = capabilityCache.get(key);
        if (capset == null) {
            capset = new LinkedHashSet<Capability>();
            capabilityCache.put(key, capset);
        }
        return capset;
    }

    private synchronized Set<XResource> getCachedResources(String type) {
        Set<XResource> typeset = resourceTypeCache.get(type);
        if (typeset == null) {
            typeset = new LinkedHashSet<XResource>();
            resourceTypeCache.put(type, typeset);
        }
        return typeset;
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