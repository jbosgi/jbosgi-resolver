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
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.v2.spi.AbstractResource;
import org.jboss.osgi.resolver.v2.spi.AbstractResourceBuilder;
import org.jboss.osgi.resolver.v2.spi.URLBasedResource;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static org.jboss.osgi.resolver.v2.XResourceConstants.CONTENT_PATH;
import static org.jboss.osgi.resolver.v2.XResourceConstants.CONTENT_URL;

/**
 * A builder for resolver modules
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class XResourceBuilder {

    public static final Map<String, Object> EMPTY_ATTRIBUTES = Collections.emptyMap();
    public static final Map<String, String> EMPTY_DIRECTIVES = Collections.emptyMap();

    protected AbstractResource resource;

    protected XResourceBuilder() {
        this.resource = new AbstractResource();
    }

    protected XResourceBuilder(XResource resource) {
        this.resource = (AbstractResource) resource;
    }

    /**
     * Create an empty resource builder
     */
    public static XResourceBuilder create() {
        return new AbstractResourceBuilder();
    }

    /**
     * Create an empty resource builder from a given resource.
     */
    public static XResourceBuilder create(XResource resource) {
        return new AbstractResourceBuilder(resource);
    }

    /**
     * Create a resource builder based on a given base URL and content path.
     */
    public static XResourceBuilder create(URL baseURL, String contentPath) {
        URLBasedResource resource = new URLBasedResource(baseURL, contentPath);
        AbstractResourceBuilder builder = new AbstractResourceBuilder(resource) {
            @Override
            public XIdentityCapability addIdentityCapability(String symbolicName, Version version, String type, Map<String, Object> atts, Map<String, String> dirs) {
                URLBasedResource urlres = (URLBasedResource) resource;
                atts.put(CONTENT_URL, urlres.getContentURL());
                atts.put(CONTENT_PATH, urlres.getContentPath());
                return super.addIdentityCapability(symbolicName, version, type, atts, dirs);
            }
        };
        InputStream content = resource.getContent();
        try {
            Manifest manifest = new JarInputStream(content).getManifest();
            OSGiMetaData metaData = OSGiMetaDataBuilder.load(manifest);
            builder.load(metaData);
        } catch (Exception ex) {
            URL contentURL = resource.getContentURL();
            throw new IllegalStateException("Cannot create capability from: " + contentURL, ex);
        } finally {
            if (content != null) {
                try {
                    content.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return builder;
    }

    /**
     * Create requirements/capabilities from OSGi metadata
     *
     * @param metadata The OSGi metadata
     */
    public abstract XResourceBuilder load(OSGiMetaData metadata) throws BundleException;

    /**
     * Add the identity capability
     *
     * @param symbolicName The resource symbolic name
     * @param version      The resource version
     * @param type         The resource type
     * @param atts         The attributes
     * @param dirs         The directives
     */
    public abstract XIdentityCapability addIdentityCapability(String symbolicName, Version version, String type, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add identity requirement
     *
     * @param symbolicName The bundle symbolic name
     * @param atts         The attributes
     * @param dirs         The directives
     */
    public abstract XBundleRequirement addBundleRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add the bundle capability
     *
     * @param symbolicName The resource symbolic name
     * @param version      The resource version
     * @param atts         The attributes
     * @param dirs         The directives
     */
    public abstract XBundleCapability addBundleCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add the fragment host capability
     *
     * @param symbolicName The resource symbolic name
     * @param version      The resource version
     * @param atts         The attributes
     * @param dirs         The directives
     */
    public abstract XHostCapability addHostCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add fragment host requirement
     *
     * @param symbolicName The bundle symbolic name
     * @param atts         The attributes
     * @param dirs         The directives
     */
    public abstract XHostRequirement addHostRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#EXPORT_PACKAGE} capability
     *
     * @param name The package name
     * @param atts The attributes
     * @param dirs The directives
     */
    public abstract XPackageCapability addPackageCapability(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#IMPORT_PACKAGE} requirement
     *
     * @param name The package name
     * @param atts The attributes
     * @param dirs The directives
     */
    public abstract XPackageRequirement addPackageRequirement(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Constants#DYNAMICIMPORT_PACKAGE} requirement
     *
     * @param name The package name
     * @param atts The attributes
     */
    public abstract XPackageRequirement addDynamicPackageRequirement(String name, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Capability}
     *
     * @param namespace The namespace
     * @param atts      The attributes
     * @param dirs      The directives
     */
    public abstract XCapability addGenericCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a generic {@link Requirement}
     *
     * @param namespace The namespace
     * @param atts      The attributes
     * @param dirs      The directives
     */
    public abstract XRequirement addGenericRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Get the final resource from the builder
     */
    public XResource getResource() {
        return resource;
    }
}