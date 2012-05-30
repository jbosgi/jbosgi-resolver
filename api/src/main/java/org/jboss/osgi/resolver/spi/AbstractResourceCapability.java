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

import org.jboss.osgi.resolver.XResourceCapability;
import org.jboss.osgi.resolver.XCapability;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.BundleNamespace;

/**
 * The abstract implementation of a {@link XResourceCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractResourceCapability extends AbstractCapabilityWrapper implements XResourceCapability {

    private final String symbolicName;
    private Version version;

    AbstractResourceCapability(XCapability delegate) {
        super(delegate);
        symbolicName = (String) delegate.getAttribute(BundleNamespace.BUNDLE_NAMESPACE);
        version = AbstractCapability.getVersion(delegate, BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        if (BundleNamespace.BUNDLE_NAMESPACE.equals(delegate.getNamespace()) == false)
            throw MESSAGES.illegalArgumentInvalidNamespace(delegate.getNamespace());
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public Version getVersion() {
        return version;
    }
}