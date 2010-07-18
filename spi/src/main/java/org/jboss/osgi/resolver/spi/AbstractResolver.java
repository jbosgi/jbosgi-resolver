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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
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
   static private Logger log = Logger.getLogger(AbstractResolver.class);
   
   private XResolverCallback callback;
   private Map<Long, XModule> moduleMap = new LinkedHashMap<Long, XModule>();
   private Map<XRequirement, XCapability> reqcapMap = new ConcurrentHashMap<XRequirement, XCapability>();
   private Map<XCapability, Set<XRequirement>> capreqMap = new ConcurrentHashMap<XCapability, Set<XRequirement>>();

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
      if (callback == null)
         throw new IllegalArgumentException("Null callback");
      
      this.callback = callback;
   }

   protected XResolverCallback getCallbackHandler()
   {
      return callback;
   }

   @Override
   public void addModule(XModule module)
   {
      if (module == null)
         throw new IllegalArgumentException("Null module");
      
      if (log.isTraceEnabled())
      {
         StringBuffer buffer = ((AbstractModule)module).toLongString(new StringBuffer());
         log.trace(buffer);
      }
         
      synchronized (moduleMap)
      {
         long moduleId = module.getModuleId();
         if (moduleMap.get(moduleId) != null)
            throw new IllegalStateException("Module already added: " + module);
         
         moduleMap.put(moduleId, module);
         ((AbstractModule)module).setResolver(this);
      }
   }

   @Override
   public void removeModule(XModule module)
   {
      if (module == null)
         throw new IllegalArgumentException("Null module");
      
      synchronized (moduleMap)
      {
         XModule result = moduleMap.remove(module.getModuleId());
         if (result != null)
            ((AbstractModule)result).setResolver(null);
      }
      
      // Cleanup the cap <--> req mapping
      synchronized (this)
      {
         for (XRequirement req : module.getRequirements())
         {
            XCapability cap = reqcapMap.get(req);
            if (cap != null)
            {
               Set<XRequirement> reqset = capreqMap.get(cap);
               if (reqset != null)
                  reqset.remove(req);
            }
         }
         for (XCapability cap : module.getCapabilities())
         {
            capreqMap.remove(cap);
         }
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
      if (module == null)
         throw new IllegalArgumentException("Null module");
      
      if (findModuleById(module.getModuleId()) == null)
         throw new IllegalStateException("Module not registered: " + module);
         
      try
      {
         module.removeAttachment(XResolverException.class);
         resolveInternal(module);
      }
      catch (XResolverException rex)
      {
         // Add the last resolver exception to the module
         module.addAttachment(XResolverException.class, rex);
         throw rex;
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
      if (module == null)
         throw new IllegalArgumentException("Null module");
      
      module.setResolved();
      
      try
      {
         callback.markResolved(module);
      }
      catch (RuntimeException ex)
      {
         // [TODO] settle on a logging strategy
         System.err.println("Error in callback: " + callback.getClass().getName());
         ex.printStackTrace();
      }
   }

   protected XWire addWire(AbstractModule importer, XRequirement req, XModule exporter, XCapability cap)
   {
      AbstractWire wire = new AbstractWire(importer, req, exporter, cap);
      importer.addWire(wire);
      
      synchronized (this)
      {
         // Map the requirement to its capability
         reqcapMap.put(req, cap);
         
         // Map the capability to the set of requirements that it is wired to
         Set<XRequirement> reqset = capreqMap.get(cap);
         if (reqset == null)
            capreqMap.put(cap, reqset = new HashSet<XRequirement>());
         reqset.add(req);
      }
      return wire;
   }

   @Override
   public Set<XRequirement> getWiredRequirements(XCapability cap)
   {
      Set<XRequirement> reqset = capreqMap.get(cap);
      if (reqset == null)
         return Collections.emptySet();
      
      return Collections.unmodifiableSet(reqset);
   }

   @Override
   public XCapability getWiredCapability(XRequirement req)
   {
      return reqcapMap.get(req);
   }
}