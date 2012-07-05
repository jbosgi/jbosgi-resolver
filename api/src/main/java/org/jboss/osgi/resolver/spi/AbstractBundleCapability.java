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

import java.util.Map;

import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XBundleRevision;
import org.jboss.osgi.resolver.XResource;

/**
 * The abstract implementation of an {@link XBundleCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 30-May-2012
 */
public class AbstractBundleCapability extends AbstractCapability implements XBundleCapability {

    public AbstractBundleCapability(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        super(resource, namespace, atts, dirs);
    }

    @Override
    public XBundleRevision getResource() {
        return (XBundleRevision) super.getResource();
    }

    @Override
    public XBundleRevision getRevision() {
        return (XBundleRevision) super.getResource();
    }
}