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

import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE;
import static org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.Constants.MANDATORY_DIRECTIVE;
import static org.osgi.framework.Constants.PACKAGE_SPECIFICATION_VERSION;
import static org.osgi.framework.Constants.VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.REQUIREMENT_RESOLUTION_DIRECTIVE;
import static org.osgi.framework.resource.ResourceConstants.REQUIREMENT_RESOLUTION_DYNAMIC;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.v2.XCapability;
import org.jboss.osgi.resolver.v2.XIdentityCapability;
import org.jboss.osgi.resolver.v2.XPackageCapability;
import org.jboss.osgi.resolver.v2.XPackageRequirement;
import org.jboss.osgi.resolver.v2.XResource;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;

/**
 * The abstract implementation of a {@link XPackageRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractPackageRequirement extends AbstractRequirement implements XPackageRequirement {

    private final String packageName;
    private final VersionRange versionrange;
    private final boolean dynamic;

    public AbstractPackageRequirement(Resource res, Map<String, Object> attrs, Map<String, String> dirs) {
        super(res, WIRING_PACKAGE_NAMESPACE, attrs, dirs);
        packageName = (String) attrs.get(WIRING_PACKAGE_NAMESPACE);
        Object versionatt = attrs.get(VERSION_ATTRIBUTE);
        if (versionatt instanceof String) {
            versionatt = VersionRange.parse((String) versionatt);
        }
        versionrange = (VersionRange) versionatt;
        dynamic = REQUIREMENT_RESOLUTION_DYNAMIC.equals(dirs.get(REQUIREMENT_RESOLUTION_DIRECTIVE));
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        return Collections.singleton(WIRING_PACKAGE_NAMESPACE);
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
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public boolean matchNamespaceValue(Capability cap) {

        String packageName = getPackageName();
        if (packageName.equals("*"))
            return true;

        XPackageCapability xcap = (XPackageCapability) cap;
        if (packageName.endsWith(".*")) {
            packageName = packageName.substring(0, packageName.length() - 2);
            return xcap.getPackageName().startsWith(packageName);
        }
        else
        {
            return packageName.equals(xcap.getPackageName());
        }
    }

    @Override
    public boolean matches(Capability cap) {

        if(super.matches(cap) == false)
            return false;

        // match the package version range
        if (versionrange != null) {
            Version version = ((XPackageCapability) cap).getVersion();
            if (versionrange.isInRange(version) == false)
                return false;
        }

        Map<String, Object> reqatts = new HashMap<String, Object> (getAttributes());
        Map<String, Object> capatts = new HashMap<String, Object> (cap.getAttributes());
        reqatts.remove(WIRING_PACKAGE_NAMESPACE);
        capatts.remove(WIRING_PACKAGE_NAMESPACE);
        reqatts.remove(PACKAGE_SPECIFICATION_VERSION);
        capatts.remove(PACKAGE_SPECIFICATION_VERSION);
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
            VersionRange versionRange = VersionRange.parse(versionstr);
            if (targetVersion != null && versionRange.isInRange(targetVersion) == false)
                return false;
        }

        // match mandatory attributes on the capability
        String dirstr = ((XCapability) cap).getDirective(MANDATORY_DIRECTIVE);
        if (dirstr != null) {
            for (String att : dirstr.split(",")) {
                Object capval = capatts.remove(att);
                if (capval != null) {
                    Object reqval = reqatts.remove(att);
                    if (!capval.equals(reqval))
                        return false;
                }
            }
        }

        // match package attributes on the requirement
        for (Map.Entry<String,Object> entry : reqatts.entrySet()) {
            String att = entry.getKey();
            Object reqval = entry.getValue();
            Object capval = capatts.remove(att);
            if (!reqval.equals(capval))
                return false;
        }

        return true;
    }
}