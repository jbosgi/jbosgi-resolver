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
package org.jboss.osgi.resolver.v2.spi;

import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;

import java.util.Comparator;

/**
 * A comparator that uses the provided resource index.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class ResourceIndexComparator implements Comparator<Capability> {

    protected abstract long getResourceIndex(Resource res);

    @Override
    public int compare(Capability o1, Capability o2) {
        long in1 = getResourceIndex(o1.getResource());
        long in2 = getResourceIndex(o2.getResource());
        return (int)(in1 - in2);
    }
}