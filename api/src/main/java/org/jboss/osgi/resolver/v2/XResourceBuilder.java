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
package org.jboss.osgi.resolver.v2;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.resolver.v2.spi.AbstractResourceBuilder;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;

import java.util.Collections;
import java.util.Map;

/**
 * A builder for resolver modules
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XResourceBuilder {

    static final Map<String, Object> EMPTY_ATTRIBUTES = Collections.emptyMap();
    static final Map<String, String> EMPTY_DIRECTIVES = Collections.emptyMap();

    XResourceBuilder INSTANCE = new AbstractResourceBuilder();

    /**
     * Create an empty resource builder
     */
    XResourceBuilder createResource();

    /**
     * Create an empty resource builder from a given resource.
     */
    XResourceBuilder associateResource(XResource resource);

    /**
     * Create requirements/capabilities from OSGi metadata
     * 
     * @param metadata The OSGi metadata
     */
    XResourceBuilder load(OSGiMetaData metadata) throws BundleException;

    /**
     * Add the identity capability
     * 
     * @param symbolicName The resource symbolic name
     * @param version The resource version
     * @param type The resource type
     * @param atts The attributes
     * @param dirs The directives
     */
    XCapability addIdentityCapability(String symbolicName, Version version, String type, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add identity requirement
     *
     * @param symbolicName The bundle symbolic name
     * @param atts The attributes
     * @param dirs The directives
     */
    XRequirement addIdentityRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add the fragment host capability
     *
     * @param symbolicName The resource symbolic name
     * @param version The resource version
     * @param atts The attributes
     * @param dirs The directives
     */
    XCapability addFragmentHostCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add fragment host requirement
     *
     * @param symbolicName The bundle symbolic name
     * @param atts The attributes
     * @param dirs The directives
     */
    XRequirement addFragmentHostRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#EXPORT_PACKAGE} capability
     *
     * @param name The package name
     * @param atts The attributes
     * @param dirs The directives
     */
    XCapability addPackageCapability(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#IMPORT_PACKAGE} requirement
     *
     * @param name The package name
     * @param atts The attributes
     * @param dirs The directives
     */
    XRequirement addPackageRequirement(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Capability}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    XCapability addGenericCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Requirement}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    XRequirement addGenericRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Get the final resource from the builder
     */
    XResource getResource();
}