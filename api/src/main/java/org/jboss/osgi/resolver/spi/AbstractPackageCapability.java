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

import org.jboss.osgi.resolver.XPackageCapability;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Resource;

import java.util.Map;

import static org.osgi.framework.Constants.VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * The abstract implementation of a {@link XPackageCapability}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractPackageCapability extends AbstractCapability implements XPackageCapability {

    private final String packageName;
    private final Version version;

    protected AbstractPackageCapability(Resource resource, Map<String, Object> attributes, Map<String, String> directives) {
        super(resource, WIRING_PACKAGE_NAMESPACE, attributes, directives);
        packageName = (String) attributes.get(WIRING_PACKAGE_NAMESPACE);
        if (packageName == null)
            throw new IllegalArgumentException("Null packageName");
        String versionatt = (String) attributes.get(VERSION_ATTRIBUTE);
        version = versionatt != null ? Version.parseVersion(versionatt) : Version.emptyVersion;
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