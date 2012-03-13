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

import org.apache.felix.resolver.FelixEnvironment;
import org.apache.felix.resolver.impl.ResolverImpl;
import org.jboss.logging.Logger;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;
import org.osgi.service.resolver.Environment;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.Resolver;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

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
    public Map<Resource, List<Wire>> resolve(Environment environment, Collection<? extends Resource> mandatory, Collection<? extends Resource> optional) throws ResolutionException {
        FelixEnvironment env = new EnvironmentDelegate(environment);
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

    static class EnvironmentDelegate implements FelixEnvironment {

        private final Environment environment;

        EnvironmentDelegate(Environment environment) {
            this.environment = environment;
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
        public SortedSet<Capability> findProviders(Requirement req) {
            return environment.findProviders(req);
        }

        @Override
        public boolean isEffective(Requirement req) {
            return environment.isEffective(req);
        }

        @Override
        public Map<Resource, Wiring> getWirings() {
            return environment.getWirings();
        }
    }
}