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
package org.jboss.osgi.resolver.v2.spi;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.PackageAttribute;
import org.jboss.osgi.metadata.Parameter;
import org.jboss.osgi.metadata.ParameterizedAttribute;
import org.jboss.osgi.resolver.v2.XCapability;
import org.jboss.osgi.resolver.v2.XRequirement;
import org.jboss.osgi.resolver.v2.XResource;
import org.jboss.osgi.resolver.v2.XResourceBuilder;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_BUNDLE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_FRAGMENT;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_UNKNOWN;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_HOST_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * A resource based on an URL
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public final class URLBasedResource extends AbstractBundleRevision {

    private final String contentPath;
    private final URL contentURL;

    public URLBasedResource(URL baseURL, String contentPath) {
        this.contentPath = contentPath;
        try {
            String base = baseURL.toExternalForm();
            if (!(base.endsWith("/") || contentPath.startsWith("/")))
                base += "/";
            this.contentURL = new URL(base + contentPath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public URL getContentURL() {
        return contentURL;
    }

    public String getContentPath() {
        return contentPath;
    }

    @Override
    public InputStream getContent() {
        try {
            if (contentURL.getProtocol().equals("file")) {
                return new FileInputStream(new File(contentURL.getPath()));
            } else {
                return contentURL.openStream();
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
