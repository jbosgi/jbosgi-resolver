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
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResolver;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * An implementation of the Resolver.
 * <p/>
 * This implemantion should use no framework specific API. It is the extension point for a framework specific Resolver.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class FelixResolver implements XResolver {

    private ResolverImpl delegate = new ResolverImpl(new LoggerDelegate());

    @Override
    public Map<Resource, List<Wire>> resolve(Environment environment, Collection<? extends Resource> mandatoryResources, Collection<? extends Resource> optionalResources) throws ResolutionException {
        ResolverState state = new EnvironmentDelegate(environment);
        Set<BundleRevision> mandatory = bundleRevisions(mandatoryResources);
        Set<BundleRevision> optional = bundleRevisions(optionalResources);
        Set<BundleRevision> fragments = Collections.emptySet();
        Map<BundleRevision, List<ResolverWire>> result = null;
        try {
            result = delegate.resolve(state, mandatory, optional, fragments);
        } catch (ResolveException ex) {
            Requirement req = (Requirement) ex.getRequirement();
            throw new ResolutionException(ex.getMessage(), ex, Collections.singleton(req));
        }
        return processResult(result);
    }

    private Map<Resource, List<Wire>> processResult(Map<BundleRevision, List<ResolverWire>> map) {
        Map<Resource, List<Wire>> result = new LinkedHashMap<Resource, List<Wire>>();
        for (Map.Entry<BundleRevision, List<ResolverWire>> entry : map.entrySet()) {
            Resource res = entry.getKey();
            List<ResolverWire> reswires = entry.getValue();
            List<Wire> wires;
            // If the res has non-optional requirements but felix
            // returns no reswires, we assume that this is a self wire
            if (reswires.isEmpty() && !res.getRequirements(null).isEmpty()) {
                wires = new ArrayList<Wire>();
                for(Requirement req : res.getRequirements(WIRING_PACKAGE_NAMESPACE)) {
                    for(Capability cap : res.getCapabilities(WIRING_PACKAGE_NAMESPACE)) {
                        if (req.matches(cap)) {
                            wires.add(new SelfWire(req, cap));
                        }
                    }
                }
                if (wires.isEmpty()) {
                    List<Requirement> exreqs = res.getRequirements(null);
                    throw new ResolutionException("Cannot obtain self wiring capabilities", null, exreqs);
                }
            }
            else {
                wires = toWires(reswires);
            }
            result.put(res, wires);
        }
        return result;
    }

    private List<Wire> toWires(List<ResolverWire> reswires) {
        List<Wire> result = new ArrayList<Wire>();
        for (ResolverWire reswire : reswires) {
            result.add(new ResolverWireDelegate(reswire));
        }
        return result;
    }


    private Set<BundleRevision> bundleRevisions(Collection<? extends Resource> resources) {
        Set<BundleRevision> result = new HashSet();
        if (resources != null && !resources.isEmpty()) {
            for (Resource res : resources) {
                if (res instanceof BundleRevision) {
                    result.add((BundleRevision) res);
                }
            }
        }
        return result;
    }

    static class EnvironmentDelegate implements ResolverState {

        private final Environment environment;

        EnvironmentDelegate(Environment environment) {
            this.environment = environment;
        }

        @Override
        public boolean isEffective(BundleRequirement req) {
            return environment.isEffective(req);
        }

        @Override
        public SortedSet<BundleCapability> getCandidates(BundleRequirement req, boolean obeyMandatory) {
            SortedSet<BundleCapability> result = new TreeSet<BundleCapability>();
            for (Capability cap : environment.findProviders(req)) {
                result.add((BundleCapability) cap);
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

    static class ResolverWireDelegate implements Wire {

        private final ResolverWire delegate;

        ResolverWireDelegate(ResolverWire delegate) {
            this.delegate = delegate;
        }

        @Override
        public Capability getCapability() {
            return (Capability) delegate.getCapability();
        }

        @Override
        public Requirement getRequirement() {
            return (Requirement) delegate.getRequirement();
        }

        @Override
        public Resource getProvider() {
            return (Resource) delegate.getProvider();
        }

        @Override
        public Resource getRequirer() {
            return (Resource) delegate.getRequirer();
        }
    }

    static class SelfWire implements Wire {

        private final Requirement req;
        private final Capability cap;

        SelfWire(Requirement req, Capability cap) {
            this.req = req;
            this.cap = cap;
        }

        @Override
        public Capability getCapability() {
            return cap;
        }

        @Override
        public Requirement getRequirement() {
            return req;
        }

        @Override
        public Resource getProvider() {
            return cap.getResource();
        }

        @Override
        public Resource getRequirer() {
            return req.getResource();
        }
    }
}