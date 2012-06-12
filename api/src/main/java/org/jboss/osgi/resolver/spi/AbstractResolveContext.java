/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.resolver.internal.ResolverLogger.LOGGER;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResolveContext;
import org.jboss.osgi.resolver.internal.ResolverLogger;
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
    public boolean isEffective(Requirement requirement) {
        return true;
    }

    @Override
    public Map<Resource, Wiring> getWirings() {
        return environment.getWirings();
    }
}