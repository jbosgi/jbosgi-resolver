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

import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wiring;
import org.osgi.service.resolver.Environment;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The abstract implementation of a {@link Environment}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractEnvironment extends AbstractElement implements Environment {

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        Set<Capability> result = new TreeSet<Capability>();
        for(Resource res : getResources(req)) {
            for(Capability cap : res.getCapabilities(req.getNamespace())) {
                if(req.matches(cap)) {
                    result.add(cap);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    protected abstract Collection<Resource> getResources(Requirement req);

    @Override
    public boolean isEffective(Requirement req) {
        return true;
    }

    @Override
    public Map<Resource, Wiring> getWirings() {
        return Collections.emptyMap();
    }
}