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

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResourceRequirement;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.resource.Capability;

/**
 * The abstract implementation of a {@link XResourceRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractResourceRequirement extends AbstractRequirementWrapper implements XResourceRequirement {

    private final String symbolicName;
    private final VersionRange versionrange;
    private final String visibility;

    AbstractResourceRequirement(XRequirement delegate) {
        super(delegate);
        symbolicName = AbstractRequirement.getNamespaceValue(delegate);
        versionrange = AbstractRequirement.getVersionRange(delegate, BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        visibility = getDirective(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE);
        if (BundleNamespace.BUNDLE_NAMESPACE.equals(delegate.getNamespace()) == false)
            throw MESSAGES.illegalArgumentInvalidNamespace(delegate.getNamespace());
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
    public boolean matches(Capability cap) {

        // match the namespace value
        String nsvalue = (String) getAttribute(getNamespace());
        if (nsvalue != null && !nsvalue.equals(cap.getAttributes().get(getNamespace())))
            return false;

        // cannot require itself
        if (getResource() == cap.getResource())
            return false;

        // match the bundle version range
        if (getVersionRange() != null) {
            Version version = AbstractCapability.getVersion(cap, BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
            if (getVersionRange().isInRange(version) == false)
                return false;
        }

        return true;
    }
}