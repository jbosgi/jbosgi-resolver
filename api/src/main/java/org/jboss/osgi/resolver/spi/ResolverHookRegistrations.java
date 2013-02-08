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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XBundleRevision;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Resource;

/**
 * The default implementation for {@link ResolverHook} functionality.
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Feb-2013
 */
public class ResolverHookRegistrations {

    private static ThreadLocal<ResolverHookRegistrations> association = new ThreadLocal<ResolverHookRegistrations>();
    private List<ResolverHookRegistration> registrations;
    private Collection<BundleRevision> candidates;
    private final BundleContext syscontext;

    public interface SingletonLocator {
        Collection<BundleCapability> findCollisionCandidates(BundleCapability icap);
    }

    public ResolverHookRegistrations(BundleContext syscontext, Collection<XBundle> unresolved) {
        this.syscontext = syscontext;

        // Get the set of unresolved candidates
        candidates = new ArrayList<BundleRevision>();
        if (unresolved != null) {
            for (XBundle bundle : unresolved) {
                candidates.add(bundle.getBundleRevision());
            }
        }
        candidates = new RemoveOnlyCollection<BundleRevision>(candidates);

        // Get the set of registered {@link ResolverFactory}
        Collection<ServiceReference<ResolverHookFactory>> srefs = null;
        try {
            srefs = syscontext.getServiceReferences(ResolverHookFactory.class, null);
        } catch (InvalidSyntaxException e) {
            // ignore
        }

        // The hooks are called in ranking order
        List<ServiceReference<ResolverHookFactory>> sorted = new ArrayList<ServiceReference<ResolverHookFactory>>(srefs);
        Collections.reverse(sorted);

        // Create list of {@link ResolverHook} from {@link ResolverFactory}
        for (ServiceReference<ResolverHookFactory> sref : sorted) {
            if (registrations == null) {
                registrations = new ArrayList<ResolverHookRegistration>();
            }
            registrations.add(new ResolverHookRegistration(sref));
        }
    }

    public static ResolverHookRegistrations getResolverHookRegistrations() {
        return association.get();
    }

    public boolean hasResolverHooks() {
        return registrations != null;
    }

    public boolean hasBundleRevision(BundleRevision brev) {
        return candidates != null && candidates.contains(brev);
    }

    public boolean hasResource(Resource res) {
        return candidates != null && candidates.contains(res);
    }

    public void begin(Collection<? extends Resource> mandatory, Collection<? extends Resource> optional) {

        // Set the thread association
        association.set(this);

        // Get the initial set of trigger bundles
        Collection<BundleRevision> triggers = new ArrayList<BundleRevision>();
        addTriggers(mandatory, triggers);
        addTriggers(optional, triggers);
        triggers = new RemoveOnlyCollection<BundleRevision>(triggers);

        // Create a {@link ResolverHook} for each factory
        if (registrations != null) {
            for (ResolverHookRegistration hookreg : registrations) {
                try {
                    ResolverHookFactory hookFactory = syscontext.getService(hookreg.sref);
                    if (hookreg.isRegistered()) {
                        hookreg.hook = hookFactory.begin(triggers);
                    }
                } catch (RuntimeException ex) {
                    hookreg.lastException = ex;
                    throw new ResolverHookException(ex);
                }
            }
        }
    }

    private void addTriggers(Collection<? extends Resource> resources, Collection<BundleRevision> triggers) {
        if (resources != null) {
            for (Resource res : resources) {
                XBundleRevision brev = (XBundleRevision) res;
                if (brev.getBundle().getState() != Bundle.UNINSTALLED) {
                    triggers.add(brev);
                }
            }
        }
    }

    public void filterResolvable() {
        if (registrations != null) {
            for (ResolverHookRegistration hookreg : registrations) {
                try {
                    ResolverHook hook = hookreg.getResolverHook();
                    if (hook != null && hookreg.lastException == null) {
                        Collection<BundleRevision> before = new HashSet<BundleRevision>(candidates);
                        hook.filterResolvable(candidates);
                        for (BundleRevision aux : before) {
                            if (candidates.contains(aux) == false) {
                                LOGGER.debugf("ResolverHook filtered resolvable: %s", aux);
                            }
                        }
                    }
                } catch (RuntimeException ex) {
                    hookreg.lastException = ex;
                    throw new ResolverHookException(ex);
                }
            }
        }
    }

    public void filterSingletonCollisions(SingletonLocator locator) {
        if (registrations != null) {

            // Collect the singleton capabilities from all candidate revisions
            Map<BundleCapability, Collection<BundleCapability>> singletons = new HashMap<BundleCapability, Collection<BundleCapability>>();
            for (BundleRevision brev : candidates) {
                List<BundleCapability> bcaps = brev.getDeclaredCapabilities(IdentityNamespace.IDENTITY_NAMESPACE);
                if (bcaps.size() == 1) {
                    BundleCapability bcap = bcaps.get(0);
                    String spec = bcap.getDirectives().get(Constants.SINGLETON_DIRECTIVE);
                    if (Boolean.parseBoolean(spec)) {
                        Collection<BundleCapability> collisions = locator.findCollisionCandidates(bcap);
                        singletons.put(bcap, new RemoveOnlyCollection<BundleCapability>(collisions));
                    }
                }
            }

            for (Map.Entry<BundleCapability, Collection<BundleCapability>> entry : singletons.entrySet()) {
                BundleCapability bcap = entry.getKey();
                Collection<BundleCapability> collisions = entry.getValue();
                for (ResolverHookRegistration hookreg : registrations) {
                    try {
                        ResolverHook hook = hookreg.getResolverHook();
                        if (hook != null && hookreg.lastException == null) {
                            hook.filterSingletonCollisions(bcap, collisions);
                        }
                    } catch (RuntimeException ex) {
                        hookreg.lastException = ex;
                        throw new ResolverHookException(ex);
                    }
                }
                // Remove the collisions that will not resolve because they are no candidates
                for (Iterator<BundleCapability> iterator = collisions.iterator(); iterator.hasNext();) {
                    BundleRevision brev = iterator.next().getResource();
                    if (brev.getBundle().getState() == Bundle.INSTALLED && !candidates.contains(brev)) {
                        iterator.remove();
                    }
                }
                if (!collisions.isEmpty()) {
                    BundleRevision brev = bcap.getResource();
                    LOGGER.debugf("ResolverHook found singleton collision of %s with %s", bcap, collisions);
                    LOGGER.debugf("ResolverHook removed resolution candidate %s", brev);
                    candidates.remove(brev);
                }
            }
        }
    }

    public void filterMatches(BundleRequirement breq, Collection<BundleCapability> matching) {
        if (registrations != null) {
            for (ResolverHookRegistration hookreg : registrations) {
                try {
                    ResolverHook hook = hookreg.getResolverHook();
                    if (hook != null && hookreg.lastException == null) {
                        Collection<BundleCapability> before = new HashSet<BundleCapability>(matching);
                        hook.filterMatches(breq, matching);
                        for (BundleCapability aux : before) {
                            if (matching.contains(aux) == false) {
                                LOGGER.debugf("ResolverHook filtered match: %s", aux);
                            }
                        }
                    }
                } catch (RuntimeException ex) {
                    hookreg.lastException = ex;
                    throw new ResolverHookException(ex);
                }
            }
        }
    }

    public void end() {
        ResolverHookException endException = null;
        try {
            // Call end on every {@link ResolverHook}
            if (registrations != null) {
                for (ResolverHookRegistration hookreg : registrations) {
                    try {
                        ResolverHook hook = hookreg.getResolverHook();
                        if (hook != null) {
                            hook.end();
                        }
                    } catch (RuntimeException ex) {
                        hookreg.lastException = ex;
                        if (endException == null) {
                            endException = new ResolverHookException(ex);
                        }
                    }
                }
            }
        } finally {
            // Clear the thread association
            association.remove();
        }
        // [TODO] The TCK expects to see the exception thrown in end() which would
        // shadow a previous exception comming from filterResolvable
        if (endException != null)
            throw endException;
    }

    class ResolverHookRegistration {
        private final ServiceReference<ResolverHookFactory> sref;
        private RuntimeException lastException;
        private ResolverHook hook;

        ResolverHookRegistration(ServiceReference<ResolverHookFactory> sref) {
            this.sref = sref;
        }

        boolean isRegistered() {
            return syscontext.getService(sref) != null;
        }

        ResolverHook getResolverHook() {
            if (isRegistered() == false) {
                throw MESSAGES.illegalStateResolverHookUnregistered(sref);
            }
            return hook;
        }


    }
}