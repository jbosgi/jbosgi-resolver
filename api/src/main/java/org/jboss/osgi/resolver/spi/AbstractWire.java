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

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;

/**
 * The abstract implementation of a {@link Wire}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractWire implements Wire {

    private final Capability capability;
    private final Requirement requirement;
    private final Resource provider;
    private final Resource requirer;

    protected AbstractWire(Capability capability, Requirement requirement, Resource provider, Resource requirer) {
        this.capability = capability;
        this.requirement = requirement;
        this.provider = provider;
        this.requirer = requirer;
    }

    public Capability getCapability() {
        return capability;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public Resource getProvider() {
        return provider;
    }

    public Resource getRequirer() {
        return requirer;
    }

    @Override
    public String toString() {
        return "Wire[" + requirer + "{" + requirement + "} => " + provider + "{" + capability + "}]";
    }
}