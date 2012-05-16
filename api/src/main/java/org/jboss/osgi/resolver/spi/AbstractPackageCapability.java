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

import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.PackageNamespace;

/**
 * The abstract implementation of a {@link XPackageCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractPackageCapability extends AbstractCapability implements XPackageCapability {

    private final String packageName;
    private final Version version;

    public AbstractPackageCapability(XResource res, Map<String, Object> attrs, Map<String, String> dirs) {
        super(res, PackageNamespace.PACKAGE_NAMESPACE, attrs, dirs);
        packageName = (String) attrs.get(PackageNamespace.PACKAGE_NAMESPACE);
        Object versionatt = attrs.get(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        if (versionatt instanceof Version)
            version = (Version) versionatt;
        else if (versionatt instanceof String)
            version = Version.parseVersion((String)versionatt);
        else
            version = Version.emptyVersion;
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
    public Version getVersion() {
        return version;
    }
}