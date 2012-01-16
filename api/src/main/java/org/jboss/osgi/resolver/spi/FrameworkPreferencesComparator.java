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

import org.jboss.osgi.resolver.XPackageCapability;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wiring;

/**
 * A comparator based on defined framework preferences.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class FrameworkPreferencesComparator extends ResourceIndexComparator {

    protected abstract Wiring getWiring(Resource res);

    @Override
    public int compare(Capability o1, Capability o2) {
        Resource res1 = o1.getResource();
        Resource res2 = o2.getResource();
        Wiring w1 = getWiring(res1);
        Wiring w2 = getWiring(res2);

        // prefer wired
        if (w1 != null && w2 == null)
            return -1;
        if (w1 == null && w2 != null)
            return +1;

        // prefer higher package version
        if (o1 instanceof XPackageCapability && o2 instanceof XPackageCapability) {
            Version v1 = ((XPackageCapability) o1).getVersion();
            Version v2 = ((XPackageCapability) o2).getVersion();
            if (!v1.equals(v2))
                return v2.compareTo(v1);
        }
        
        return super.compare(o1, o2);
    }
}