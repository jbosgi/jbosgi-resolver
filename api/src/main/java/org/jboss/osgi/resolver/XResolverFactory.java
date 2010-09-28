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
package org.jboss.osgi.resolver;

import org.jboss.osgi.spi.util.ServiceLoader;


/**
 * A factory for resolver instances.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class XResolverFactory
{
   /**
    * Get an instance of the resolver factory
    */
   public static XResolverFactory getInstance()
   {
      return getInstance(null);
   }
   
   /**
    * Get an instance of the resolver factory
    */
   public static XResolverFactory getInstance(ClassLoader classloader)
   {
      XResolverFactory factory = ServiceLoader.loadService(XResolverFactory.class);
      if (factory == null)
         throw new IllegalStateException("Cannot load service: " + XResolverFactory.class.getName());
      return factory;

      // [JBAS-8458] Cannot use java.util.ServiceLoader in subsystem
      //ServiceLoader<XResolver> loader = ServiceLoader.load(XResolver.class, classloader);
      //return loader.iterator().next();
   }

   /**
    * Get a new instance of an {@link XResolver} 
    */
   public abstract XResolver newResolver();
   
   /**
    * Get a new instance of an {@link XModuleBuilder} 
    */
   public abstract XModuleBuilder newModuleBuilder();
}