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
import org.jboss.osgi.resolver.XResourceBuilder;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Resource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.osgi.resolver.XResourceBuilder.EMPTY_DIRECTIVES;
import static org.osgi.framework.Constants.VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_BUNDLE_NAMESPACE;

/**
 * The abstract implementation of a {@link XIdentityCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractIdentityCapability extends AbstractCapability implements XIdentityCapability {

    private final String symbolicName;
    private final Version version;

    protected AbstractIdentityCapability(Resource resource, String symbolicName, Version version) {
        super(resource, WIRING_BUNDLE_NAMESPACE, initAttributes(symbolicName, version), EMPTY_DIRECTIVES);
        this.symbolicName = symbolicName;
        if (symbolicName == null)
            throw new IllegalArgumentException("Null symbolicName");
        this.version = version;
    }
    
    private static Map<String, Object> initAttributes(String symbolicName, Version version) {
        if (symbolicName == null)
            throw new IllegalArgumentException("Null symbolic name");
        if (version == null)
            throw new IllegalArgumentException("Null version");
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put(WIRING_BUNDLE_NAMESPACE, symbolicName);
        atts.put(VERSION_ATTRIBUTE, version);
        return atts;
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