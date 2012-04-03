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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.PackageAttribute;
import org.jboss.osgi.metadata.Parameter;
import org.jboss.osgi.metadata.ParameterizedAttribute;
import org.jboss.osgi.resolver.ResourceBuilderException;
import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XBundleRequirement;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XHostCapability;
import org.jboss.osgi.resolver.XHostRequirement;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;

/**
 * A builder for resolver resources
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResourceBuilder implements XResourceBuilder {

    private final XResource resource;

    public AbstractResourceBuilder(XResourceBuilderFactory factory) {
        this.resource = factory.createResource();
    }

    @Override
    public XIdentityCapability addIdentityCapability(String symbolicName, Version version, String type, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        atts.put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version != null ? version : Version.emptyVersion);
        atts.put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, type != null ? type : IdentityNamespace.TYPE_UNKNOWN);
        XIdentityCapability cap = new AbstractIdentityCapability(resource, atts, dirs);
        addCapability(cap);
        ;
        return cap;
    }

    @Override
    public XBundleRequirement addBundleRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(BundleNamespace.BUNDLE_NAMESPACE, symbolicName);
        XBundleRequirement req = new AbstractBundleRequirement(resource, atts, dirs);
        addRequirement(req);
        return req;
    }

    @Override
    public XBundleCapability addBundleCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(BundleNamespace.BUNDLE_NAMESPACE, symbolicName);
        atts.put(BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, version != null ? version : Version.emptyVersion);
        XBundleCapability cap = new AbstractBundleCapability(resource, atts, dirs);
        addCapability(cap);
        ;
        return cap;
    }

    @Override
    public XHostCapability addHostCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(HostNamespace.HOST_NAMESPACE, symbolicName);
        atts.put(HostNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, version != null ? version : Version.emptyVersion);
        XHostCapability cap = new AbstractHostCapability(resource, atts, dirs);
        addCapability(cap);
        ;
        return cap;
    }

    @Override
    public XHostRequirement addHostRequirement(String symbolicName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(HostNamespace.HOST_NAMESPACE, symbolicName);
        XHostRequirement req = new AbstractHostRequirement(resource, atts, dirs);
        addRequirement(req);
        return req;
    }

    @Override
    public XPackageCapability addPackageCapability(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(PackageNamespace.PACKAGE_NAMESPACE, packageName);
        XPackageCapability cap = new AbstractPackageCapability(resource, atts, dirs);
        addCapability(cap);
        ;
        return cap;
    }

    @Override
    public XPackageRequirement addPackageRequirement(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(PackageNamespace.PACKAGE_NAMESPACE, packageName);
        XPackageRequirement req = new AbstractPackageRequirement(resource, atts, dirs);
        addRequirement(req);
        return req;
    }

    @Override
    public XPackageRequirement addDynamicPackageRequirement(String packageName, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        atts.put(PackageNamespace.PACKAGE_NAMESPACE, packageName);
        dirs.put(PackageNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE, PackageNamespace.RESOLUTION_DYNAMIC);
        XPackageRequirement req = new AbstractPackageRequirement(resource, atts, dirs);
        addRequirement(req);
        return req;
    }

    @Override
    public XCapability addGenericCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        XCapability cap;
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        if (IdentityNamespace.IDENTITY_NAMESPACE.equals(namespace)) {
            cap = new AbstractIdentityCapability(resource, atts, dirs);
        } else if (PackageNamespace.PACKAGE_NAMESPACE.equals(namespace)) {
            cap = new AbstractPackageCapability(resource, atts, dirs);
        } else if (HostNamespace.HOST_NAMESPACE.equals(namespace)) {
            cap = new AbstractHostCapability(resource, atts, dirs);
        } else {
            cap = new AbstractCapability(resource, namespace, atts, dirs);
        }
        addCapability(cap);
        ;
        return cap;
    }

    @Override
    public XRequirement addGenericRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        XRequirement req;
        atts = mutableAttributes(atts);
        dirs = mutableDirectives(dirs);
        if (IdentityNamespace.IDENTITY_NAMESPACE.equals(namespace)) {
            req = new AbstractBundleRequirement(resource, atts, dirs);
        } else if (PackageNamespace.PACKAGE_NAMESPACE.equals(namespace)) {
            req = new AbstractPackageRequirement(resource, atts, dirs);
        } else if (HostNamespace.HOST_NAMESPACE.equals(namespace)) {
            req = new AbstractHostRequirement(resource, atts, dirs);
        } else {
            req = new AbstractRequirement(resource, namespace, atts, dirs);
        }
        addRequirement(req);
        return req;
    }

    @Override
    public XResourceBuilder loadFrom(OSGiMetaData metadata) throws ResourceBuilderException {
        assertResourceCreated();
        try {
            String symbolicName = metadata.getBundleSymbolicName();
            Version bundleVersion = metadata.getBundleVersion();
            ParameterizedAttribute idparams = metadata.getBundleParameters();
            Map<String, Object> idatts = getAttributes(idparams);
            Map<String, String> iddirs = getDirectives(idparams);

            // Identity Capability
            ParameterizedAttribute fragmentHost = metadata.getFragmentHost();
            String identityType = fragmentHost != null ? IdentityNamespace.TYPE_FRAGMENT : IdentityNamespace.TYPE_BUNDLE;
            addIdentityCapability(symbolicName, bundleVersion, identityType, idatts, iddirs);

            // Bundle Capability
            if (IdentityNamespace.TYPE_BUNDLE.equals(identityType)) {
                Map<String, Object> atts = getAttributes(idparams);
                Map<String, String> dirs = getDirectives(idparams);
                addBundleCapability(symbolicName, bundleVersion, atts, dirs);
            }

            // Host Capability 
            if (fragmentHost != null) {
                String hostName = fragmentHost.getAttribute();
                Map<String, Object> atts = getAttributes(fragmentHost);
                Map<String, String> dirs = getDirectives(fragmentHost);
                addHostRequirement(hostName, atts, dirs);
            } else if (Constants.SYSTEM_BUNDLE_SYMBOLICNAME.equals(symbolicName) == false) {
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
                    addBundleRequirement(name, atts, dirs);
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
            throw new ResourceBuilderException("Cannot initialize XResource from: " + metadata, ex);
        }
        return this;
    }

    @Override
    public XResourceBuilder loadFrom(Module module) throws ResourceBuilderException {
        assertResourceCreated();
        ModuleIdentifier identifier = module.getIdentifier();
        String symbolicName = identifier.getName();
        Version version;
        try {
            version = Version.parseVersion(identifier.getSlot());
        } catch (IllegalArgumentException ex) {
            version = Version.emptyVersion;
        }

        // Add the identity capability
        addIdentityCapability(symbolicName, version, IdentityNamespace.TYPE_UNKNOWN, null, null);
        resource.addAttachment(Module.class, module);

        // Add a package capability for every exported path
        for (String path : module.getExportedPaths()) {
            if (path.startsWith("/"))
                path = path.substring(1);
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);
            if (!path.isEmpty() && !path.startsWith("META-INF")) {
                String packageName = path.replace('/', '.');
                addPackageCapability(packageName, null, null);
            }
        }
        return this;
    }

    @Override
    public XResource getResource() {
        return resource;
    }

    private void addCapability(XCapability cap) {
        if (resource instanceof AbstractResource) {
            ((AbstractResource) resource).addCapability(cap);
        }
    }

    private void addRequirement(XRequirement req) {
        if (resource instanceof AbstractResource) {
            ((AbstractResource) resource).addRequirement(req);
        }
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

    private Map<String, Object> mutableAttributes(Map<String, Object> atts) {
        return new HashMap<String, Object>(atts != null ? atts : EMPTY_ATTRIBUTES);
    }

    private Map<String, String> mutableDirectives(Map<String, String> dirs) {
        return new HashMap<String, String>(dirs != null ? dirs : EMPTY_DIRECTIVES);
    }

    private void assertResourceCreated() {
        if (resource == null)
            throw new IllegalStateException("Resource not created");
    }
}