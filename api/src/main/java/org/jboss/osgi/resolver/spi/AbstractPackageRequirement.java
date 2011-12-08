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

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.wiring.BundleRevision;

import java.util.Map;

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE;
import static org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.Constants.VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * The abstract implementation of a {@link XPackageRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractPackageRequirement extends AbstractRequirement implements XPackageRequirement {

    private final String packageName;
    private final VersionRange versionrange;

    protected AbstractPackageRequirement(Resource resource, Map<String, Object> attributes, Map<String, String> directives) {
        super(resource, WIRING_PACKAGE_NAMESPACE, attributes, directives);
        packageName = (String) attributes.get(WIRING_PACKAGE_NAMESPACE);
        if (packageName == null)
            throw new IllegalArgumentException("Null packageName");
        Object versionatt = attributes.get(VERSION_ATTRIBUTE);
        if (versionatt instanceof String) {
            versionatt = new VersionRange((String) versionatt);
        }
        versionrange = (VersionRange) versionatt;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public VersionRange getVersionRange() {
        return versionrange;
    }

    @Override
    public boolean matches(Capability cap) {
        boolean matches = super.matches(cap);

        // match the package version range
        if (matches && versionrange != null) {
            Version version = ((XPackageCapability) cap).getVersion();
            matches = versionrange.includes(version);
        }

        // match package's bundle-symbolic-name
        String symbolicName = (String) getAttribute(BUNDLE_SYMBOLICNAME_ATTRIBUTE);
        if (matches && symbolicName != null) {
            String targetSymbolicName = null;
            Resource capres = cap.getResource();
            if (capres instanceof BundleRevision) {
                targetSymbolicName = ((BundleRevision) capres).getSymbolicName();
            }
            matches = symbolicName.equals(targetSymbolicName);
        }

        // match package's bundle-version
        String versionstr = (String) getAttribute(BUNDLE_VERSION_ATTRIBUTE);
        if (matches && versionstr != null) {
            Version targetVersion = null;
            Resource capres = cap.getResource();
            if (capres instanceof BundleRevision) {
                targetVersion = ((BundleRevision) capres).getVersion();
            }
            VersionRange versionRange = new VersionRange(versionstr);
            matches = targetVersion != null ? versionRange.includes(targetVersion) : false;
        }
        return matches;
    }
}