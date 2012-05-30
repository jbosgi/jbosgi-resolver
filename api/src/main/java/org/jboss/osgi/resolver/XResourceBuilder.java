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
public interface XResourceBuilder {

    /**
     * Create requirements/capabilities from OSGi metadata
     *
     * @param metadata The OSGi metadata
     */
    XResourceBuilder loadFrom(OSGiMetaData metadata) throws ResourceBuilderException;

    /**
     * Create requirements/capabilities from the given module.
     *
     * @param module The module
     */
    XResourceBuilder loadFrom(Module module) throws ResourceBuilderException;

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
    XResource getResource();
}