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
import java.util.jar.Manifest;

import org.jboss.osgi.msc.metadata.OSGiMetaData;
import org.jboss.osgi.msc.metadata.PackageAttribute;
import org.jboss.osgi.msc.metadata.Parameter;
import org.jboss.osgi.msc.metadata.ParameterizedAttribute;
import org.jboss.osgi.msc.metadata.internal.OSGiManifestMetaData;
import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XFragmentHostRequirement;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XModuleBuilder;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequireBundleRequirement;
import org.osgi.framework.Version;

/**
 * A builder for resolver modules
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractModuleBuilder implements XModuleBuilder
{
   private AbstractModule module;

   @Override
   public XModule createModule(long moduleId, Manifest manifest)
   {
      OSGiManifestMetaData metadata = new OSGiManifestMetaData(manifest);
      return createModule(moduleId, metadata);
   }

   @Override
   public XModule createModule(long moduleId, String symbolicName, Version version)
   {
      module = new AbstractModule(moduleId, symbolicName, version);
      return module;
   }

   @Override
   public XBundleCapability addBundleCapability(String symbolicName, Version version)
   {
      XBundleCapability cap = new AbstractBundleCapability(module, symbolicName, version);
      module.addCapability(cap);
      return cap;
   }

   @Override
   public XRequireBundleRequirement addBundleRequirement(String symbolicName, Map<String, String> dirs, Map<String, Object> atts)
   {
      XRequireBundleRequirement req = new AbstractBundleRequirement(module, symbolicName, dirs, atts);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XFragmentHostRequirement addFragmentHostRequirement(String symbolicName, Map<String, String> dirs, Map<String, Object> atts)
   {
      XFragmentHostRequirement req = new AbstractFragmentHostRequirement(module, symbolicName, dirs, atts);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XPackageCapability addPackageCapability(String name, Map<String, String> dirs, Map<String, Object> atts)
   {
      XPackageCapability cap = new AbstractPackageCapability(module, name, dirs, atts);
      module.addCapability(cap);
      return cap;
   }

   @Override
   public XPackageRequirement addPackageRequirement(String name, Map<String, String> dirs, Map<String, Object> atts)
   {
      XPackageRequirement req = new AbstractPackageRequirement(module, name, dirs, atts, false);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XPackageRequirement addDynamicPackageRequirement(String name, Map<String, Object> atts)
   {
      XPackageRequirement req = new AbstractPackageRequirement(module, name, null, atts, true);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XModule getModule()
   {
      return module;
   }

   @Override
   public XModule createModule(long moduleId, OSGiMetaData osgiMetaData)
   {
      XModule module = createModule(moduleId, osgiMetaData.getBundleSymbolicName(), osgiMetaData.getBundleVersion());
      addBundleCapability(osgiMetaData.getBundleSymbolicName(), osgiMetaData.getBundleVersion());

      // Required Bundles
      List<ParameterizedAttribute> requireBundles = osgiMetaData.getRequireBundles();
      if (requireBundles != null && requireBundles.isEmpty() == false)
      {
         for (ParameterizedAttribute metadata : requireBundles)
         {
            String name = metadata.getAttribute();
            Map<String, String> dirs = getDirectives(metadata);
            Map<String, Object> atts = getAttributes(metadata);
            addBundleRequirement(name, dirs, atts);
         }
      }

      // Export-Package
      List<PackageAttribute> exports = osgiMetaData.getExportPackages();
      if (exports != null && exports.isEmpty() == false)
      {
         for (PackageAttribute metadata : exports)
         {
            String name = metadata.getAttribute();
            Map<String, String> dirs = getDirectives(metadata);
            Map<String, Object> atts = getAttributes(metadata);
            addPackageCapability(name, dirs, atts);
         }
      }

      // Import-Package
      List<PackageAttribute> imports = osgiMetaData.getImportPackages();
      if (imports != null && imports.isEmpty() == false)
      {
         for (PackageAttribute metadata : imports)
         {
            String name = metadata.getAttribute();
            Map<String, String> dirs = getDirectives(metadata);
            Map<String, Object> atts = getAttributes(metadata);
            addPackageRequirement(name, dirs, atts);
         }
      }

      // DynamicImport-Package
      List<PackageAttribute> dynamicImports = osgiMetaData.getDynamicImports();
      if (dynamicImports != null && dynamicImports.isEmpty() == false)
      {
         for (PackageAttribute metadata : dynamicImports)
         {
            String name = metadata.getAttribute();
            Map<String, Object> atts = getAttributes(metadata);
            addDynamicPackageRequirement(name, atts);
         }
      }

      // Fragment-Host
      ParameterizedAttribute fragmentHost = osgiMetaData.getFragmentHost();
      if (fragmentHost != null)
      {
         String symbolicName = fragmentHost.getAttribute();
         Map<String, String> dirs = getDirectives(fragmentHost);
         Map<String, Object> atts = getAttributes(fragmentHost);
         addFragmentHostRequirement(symbolicName, dirs, atts);
      }
      return module;
   }

   private Map<String, String> getDirectives(ParameterizedAttribute metadata)
   {
      Map<String, String> dirs = new HashMap<String, String>();
      for (String key : metadata.getDirectives().keySet())
      {
         Parameter param = metadata.getDirective(key);
         dirs.put(key, param.getValue().toString());
      }
      return dirs;
   }

   private Map<String, Object> getAttributes(ParameterizedAttribute metadata)
   {
      Map<String, Object> atts = new HashMap<String, Object>();
      for (String key : metadata.getAttributes().keySet())
      {
         Parameter param = metadata.getAttribute(key);
         atts.put(key, param.getValue().toString());
      }
      return atts;
   }
}