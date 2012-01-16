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

import org.jboss.osgi.resolver.XCapability;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

import java.util.Map;

/**
 * The abstract implementation of a {@link BundleRequirement}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractBundleRequirement extends AbstractRequirement implements BundleRequirement {

    private Filter filter;

    protected AbstractBundleRequirement(BundleRevision brev, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        super(brev, namespace, atts, dirs);
        String filterdir = getDirective(Constants.FILTER_DIRECTIVE);
        if (filterdir != null) {
            try {
                filter = FrameworkUtil.createFilter(filterdir);
            } catch (InvalidSyntaxException e) {
                throw new IllegalArgumentException("Invalid filter directive: " + filterdir);
            }
        }
    }

    @Override
    public BundleRevision getRevision() {
        return (BundleRevision) super.getResource();
    }

    @Override
    public BundleRevision getResource() {
        return (BundleRevision) super.getResource();
    }

    @Override
    public boolean matches(BundleCapability cap) {
        String namespace = getNamespace();
        boolean matches = namespace.equals(cap.getNamespace());

        // match namespace value
        if (matches) {
            XCapability xcap = (XCapability) cap;
            Object thisatt = getAttribute(namespace);
            Object otheratt = xcap.getAttribute(namespace);
            matches = thisatt.equals(otheratt);
        }

        // match filter
        if (matches && filter != null) {
            matches = filter.matches(cap.getAttributes());
        }

        return matches;
    }
}