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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;

/**
 * The abstract implementation of a {@link XIdentityCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractIdentityCapability extends AbstractCapability implements XIdentityCapability {

    private final String symbolicName;
    private Version version;

    protected AbstractIdentityCapability(XResource resource, Map<String, Object> atts, Map<String, String> dirs) {
        super(resource, IdentityNamespace.IDENTITY_NAMESPACE, atts, dirs);
        symbolicName = (String) atts.get(IdentityNamespace.IDENTITY_NAMESPACE);
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        HashSet<String> result = new HashSet<String>();
        Collections.addAll(result, IdentityNamespace.IDENTITY_NAMESPACE, IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        return Collections.unmodifiableSet(result);
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public Version getVersion() {
        if (version == null) {
            version = getVersion(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        }
        return version;
    }

    @Override
    public String getType() {
        return (String) getAttribute(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE);
    }

    public boolean isSingleton() {
        return Boolean.parseBoolean(getDirective(IdentityNamespace.CAPABILITY_SINGLETON_DIRECTIVE));
    }
}