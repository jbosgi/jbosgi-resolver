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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Resource;

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
        super(res, PackageNamespace.PACKAGE_NAMESPACE, attrs, dirs);
        packageName = (String) attrs.get(PackageNamespace.PACKAGE_NAMESPACE);
        Object versionatt = attrs.get(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        if (versionatt instanceof String) {
            versionatt = VersionRange.parse((String) versionatt);
        }
        versionrange = (VersionRange) versionatt;
        dynamic = PackageNamespace.RESOLUTION_DYNAMIC.equals(dirs.get(PackageNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE));
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        return Collections.singleton(PackageNamespace.PACKAGE_NAMESPACE);
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
    public boolean matchNamespaceValue(XCapability cap) {

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
    @SuppressWarnings("deprecation")
    public boolean matches(XCapability cap) {

        if(super.matches(cap) == false)
            return false;

        // match the package version range
        if (versionrange != null) {
            Version version = ((XPackageCapability) cap).getVersion();
            if (versionrange.isInRange(version) == false)
                return false;
        }

        Map<String, Object> reqatts = new HashMap<String, Object> (getAttributes());
        reqatts.remove(PackageNamespace.PACKAGE_NAMESPACE);
        reqatts.remove(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        reqatts.remove(Constants.PACKAGE_SPECIFICATION_VERSION);

        Map<String, Object> capatts = new HashMap<String, Object> (cap.getAttributes());
        capatts.remove(PackageNamespace.PACKAGE_NAMESPACE);
        capatts.remove(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        capatts.remove(Constants.PACKAGE_SPECIFICATION_VERSION);


        // match package's bundle-symbolic-name
        String symbolicName = (String) reqatts.remove(PackageNamespace.CAPABILITY_BUNDLE_SYMBOLICNAME_ATTRIBUTE);
        if (symbolicName != null) {
            XResource capres = (XResource) cap.getResource();
            XIdentityCapability idcap = capres.getIdentityCapability();
            String targetSymbolicName = idcap != null ? idcap.getSymbolicName() : null;
            if (symbolicName.equals(targetSymbolicName) == false)
                return false;
        }

        // match package's bundle-version
        String versionstr = (String) reqatts.remove(PackageNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        if (versionstr != null) {
            XResource capres = (XResource) cap.getResource();
            XIdentityCapability idcap = capres.getIdentityCapability();
            Version targetVersion = idcap != null ? idcap.getVersion() : null;
            VersionRange versionRange = VersionRange.parse(versionstr);
            if (targetVersion != null && versionRange.isInRange(targetVersion) == false)
                return false;
        }

        // match mandatory attributes on the capability
        String dirstr = ((XCapability) cap).getDirective(PackageNamespace.CAPABILITY_MANDATORY_DIRECTIVE);
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