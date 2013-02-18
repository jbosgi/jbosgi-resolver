/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.resolver.ResolverLogger.LOGGER;
import static org.jboss.osgi.resolver.ResolverMessages.MESSAGES;
import static org.jboss.osgi.resolver.spi.ResolverHookProcessor.getCurrentProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.omg.CORBA.Environment;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
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

    private final AtomicLong resourceIndex = new AtomicLong();
    private final Map<CacheKey, Set<Capability>> capabilityCache = new ConcurrentHashMap<CacheKey, Set<Capability>>();
    private final Map<String, Set<XResource>> resourceTypeCache = new ConcurrentHashMap<String, Set<XResource>>();
    private final Map<Long, XResource> resourceIndexCache = new ConcurrentHashMap<Long, XResource>();

    @Override
    public Long nextResourceIdentifier(Long value, String symbolicName) {
        synchronized (resourceIndex) {
            Long result;
            if (value != null) {
                resourceIndex.addAndGet(Math.max(0, value - resourceIndex.get()));
                return result = value;
            } else {
                result = resourceIndex.incrementAndGet();
            }
            LOGGER.tracef("Resource identifier for %s: [%d,%d]", symbolicName, value, result);
            return result;
        }
    }

    @Override
    public synchronized void installResources(XResource... resources) {
        for (XResource res : resources) {
            XIdentityCapability icap = res.getIdentityCapability();
            if (getCachedCapabilities(CacheKey.create(icap)).contains(icap))
                throw MESSAGES.illegalStateResourceAlreadyInstalled(res);

            LOGGER.debugf("Install resource: %s", res);

            // Add resource to index
            Long index = nextResourceIdentifier(res.getAttachment(Long.class), icap.getSymbolicName());
            res.addAttachment(Long.class, index);
            resourceIndexCache.put(index, res);

            // Add resource by type
            getCachedResources(icap.getType()).add(res);

            // Add resource capabilites
            for (Capability cap : res.getCapabilities(null)) {
                CacheKey cachekey = CacheKey.create(cap);
                getCachedCapabilities(cachekey).add(cap);
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
                Set<Capability> cachecaps = getCachedCapabilities(cachekey);
                cachecaps.remove(cap);
                if (cachecaps.isEmpty()) {
                    capabilityCache.remove(cachekey);
                }
            }

            // Remove wirings
            res.removeResourceWiring();
        }
    }

    @Override
    public synchronized void refreshResources(XResource... resources) {
        for (XResource res : resources) {
            res.removeResourceWiring();
        }
    }

    @Override
    public synchronized Collection<XResource> getResources(String... types) {
        Set<XResource> result = new HashSet<XResource>();
        for (String type : (types != null ? types : ALL_IDENTITY_TYPES)) {
            result.addAll(getCachedResources(type));
        }
        return result;
    }

    @Override
    public synchronized List<Capability> findProviders(Requirement req) {
        XRequirement xreq = (XRequirement) req;
        CacheKey cachekey = CacheKey.create(req);
        List<Capability> result = new ArrayList<Capability>();
        for (Capability cap : findCachedCapabilities(cachekey)) {
            if (xreq.matches(cap)) {
                boolean ignoreCapability = false;
                XCapability xcap = (XCapability) cap;
                XResource res = (XResource) xcap.getResource();

                // Check if the package capability has been substituted
                Wiring wiring = res.getResourceWiring();
                if (wiring != null && xcap.adapt(XPackageCapability.class) != null) {
                    String pkgname = xcap.adapt(XPackageCapability.class).getPackageName();
                    for (Wire wire : wiring.getRequiredResourceWires(cap.getNamespace())) {
                        XRequirement wirereq = (XRequirement) wire.getRequirement();
                        XPackageRequirement preq = wirereq.adapt(XPackageRequirement.class);
                        if (pkgname.equals(preq.getPackageName())) {
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
                    XRequirement hostreq = (XRequirement) hostreqs.get(0);
                    boolean unresolvedHost = false;
                    for (Capability hostcap : capabilityCache.get(CacheKey.create(hostreq))) {
                        if (hostreq.matches(hostcap)) {
                            XResource host = (XResource) hostcap.getResource();
                            if (host.getResourceWiring() == null) {
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

        // Filter the matches by calling the registered {@link ResolverHook}s
        ResolverHookProcessor hookregs = getCurrentProcessor();
        if (hookregs != null && req instanceof BundleRequirement) {
            Collection<BundleCapability> bcaps = new ArrayList<BundleCapability>();
            for (Capability cap : result) {
                XResource res = (XResource) cap.getResource();
                if (res.getResourceWiring() != null || hookregs.hasResource(res)) {
                    bcaps.add((BundleCapability) cap);
                }
            }
            bcaps = new RemoveOnlyCollection<BundleCapability>(bcaps);
            hookregs.filterMatches((BundleRequirement) req, bcaps);

            // Remove the filtered caps
            Iterator<Capability> iterator = result.iterator();
            while(iterator.hasNext()) {
                Capability cap = iterator.next();
                if (!bcaps.contains(cap)) {
                    iterator.remove();
                }
            }
        }

        LOGGER.tracef("Env provides: %s => %s", req, result);
        return result;
    }

    @Override
    public synchronized Map<Resource, Wiring> updateWiring(Map<Resource, List<Wire>> wiremap) {
        Map<Resource, WireInfo> infos = new HashMap<Resource, WireInfo>();
        for (Map.Entry<Resource, List<Wire>> entry : wiremap.entrySet()) {
            XResource resource = (XResource) entry.getKey();
            WireInfo reqinfo = getWireInfo(infos, resource);
            List<Wire> wires = entry.getValue();
            reqinfo.required.addAll(wires);
            for (Wire wire : wires) {
                XResource provider = (XResource) wire.getProvider();
                WireInfo provinfo = getWireInfo(infos, provider);
                provinfo.provided.add(wire);
            }
        }

        Map<Resource, Wiring> result = new HashMap<Resource, Wiring>();
        for (WireInfo info : infos.values()) {
            Wiring reswiring = updateResourceWiring(info);
            result.put(info.resource, reswiring);
        }
        return Collections.unmodifiableMap(result);
    }

    private WireInfo getWireInfo(Map<Resource, WireInfo> infos, XResource resource) {
        WireInfo info = infos.get(resource);
        if (info == null) {
            Wiring reswiring = resource.getResourceWiring();
            info = new WireInfo(resource, reswiring);
            infos.put(resource, info);
        }
        return info;
    }

    private Wiring updateResourceWiring(WireInfo info) {
        Wiring wiring = createWiring(info.resource, info.required, info.provided);
        info.resource.setResourceWiring(wiring);
        return wiring;
    }

    @Override
    public Wiring createWiring(XResource res, List<Wire> required, List<Wire> provided) {
        return new AbstractWiring(res, required, provided);
    }

    @Override
    public synchronized Map<Resource, Wiring> getWirings() {
        Map<Resource, Wiring> result = new HashMap<Resource, Wiring>();
        for(XResource res : resourceIndexCache.values()) {
            Wiring wiring = res.getResourceWiring();
            if (wiring != null) {
                result.put(res, wiring);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private synchronized Set<Capability> getCachedCapabilities(CacheKey key) {
        Set<Capability> capset = capabilityCache.get(key);
        if (capset == null) {
            capset = new LinkedHashSet<Capability>();
            capabilityCache.put(key, capset);
        }
        return capset;
    }

    private synchronized Set<Capability> findCachedCapabilities(CacheKey key) {
        Set<Capability> capset = capabilityCache.get(key);
        if (capset == null) {
            capset = new LinkedHashSet<Capability>();
            // do not add this to the capabilityCache
        }
        if (capset.isEmpty() && key.getValue() == null) {
            for (Entry<CacheKey, Set<Capability>> entry : capabilityCache.entrySet()) {
                CacheKey auxkey = entry.getKey();
                if (auxkey.getNamespace().equals(key.getNamespace())) {
                    capset.addAll(entry.getValue());
                }
            }
        }
        return Collections.unmodifiableSet(capset);
    }

    private Set<XResource> getCachedResources(String type) {
        Set<XResource> typeset = resourceTypeCache.get(type);
        if (typeset == null) {
            typeset = new LinkedHashSet<XResource>();
            resourceTypeCache.put(type, typeset);
        }
        return typeset;
    }

    private static class WireInfo {
        final XResource resource;
        final List<Wire> required = new ArrayList<Wire>();
        final List<Wire> provided = new ArrayList<Wire>();
        WireInfo(Resource resource, Wiring wiring) {
            this.resource = (XResource) resource;
            if (wiring != null) {
                required.addAll(wiring.getRequiredResourceWires(null));
                provided.addAll(wiring.getProvidedResourceWires(null));
            }
        }
    }

    private static class CacheKey {

        private final String namespace;
        private final String value;
        private final String keyspec;

        static CacheKey create(Capability cap) {
            String namespace = cap.getNamespace();
            String nsvalue = (String) cap.getAttributes().get(namespace);
            return new CacheKey(namespace, nsvalue);
        }

        static CacheKey create(Requirement req) {
            String namespace = req.getNamespace();
            String nsvalue = AbstractRequirement.getNamespaceValue(req);
            return new CacheKey(namespace, nsvalue);
        }

        private CacheKey(String namespace, String value) {
            this.namespace = namespace;
            this.value = value;
            this.keyspec = namespace + ":" + value;
        }

        String getNamespace() {
            return namespace;
        }

        String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return keyspec.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof CacheKey))
                return false;
            CacheKey other = (CacheKey) obj;
            return keyspec.equals(other.keyspec);
        }

        @Override
        public String toString() {
            return "[" + keyspec + "]";
        }
    }
}
