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

import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;

import java.util.Map;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.service.resolver.HostedCapability;

/**
 * The abstract implementation of a {@link HostedCapability}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 29-Jun-2012
 */
public class AbstractHostedCapability implements HostedCapability {

    private final Resource resource;
    private final Capability capability;

    public AbstractHostedCapability(Resource resource, Capability capability) {
        if (resource == null)
            throw MESSAGES.illegalArgumentNull("resource");
        if (capability == null)
            throw MESSAGES.illegalArgumentNull("capability");
        this.resource = resource;
        this.capability = capability;
    }

    @Override
    public Resource getResource() {
        return resource;
    }


    @Override
    public Capability getDeclaredCapability() {
        return capability;
    }
    
    @Override
    public String getNamespace() {
        return capability.getNamespace();
    }


    @Override
    public Map<String, String> getDirectives() {
        return capability.getDirectives();
    }


    @Override
    public Map<String, Object> getAttributes() {
        return capability.getAttributes();
    }

    @Override
    public String toString() {
        return "HostedCapability[" + resource + "," + capability + "]";
    }
}