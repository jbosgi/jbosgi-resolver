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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.osgi.framework.Constants.BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.Constants.RESOLUTION_DIRECTIVE;
import static org.osgi.framework.Constants.SYSTEM_BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_BUNDLE;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_FRAGMENT;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_UNKNOWN;
import static org.osgi.framework.resource.ResourceConstants.IDENTITY_VERSION_ATTRIBUTE;
import static org.osgi.framework.resource.ResourceConstants.REQUIREMENT_RESOLUTION_OPTIONAL;
import static org.osgi.framework.resource.ResourceConstants.WIRING_HOST_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * A builder for resolver resources
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResourceBuilder extends XResourceBuilder {

    public AbstractResourceBuilder(XResource resource) {
        super(resource);
    }

    public AbstractResourceBuilder() {
    }

    @Override
    public XCapability addIdentityCapability(String symbolicName, Version version, String type, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts.put(IDENTITY_NAMESPACE, symbolicName);
        atts.put(IDENTITY_VERSION_ATTRIBUTE, version);
        atts.put(IDENTITY_TYPE_ATTRIBUTE, type != null ? type : IDENTITY_TYPE_UNKNOWN);
        XCapability cap = new AbstractIdentityCapability(resource, atts, dirs);
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public XRequirement addIdentityRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts.put(IDENTITY_NAMESPACE, symbolicName);
        XRequirement req = new AbstractIdentityRequirement(resource, atts, dirs);
        resource.addRequirement(req);
        return req;
    }

    @Override
    public XCapability addHostCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts.put(WIRING_HOST_NAMESPACE, symbolicName);
        atts.put(BUNDLE_VERSION_ATTRIBUTE, version);
        XCapability cap = new AbstractHostCapability(resource, atts, dirs);
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public XRequirement addHostRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts.put(WIRING_HOST_NAMESPACE, symbolicName);
        XRequirement req = new AbstractHostRequirement(resource, atts, dirs);
        resource.addRequirement(req);
        return req;
    }

    @Override
    public XCapability addPackageCapability(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts.put(WIRING_PACKAGE_NAMESPACE, packageName);
        XCapability cap = new AbstractPackageCapability(resource, atts, dirs);
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public XRequirement addPackageRequirement(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts.put(WIRING_PACKAGE_NAMESPACE, packageName);
        XRequirement req = new AbstractPackageRequirement(resource, atts, dirs, false);
        resource.addRequirement(req);
        return req;
    }

    @Override
    public XRequirement addDynamicPackageRequirement(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts.put(WIRING_PACKAGE_NAMESPACE, packageName);
        dirs.put(RESOLUTION_DIRECTIVE, REQUIREMENT_RESOLUTION_OPTIONAL);
        XRequirement req = new AbstractPackageRequirement(resource, atts, dirs, true);
        resource.addRequirement(req);
        return req;
    }

    @Override
    public XCapability addGenericCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        XCapability cap;
        if (IDENTITY_NAMESPACE.equals(namespace)) {
            cap = new AbstractIdentityCapability(resource, atts, dirs);
        } else if (WIRING_PACKAGE_NAMESPACE.equals(namespace)) {
            cap = new AbstractPackageCapability(resource, atts, dirs);
        } else if (WIRING_HOST_NAMESPACE.equals(namespace)) {
            cap = new AbstractHostCapability(resource, atts, dirs);
        } else {
            cap = new AbstractCapability(resource, namespace, atts, dirs);
        }
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public XRequirement addGenericRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        XRequirement req;
        if (IDENTITY_NAMESPACE.equals(namespace)) {
            req = new AbstractIdentityRequirement(resource, atts, dirs);
        } else if (WIRING_PACKAGE_NAMESPACE.equals(namespace)) {
            String packageName = (String) atts.get(namespace);
            req = new AbstractPackageRequirement(resource, atts, dirs, packageName.endsWith("*"));
        } else if (WIRING_HOST_NAMESPACE.equals(namespace)) {
            req = new AbstractHostRequirement(resource, atts, dirs);
        } else {
            req = new AbstractRequirement(resource, namespace, atts, dirs);
        }
        resource.addRequirement(req);
        return req;
    }

    @Override
    public XResourceBuilder load(OSGiMetaData metadata) throws BundleException {
        assertResourceCreated();
        try {
            String symbolicName = metadata.getBundleSymbolicName();
            Version bundleVersion = metadata.getBundleVersion();
            ParameterizedAttribute idparams = metadata.getBundleParameters();
            Map<String, Object> idatts = getAttributes(idparams);
            Map<String, String> iddirs = getDirectives(idparams);

            // Identity Capability
            ParameterizedAttribute fragmentHost = metadata.getFragmentHost();
            String identityType = fragmentHost != null ? IDENTITY_TYPE_FRAGMENT : IDENTITY_TYPE_BUNDLE;
            addIdentityCapability(symbolicName, bundleVersion, identityType, idatts, iddirs);
            
            // Host Capability 
            if (fragmentHost != null) {
                String hostName = fragmentHost.getAttribute();
                Map<String, Object> atts = getAttributes(fragmentHost);
                Map<String, String> dirs = getDirectives(fragmentHost);
                addHostRequirement(hostName, atts, dirs);
            } else if (SYSTEM_BUNDLE_SYMBOLICNAME.equals(symbolicName) == false) {
                Map<String, Object> atts = getAttributes(idparams);
                Map<String, String> dirs = getDirectives(idparams);
                addHostCapability(symbolicName, bundleVersion, atts, dirs);
            }

            // Required Bundles
            List<ParameterizedAttribute> requireBundles = metadata.getRequireBundles();
            if (requireBundles != null && requireBundles.isEmpty() == false) {
                for (ParameterizedAttribute attr : requireBundles) {
                    String name = attr.getAttribute();
                    Map<String, Object> atts = getAttributes(attr);
                    Map<String, String> dirs = getDirectives(attr);
                    addIdentityRequirement(name, atts, dirs);
                }
            }

            // Export-Package
            List<PackageAttribute> exports = metadata.getExportPackages();
            if (exports != null && exports.isEmpty() == false) {
                for (PackageAttribute attr : exports) {
                    String name = attr.getAttribute();
                    Map<String, Object> atts = getAttributes(attr);
                    Map<String, String> dirs = getDirectives(attr);
                    addPackageCapability(name, atts, dirs);
                }
            }

            // Import-Package
            List<PackageAttribute> imports = metadata.getImportPackages();
            if (imports != null && imports.isEmpty() == false) {
                for (PackageAttribute attr : imports) {
                    String name = attr.getAttribute();
                    Map<String, Object> atts = getAttributes(attr);
                    Map<String, String> dirs = getDirectives(attr);
                    addPackageRequirement(name, atts, dirs);
                }
            }

            // DynamicImport-Package
            List<PackageAttribute> dynamicImports = metadata.getDynamicImports();
            if (dynamicImports != null && dynamicImports.isEmpty() == false) {
                for (PackageAttribute attr : dynamicImports) {
                    String name = attr.getAttribute();
                    Map<String, Object> atts = getAttributes(attr);
                    Map<String, String> dirs = getDirectives(attr);
                    addDynamicPackageRequirement(name, atts, dirs);
                }
            }

        } catch (RuntimeException ex) {
            throw new BundleException("Cannot initialize XResource from: " + metadata, ex);
        }
        return this;
    }

    private Map<String, String> getDirectives(ParameterizedAttribute patts) {
        Map<String, String> dirs = new HashMap<String, String>();
        if (patts != null) {
            for (String key : patts.getDirectives().keySet()) {
                String value = patts.getDirectiveValue(key, String.class);
                dirs.put(key.trim(), value.trim());
            }
        }
        return dirs;
    }

    private Map<String, Object> getAttributes(ParameterizedAttribute patts) {
        Map<String, Object> atts = new HashMap<String, Object>();
        if (patts != null) {
            for (String key : patts.getAttributes().keySet()) {
                Parameter param = patts.getAttribute(key);
                atts.put(key.trim(), param.getValue().toString().trim());
            }
        }
        return atts;
    }

    private void assertResourceCreated() {
        if (resource == null)
            throw new IllegalStateException("Resource not created");
    }
}