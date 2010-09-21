/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
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

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;

/**
 * A properties based parser for {@link ModuleMetaData}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 09-Aug-2010
 */
public final class XModuleParser
{
   // Hide ctor
   private XModuleParser()
   {
   }

   public static XModuleParser newInstance()
   {
      return new XModuleParser();
   }

   public XModule parse(Reader reader) throws IOException
   {
      Properties props = new Properties();
      props.load(reader);
      return parse(props);
   }

   public XModule parse(Properties props)
   {
      if (props == null)
         throw new IllegalArgumentException("Null props");

      String symbolicName = props.getProperty(Constants.BUNDLE_SYMBOLICNAME);
      if (symbolicName == null)
         throw new IllegalStateException("Cannot obtain: " + Constants.BUNDLE_SYMBOLICNAME);

      String version = props.getProperty(Constants.BUNDLE_VERSION);
      XModuleIdentity moduleId = XModuleIdentity.create(symbolicName, version, null);

      Manifest manifest = new Manifest();
      Attributes mainAttributes = manifest.getMainAttributes();
      for (Object key : props.keySet())
      {
         Attributes.Name name = new Attributes.Name((String)key);
         mainAttributes.put(name, props.get(key));
      }

      try
      {
         XModuleBuilder builder = XResolverFactory.loadModuleBuilder(getClass().getClassLoader());
         builder.createModule(moduleId, manifest);
         return builder.getModule();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot create XModule from: " + props, ex);
      }
   }
}
