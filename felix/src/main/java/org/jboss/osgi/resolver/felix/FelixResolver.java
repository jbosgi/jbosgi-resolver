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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.felix.resolver.FelixEnvironment;
import org.apache.felix.resolver.impl.ResolverImpl;
import org.jboss.logging.Logger;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResolveContext;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.HostedCapability;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.ResolveContext;
import org.osgi.service.resolver.Resolver;

/**
 * An implementation of the Resolver.
 * <p/>
 * This implemantion should use no framework specific API. It is the extension point for a framework specific Resolver.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class FelixResolver implements Resolver {

    private static Logger log = Logger.getLogger(FelixResolver.class);

    private ResolverImpl delegate = new ResolverImpl(new LoggerDelegate());

    @Override
    public Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        FelixEnvironment env = new EnvironmentDelegate(context);
        Collection<Resource> mandatory = context.getMandatoryResources();
        Collection<Resource> optional = context.getOptionalResources();
        log.debugf("Resolve: %s, %s", mandatory, optional);
        Map<Resource, List<Wire>> result = delegate.resolve(env, mandatory, optional);
        if (log.isDebugEnabled()) {
            log.debugf("Resolution result: %d", result.size());
            for (Map.Entry<Resource, List<Wire>> entry : result.entrySet()) {
                Resource res = entry.getKey();
                List<Wire> wires = entry.getValue();
                log.debugf("   %s: %d wires", res, wires.size());
                for (Wire wire : wires) {
                    log.debugf("      %s", wire);
                }
            }
        }
        return result;
    }

    static class EnvironmentDelegate extends FelixEnvironment {

        private final XResolveContext context;

        EnvironmentDelegate(ResolveContext context) {
            this.context = (XResolveContext) context;
        }

        @Override
        public SortedSet<Capability> findSortedSetProviders(Requirement req) {
            return context.getEnvironment().findProviders(req);
        }

        @Override
        public boolean isEffective(Requirement req) {
            return context.isEffective(req);
        }

        @Override
        public Map<Resource, Wiring> getWirings() {
            return context.getWirings();
        }

        @Override
        public boolean matches(Requirement req, Capability cap) {
            return ((XRequirement)req).matches((XCapability) cap);
        }

        @Override
        public List<Capability> findProviders(Requirement requirement) {
            SortedSet<Capability> caps = findSortedSetProviders(requirement);
            return new ArrayList<Capability>(caps);
        }

        @Override
        public void checkExecutionEnvironment(Resource resource) throws ResolutionException {
            // not implemented
        }

        @Override
        public void checkNativeLibraries(Resource resource) throws ResolutionException {
            // not implemented
        }

        @Override
        public int insertHostedCapability(List<Capability> capabilities, HostedCapability hostedCapability) {
            // not implemented
            return 0;
        }
    }
}