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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.osgi.resolver.XAttachmentSupport;
import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XElement;

/**
 * The abstract implementation of an {@link XElement}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractElement implements XElement {
    private String name;

    AbstractElement(String name) {
        if (name == null)
            throw new IllegalArgumentException("Null name");
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    static class AttachmentSupporter implements XAttachmentSupport {
        private Map<Class<?>, Object> attachments;

        @Override
        @SuppressWarnings("unchecked")
        public <T> T addAttachment(Class<T> clazz, T value) {
            if (attachments == null)
                attachments = new HashMap<Class<?>, Object>();

            T result = (T) attachments.get(clazz);
            attachments.put(clazz, value);
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getAttachment(Class<T> clazz) {
            if (attachments == null)
                return null;

            T result = (T) attachments.get(clazz);
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T removeAttachment(Class<T> clazz) {
            if (attachments == null)
                return null;

            T result = (T) attachments.remove(clazz);
            return result;
        }
    }

    static class AttributeSupporter implements XAttributeSupport {
        private Map<String, Object> attributes;

        AttributeSupporter(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Object getAttribute(String key) {
            return attributes != null ? attributes.get(key) : null;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return Collections.unmodifiableMap(attributes);
        }
    }

    static class DirectiveSupporter implements XDirectiveSupport {
        private Map<String, String> directives;

        DirectiveSupporter(Map<String, String> directives) {
            this.directives = directives;
        }

        @Override
        public String getDirective(String key) {
            return directives != null ? directives.get(key) : null;
        }

        @Override
        public Map<String, String> getDirectives() {
            return Collections.unmodifiableMap(directives);
        }
    }
}