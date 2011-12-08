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
package org.jboss.osgi.resolver.spi;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.PackageAttribute;
import org.jboss.osgi.metadata.Parameter;
import org.jboss.osgi.metadata.ParameterizedAttribute;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.osgi.framework.resource.ResourceConstants.WIRING_BUNDLE_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * A builder for resolver resources
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResourceBuilder implements XResourceBuilder {

    private AbstractResource resource;

    @Override
    public XResourceBuilder createResource() {
        resource = new AbstractResource();
        return this;
    }

    @Override
    public XResourceBuilder createResource(OSGiMetaData metadata) throws BundleException {
        String symbolicName = metadata.getBundleSymbolicName();
        Version version = metadata.getBundleVersion();
        resource = new AbstractBundleRevision(symbolicName, version);
        load(metadata);
        return this;
    }

    @Override
    public Capability addIdentityCapability(String symbolicName, Version version) {
        assertModuleCreated();
        XCapability cap = new AbstractIdentityCapability(resource, symbolicName, version);
        if (resource instanceof BundleRevision) {
            BundleCapability bcap = new AbstractBundleCapability(cap);
            cap.addAttachment(BundleCapability.class, bcap);
        }
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs) {
        assertModuleCreated();
        atts.put(WIRING_BUNDLE_NAMESPACE, symbolicName);
        XRequirement req = new AbstractIdentityRequirement(resource, atts, dirs);
        if (resource instanceof BundleRevision) {
            BundleRequirement breq = new AbstractBundleRequirement(req);
            req.addAttachment(BundleRequirement.class, breq);
        }
        resource.addRequirement(req);
        return req;
    }

    @Override
    public Capability addPackageCapability(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertModuleCreated();
        atts.put(WIRING_PACKAGE_NAMESPACE, packageName);
        XCapability cap = new AbstractPackageCapability(resource, atts, dirs);
        if (resource instanceof BundleRevision) {
            BundleCapability bcap = new AbstractBundleCapability(cap);
            cap.addAttachment(BundleCapability.class, bcap);
        }
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public Requirement addPackageRequirement(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertModuleCreated();
        atts.put(WIRING_PACKAGE_NAMESPACE, packageName);
        XRequirement req = new AbstractPackageRequirement(resource, atts, dirs);
        if (resource instanceof BundleRevision) {
            BundleRequirement breq = new AbstractBundleRequirement(req);
            req.addAttachment(BundleRequirement.class, breq);
        }
        resource.addRequirement(req);
        return req;
    }

    @Override
    public Resource getResource() {
        try {
            return resource;
        } finally {
            resource = null;
        }
    }

    private void load(OSGiMetaData metadata) throws BundleException {
        try {
            String symbolicName = metadata.getBundleSymbolicName();
            Version bundleVersion = metadata.getBundleVersion();
            addIdentityCapability(symbolicName, bundleVersion);

            // Required Bundles
            List<ParameterizedAttribute> requireBundles = metadata.getRequireBundles();
            if (requireBundles != null && requireBundles.isEmpty() == false) {
                for (ParameterizedAttribute attribs : requireBundles) {
                    String name = attribs.getAttribute();
                    Map<String, String> dirs = getDirectives(attribs);
                    Map<String, Object> atts = getAttributes(attribs);
                    addIdentityRequirement(name, atts, dirs);
                }
            }

            // Export-Package
            List<PackageAttribute> exports = metadata.getExportPackages();
            if (exports != null && exports.isEmpty() == false) {
                for (PackageAttribute attribs : exports) {
                    String name = attribs.getAttribute();
                    Map<String, String> dirs = getDirectives(attribs);
                    Map<String, Object> atts = getAttributes(attribs);
                    addPackageCapability(name, atts, dirs);
                }
            }

            // Import-Package
            List<PackageAttribute> imports = metadata.getImportPackages();
            if (imports != null && imports.isEmpty() == false) {
                for (PackageAttribute attribs : imports) {
                    String name = attribs.getAttribute();
                    Map<String, String> dirs = getDirectives(attribs);
                    Map<String, Object> atts = getAttributes(attribs);
                    addPackageRequirement(name, atts, dirs);
                }
            }

            // DynamicImport-Package
            List<PackageAttribute> dynamicImports = metadata.getDynamicImports();
            if (dynamicImports != null && dynamicImports.isEmpty() == false) {
                for (PackageAttribute attribs : dynamicImports) {
                    String name = attribs.getAttribute();
                    Map<String, Object> atts = getAttributes(attribs);
                    //addDynamicPackageRequirement(name, atts);
                }
            }

            // Fragment-Host
            ParameterizedAttribute fragmentHost = metadata.getFragmentHost();
            if (fragmentHost != null) {
                String hostName = fragmentHost.getAttribute();
                Map<String, String> dirs = getDirectives(fragmentHost);
                Map<String, Object> atts = getAttributes(fragmentHost);
                //addFragmentHostRequirement(hostName, dirs, atts);
            }
        } catch (RuntimeException ex) {
            throw new BundleException("Cannot initialize XResource from: " + metadata, ex);
        }
    }

    private Map<String, String> getDirectives(ParameterizedAttribute attribs) {
        Map<String, String> dirs = new HashMap<String, String>();
        for (String key : attribs.getDirectives().keySet()) {
            Parameter param = attribs.getDirective(key);
            dirs.put(key.trim(), param.getValue().toString().trim());
        }
        return dirs;
    }

    private Map<String, Object> getAttributes(ParameterizedAttribute attribs) {
        Map<String, Object> atts = new HashMap<String, Object>();
        for (String key : attribs.getAttributes().keySet()) {
            Parameter param = attribs.getAttribute(key);
            atts.put(key.trim(), param.getValue().toString().trim());
        }
        return atts;
    }

    private void assertModuleCreated() {
        if (resource == null)
            throw new IllegalStateException("Resource not created");
    }
}