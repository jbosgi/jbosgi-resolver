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

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.PackageAttribute;
import org.jboss.osgi.metadata.Parameter;
import org.jboss.osgi.metadata.ParameterizedAttribute;
import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XFragmentHostRequirement;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XModuleBuilder;
import org.jboss.osgi.resolver.XModuleIdentity;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequireBundleRequirement;
import org.osgi.framework.BundleException;
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
   public XModuleBuilder createModule(OSGiMetaData metadata, int revision) throws BundleException
   {
      XModuleIdentity moduleId = new AbstractModuleIdentity(metadata, revision);
      module = new AbstractModule(moduleId);
      load(metadata);
      return this;
   }

   @Override
   public XModuleBuilder createModule(String name, Version version, int revision)
   {
      XModuleIdentity moduleId = new AbstractModuleIdentity(name, version, revision);
      module = new AbstractModule(moduleId);
      return this;
   }

   @Override
   public XModuleIdentity getModuleIdentity()
   {
      assertModuleCreated();
      return module.getModuleId();
   }

   @Override
   public XBundleCapability addBundleCapability(String symbolicName, Version version)
   {
      assertModuleCreated();
      XBundleCapability cap = new AbstractBundleCapability(module, symbolicName, version);
      module.addCapability(cap);
      return cap;
   }

   @Override
   public XRequireBundleRequirement addBundleRequirement(String symbolicName, Map<String, String> dirs, Map<String, Object> atts)
   {
      assertModuleCreated();
      XRequireBundleRequirement req = new AbstractBundleRequirement(module, symbolicName, dirs, atts);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XFragmentHostRequirement addFragmentHostRequirement(String symbolicName, Map<String, String> dirs, Map<String, Object> atts)
   {
      assertModuleCreated();
      XFragmentHostRequirement req = new AbstractFragmentHostRequirement(module, symbolicName, dirs, atts);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XPackageCapability addPackageCapability(String name, Map<String, String> dirs, Map<String, Object> atts)
   {
      assertModuleCreated();
      XPackageCapability cap = new AbstractPackageCapability(module, name, dirs, atts);
      module.addCapability(cap);
      return cap;
   }

   @Override
   public XPackageRequirement addPackageRequirement(String name, Map<String, String> dirs, Map<String, Object> atts)
   {
      assertModuleCreated();
      XPackageRequirement req = new AbstractPackageRequirement(module, name, dirs, atts, false);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XPackageRequirement addDynamicPackageRequirement(String name, Map<String, Object> atts)
   {
      assertModuleCreated();
      XPackageRequirement req = new AbstractPackageRequirement(module, name, null, atts, true);
      module.addRequirement(req);
      return req;
   }

   @Override
   public XModuleBuilder addBundleClassPath(String... paths)
   {
      assertModuleCreated();
      module.addBundleClassPath(paths);
      return this;
   }

   @Override
   public XModuleBuilder addModuleActivator(String moduleActivator)
   {
      assertModuleCreated();
      module.setModuleActivator(moduleActivator);
      return this;
   }

   @Override
   public XModule getModule()
   {
      return module;
   }

   private void load(OSGiMetaData metadata) throws BundleException
   {
      try
      {
         XModuleIdentity moduleId = module.getModuleId();
         addBundleCapability(moduleId.getName(), moduleId.getVersion());
         addModuleActivator(metadata.getBundleActivator());
         // Required Bundles
         List<ParameterizedAttribute> requireBundles = metadata.getRequireBundles();
         if (requireBundles != null && requireBundles.isEmpty() == false)
         {
            for (ParameterizedAttribute attribs : requireBundles)
            {
               String name = attribs.getAttribute();
               Map<String, String> dirs = getDirectives(attribs);
               Map<String, Object> atts = getAttributes(attribs);
               addBundleRequirement(name, dirs, atts);
            }
         }

         // Export-Package
         List<PackageAttribute> exports = metadata.getExportPackages();
         if (exports != null && exports.isEmpty() == false)
         {
            for (PackageAttribute attribs : exports)
            {
               String name = attribs.getAttribute();
               Map<String, String> dirs = getDirectives(attribs);
               Map<String, Object> atts = getAttributes(attribs);
               addPackageCapability(name, dirs, atts);
            }
         }

         // Import-Package
         List<PackageAttribute> imports = metadata.getImportPackages();
         if (imports != null && imports.isEmpty() == false)
         {
            for (PackageAttribute attribs : imports)
            {
               String name = attribs.getAttribute();
               Map<String, String> dirs = getDirectives(attribs);
               Map<String, Object> atts = getAttributes(attribs);
               addPackageRequirement(name, dirs, atts);
            }
         }

         // DynamicImport-Package
         List<PackageAttribute> dynamicImports = metadata.getDynamicImports();
         if (dynamicImports != null && dynamicImports.isEmpty() == false)
         {
            for (PackageAttribute attribs : dynamicImports)
            {
               String name = attribs.getAttribute();
               Map<String, Object> atts = getAttributes(attribs);
               addDynamicPackageRequirement(name, atts);
            }
         }

         // Fragment-Host
         ParameterizedAttribute fragmentHost = metadata.getFragmentHost();
         if (fragmentHost != null)
         {
            String hostName = fragmentHost.getAttribute();
            Map<String, String> dirs = getDirectives(fragmentHost);
            Map<String, Object> atts = getAttributes(fragmentHost);
            addFragmentHostRequirement(hostName, dirs, atts);
         }

         // Bundle-ClassPath
         List<String> classPath = metadata.getBundleClassPath();
         if (classPath != null && classPath.isEmpty() == false)
            addBundleClassPath(classPath.toArray(new String[classPath.size()]));
      }
      catch (RuntimeException ex)
      {
         throw new BundleException("Cannot initialize XModule from: " + metadata, ex);
      }
   }

   private void assertModuleCreated()
   {
      if (module == null)
         throw new IllegalStateException("Module not created");
   }

   private Map<String, String> getDirectives(ParameterizedAttribute attribs)
   {
      Map<String, String> dirs = new HashMap<String, String>();
      for (String key : attribs.getDirectives().keySet())
      {
         Parameter param = attribs.getDirective(key);
         dirs.put(key.trim(), param.getValue().toString().trim());
      }
      return dirs;
   }

   private Map<String, Object> getAttributes(ParameterizedAttribute attribs)
   {
      Map<String, Object> atts = new HashMap<String, Object>();
      for (String key : attribs.getAttributes().keySet())
      {
         Parameter param = attribs.getAttribute(key);
         atts.put(key.trim(), param.getValue().toString().trim());
      }
      return atts;
   }
}