/*
 * Copyright (C) 2015 Computer Science Corporation
 * All rights reserved.
 *
 */
package org.jboss.osgi.resolver.spi;

import java.util.List;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.XBundleRevision;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;

/**
 * @author arcivanov
 */
public class OSGiMetaDataProcessor
{
    public static abstract class ModuleExportPackagesCollector
    {
        private final Module module;

        public ModuleExportPackagesCollector(Module module)
        {
            this.module = module;
        }

        public final void collectExportPackages()
        {
            for (String path : module.getExportedPaths()) {
                if (path.length() > 0) {
                    String packageName = path.replace('/', '.');
                    addPackage(packageName);
                }
            }
        }

        protected abstract void addPackage(String packageName);
    }

    private OSGiMetaDataProcessor()
    {
    }

    public static OSGiMetaData loadOsgiMetaData(Module module)
    {
        // Get symbolic name & version
        ModuleIdentifier moduleId = module.getIdentifier();
        String symbolicName = moduleId.getName();
        Version version;
        try {
            version = Version.parseVersion(moduleId.getSlot());
        }
        catch (IllegalArgumentException ex) {
            version = Version.emptyVersion;
        }
        final OSGiMetaDataBuilder builder = OSGiMetaDataBuilder.createBuilder(symbolicName, version);

        // Add a package capability for every exported path
        new ModuleExportPackagesCollector(module) {

            @Override
            protected void addPackage(String packageName)
            {
                builder.addExportPackages(packageName);
            }
        }.collectExportPackages();
        return builder.getOSGiMetaData();
    }

    public static OSGiMetaData getOsgiMetaData(XBundleRevision brev)
    {
        OSGiMetaDataBuilder metaBuilder = OSGiMetaDataBuilder.createBuilder(brev.getSymbolicName(), brev.getVersion());
        List<Capability> exportedPaths = brev.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE);
        for (Capability cap : exportedPaths) {
            metaBuilder.addExportPackages(cap.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE).toString());
        }
        return metaBuilder.getOSGiMetaData();
    }
}
