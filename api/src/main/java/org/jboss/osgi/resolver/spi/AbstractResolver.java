/*
 * #%L
 * JBossOSGi Resolver Felix
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XBundleRevision;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResolveContext;
import org.jboss.osgi.resolver.XResolver;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.ResolveContext;
import org.osgi.service.resolver.Resolver;

/**
 * An implementation of the Resolver.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class AbstractResolver implements XResolver {

    private final Resolver delegate;

    public AbstractResolver() {
        ClassLoader classLoader = AbstractResolver.class.getClassLoader();
        ServiceLoader<Resolver> loader = ServiceLoader.load(Resolver.class, classLoader);
        delegate = loader.iterator().next();
    }

    @Override
    public XResolveContext createResolveContext(XEnvironment env, final Collection<? extends Resource> mandatory, final Collection<? extends Resource> optional) {
        final Collection<Resource> manres = new HashSet<Resource>(mandatory != null ? mandatory : Collections.<Resource> emptySet());
        final Collection<Resource> optres = new HashSet<Resource>(optional != null ? optional : Collections.<Resource> emptySet());
        removeUninstalled(manres, optres);
        appendOptionalFragments(env, manres, optres);
        appendOptionalHostBundles(env, manres, optres);
        return new AbstractResolveContext(env) {

            @Override
            public Collection<Resource> getMandatoryResources() {
                return Collections.unmodifiableCollection(manres);
            }

            @Override
            public Collection<Resource> getOptionalResources() {
                return Collections.unmodifiableCollection(optres);
            }
        };
    }

    @Override
    public Map<Resource, List<Wire>> resolve(ResolveContext resolveContext) throws ResolutionException {
        return delegate.resolve(resolveContext);
    }

    @Override
    public Map<Resource, List<Wire>> resolveAndApply(XResolveContext context) throws ResolutionException {
        throw new UnsupportedOperationException();
    }

    private void removeUninstalled(Collection<Resource> manres, Collection<Resource> optres) {
        for (Resource res : getCombinedResources(manres, optres)) {
            if (res instanceof XBundleRevision) {
                XBundle bundle = ((XBundleRevision) res).getBundle();
                if (bundle != null && bundle.getState() == Bundle.UNINSTALLED) {
                    manres.remove(res);
                    optres.remove(res);
                }
            }
        }
    }

    private void appendOptionalFragments(XEnvironment env, Collection<? extends Resource> manres, Collection<Resource> optres) {
        Collection<Capability> hostcaps = new HashSet<Capability>();
        HashSet<Resource> combined = getCombinedResources(manres, optres);
        for (Resource res : combined) {
            for (Capability hostcap : res.getCapabilities(HostNamespace.HOST_NAMESPACE)) {
                hostcaps.add(hostcap);
            }
        }
        HashSet<Resource> fragments = new HashSet<Resource>();
        Iterator<XResource> itres = env.getResources(Collections.singleton(IdentityNamespace.TYPE_FRAGMENT));
        while (itres.hasNext()) {
            XBundleRevision brev = (XBundleRevision) itres.next();
            XBundle bundle = brev.getBundle();
            if (bundle != null && bundle.getState() != Bundle.UNINSTALLED) {
                XRequirement xreq = (XRequirement) brev.getRequirements(HostNamespace.HOST_NAMESPACE).get(0);
                for (Capability cap : hostcaps) {
                    if (xreq.matches(cap) && !combined.contains(brev)) {
                        fragments.add(brev);
                    }
                }
            }
        }
        if (!fragments.isEmpty()) {
            LOGGER.debugf("Adding attachable fragments: %s", fragments);
            optres.addAll(fragments);
        }
    }

    // Append the set of all unresolved resources if there is at least one optional package requirement
    private void appendOptionalHostBundles(XEnvironment env, Collection<? extends Resource> manres, Collection<Resource> optres) {
        for (Resource res : getCombinedResources(manres, optres)) {
            for (Requirement req : res.getRequirements(PackageNamespace.PACKAGE_NAMESPACE)) {
                XPackageRequirement preq = (XPackageRequirement) req;
                if (preq.isOptional()) {
                    Iterator<XResource> itres = env.getResources(Collections.singleton(XResource.TYPE_BUNDLE));
                    while (itres.hasNext()) {
                        XResource brev = itres.next();
                        if (brev.getWiring(false) == null && !manres.contains(brev)) {
                            optres.add(brev);
                        }
                    }
                    return;
                }
            }
        }
    }

    private HashSet<Resource> getCombinedResources(Collection<? extends Resource> manres, Collection<Resource> optres) {
        HashSet<Resource> combined = new HashSet<Resource>(manres);
        combined.addAll(optres);
        return combined;
    }
}
