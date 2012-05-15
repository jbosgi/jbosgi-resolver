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

import java.util.Comparator;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResource;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

/**
 * A comparator that uses the provided resource index.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class ResourceIndexComparator implements Comparator<Capability> {

    private final XEnvironment environment;

    ResourceIndexComparator(XEnvironment environment) {
        this.environment = environment;
    }
    
    XEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public int compare(Capability o1, Capability o2) {
        Long in1 = getResourceIndex(o1.getResource());
        Long in2 = getResourceIndex(o2.getResource());
        return in1.compareTo(in2);
    }

    Long getResourceIndex(Resource res) {
        return environment.getResourceIndex((XResource) res);
    }
}