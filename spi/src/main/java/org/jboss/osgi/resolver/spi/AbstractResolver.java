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

import org.jboss.osgi.resolver.XResolver;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.service.resolver.Environment;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.Resolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An abstract base implementation of a Resolver.
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Jul-2010
 */
public class AbstractResolver implements XResolver {

    private final Resolver resolver;

    public AbstractResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Map<Resource, List<Wire>> resolve(Environment environment, Collection<? extends Resource> mandatoryResources, Collection<? extends Resource> optionalResources) throws ResolutionException {
        return resolver.resolve(environment, mandatoryResources, optionalResources);
    }
}