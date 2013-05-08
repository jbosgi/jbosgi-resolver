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

import org.jboss.modules.Module;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.osgi.framework.Filter;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * A builder for resources.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XResourceBuilder<T extends XResource> {

    /**
     * Create requirements/capabilities from OSGi metadata
     *
     * @param metadata The OSGi metadata
     */
    XResourceBuilder<T> loadFrom(OSGiMetaData metadata) throws ResourceBuilderException;

    /**
     * Create requirements/capabilities from the given module.
     *
     * @param module The module
     */
    XResourceBuilder<T> loadFrom(Module module) throws ResourceBuilderException;

    /**
     * Add a resource attributes
     */
    XResourceBuilder<T> addAttribute(String key, String value);
    
    /**
     * Add a {@link Capability}
     *
     * @param namespace The namespace
     * @param nsvalue The namespace value
     */
    XCapability addCapability(String namespace, String nsvalue);

    /**
     * Add a {@link Capability}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    XCapability addCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Requirement}
     *
     * @param namespace The namespace
     * @param nsvalue The namespace value
     */
    XRequirement addRequirement(String namespace, String nsvalue);

    /**
     * Add a {@link Requirement}
     *
     * @param namespace The namespace
     * @param filter The filter
     */
    XRequirement addRequirement(String namespace, Filter filter);

    /**
     * Add a {@link Requirement}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    XRequirement addRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Get the final resource from the builder
     */
    T getResource();
}
