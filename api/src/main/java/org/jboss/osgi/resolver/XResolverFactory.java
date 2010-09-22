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
public final class XResolverFactory
{
   // Hide ctor
   XResolverFactory()
   {
   }

   /**
    * Get a new instance of a module builder.
    */
   public static XModuleBuilder loadModuleBuilder(ClassLoader classloader)
   {
      //classloader = fixupClassLoader(classloader);
      //ServiceLoader<XModuleBuilder> loader = ServiceLoader.load(XModuleBuilder.class, classloader);
      //return loader.iterator().next();

      // [JBAS-8458] Cannot use java.util.ServiceLoader in subsystem
      XModuleBuilder builder = ServiceLoader.loadService(XModuleBuilder.class);
      if (builder == null)
         throw new IllegalStateException("Cannot load XModuleBuilder");
      return builder;
   }

   /**
    * Get a new instance of a resolver.
    */
   public static XResolver loadResolver(ClassLoader classloader)
   {
      XResolver resolver = ServiceLoader.loadService(XResolver.class);
      if (resolver == null)
         throw new IllegalStateException("Cannot load XResolver");
      return resolver;

      // [JBAS-8458] Cannot use java.util.ServiceLoader in subsystem
      //classloader = fixupClassLoader(classloader);
      //ServiceLoader<XResolver> loader = ServiceLoader.load(XResolver.class, classloader);
      //return loader.iterator().next();
   }

   private static ClassLoader fixupClassLoader(ClassLoader classloader)
   {
      if (classloader == null)
         classloader = Thread.currentThread().getContextClassLoader();
      if (classloader == null)
         classloader = XResolverFactory.class.getClassLoader();
      return classloader;
   }
}