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

package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.metadata.OSGiMetaData.ANONYMOUS_BUNDLE_SYMBOLIC_NAME;
import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;

import java.util.Collections;
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
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.AbstractWiringNamespace;
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

    private final XResourceBuilderFactory factory;
    private final XResource resource;

    public AbstractResourceBuilder(XResourceBuilderFactory factory) {
        if (factory == null)
            throw MESSAGES.illegalArgumentNull("factory");
        this.factory = factory;
        this.resource = factory.createResource();
    }

    @Override
    public XCapability addCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        XCapability cap = factory.createCapability(resource, namespace, mutableAttributes(atts), mutableDirectives(dirs));
        addCapability(cap);
        return cap;
    }

    @Override
    public XCapability addCapability(String namespace, String nsvalue) {
        return addCapability(namespace, Collections.singletonMap(namespace, (Object)nsvalue), null);
    }

    @Override
    public XRequirement addRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        assertResourceCreated();
        XRequirement req = factory.createRequirement(resource, namespace, mutableAttributes(atts), mutableDirectives(dirs));
        addRequirement(req);
        return req;
    }

    @Override
    public XRequirement addRequirement(String namespace, Filter filter) {
        assertResourceCreated();
        Map<String, String> dirs = new HashMap<String, String>();
        if (filter != null)
            dirs.put(AbstractWiringNamespace.REQUIREMENT_FILTER_DIRECTIVE, filter.toString());
        return addRequirement(namespace, null, dirs);
    }

    @Override
    public XRequirement addRequirement(String namespace, String nsvalue) {
        assertResourceCreated();
        Map<String, Object> atts = new HashMap<String, Object>();
        if (nsvalue != null)
            atts.put(namespace, nsvalue);
        return addRequirement(namespace, atts, null);
    }

    @Override
    public XResourceBuilder loadFrom(OSGiMetaData metadata) throws ResourceBuilderException {
        assertResourceCreated();
        try {
            String symbolicName = metadata.getBundleSymbolicName();
            Version bundleVersion = metadata.getBundleVersion();
            ParameterizedAttribute idparams = metadata.getBundleParameters();

            if (symbolicName == null)
                symbolicName = ANONYMOUS_BUNDLE_SYMBOLIC_NAME;

            // Identity Capability
            ParameterizedAttribute fragmentHost = metadata.getFragmentHost();
            String identityType = fragmentHost != null ? IdentityNamespace.TYPE_FRAGMENT : IdentityNamespace.TYPE_BUNDLE;
            XCapability icap = addCapability(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, bundleVersion);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, identityType);
            icap.getAttributes().putAll(getAttributes(idparams));
            icap.getDirectives().putAll(getDirectives(idparams));

            // Bundle Capability
            if (IdentityNamespace.TYPE_BUNDLE.equals(identityType)) {
                XCapability cap = addCapability(BundleNamespace.BUNDLE_NAMESPACE, symbolicName);
                cap.getAttributes().put(BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, bundleVersion);
                cap.getAttributes().putAll(getAttributes(idparams));
                cap.getDirectives().putAll(getDirectives(idparams));
            }

            // Host Capability
            if (fragmentHost != null) {
                String hostName = fragmentHost.getAttribute();
                XRequirement req = addRequirement(HostNamespace.HOST_NAMESPACE, hostName);
                req.getAttributes().putAll(getAttributes(fragmentHost));
                req.getDirectives().putAll(getDirectives(fragmentHost));
            } else if (Constants.SYSTEM_BUNDLE_SYMBOLICNAME.equals(symbolicName) == false) {
                XCapability cap = addCapability(HostNamespace.HOST_NAMESPACE, symbolicName);
                cap.getAttributes().put(HostNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, bundleVersion);
                cap.getAttributes().putAll(getAttributes(idparams));
                cap.getDirectives().putAll(getDirectives(idparams));
            }

            // Required Bundles
            List<ParameterizedAttribute> requireBundles = metadata.getRequireBundles();
            if (requireBundles != null && requireBundles.isEmpty() == false) {
                for (ParameterizedAttribute attr : requireBundles) {
                    String bundleName = attr.getAttribute();
                    XRequirement req = addRequirement(BundleNamespace.BUNDLE_NAMESPACE, bundleName);
                    req.getAttributes().putAll(getAttributes(attr));
                    req.getDirectives().putAll(getDirectives(attr));
                }
            }

            // Export-Package
            List<PackageAttribute> exports = metadata.getExportPackages();
            if (exports != null && exports.isEmpty() == false) {
                for (PackageAttribute attr : exports) {
                    String packageName = attr.getAttribute();
                    XCapability cap = addCapability(PackageNamespace.PACKAGE_NAMESPACE, packageName);
                    cap.getAttributes().putAll(getAttributes(attr));
                    cap.getDirectives().putAll(getDirectives(attr));
                }
            }

            // Import-Package
            List<PackageAttribute> imports = metadata.getImportPackages();
            if (imports != null && imports.isEmpty() == false) {
                for (PackageAttribute attr : imports) {
                    String packageName = attr.getAttribute();
                    XRequirement req = addRequirement(PackageNamespace.PACKAGE_NAMESPACE, packageName);
                    req.getAttributes().putAll(getAttributes(attr));
                    req.getDirectives().putAll(getDirectives(attr));
                }
            }

            // DynamicImport-Package
            List<PackageAttribute> dynamicImports = metadata.getDynamicImports();
            if (dynamicImports != null && dynamicImports.isEmpty() == false) {
                for (PackageAttribute attr : dynamicImports) {
                    String packageName = attr.getAttribute();
                    Map<String, Object> atts = new HashMap<String, Object>();
                    Map<String, String> dirs = new HashMap<String, String>();
                    atts.put(PackageNamespace.PACKAGE_NAMESPACE, packageName);
                    dirs.put(PackageNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE, PackageNamespace.RESOLUTION_DYNAMIC);
                    XRequirement req = addRequirement(PackageNamespace.PACKAGE_NAMESPACE, atts, dirs);
                    req.getAttributes().putAll(getAttributes(attr));
                    req.getDirectives().putAll(getDirectives(attr));
                }
            }
            resource.validate();
        } catch (ResourceValidationException ex) {
            throw MESSAGES.resourceBuilderCannotInitializeResource(ex, ex.getOffendingInput());
        } catch (RuntimeException ex) {
            throw MESSAGES.resourceBuilderCannotInitializeResource(ex, metadata.toString());
        }
        return this;
    }

    @Override
    public XResourceBuilder loadFrom(Module module) throws ResourceBuilderException {
        assertResourceCreated();
        try {
            ModuleIdentifier identifier = module.getIdentifier();
            String symbolicName = identifier.getName();
            Version version;
            try {
                version = Version.parseVersion(identifier.getSlot());
            } catch (IllegalArgumentException ex) {
                version = Version.emptyVersion;
            }

            // Add identity capability
            XCapability icap = addCapability(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);

            // Add bundle capability
            XCapability bcap = addCapability(BundleNamespace.BUNDLE_NAMESPACE, symbolicName);
            bcap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);

            // Add a package capability for every exported path
            for (String path : module.getExportedPaths()) {
                if (path.startsWith("/"))
                    path = path.substring(1);
                if (path.endsWith("/"))
                    path = path.substring(0, path.length() - 1);
                if (!path.isEmpty() && !path.startsWith("META-INF")) {
                    String packageName = path.replace('/', '.');
                    addCapability(PackageNamespace.PACKAGE_NAMESPACE, packageName);
                }
            }
            resource.validate();
        } catch (RuntimeException ex) {
            throw MESSAGES.resourceBuilderCannotInitializeResource(ex, resource.toString());
        }
        return this;
    }

    @Override
    public XResource getResource() {
        resource.validate();
        resource.makeImmutable();
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
        return new HashMap<String, Object>(atts != null ? atts : new HashMap<String, Object>());
    }

    private Map<String, String> mutableDirectives(Map<String, String> dirs) {
        return new HashMap<String, String>(dirs != null ? dirs : new HashMap<String, String>());
    }

    private void assertResourceCreated() {
        if (resource == null)
            throw MESSAGES.illegalStateResourceNotCreated();
    }
}