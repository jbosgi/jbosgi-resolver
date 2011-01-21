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

import java.util.Map;

import org.jboss.osgi.resolver.XFragmentHostRequirement;
import org.jboss.osgi.resolver.XVersionRange;
import org.osgi.framework.Constants;

/**
 * The abstract implementation of a {@link XFragmentHostRequirement}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractFragmentHostRequirement extends AbstractRequirement implements XFragmentHostRequirement {
    private XVersionRange versionRange = XVersionRange.infiniteRange;
    private String extension;

    public AbstractFragmentHostRequirement(AbstractModule module, String symbolicName, Map<String, String> dirs, Map<String, Object> atts) {
        super(module, symbolicName, dirs, atts);

        Object att = getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE);
        if (att != null)
            versionRange = XVersionRange.parse(att.toString());

        String dir = getDirective(Constants.EXTENSION_DIRECTIVE);
        if (dir != null)
            extension = dir;
    }

    @Override
    public XVersionRange getVersionRange() {
        return versionRange;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return Constants.FRAGMENT_HOST + "[" + getName() + ":" + versionRange + "]";
    }
}