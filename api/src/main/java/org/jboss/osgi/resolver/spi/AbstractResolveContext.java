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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResolveContext;
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

    @Override
    public XEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public List<Capability> findProviders(Requirement requirement) {
        SortedSet<Capability> caps = environment.findProviders(requirement);
        return new ArrayList<Capability>(caps);
    }

    @Override
    public int insertHostedCapability(List<Capability> capabilities, HostedCapability hostedCapability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEffective(Requirement requirement) {
        return environment.isEffective(requirement);
    }

    @Override
    public Map<Resource, Wiring> getWirings() {
        return environment.getWirings();
    }
}