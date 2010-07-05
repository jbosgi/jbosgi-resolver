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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResolver;
import org.jboss.osgi.resolver.XResolverCallback;
import org.jboss.osgi.resolver.XResolverException;
import org.jboss.osgi.resolver.XWire;

/**
 * An abstract base implementation of a Resolver.
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Jul-2010
 */
public abstract class AbstractResolver implements XResolver
{
   private XResolverCallback callback;
   private Map<Long, XModule> moduleMap = new LinkedHashMap<Long, XModule>();

   public AbstractResolver()
   {
      // Initialize the noop callback
      callback = new XResolverCallback()
      {
         @Override
         public void releaseGlobalLock()
         {
         }
         
         @Override
         public void markResolved(XModule module)
         {
         }
         
         @Override
         public boolean acquireGlobalLock()
         {
            return true;
         }
      };
   }

   @Override
   public void setCallbackHandler(XResolverCallback callback)
   {
      this.callback = callback;
   }

   protected XResolverCallback getCallbackHandler()
   {
      return callback;
   }

   @Override
   public void addModule(XModule module)
   {
      synchronized (moduleMap)
      {
         ((AbstractModule)module).setResolver(this);
         moduleMap.put(module.getModuleId(), module);
      }
   }

   @Override
   public XModule removeModule(long moduleId)
   {
      synchronized (moduleMap)
      {
         return moduleMap.remove(moduleId);
      }
   }

   @Override
   public List<XModule> getModules()
   {
      synchronized (moduleMap)
      {
         List<XModule> values = new ArrayList<XModule>(moduleMap.values());
         return Collections.unmodifiableList(values);
      }
   }

   @Override
   public XModule findModuleById(long moduleId)
   {
      synchronized (moduleMap)
      {
         return moduleMap.get(moduleId);
      }
   }

   @Override
   public final void resolve(XModule module) throws XResolverException
   {
      try
      {
         module.removeAttachment(XResolverException.class);
         resolveInternal(module);
      }
      catch (XResolverException ex)
      {
         // Add the last resolver exception to the module
         module.addAttachment(XResolverException.class, ex);
      }
   }

   protected abstract void resolveInternal(XModule rootModule); 

   @Override
   public List<XModule> resolve(List<XModule> modules)
   {
      if (modules == null)
         modules = getModules();
      
      List<XModule> result = new ArrayList<XModule>();
      for (XModule module : modules)
      {
         try
         {
            module.removeAttachment(XResolverException.class);
            resolveInternal(module);
            result.add(module);
         }
         catch (XResolverException ex)
         {
            // Add the last resolver exception to the module
            module.addAttachment(XResolverException.class, ex);
         }
      }
      return Collections.unmodifiableList(result);
   }

   protected void setResolved(AbstractModule module)
   {
      module.setResolved();
   }

   protected XWire addWire(AbstractModule importer, XRequirement requirement, XModule exporter, XCapability capability)
   {
      AbstractWire wire = new AbstractWire(importer, requirement, exporter, capability);
      importer.addWire(wire);
      return wire;
   }
}