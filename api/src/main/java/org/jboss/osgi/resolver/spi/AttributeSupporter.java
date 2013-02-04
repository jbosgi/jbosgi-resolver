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

import org.jboss.osgi.resolver.XAttributeSupport;

/**
 * An implementation of {@link XAttributeSupport}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AttributeSupporter implements XAttributeSupport {
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