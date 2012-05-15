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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.resolver.XHostCapability;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.resource.Resource;

/**
 * The abstract implementation of a {@link org.jboss.osgi.resolver.XHostCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractHostCapability extends AbstractCapability implements XHostCapability {

    private final String symbolicName;
    private final Version version;

    protected AbstractHostCapability(Resource res, Map<String, Object> atts, Map<String, String> dirs) {
        super(res, HostNamespace.HOST_NAMESPACE, atts, dirs);
        symbolicName = (String) atts.get(HostNamespace.HOST_NAMESPACE);
        version = (Version) atts.get(HostNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        return Collections.singleton(HostNamespace.HOST_NAMESPACE);
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public Version getVersion() {
        return version;
    }
}