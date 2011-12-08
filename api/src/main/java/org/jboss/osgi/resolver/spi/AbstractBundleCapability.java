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
import org.osgi.framework.resource.Capability;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;

import java.util.Map;

/**
 * The abstract implementation of a {@link BundleCapability}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractBundleCapability extends AbstractElement implements XCapability, BundleCapability {

    private final XCapability delegate;

    protected AbstractBundleCapability(XCapability delegate) {
        addAttachment(Capability.class, delegate);
        this.delegate = delegate;
    }

    @Override
    public String getNamespace() {
        return delegate.getNamespace();
    }

    @Override
    public Map<String, String> getDirectives() {
        return delegate.getDirectives();
    }

    @Override
    public String getDirective(String key) {
        return delegate.getDirective(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Object getAttribute(String key) {
        return delegate.getAttribute(key);
    }

    @Override
    public BundleRevision getRevision() {
        return (BundleRevision) delegate.getResource();
    }

    @Override
    public BundleRevision getResource() {
        return (BundleRevision) delegate.getResource();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + delegate.toString();
    }
}