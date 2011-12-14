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
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE;
import static org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.Constants.MANDATORY_DIRECTIVE;
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

        if(super.matches(cap) == false)
            return false;

        // match the package version range
        if (versionrange != null) {
            Version version = ((XPackageCapability) cap).getVersion();
            if (versionrange.includes(version) == false)
                return false;
        }

        Map<String, Object> reqatts = new HashMap<String, Object> (getAttributes());
        Map<String, Object> capatts = new HashMap<String, Object> (cap.getAttributes());
        reqatts.remove(WIRING_PACKAGE_NAMESPACE);
        capatts.remove(WIRING_PACKAGE_NAMESPACE);
        reqatts.remove(VERSION_ATTRIBUTE);
        capatts.remove(VERSION_ATTRIBUTE);

        // match package's bundle-symbolic-name
        String symbolicName = (String) reqatts.remove(BUNDLE_SYMBOLICNAME_ATTRIBUTE);
        if (symbolicName != null) {
            XResource capres = (XResource) cap.getResource();
            XIdentityCapability idcap = capres.getIdentityCapability();
            String targetSymbolicName = idcap != null ? idcap.getSymbolicName() : null;
            if (symbolicName.equals(targetSymbolicName) == false)
                return false;
        }

        // match package's bundle-version
        String versionstr = (String) reqatts.remove(BUNDLE_VERSION_ATTRIBUTE);
        if (versionstr != null) {
            XResource capres = (XResource) cap.getResource();
            XIdentityCapability idcap = capres.getIdentityCapability();
            Version targetVersion = idcap != null ? idcap.getVersion() : null;
            VersionRange versionRange = new VersionRange(versionstr);
            if (targetVersion != null && versionRange.includes(targetVersion) == false)
                return false;
        }

        // match mandatory attributes on the capability
        String dirstr = ((XCapability) cap).getDirective(MANDATORY_DIRECTIVE);
        if (dirstr != null) {
            for (String att : dirstr.split(",")) {
                Object capval = capatts.remove(att);
                Object reqval = reqatts.remove(att);
                if (capval.equals(reqval) == false)
                    return false;
            }
        }

        // match package attributes on the requirement
        for (Map.Entry<String,Object> entry : reqatts.entrySet()) {
            String att = entry.getKey();
            Object reqval = entry.getValue();
            Object capval = capatts.remove(att);
            if (reqval.equals(capval) == false)
                return false;
        }


        return true;
    }
}