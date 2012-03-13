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

import static org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_BUNDLE_NAMESPACE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.resolver.XBundleCapability;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Resource;

/**
 * The abstract implementation of a {@link XBundleCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractBundleCapability extends AbstractCapability implements XBundleCapability {

    private final String symbolicName;
    private final Version version;

    protected AbstractBundleCapability(Resource res, Map<String, Object> atts, Map<String, String> dirs) {
        super(res, WIRING_BUNDLE_NAMESPACE, atts, dirs);
        this.symbolicName = (String) atts.get(WIRING_BUNDLE_NAMESPACE);
        this.version = (Version) atts.get(BUNDLE_VERSION_ATTRIBUTE);
    }

    @Override
    protected Set<String> getMandatoryAttributes() {
        return Collections.singleton(WIRING_BUNDLE_NAMESPACE);
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