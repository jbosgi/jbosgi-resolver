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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.osgi.resolver.XAttachmentSupport;
import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XFragmentHostRequirement;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequireBundleRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResolver;
import org.jboss.osgi.resolver.XWire;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * The abstract implementation of an {@link XModule}.
 *
 * This is the resolver representation of a {@link Bundle}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractModule extends AbstractElement implements XModule
{
   private XResolver resolver;
   private long moduleId;
   private Version version;
   private XBundleCapability bundleCapability;
   private List<XCapability> capabilities;
   private List<XRequirement> requirements;
   private List<String> classPaths;
   private XFragmentHostRequirement hostRequirement;
   private XAttachmentSupport attachments;
   private List<XWire> wires;
   private boolean resolved;

   AbstractModule(long moduleId, String name, Version version)
   {
      super(name);
      
      if (version == null)
         throw new IllegalArgumentException("Null version");
      
      this.moduleId = moduleId;
      this.version = version;
   }

   public XResolver getResolver()
   {
      return resolver;
   }

   void setResolver(XResolver resolver)
   {
      this.resolver = resolver;
   }

   @Override
   public long getModuleId()
   {
      return moduleId;
   }

   @Override
   public Version getVersion()
   {
      return version;
   }

   @Override
   public boolean isResolved()
   {
      return resolved;
   }

   void setResolved()
   {
      this.resolved = true;
   }

   @Override
   public List<XCapability> getCapabilities()
   {
      if (capabilities == null)
         return Collections.emptyList();
      
      return Collections.unmodifiableList(capabilities);
   }

   @Override
   public List<XRequirement> getRequirements()
   {
      if (requirements == null)
         return Collections.emptyList();
      
      return Collections.unmodifiableList(requirements);
   }

   @Override
   public List<XRequireBundleRequirement> getBundleRequirements()
   {
      if (requirements == null)
         return Collections.emptyList();
      
      List<XRequireBundleRequirement> result = new ArrayList<XRequireBundleRequirement>();
      for (XRequirement aux : requirements)
      {
         if (aux instanceof XRequireBundleRequirement)
            result.add((XRequireBundleRequirement)aux);
      }
      return Collections.unmodifiableList(result);
   }

   @Override
   public XBundleCapability getBundleCapability()
   {
      return bundleCapability;
   }

   @Override
   public List<XPackageCapability> getPackageCapabilities()
   {
      if (capabilities == null)
         return Collections.emptyList();
      
      List<XPackageCapability> result = new ArrayList<XPackageCapability>();
      for (XCapability aux : capabilities)
      {
         if (aux instanceof XPackageCapability)
         {
            XPackageCapability packcap = (XPackageCapability)aux;
            result.add(packcap);
         }
      }
      return Collections.unmodifiableList(result);
   }

   @Override
   public List<XPackageRequirement> getPackageRequirements()
   {
      if (requirements == null)
         return Collections.emptyList();
      
      List<XPackageRequirement> result = new ArrayList<XPackageRequirement>();
      for (XRequirement aux : requirements)
      {
         if (aux instanceof XPackageRequirement)
         {
            XPackageRequirement packreq = (XPackageRequirement)aux;
            if (packreq.isDynamic() == false)
               result.add(packreq);
         }
      }
      return Collections.unmodifiableList(result);
   }

   @Override
   public List<XPackageRequirement> getDynamicPackageRequirements()
   {
      if (requirements == null)
         return Collections.emptyList();
      
      List<XPackageRequirement> result = new ArrayList<XPackageRequirement>();
      for (XRequirement aux : requirements)
      {
         if (aux instanceof XPackageRequirement)
         {
            XPackageRequirement packreq = (XPackageRequirement)aux;
            if (packreq.isDynamic() == true)
               result.add(packreq);
         }
      }
      return Collections.unmodifiableList(result);
   }

   @Override
   public XFragmentHostRequirement getHostRequirement()
   {
      if (hostRequirement != null)
         return hostRequirement;
      
      if (requirements != null)
      {
         for (XRequirement aux : requirements)
         {
            if (aux instanceof XFragmentHostRequirement)
            {
               hostRequirement = (XFragmentHostRequirement)aux;
               break;
            }
         }
      }
      
      return hostRequirement;
   }

   @Override
   public boolean isFragment()
   {
      return getHostRequirement() != null;
   }

   @Override
   public List<String> getBundleClassPath()
   {
      if (classPaths == null)
         return Collections.emptyList();
      
      return Collections.unmodifiableList(classPaths);
   }

   void addBundleClassPath(String... paths)
   {
      if (classPaths == null)
         classPaths = new ArrayList<String>();
      
      classPaths.addAll(Arrays.asList(paths));
   }
   
   @Override
   public List<XWire> getWires()
   {
      if (wires == null)
         return null;
      
      return Collections.unmodifiableList(wires);
   }

   protected void addWire(AbstractWire wire)
   {
      if (wires == null)
         wires = new ArrayList<XWire>();
      
      wires.add(wire);
   }
   
   void addCapability(XCapability capability)
   {
      if (capabilities == null)
         capabilities = new ArrayList<XCapability>();

      if (capability instanceof XBundleCapability)
         bundleCapability = (XBundleCapability)capability;

      capabilities.add(capability);
   }

   void addRequirement(XRequirement requirement)
   {
      if (requirements == null)
         requirements = new ArrayList<XRequirement>();

      requirements.add(requirement);
   }

   @Override
   public <T> T addAttachment(Class<T> clazz, T value)
   {
      if (attachments  == null)
         attachments = new AttachmentSupporter();
      
      return attachments.addAttachment(clazz, value);
   }

   @Override
   public <T> T getAttachment(Class<T> clazz)
   {
      if (attachments  == null)
         return null;
      
      return attachments.getAttachment(clazz);
   }

   @Override
   public <T> T removeAttachment(Class<T> clazz)
   {
      if (attachments  == null)
         return null;
      
      return attachments.removeAttachment(clazz);
   }

   @Override
   public int hashCode()
   {
      return (getName() + ":" + getVersion()).hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof AbstractModule == false)
         return false;
      if (obj == this)
         return true;
      AbstractModule other = (AbstractModule)obj;
      return getName().equals(other.getName()) && getVersion().equals(other.getVersion());
   }

   public StringBuffer toLongString(StringBuffer buffer)
   {
      if (buffer == null)
         throw new IllegalArgumentException("Null buffer");
      
      String simpleName = getClass().getSimpleName();
      buffer.append("\n" + simpleName + ": " + toString());
      if (resolved)
         buffer.append(" - resolved");
      
      buffer.append("\nCapabilities");
      for (XCapability cap : getCapabilities())
         buffer.append("\n " + cap);
      
      buffer.append("\nRequirements");
      for (XRequirement req : getRequirements())
         buffer.append("\n " + req);
      
      if (wires != null)
      {
         buffer.append("\nWires");
         for (XWire wire : getWires())
            buffer.append("\n " + wire);
      }
      return buffer;
   }
   
   @Override
   public String toString()
   {
      return "[" + getModuleId() + "]:" + getName() + ":" + getVersion();
   }
}