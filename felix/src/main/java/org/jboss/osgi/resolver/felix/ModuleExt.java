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
package org.jboss.osgi.resolver.felix;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.framework.capabilityset.Attribute;
import org.apache.felix.framework.capabilityset.Capability;
import org.apache.felix.framework.capabilityset.Directive;
import org.apache.felix.framework.capabilityset.Requirement;
import org.apache.felix.framework.resolver.Content;
import org.apache.felix.framework.resolver.FragmentRequirement;
import org.apache.felix.framework.resolver.Module;
import org.apache.felix.framework.resolver.Wire;
import org.apache.felix.framework.util.VersionRange;
import org.apache.felix.framework.util.manifestparser.CapabilityImpl;
import org.apache.felix.framework.util.manifestparser.R4Library;
import org.apache.felix.framework.util.manifestparser.RequirementImpl;
import org.jboss.logging.Logger;
import org.jboss.osgi.resolver.XFragmentHostRequirement;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequireBundleRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.spi.AbstractModule;
import org.jboss.osgi.spi.NotImplementedException;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * An implementation of the ModuleExtension.
 * 
 * This implemantion should use no framework specific API.
 * It is the extension point for a framework specific Module.
 *  
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class ModuleExt implements Module
{
   // Provide logging
   final Logger log = Logger.getLogger(ModuleExt.class);

   private AbstractModule module;
   private Map<String, String> headerMap;
   private List<Capability> capabilities;
   private List<Requirement> requirements;
   private List<Requirement> dynamicreqs;
   private List<Module> fragments;
   private List<Wire> wires;

   public ModuleExt(AbstractModule module)
   {
      this.module = module;
   }

   AbstractModule getModule()
   {
      return module;
   }

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Map getHeaders()
   {
      if (headerMap == null)
      {
         headerMap = new HashMap<String, String>();
         Dictionary<String, String> headers = getBundle().getHeaders();
         Enumeration<String> keys = headers.keys();
         while (keys.hasMoreElements())
         {
            String key = keys.nextElement();
            String value = headers.get(key);
            headerMap.put(key, value);
         }
      }
      return Collections.unmodifiableMap(headerMap);
   }

   @Override
   public boolean isExtension()
   {
      return false;
   }

   @Override
   public String getSymbolicName()
   {
      return module.getName();
   }

   @Override
   public Version getVersion()
   {
      return module.getVersion();
   }

   @Override
   public boolean isStale()
   {
      return false;
   }

   @Override
   public boolean isRemovalPending()
   {
      return false;
   }

   @Override
   public List<Capability> getCapabilities()
   {
      if (capabilities == null)
         capabilities = createCapabilities();

      return capabilities;
   }

   private List<Capability> createCapabilities()
   {
      List<Capability> result = new ArrayList<Capability>();

      String name = module.getName();
      Version version = module.getVersion();

      // Add a module capability and a host capability to all non-fragment bundles. 
      // A host capability is the same as a module capability, but with a different capability namespace. 
      // Module capabilities resolve required-bundle dependencies, while host capabilities resolve fragment-host dependencies.
      if (module.isFragment() == false)
      {
         List<Attribute> attrs = new ArrayList<Attribute>(2);
         attrs.add(new Attribute(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, name, false));
         attrs.add(new Attribute(Constants.BUNDLE_VERSION_ATTRIBUTE, version, false));
         Capability fcap = new CapabilityImpl(this, Capability.HOST_NAMESPACE, new ArrayList<Directive>(0), attrs);
         result.add(fcap);
      }

      // Always add the module capability 
      List<Attribute> attrs = new ArrayList<Attribute>(2);
      attrs.add(new Attribute(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, name, false));
      attrs.add(new Attribute(Constants.BUNDLE_VERSION_ATTRIBUTE, version, false));
      Capability fcap = new CapabilityImpl(this, Capability.MODULE_NAMESPACE, new ArrayList<Directive>(0), attrs);
      module.getBundleCapability().addAttachment(Capability.class, fcap);
      result.add(fcap);

      for (XPackageCapability cap : module.getPackageCapabilities())
      {
         // Add the package capabilities
         fcap = packageCapability(cap);
         cap.addAttachment(Capability.class, fcap);
         result.add(fcap);
      }

      // Add the capabilities of attached fragments
      for (Module aux : getFragments())
      {
         for (Capability fragCap : aux.getCapabilities())
         {
            fcap = new CapabilityImpl(this, fragCap.getNamespace(), fragCap.getDirectives(), fragCap.getAttributes());
            result.add(fcap);
         }
      }

      return Collections.unmodifiableList(result);
   }

   @Override
   public List<Requirement> getRequirements()
   {
      if (requirements == null)
         requirements = createRequirements();

      if (dynamicreqs == null)
         dynamicreqs = createDynamicRequirements();
      
      return requirements;
   }

   private List<Requirement> createRequirements()
   {
      List<Requirement> result = new ArrayList<Requirement>();
      for (XRequirement req : module.getRequirements())
      {
         if (req instanceof XRequireBundleRequirement)
         {
            Requirement freq = requireBundleRequiment((XRequireBundleRequirement)req);
            req.addAttachment(Requirement.class, freq);
            result.add(freq);
         }
         else if (req instanceof XFragmentHostRequirement)
         {
            Requirement freq = fragmentHostRequirement((XFragmentHostRequirement)req);
            req.addAttachment(Requirement.class, freq);
            result.add(freq);
         }
         else if (req instanceof XPackageRequirement)
         {
            if (((XPackageRequirement)req).isDynamic() == false)
            {
               Requirement freq = packageRequirement((XPackageRequirement)req);
               req.addAttachment(Requirement.class, freq);
               result.add(freq);
            }
         }
      }

      // Add the requirements of attached fragments
      for (Module auxModule : getFragments())
      {
         for (Requirement aux : auxModule.getRequirements())
         {
            if (Capability.PACKAGE_NAMESPACE.equals(aux.getNamespace()) || Capability.MODULE_NAMESPACE.equals(aux.getNamespace()))
            {
               FragmentRequirement req = new FragmentRequirement(this, aux);
               result.add(req);
            }
         }
      }

      return Collections.unmodifiableList(result);
   }

   @Override
   public List<Requirement> getDynamicRequirements()
   {
      if (dynamicreqs == null)
         dynamicreqs = createDynamicRequirements();

      return requirements;
   }

   private List<Requirement> createDynamicRequirements()
   {
      List<Requirement> result = new ArrayList<Requirement>();
      for (XPackageRequirement req : module.getDynamicPackageRequirements())
      {
         // Add the package requirements
         Requirement freq = packageRequirement(req);
         req.addAttachment(Requirement.class, freq);
         result.add(freq);
      }

      return Collections.unmodifiableList(result);
   }

   private Capability packageCapability(XPackageCapability cap)
   {
      String symbolicName = null;
      Version bundleVersion = null;

      // Get the capabiliy attributes
      List<Attribute> attrs = new ArrayList<Attribute>();
      attrs.add(new Attribute(Capability.PACKAGE_ATTR, cap.getName(), false));
      for (Entry<String, Object> entry : cap.getAttributes().entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         if (Capability.VERSION_ATTR.equals(key))
            value = Version.parseVersion(value.toString());
         else if (Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE.equals(key))
            symbolicName = (String)value;
         else if (Constants.BUNDLE_VERSION_ATTRIBUTE.equals(key))
            bundleVersion = Version.parseVersion((String)value);

         attrs.add(new Attribute(key, value, false));
      }

      // Now that we know that there are no bundle symbolic name and version
      // attributes, add them since the spec says they are there implicitly.
      if (symbolicName == null)
      {
         symbolicName = cap.getModule().getName();
         attrs.add(new Attribute(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, symbolicName, false));
      }
      if (bundleVersion == null)
      {
         bundleVersion = cap.getModule().getVersion();
         attrs.add(new Attribute(Constants.BUNDLE_VERSION_ATTRIBUTE, bundleVersion, false));
      }

      // Get the capabiliy directives
      List<Directive> dirs = new ArrayList<Directive>();
      for (Entry<String, String> entry : cap.getDirectives().entrySet())
         dirs.add(new Directive(entry.getKey(), entry.getValue()));

      CapabilityImpl fcap = new CapabilityImpl(this, Capability.PACKAGE_NAMESPACE, dirs, attrs);
      return fcap;
   }

   private Requirement packageRequirement(XPackageRequirement req)
   {
      // Get the requirements attributes
      List<Attribute> attrs = new ArrayList<Attribute>();
      attrs.add(new Attribute(Capability.PACKAGE_ATTR, req.getName(), false));
      for (Entry<String, Object> entry : req.getAttributes().entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         if (Capability.VERSION_ATTR.equals(key))
            value = VersionRange.parse((String)value);
         else if (Constants.BUNDLE_VERSION_ATTRIBUTE.equals(key))
            value = VersionRange.parse((String)value);

         attrs.add(new Attribute(key, value, false));
      }

      // Get the requirements directives
      List<Directive> dirs = new ArrayList<Directive>();
      for (Entry<String, String> entry : req.getDirectives().entrySet())
         dirs.add(new Directive(entry.getKey(), entry.getValue()));

      RequirementImpl freq = new RequirementImpl(this, Capability.PACKAGE_NAMESPACE, dirs, attrs);
      return freq;
   }

   private Requirement requireBundleRequiment(XRequireBundleRequirement req)
   {
      // Get the requirements attributes
      List<Attribute> attrs = new ArrayList<Attribute>();
      attrs.add(new Attribute(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, req.getName(), false));
      for (Entry<String, Object> entry : req.getAttributes().entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         if (Constants.BUNDLE_VERSION_ATTRIBUTE.equals(key))
            value = VersionRange.parse((String)value);

         attrs.add(new Attribute(key, value, false));
      }

      // Get the requirements directives
      List<Directive> dirs = new ArrayList<Directive>();
      for (Entry<String, String> entry : req.getDirectives().entrySet())
         dirs.add(new Directive(entry.getKey(), entry.getValue()));

      RequirementImpl freq = new RequirementImpl(this, Capability.MODULE_NAMESPACE, dirs, attrs);
      return freq;
   }

   private Requirement fragmentHostRequirement(XFragmentHostRequirement req)
   {
      // Get the requirements attributes
      List<Attribute> attrs = new ArrayList<Attribute>();
      attrs.add(new Attribute(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, req.getName(), false));
      for (Entry<String, Object> entry : req.getAttributes().entrySet())
      {
         String key = entry.getKey();
         Object value = entry.getValue();
         if (Constants.BUNDLE_VERSION_ATTRIBUTE.equals(key))
            value = VersionRange.parse((String)value);

         attrs.add(new Attribute(key, value, false));
      }

      // Get the requirements directives
      List<Directive> dirs = new ArrayList<Directive>();
      for (Entry<String, String> entry : req.getDirectives().entrySet())
         dirs.add(new Directive(entry.getKey(), entry.getValue()));

      RequirementImpl freq = new RequirementImpl(this, Capability.HOST_NAMESPACE, dirs, attrs);
      return freq;
   }

   @Override
   public List<R4Library> getNativeLibraries()
   {
      throw new NotImplementedException();
   }

   @Override
   public int getDeclaredActivationPolicy()
   {
      throw new NotImplementedException();
   }

   @Override
   public Bundle getBundle()
   {
      Bundle bundle = module.getAttachment(Bundle.class);
      if (bundle == null)
         throw new IllegalStateException("Cannot obtain the associated bundle from: " + module);
      return bundle;
   }

   @Override
   public String getId()
   {
      return getBundle().getLocation();
   }

   @Override
   public List<Wire> getWires()
   {
      return (wires != null ? Collections.unmodifiableList(wires) : null);
   }

   @Override
   public void setWires(List<Wire> wires)
   {
      this.wires = wires;
   }

   @Override
   public void removeWires()
   {
      this.wires = null;
   }

   @Override
   public void attachFragments(List<Module> modules) throws Exception
   {
      fragments = modules;
      capabilities = null;
      requirements = null;
   }

   @Override
   public void removeFragments()
   {
      fragments = null;
      capabilities = null;
      requirements = null;
   }

   @Override
   public List<Module> getFragments()
   {
      if (fragments == null)
         return Collections.emptyList();

      return fragments;
   }

   @Override
   public boolean isResolved()
   {
      return module.isResolved();
   }

   @Override
   public void setResolved()
   {
      // do nothing
   }

   @Override
   public ProtectionDomain getSecurityContext()
   {
      throw new NotImplementedException();
   }

   @Override
   public boolean impliesDirect(Permission permission)
   {
      return true;
   }

   @Override
   public Content getContent()
   {
      throw new NotImplementedException();
   }

   @Override
   public Class<?> getClassByDelegation(String name) throws ClassNotFoundException
   {
      return getBundle().loadClass(name);
   }

   @Override
   public URL getResourceByDelegation(String name)
   {
      return getBundle().getResource(name);
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Enumeration getResourcesByDelegation(String name)
   {
      // TODO: why doesn't this throw an IOException
      try
      {
         return getBundle().getResources(name);
      }
      catch (IOException ex)
      {
         return null;
      }
   }

   @Override
   public URL getEntry(String name)
   {
      return getBundle().getEntry(name);
   }

   @Override
   public boolean hasInputStream(int index, String urlPath) throws IOException
   {
      throw new NotImplementedException();
   }

   @Override
   public InputStream getInputStream(int index, String urlPath) throws IOException
   {
      throw new NotImplementedException();
   }

   /** 
    * Gets the potential wire for a given requirement.
    * @return The wire or null 
    */
   public Wire getWireForRequirement(Requirement requirement)
   {
      Wire result = null;
      if (wires != null)
      {
         for (Wire aux : wires)
         {
            Requirement auxreq = aux.getRequirement();
            if (auxreq instanceof FragmentRequirement)
               auxreq = ((FragmentRequirement)auxreq).getRequirement();

            if (auxreq.equals(requirement))
            {
               result = aux;
               break;
            }
         }
      }
      return result;
   }

   @Override
   public String toString()
   {
      return "Module[" + getBundle() + "]";
   }
}