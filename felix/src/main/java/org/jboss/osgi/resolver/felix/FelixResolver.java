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
package org.jboss.osgi.resolver.felix;

import org.apache.felix.framework.resolver.ResolveException;
import org.apache.felix.framework.resolver.Resolver.ResolverState;
import org.apache.felix.framework.resolver.ResolverImpl;
import org.apache.felix.framework.resolver.ResolverWire;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.spi.AbstractBundleRevision;
import org.jboss.osgi.resolver.spi.AbstractWire;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.ResourceConstants;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.resolver.Environment;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.Resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.osgi.framework.resource.ResourceConstants.WIRING_HOST_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;
import static org.osgi.framework.wiring.BundleRevision.TYPE_FRAGMENT;

/**
 * An implementation of the Resolver.
 * <p/>
 * This implemantion should use no framework specific API. It is the extension point for a framework specific Resolver.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class FelixResolver implements Resolver {

    private ResolverImpl delegate = new ResolverImpl(new LoggerDelegate());

    @Override
    public Map<Resource, List<Wire>> resolve(Environment environment, Collection<? extends Resource> mandatoryResources, Collection<? extends Resource> optionalResources) throws ResolutionException {
        ResolverState state = new EnvironmentDelegate((XEnvironment) environment);
        Set<BundleRevision> fragments = new HashSet<BundleRevision>();
        Set<BundleRevision> mandatory = bundleRevisions(mandatoryResources, fragments);
        Set<BundleRevision> optional = bundleRevisions(optionalResources, fragments);
        Map<BundleRevision, List<ResolverWire>> result;
        try {
            result = delegate.resolve(state, mandatory, optional, fragments);
        } catch (ResolveException ex) {
            Requirement req = (Requirement) ex.getRequirement();
            throw new ResolutionException(ex.getMessage(), ex, Collections.singleton(req));
        }
        return processResult(result, fragments);
    }

    private Map<Resource, List<Wire>> processResult(Map<BundleRevision, List<ResolverWire>> map, Set<BundleRevision> fragments) {
        Map<Resource, List<Wire>> result = new LinkedHashMap<Resource, List<Wire>>();
        List<Requirement> unresolved = new ArrayList<Requirement>();
        for (Map.Entry<BundleRevision, List<ResolverWire>> entry : map.entrySet()) {
            BundleRevision brev = entry.getKey();
            BundleRevision bhost = brev;

            List<ResolverWire> reswires = entry.getValue();
            Map<BundleRequirement, Wire> wiremap = toWireMap(brev, reswires);

            // If the resource has non-optional requirements but felix
            // returns no reswires, we search for a self wire
            for (BundleRequirement breq : brev.getDeclaredRequirements(null)) {
                XRequirement xreq = (XRequirement) breq;
                xreq = (XRequirement) xreq.adapt(Requirement.class);
                if (wiremap.get(breq) == null && xreq.isOptional() == false) {

                    // Find self matching capability
                    BundleCapability bcap = findMatchingCapability(brev, breq);

                    // Find capability from attached fragments
                    if (bcap == null) {
                        outerfor:
                        for (BundleRevision frag : fragments) {
                            List<ResolverWire> fragWires = map.get(frag);
                            if (fragWires != null) {
                                for (ResolverWire fragwire : fragWires) {
                                    BundleRevision fragprov = fragwire.getProvider();
                                    String reqns = fragwire.getRequirement().getNamespace();
                                    if (WIRING_HOST_NAMESPACE.equals(reqns) && fragprov == brev) {
                                        bcap = findMatchingCapability(frag, breq);
                                        if (bcap != null) {
                                            break outerfor;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // If the the requirement comes from a fragment look for the 
                    // capability in the attached host
                    if (bcap == null && (brev.getTypes() & TYPE_FRAGMENT) != 0) {
                        BundleRequirement hostreq = brev.getDeclaredRequirements(WIRING_HOST_NAMESPACE).get(0);
                        XResource hostres = (XResource) wiremap.get(hostreq).getProvider();
                        bhost = hostres.adapt(BundleRevision.class);
                        bcap = findMatchingCapability(bhost, breq);
                    }
                    
                    // Add a self wire to the wire map
                    if (bcap != null) {
                        Wire wire = new SelfWire(bhost, breq, bcap);
                        wiremap.put(breq, wire);
                    } else {
                        unresolved.add(xreq);
                    }
                }
            }

            Resource res = ((XResource) brev).adapt(Resource.class);
            List<Wire> wires = new ArrayList<Wire>(wiremap.values());
            result.put(res, Collections.unmodifiableList(wires));
        }

        // Throw a ResolutionException if there were mandatory requirements
        // that could not get associated with a capability
        if (unresolved.isEmpty() == false)
            throw new ResolutionException("Cannot obtain self wiring capabilities: " + unresolved, null, unresolved);

        return Collections.unmodifiableMap(result);
    }

    private BundleCapability findMatchingCapability(BundleRevision brev, BundleRequirement breq) {
        BundleCapability result = null;
        for (BundleCapability bcap : brev.getDeclaredCapabilities(WIRING_PACKAGE_NAMESPACE)) {
            if (breq.matches(bcap)) {
                result = bcap;
                break;
            }
        }
        return result;
    }

    private Map<BundleRequirement, Wire> toWireMap(Resource requirer, List<ResolverWire> reswires) {
        Map<BundleRequirement, Wire> result = new LinkedHashMap<BundleRequirement, Wire>();
        for (ResolverWire reswire : reswires) {
            ResolverWireDelegate wire = new ResolverWireDelegate(reswire);
            // we assume that every requirer is associated with the resource that is currently processed
            if (!reswire.getRequirer().equals(requirer)) {
                throw new IllegalStateException("Expected requirer: " + requirer + " but was: " + reswire.getRequirer());
            }
            result.put(reswire.getRequirement(), wire);
        }
        return result;
    }


    private Set<BundleRevision> bundleRevisions(Collection<? extends Resource> resources, Set<BundleRevision> fragments) {
        Set<BundleRevision> result = new HashSet<BundleRevision>();
        if (resources != null && !resources.isEmpty()) {
            for (Resource res : resources) {
                XResource xres = (XResource) res;
                String type = xres.getIdentityCapability().getType();
                if (ResourceConstants.IDENTITY_TYPE_FRAGMENT.equals(type)) {
                    fragments.add(new AbstractBundleRevision(xres));
                } else {
                    result.add(new AbstractBundleRevision(xres));
                }
            }
        }
        return result;
    }

    static class EnvironmentDelegate implements ResolverState {

        private final XEnvironment environment;

        EnvironmentDelegate(XEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public boolean isEffective(BundleRequirement req) {
            return environment.isEffective(req);
        }

        @Override
        public SortedSet<BundleCapability> getCandidates(BundleRequirement req, boolean obeyMandatory) {
            Comparator<Capability> comparator = environment.getComparator();
            SortedSet<BundleCapability> result = new TreeSet<BundleCapability>(comparator);
            for (Capability cap : environment.findProviders(req)) {
                XCapability xcap = (XCapability) cap;
                result.add(xcap.adapt(BundleCapability.class));
            }
            return result;
        }

        @Override
        public void checkExecutionEnvironment(BundleRevision revision) throws ResolveException {
        }

        @Override
        public void checkNativeLibraries(BundleRevision revision) throws ResolveException {
        }
    }

    static class ResolverWireDelegate extends AbstractWire {
        ResolverWireDelegate(ResolverWire rw) {
            super(toCapability(rw.getCapability()), toRequirement(rw.getRequirement()), toResource(rw.getProvider()), toResource(rw.getRequirer()));
        }
    }

    static class SelfWire extends AbstractWire {
        SelfWire(BundleRevision bhost, BundleRequirement breq, BundleCapability bcap) {
            super(toCapability(bcap), toRequirement(breq), toResource(bhost), toResource(bhost));
        }
    }

    private static Capability toCapability(BundleCapability bcap) {
        XCapability xcap = (XCapability) bcap;
        return xcap.adapt(Capability.class);
    }

    private static Requirement toRequirement(BundleRequirement breq) {
        XRequirement xreq = (XRequirement) breq;
        return xreq.adapt(Requirement.class);
    }

    private static Resource toResource(BundleRevision brev) {
        XResource xres = (XResource) brev;
        return xres.adapt(Resource.class);
    }
}