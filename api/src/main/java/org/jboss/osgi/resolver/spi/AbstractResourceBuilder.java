/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.metadata.OSGiMetaData.ANONYMOUS_BUNDLE_SYMBOLIC_NAME;
import static org.jboss.osgi.resolver.ResolverMessages.MESSAGES;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.Resource;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.metadata.PackageAttribute;
import org.jboss.osgi.metadata.Parameter;
import org.jboss.osgi.metadata.ParameterizedAttribute;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.ResourceBuilderException;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.AbstractWiringNamespace;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.ExecutionEnvironmentNamespace;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;

/**
 * A builder for resolver resources
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResourceBuilder<T extends XResource> implements XResourceBuilder<T> {

    private final XResourceBuilderFactory<T> factory;
    private final T resource;

    public AbstractResourceBuilder(XResourceBuilderFactory<T> factory) {
        if (factory == null)
            throw MESSAGES.illegalArgumentNull("factory");
        this.factory = factory;
        this.resource = factory.createResource();
    }

    @Override
    public XResourceBuilder<T> addAttribute(String key, String value) {
        assertResourceCreated();
        resource.getAttributes().put(key, value);
        return this;
    }

    @Override
    public XIdentityCapability addIdentityCapability(String symbolicName, Version version) {
        assertResourceCreated();
        XIdentityCapability icap = (XIdentityCapability) addCapability(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
        return icap;
    }

    @Override
    public XIdentityCapability addIdentityCapability(ModuleIdentifier moduleId) {
        assertResourceCreated();
        String nsvalue = moduleId.getName();
        Version version;
        try {
            version = Version.parseVersion(moduleId.getSlot());
        } catch (IllegalArgumentException ex) {
            version = Version.emptyVersion;
        }
        XIdentityCapability icap = (XIdentityCapability) addCapability(XResource.MODULE_IDENTITY_NAMESPACE, nsvalue);
        icap.getAttributes().put("type", XResource.TYPE_MODULE);
        icap.getAttributes().put("name", moduleId.getName());
        icap.getAttributes().put("slot", moduleId.getSlot());
        icap.getAttributes().put("version", version);
        return icap;
    }

    @Override
    public XIdentityCapability addIdentityCapability(MavenCoordinates mavenId) {
        assertResourceCreated();
        String nsvalue = mavenId.getArtifactId();
        Version version;
        try {
            version = Version.parseVersion(mavenId.getVersion());
        } catch (IllegalArgumentException ex) {
            version = Version.emptyVersion;
        }
        XIdentityCapability icap = (XIdentityCapability) addCapability(XResource.MAVEN_IDENTITY_NAMESPACE, nsvalue);
        icap.getAttributes().put("type", mavenId.getType());
        icap.getAttributes().put("groupId", mavenId.getGroupId());
        icap.getAttributes().put("artifactId", mavenId.getArtifactId());
        icap.getAttributes().put("version", version);
        if (mavenId.getClassifier() != null)
            icap.getAttributes().put("classifier", mavenId.getClassifier());
        return icap;
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
        Map<String, String> dirs = new LinkedHashMap<String, String>();
        if (filter != null)
            dirs.put(AbstractWiringNamespace.REQUIREMENT_FILTER_DIRECTIVE, filter.toString());
        return addRequirement(namespace, null, dirs);
    }

    @Override
    public XRequirement addRequirement(String namespace, String nsvalue) {
        assertResourceCreated();
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        if (nsvalue != null)
            atts.put(namespace, nsvalue);
        return addRequirement(namespace, atts, null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public XResourceBuilder<T> loadFrom(OSGiMetaData metadata) throws ResourceBuilderException {
        assertResourceCreated();
        try {
            String symbolicName = metadata.getBundleSymbolicName();
            Version bundleVersion = metadata.getBundleVersion();
            ParameterizedAttribute fragmentHost = metadata.getFragmentHost();
            ParameterizedAttribute idparams = metadata.getBundleParameters();
            Map<String, Object> idatts = getAttributes(idparams);
            Map<String, String> isdirs = getDirectives(idparams);

            if (symbolicName == null)
                symbolicName = ANONYMOUS_BUNDLE_SYMBOLIC_NAME;

            // Identity Capability
            XCapability icap = addIdentityCapability(symbolicName, bundleVersion);
            String identityType = fragmentHost != null ? IdentityNamespace.TYPE_FRAGMENT : IdentityNamespace.TYPE_BUNDLE;
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, identityType);
            icap.getAttributes().putAll(idatts);
            icap.getDirectives().putAll(isdirs);

            // Bundle Capability
            if (IdentityNamespace.TYPE_BUNDLE.equals(identityType)) {
                XCapability cap = addCapability(BundleNamespace.BUNDLE_NAMESPACE, symbolicName);
                cap.getAttributes().put(BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, bundleVersion);
                cap.getAttributes().putAll(idatts);
                cap.getDirectives().putAll(isdirs);
            }

            // Host Capability
            String fragmentAttachemnt = isdirs.get(HostNamespace.CAPABILITY_FRAGMENT_ATTACHMENT_DIRECTIVE);
            if (fragmentHost == null && !HostNamespace.FRAGMENT_ATTACHMENT_NEVER.equals(fragmentAttachemnt)) {
                XCapability cap = addCapability(HostNamespace.HOST_NAMESPACE, symbolicName);
                cap.getAttributes().put(HostNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, bundleVersion);
                cap.getAttributes().putAll(idatts);
                cap.getDirectives().putAll(isdirs);
            }

            // Host Requirement
            if (fragmentHost != null) {
                String hostName = fragmentHost.getAttribute();
                XRequirement req = addRequirement(HostNamespace.HOST_NAMESPACE, hostName);
                req.getAttributes().putAll(getAttributes(fragmentHost));
                req.getDirectives().putAll(getDirectives(fragmentHost));
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

            // Required Execution Environment
            List<String> requiredEnvironments = metadata.getRequiredExecutionEnvironment();
            if (requiredEnvironments != null && requiredEnvironments.isEmpty() == false) {
                // Frameworks must convert a Bundle-RequiredExecutionEnvironment header to a requirement in the osgi.ee namespace
                Filter filter = OSGiMetaDataBuilder.convertExecutionEnvironmentHeader(requiredEnvironments);
                addRequirement(ExecutionEnvironmentNamespace.EXECUTION_ENVIRONMENT_NAMESPACE, filter);
            }

            // Export-Package
            List<PackageAttribute> exports = metadata.getExportPackages();
            if (exports != null && exports.isEmpty() == false) {
                for (PackageAttribute attr : exports) {
                    String packageName = attr.getAttribute();
                    XCapability cap = addCapability(PackageNamespace.PACKAGE_NAMESPACE, packageName);
                    cap.getAttributes().putAll(getAttributes(attr));
                    cap.getDirectives().putAll(getDirectives(attr));

                    // Add infered package capability attributes
                    Map<String, Object> capatts = cap.getAttributes();
                    if (!capatts.containsKey(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE))
                        capatts.put(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, symbolicName);
                    if (!capatts.containsKey(Constants.BUNDLE_VERSION_ATTRIBUTE))
                        capatts.put(Constants.BUNDLE_VERSION_ATTRIBUTE, bundleVersion);
                    if (!capatts.containsKey(Constants.PACKAGE_SPECIFICATION_VERSION)) {
                        Object vspec = capatts.get(Constants.VERSION_ATTRIBUTE);
                        if (vspec != null) {
                            try {
                                Version version = Version.parseVersion(vspec.toString());
                                capatts.put(Constants.PACKAGE_SPECIFICATION_VERSION, version);
                            } catch (RuntimeException ex) {
                                // ignore
                            }
                        }
                    }
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
                    Map<String, Object> atts = new LinkedHashMap<String, Object>();
                    Map<String, String> dirs = new LinkedHashMap<String, String>();
                    atts.put(PackageNamespace.PACKAGE_NAMESPACE, packageName);
                    dirs.put(PackageNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE, PackageNamespace.RESOLUTION_DYNAMIC);
                    XRequirement req = addRequirement(PackageNamespace.PACKAGE_NAMESPACE, atts, dirs);
                    req.getAttributes().putAll(getAttributes(attr));
                    req.getDirectives().putAll(getDirectives(attr));
                }
            }

            // Provide-Capability
            List<ParameterizedAttribute> providedCapabilities = metadata.getProvidedCapabilities();
            if (providedCapabilities != null && providedCapabilities.isEmpty() == false) {
                for (ParameterizedAttribute attr : providedCapabilities) {
                    String capname = attr.getAttribute();
                    XCapability cap = addCapability(capname, null, null);
                    cap.getAttributes().putAll(getAttributes(attr));
                    cap.getDirectives().putAll(getDirectives(attr));
                }
            }

            // Require-Capability
            List<ParameterizedAttribute> requiredCapabilities = metadata.getRequiredCapabilities();
            if (requiredCapabilities != null && requiredCapabilities.isEmpty() == false) {
                for (ParameterizedAttribute attr : requiredCapabilities) {
                    String reqname = attr.getAttribute();
                    XRequirement req = addRequirement(reqname, null, null);
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
    public XResourceBuilder<T> loadFrom(Module module) throws ResourceBuilderException {
        assertResourceCreated();
        try {
            ModuleIdentifier moduleId = module.getIdentifier();
            String symbolicName = moduleId.getName();
            Version version;
            try {
                version = Version.parseVersion(moduleId.getSlot());
            } catch (IllegalArgumentException ex) {
                version = Version.emptyVersion;
            }

            // Add identity capability
            XCapability icap = addCapability(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, XResource.TYPE_MODULE);

            // Add bundle capability
            XCapability bcap = addCapability(BundleNamespace.BUNDLE_NAMESPACE, symbolicName);
            bcap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);

            // Add a package capability for every exported path
            Set<String> paths = new HashSet<String>();
            Iterator<Resource> it = module.getClassLoader().iterateResources("", true);
            while (it.hasNext()) {
                Resource res = it.next();
                String path = res.getName();
                if (!path.endsWith(".class"))
                    continue;

                int index = path.lastIndexOf("/");
                if (index <= 0)
                    continue;

                path = path.substring(0, index);
                if (!paths.contains(path)) {
                    paths.add(path);
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
    public T getResource() {
        resource.validate();
        if (resource instanceof AbstractResource) {
            ((AbstractResource) resource).setMutable(false);
        }
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
        Map<String, Object> atts = new LinkedHashMap<String, Object>();
        if (patts != null) {
            for (String key : patts.getAttributes().keySet()) {
                Parameter param = patts.getAttribute(key);
                atts.put(key, param.getValue());
            }
        }
        return atts;
    }

    private Map<String, String> getDirectives(ParameterizedAttribute patts) {
        Map<String, String> dirs = new LinkedHashMap<String, String>();
        if (patts != null) {
            for (String key : patts.getDirectives().keySet()) {
                String value = patts.getDirectiveValue(key, String.class);
                dirs.put(key, value);
            }
        }
        return dirs;
    }

    private Map<String, Object> mutableAttributes(Map<String, Object> atts) {
        return new LinkedHashMap<String, Object>(atts != null ? atts : new LinkedHashMap<String, Object>());
    }

    private Map<String, String> mutableDirectives(Map<String, String> dirs) {
        return new LinkedHashMap<String, String>(dirs != null ? dirs : new LinkedHashMap<String, String>());
    }

    private void assertResourceCreated() {
        if (resource == null)
            throw MESSAGES.illegalStateResourceNotCreated();
    }
}
