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

import org.jboss.osgi.resolver.spi.AbstractBundleCapability;
import org.jboss.osgi.resolver.spi.AbstractBundleRequirement;
import org.jboss.osgi.resolver.spi.AbstractBundleRevision;

/**
 * A factory for resource builders.
 *
 * @author thomas.diesler@jboss.com
 * @since 30-Mar-2012
 */
public class XBundleRevisionBuilderFactory extends XResourceBuilderFactory {

    public static XResourceBuilder create(XBundleRevisionBuilderFactory factory) {
        return factory.createResourceBuilder();
    }

    public static XResourceBuilder create() {
        return new XBundleRevisionBuilderFactory().createResourceBuilder();
    }

    public XBundleRevision createResource() {
        return new AbstractBundleRevision();
    }

    public XBundleCapability createCapability(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        return new AbstractBundleCapability(resource, namespace, atts, dirs);
    }

    public XBundleRequirement createRequirement(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        return new AbstractBundleRequirement(resource, namespace, atts, dirs);
    }
}
