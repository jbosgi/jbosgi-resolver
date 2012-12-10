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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResolveContext;
import org.osgi.framework.Constants;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.HostedCapability;

/**
 * The abstract implementation of a {@link XResolveContext}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Apr-2012
 */
public class AbstractResolveContext extends XResolveContext {

    private final XEnvironment environment;

    public AbstractResolveContext(XEnvironment environment) {
        this.environment = environment;
    }

    protected Comparator<Capability> getComparator() {
        return new FrameworkPreferencesComparator(environment);
    }

    @Override
    public XEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public List<Capability> findProviders(Requirement req) {
        List<Capability> providers = environment.findProviders(req);
        Collections.sort(providers, getComparator());
        LOGGER.tracef("Ctx provides: %s => %s", req, providers);
        return providers;
    }

    @Override
    public int insertHostedCapability(List<Capability> caps, HostedCapability hostedCapability) {
        caps.add(hostedCapability);
        Collections.sort(caps, getComparator());
        LOGGER.tracef("Insert hosted capability: %s => %s", hostedCapability, caps);
        return caps.indexOf(hostedCapability);
    }

    @Override
    public boolean isEffective(Requirement req) {
        // Ignore reqs that are not effective:=resolve
        String effective = req.getDirectives().get(Constants.EFFECTIVE_DIRECTIVE);
        return effective == null || effective.equals(Constants.EFFECTIVE_RESOLVE);
    }

    @Override
    public Map<Resource, Wiring> getWirings() {
        return environment.getWirings();
    }
}
