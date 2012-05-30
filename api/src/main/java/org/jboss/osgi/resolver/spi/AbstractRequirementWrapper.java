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

import java.util.Map;

import org.jboss.osgi.resolver.XRequirement;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

/**
 * A wrapper for {@link XRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 30-May-2012
 */
class AbstractRequirementWrapper implements XRequirement {

    final XRequirement delegate;

    public AbstractRequirementWrapper(XRequirement delegate) {
        if (delegate == null)
            throw MESSAGES.illegalArgumentNull("delegate");
        this.delegate = delegate;
    }

    public <T> T addAttachment(Class<T> clazz, T value) {
        return delegate.addAttachment(clazz, value);
    }

    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    public Map<String, String> getDirectives() {
        return delegate.getDirectives();
    }

    public void validate() {
        delegate.validate();
    }

    public Object getAttribute(String key) {
        return delegate.getAttribute(key);
    }

    public String getDirective(String key) {
        return delegate.getDirective(key);
    }

    public <T> T getAttachment(Class<T> clazz) {
        return delegate.getAttachment(clazz);
    }

    public <T> T removeAttachment(Class<T> clazz) {
        return delegate.removeAttachment(clazz);
    }

    public String getNamespace() {
        return delegate.getNamespace();
    }

    public Resource getResource() {
        return delegate.getResource();
    }

    @Override
    public boolean isOptional() {
        return delegate.isOptional();
    }

    @Override
    public <T extends XRequirement> T adapt(Class<T> clazz) {
        return delegate.adapt(clazz);
    }

    @Override
    public boolean matches(Capability cap) {
        return delegate.matches(cap);
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}