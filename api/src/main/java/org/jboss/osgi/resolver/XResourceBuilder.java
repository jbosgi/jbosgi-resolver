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
import org.osgi.framework.Constants;
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
     * Add the identity capability
     *
     * @param symbolicName The resource symbolic name
     */
    XIdentityCapability addIdentityCapability(String symbolicName);

    /**
     * Add the bundle capability
     *
     * @param symbolicName The resource symbolic name
     */
    XBundleCapability addBundleCapability(String symbolicName);

    /**
     * Add the fragment host capability
     *
     * @param symbolicName The resource symbolic name
     */
    XHostCapability addHostCapability(String symbolicName);

    /**
     * Add a {@link Constants#EXPORT_PACKAGE} capability
     *
     * @param name The package name
     */
    XPackageCapability addPackageCapability(String name);

    /**
     * Add a generic {@link Capability}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    XCapability addGenericCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Capability}
     *
     * @param namespace The namespace
     * @param nsvalue The namespace value
     */
    XCapability addGenericCapability(String namespace, String nsvalue);

    /**
     * Add bundle requirement
     *
     * @param symbolicName The bundle symbolic name
     */
    XBundleRequirement addBundleRequirement(String symbolicName);

    /**
     * Add fragment host requirement
     *
     * @param symbolicName The bundle symbolic name
     */
    XHostRequirement addHostRequirement(String symbolicName);

    /**
     * Add a package requirement
     *
     * @param name The package name
     */
    XPackageRequirement addPackageRequirement(String name);

    /**
     * Add a dynamic package requirement
     *
     * @param name The package name
     */
    XPackageRequirement addDynamicPackageRequirement(String name);

    /**
     * Add a generic {@link Requirement}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    XRequirement addGenericRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Requirement}
     *
     * @param namespace The namespace
     */
    XRequirement addGenericRequirement(String namespace);

    /**
     * Get the final resource from the builder
     */
    XResource getResource();
}