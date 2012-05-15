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

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XBundleRequirement;
import org.jboss.osgi.resolver.XCapability;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.resource.Resource;

/**
 * The abstract implementation of a {@link XBundleRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractBundleRequirement extends AbstractRequirement implements XBundleRequirement {

    private final String symbolicName;
    private final VersionRange versionrange;
    private final String visibility;

    protected AbstractBundleRequirement(Resource res, Map<String, Object> atts, Map<String, String> dirs) {
        super(res, BundleNamespace.BUNDLE_NAMESPACE, atts, dirs);
        symbolicName = (String) getAttribute(BundleNamespace.BUNDLE_NAMESPACE);
        visibility = getDirective(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE);
        Object versionatt = atts.get(BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        if (versionatt instanceof String) {
            versionatt = VersionRange.parse((String) versionatt);
        }
        versionrange = (VersionRange) versionatt;
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        return Collections.singleton(BundleNamespace.BUNDLE_NAMESPACE);
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
    public String getVisibility() {
        return visibility;
    }

    @Override
    public boolean matches(XCapability cap) {

        // cannot require itself
        if (getResource() == cap.getResource())
            return false;

        if (super.matches(cap) == false)
            return false;

        // match the bundle version range
        if (versionrange != null) {
            Version version = ((XBundleCapability) cap).getVersion();
            if (versionrange.isInRange(version) == false)
                return false;
        }

        return true;
    }
}