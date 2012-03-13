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

import org.jboss.osgi.resolver.XIdentityCapability;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.osgi.framework.Constants.SINGLETON_DIRECTIVE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_VERSION_ATTRIBUTE;

/**
 * The abstract implementation of a {@link XIdentityCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractIdentityCapability extends AbstractCapability implements XIdentityCapability {

    private final String symbolicName;
    private final Version version;
    private final boolean singleton;
    private final String type;

    protected AbstractIdentityCapability(Resource brev, Map<String, Object> atts, Map<String, String> dirs) {
        super(brev, IDENTITY_NAMESPACE, atts, dirs);
        this.symbolicName = (String) atts.get(IDENTITY_NAMESPACE);
        this.version = (Version) atts.get(IDENTITY_VERSION_ATTRIBUTE);
        this.type = (String) atts.get(IDENTITY_TYPE_ATTRIBUTE);
        this.singleton = Boolean.parseBoolean(dirs.get(SINGLETON_DIRECTIVE));
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        HashSet<String> result = new HashSet<String>();
        Collections.addAll(result, IDENTITY_NAMESPACE, IDENTITY_VERSION_ATTRIBUTE, IDENTITY_TYPE_ATTRIBUTE);
        return Collections.unmodifiableSet(result);
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String getType() {
        return type;
    }

    public boolean isSingleton() {
        return singleton;
    }
}