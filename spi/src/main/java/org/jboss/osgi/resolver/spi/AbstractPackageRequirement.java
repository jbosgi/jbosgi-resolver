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

import java.util.Map;
import java.util.Map.Entry;

import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XVersionRange;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * The abstract implementation of a {@link XPackageRequirement}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractPackageRequirement extends AbstractRequirement implements XPackageRequirement {
    private XVersionRange versionRange = XVersionRange.infiniteRange;
    private String resolution;

    public AbstractPackageRequirement(AbstractModule module, String name, Map<String, String> dirs, Map<String, Object> atts) {
        super(module, name, dirs, atts);

        String dir = getDirective(Constants.RESOLUTION_DIRECTIVE);
        resolution = (dir != null ? dir : Constants.RESOLUTION_MANDATORY);

        // A dynamic requirement is also optional
        setOptional(resolution.equals(Constants.RESOLUTION_OPTIONAL));

        Object att = getAttribute(Constants.VERSION_ATTRIBUTE);
        if (att != null)
            versionRange = XVersionRange.parse(att.toString());
    }

    @Override
    public String getResolution() {
        return resolution;
    }

    @Override
    public XVersionRange getVersionRange() {
        return versionRange;
    }

    @Override
    public String getBundleSymbolicName() {
        return (String) getAttribute(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE);
    }

    @Override
    public Version getBundleVersion() {
        Object att = getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE);
        return (att != null ? Version.parseVersion(att.toString()) : null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean match(XPackageCapability cap) {
        
        if (matchPackageName(cap) == false)
            return false;

        // Match the version range
        if (matchPackageVersion(cap) == false)
            return false;

        boolean validMatch = true;

        // Match attributes
        for (Entry<String, Object> entry : getAttributes().entrySet()) {
            String key = entry.getKey();
            String reqValue = (String) entry.getValue();
            
            if (Constants.VERSION_ATTRIBUTE.equals(key) || Constants.PACKAGE_SPECIFICATION_VERSION.equals(key))
                continue;
            
            if (Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE.equals(key)) {
                String capModuleName = cap.getModule().getName();
                if (reqValue.equals(capModuleName) == false) {
                    validMatch = false;
                    break;
                }
            } else {
                String capValue = (String) cap.getAttribute(key);
                if (reqValue.equals(capValue) == false) {
                    validMatch = false;
                    break;
                }
            }
        }

        return validMatch;
    }

    public boolean matchPackageName(XPackageCapability cap) {
        return getName().equals(cap.getName());
    }

    private boolean matchPackageVersion(XPackageCapability cap) {
        return getVersionRange().isInRange(cap.getVersion());
    }

    @Override
    public String toString() {
        return Constants.IMPORT_PACKAGE + "[" + getName() + ":" + versionRange + ";resolution:=" + resolution + "]";
    }
}