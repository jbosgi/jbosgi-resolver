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

import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.PackageNamespace;

/**
 * The abstract implementation of a {@link XPackageCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractPackageCapability extends AbstractCapabilityWrapper implements XPackageCapability {

    private final String packageName;
    private Version version;

    AbstractPackageCapability(XCapability delegate) {
        super(delegate);
        packageName = (String) delegate.getAttribute(PACKAGE_NAMESPACE);
        if (packageName == null)
            throw MESSAGES.illegalStateCannotObtainAttribute(PACKAGE_NAMESPACE);
        version = AbstractCapability.getVersion(delegate, PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        if (PACKAGE_NAMESPACE.equals(delegate.getNamespace()) == false)
            throw MESSAGES.illegalArgumentInvalidNamespace(delegate.getNamespace());
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