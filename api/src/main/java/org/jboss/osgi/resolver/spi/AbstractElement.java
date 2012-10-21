/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public abstract class AbstractElement implements XElement {

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

            @SuppressWarnings("unchecked")
            T result = (T) attachments.get(clazz);
            attachments.put(clazz, value);
            return result;
        }

        @Override
        public <T> T getAttachment(Class<T> clazz) {
            if (attachments == null)
                return null;

            @SuppressWarnings("unchecked")
            T result = (T) attachments.get(clazz);
            return result;
        }

        @Override
        public <T> T removeAttachment(Class<T> clazz) {
            if (attachments == null)
                return null;

            @SuppressWarnings("unchecked")
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

        @Override
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

        @Override
        public String toString() {
            return getDirectives().toString();
        }
    }
}
