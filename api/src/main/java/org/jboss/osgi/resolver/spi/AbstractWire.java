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