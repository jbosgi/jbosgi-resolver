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
package org.jboss.osgi.resolver;

import java.util.Collections;
import java.util.Map;

import org.jboss.modules.Module;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * A builder for resources.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XResourceBuilder {

    Map<String, Object> EMPTY_ATTRIBUTES = Collections.emptyMap();
    Map<String, String> EMPTY_DIRECTIVES = Collections.emptyMap();

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
     * @param version      The resource version
     * @param type         The resource type
     * @param atts         The attributes
     * @param dirs         The directives
     */
    XIdentityCapability addIdentityCapability(String symbolicName, Version version, String type, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add identity requirement
     *
     * @param symbolicName The bundle symbolic name
     * @param atts         The attributes
     * @param dirs         The directives
     */
    XBundleRequirement addBundleRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add the bundle capability
     *
     * @param symbolicName The resource symbolic name
     * @param version      The resource version
     * @param atts         The attributes
     * @param dirs         The directives
     */
    XBundleCapability addBundleCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add the fragment host capability
     *
     * @param symbolicName The resource symbolic name
     * @param version      The resource version
     * @param atts         The attributes
     * @param dirs         The directives
     */
    XHostCapability addHostCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add fragment host requirement
     *
     * @param symbolicName The bundle symbolic name
     * @param atts         The attributes
     * @param dirs         The directives
     */
    XHostRequirement addHostRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#EXPORT_PACKAGE} capability
     *
     * @param name The package name
     * @param atts The attributes
     * @param dirs The directives
     */
    XPackageCapability addPackageCapability(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#IMPORT_PACKAGE} requirement
     *
     * @param name The package name
     * @param atts The attributes
     * @param dirs The directives
     */
    XPackageRequirement addPackageRequirement(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#DYNAMICIMPORT_PACKAGE} requirement
     *
     * @param name The package name
     * @param atts The attributes
     */
    XPackageRequirement addDynamicPackageRequirement(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Capability}
     *
     * @param namespace The namespace
     * @param atts      The attributes
     * @param dirs      The directives
     */
    XCapability addGenericCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Requirement}
     *
     * @param namespace The namespace
     * @param atts      The attributes
     * @param dirs      The directives
     */
    XRequirement addGenericRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Get the final resource from the builder
     */
    XResource getResource();
}