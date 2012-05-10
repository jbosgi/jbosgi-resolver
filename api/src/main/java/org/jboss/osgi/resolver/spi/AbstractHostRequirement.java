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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XHostCapability;
import org.jboss.osgi.resolver.XHostRequirement;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.resource.Resource;

/**
 * The abstract implementation of a {@link XHostRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractHostRequirement extends AbstractRequirement implements XHostRequirement {

    private final String symbolicName;
    private final VersionRange versionrange;

    protected AbstractHostRequirement(Resource res, Map<String, Object> atts, Map<String, String> dirs) {
        super(res, HostNamespace.HOST_NAMESPACE, atts, dirs);
        symbolicName = (String) getAttribute(HostNamespace.HOST_NAMESPACE);
        Object versionatt = atts.get(HostNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        if (versionatt instanceof String) {
            versionatt = VersionRange.parse((String) versionatt);
        }
        versionrange = (VersionRange) versionatt;
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
    public VersionRange getVersionRange() {
        return versionrange;
    }

    @Override
    public boolean matches(XCapability cap) {

        if (super.matches(cap) == false)
            return false;

        // match the bundle version range
        if (versionrange != null) {
            Version version = ((XHostCapability) cap).getVersion();
            if (versionrange.isInRange(version) == false)
                return false;
        }

        return true;
    }
}