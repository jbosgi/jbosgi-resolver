/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.osgi.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * A builder for {@link OSGiMetaData}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 04-Jun-2010
 */
public class OSGiMetaDataBuilder
{
   private DynamicOSGiMetaData metadata;
   private List<String> importPackages = new ArrayList<String>();
   private List<String> exportPackages = new ArrayList<String>();
   private List<String> dynamicImportPackages = new ArrayList<String>();

   public static OSGiMetaData load(InputStream input) throws IOException
   {
      if (input == null)
         throw new IllegalArgumentException("Null input");

      return load(new InputStreamReader(input));
   }

   public static OSGiMetaData load(Reader reader) throws IOException
   {
      if (reader == null)
         throw new IllegalArgumentException("Null reader");

      Properties props = new Properties();
      props.load(reader);
      return load(props);
   }

   public static OSGiMetaData load(Properties props)
   {
      if (props == null)
         throw new IllegalArgumentException("Null props");

      Manifest manifest = new Manifest();
      Attributes mainAttributes = manifest.getMainAttributes();
      for (Object key : props.keySet())
      {
         Attributes.Name name = new Attributes.Name((String)key);
         mainAttributes.put(name, props.get(key));
      }

      return load(manifest);
   }

   public static OSGiMetaData load(Manifest manifest)
   {
      return new OSGiManifestMetaData(manifest);
   }

   public static OSGiMetaDataBuilder createBuilder(String symbolicName)
   {
      return new OSGiMetaDataBuilder(symbolicName, Version.emptyVersion);
   }

   public static OSGiMetaDataBuilder createBuilder(String symbolicName, Version version)
   {
      return new OSGiMetaDataBuilder(symbolicName, version);
   }

   private OSGiMetaDataBuilder(String symbolicName, Version version)
   {
      metadata = new DynamicOSGiMetaData(symbolicName, version);
   }

   public OSGiMetaDataBuilder setBundleManifestVersion(int version)
   {
      metadata.addMainAttribute(Constants.BUNDLE_MANIFESTVERSION, "" + version);
      return this;
   }

   public OSGiMetaDataBuilder setBundleActivator(String value)
   {
      metadata.addMainAttribute(Constants.BUNDLE_ACTIVATOR, value);
      return this;
   }

   public OSGiMetaDataBuilder addImportPackages(Class<?>... packages)
   {
      for (Class<?> aux : packages)
         addImportPackages(aux.getPackage().getName());

      return this;
   }

   public OSGiMetaDataBuilder addImportPackages(String... packages)
   {
      for (String aux : packages)
         importPackages.add(aux);

      return this;
   }

   public OSGiMetaDataBuilder addExportPackages(Class<?>... packages)
   {
      for (Class<?> aux : packages)
         addExportPackages(aux.getPackage().getName());

      return this;
   }

   public OSGiMetaDataBuilder addExportPackages(String... packages)
   {
      for (String aux : packages)
         exportPackages.add(aux);

      return this;
   }

   public OSGiMetaDataBuilder addDynamicImportPackages(Class<?>... packages)
   {
      for (Class<?> aux : packages)
         addDynamicImportPackages(aux.getPackage().getName());

      return this;
   }

   public OSGiMetaDataBuilder addDynamicImportPackages(String... packages)
   {
      for (String aux : packages)
         dynamicImportPackages.add(aux);

      return this;
   }

   public OSGiMetaDataBuilder addMainAttribute(String key, String value)
   {
      metadata.addMainAttribute(key, value);
      return this;
   }

   public OSGiMetaData getOSGiMetaData()
   {
      // Export-Package
      if (exportPackages.size() > 0)
      {
         StringBuffer value = new StringBuffer();
         for (int i = 0; i < exportPackages.size(); i++)
         {
            value.append(i > 0 ? "," : "");
            value.append(exportPackages.get(i));
         }
         metadata.addMainAttribute(Constants.EXPORT_PACKAGE, value.toString());
      }

      // Import-Package
      if (importPackages.size() > 0)
      {
         StringBuffer value = new StringBuffer();
         for (int i = 0; i < importPackages.size(); i++)
         {
            value.append(i > 0 ? "," : "");
            value.append(importPackages.get(i));
         }
         metadata.addMainAttribute(Constants.IMPORT_PACKAGE, value.toString());
      }

      // DynamicImport-Package
      if (dynamicImportPackages.size() > 0)
      {
         StringBuffer value = new StringBuffer();
         for (int i = 0; i < dynamicImportPackages.size(); i++)
         {
            value.append(i > 0 ? "," : "");
            value.append(dynamicImportPackages.get(i));
         }
         metadata.addMainAttribute(Constants.DYNAMICIMPORT_PACKAGE, value.toString());
      }
      return metadata;
   }
}
