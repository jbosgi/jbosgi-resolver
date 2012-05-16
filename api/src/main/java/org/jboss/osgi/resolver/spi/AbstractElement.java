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

import java.util.HashMap;
import java.util.Map;

import org.jboss.osgi.resolver.XAttachmentSupport;
import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XElement;

/**
 * The abstract implementation of a {@link XElement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractElement implements XElement {

    private XAttachmentSupport attachments;

    @Override
    public <T> T addAttachment(Class<T> clazz, T value) {
        if (attachments == null)
            attachments = new AttachmentSupporter();
        return attachments.addAttachment(clazz, value);
    }

    @Override
    public <T> T getAttachment(Class<T> clazz) {
        if (attachments == null)
            return null;
        return attachments.getAttachment(clazz);
    }

    @Override
    public <T> T removeAttachment(Class<T> clazz) {
        if (attachments == null)
            return null;
        return attachments.removeAttachment(clazz);
    }

    static class AttachmentSupporter implements XAttachmentSupport {
        private Map<Class<?>, Object> attachments;

        @Override
        public <T> T addAttachment(Class<T> clazz, T value) {
            if (attachments == null)
                attachments = new HashMap<Class<?>, Object>();

            T result = (T) attachments.get(clazz);
            attachments.put(clazz, value);
            return result;
        }

        @Override
        public <T> T getAttachment(Class<T> clazz) {
            if (attachments == null)
                return null;

            T result = (T) attachments.get(clazz);
            return result;
        }

        @Override
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
            if (attributes == null)
                attributes = new HashMap<String, Object>();
            return attributes;
        }

        public String toString() {
            return getAttributes().toString();
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
            if (directives == null)
                directives = new HashMap<String, String>();
            return directives;
        }

        public String toString() {
            return getDirectives().toString();
        }
    }
}