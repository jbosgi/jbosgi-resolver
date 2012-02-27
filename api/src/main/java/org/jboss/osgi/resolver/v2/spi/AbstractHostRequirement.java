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

import org.jboss.osgi.resolver.v2.VersionRange;
import org.jboss.osgi.resolver.v2.XHostRequirement;
import org.jboss.osgi.resolver.v2.XIdentityCapability;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_HOST_NAMESPACE;

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
        super(res, WIRING_HOST_NAMESPACE, atts, dirs);
        symbolicName = (String) getAttribute(WIRING_HOST_NAMESPACE);
        Object versionatt = atts.get(BUNDLE_VERSION_ATTRIBUTE);
        if (versionatt instanceof String) {
            versionatt = VersionRange.parse((String) versionatt);
        }
        versionrange = (VersionRange) versionatt;
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        return Collections.singleton(WIRING_HOST_NAMESPACE);
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
    public boolean matches(Capability cap) {

        if (super.matches(cap) == false)
            return false;

        // match the bundle version range
        if (versionrange != null) {
            Version version = ((XIdentityCapability) cap).getVersion();
            if (versionrange.isInRange(version) == false)
                return false;
        }

        return true;
    }
}