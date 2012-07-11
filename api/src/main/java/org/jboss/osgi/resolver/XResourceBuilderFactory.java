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

package org.jboss.osgi.resolver;

import java.util.Map;

import org.jboss.osgi.resolver.spi.AbstractCapability;
import org.jboss.osgi.resolver.spi.AbstractRequirement;
import org.jboss.osgi.resolver.spi.AbstractResource;
import org.jboss.osgi.resolver.spi.AbstractResourceBuilder;

/**
 * A factory for resource builders.
 *
 * @author thomas.diesler@jboss.com
 * @since 15-Mar-2012
 */
public class XResourceBuilderFactory {

    public static XResourceBuilder create(XResourceBuilderFactory factory) {
        return factory.createResourceBuilder();
    }

    public static XResourceBuilder create() {
        return new XResourceBuilderFactory().createResourceBuilder();
    }

    public XResourceBuilder createResourceBuilder() {
        return new AbstractResourceBuilder(this);
    }

    public XResource createResource() {
        return new AbstractResource();
    }

    public XCapability createCapability(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        return new AbstractCapability(resource, namespace, atts, dirs);
    }

    public XRequirement createRequirement(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        return new AbstractRequirement(resource, namespace, atts, dirs);
    }
}
