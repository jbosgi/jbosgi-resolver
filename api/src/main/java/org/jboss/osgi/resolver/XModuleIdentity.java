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

import java.io.Serializable;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.osgi.framework.Version;

/**
 * An {@link XModule} identity.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Sep-2010
 */
public final class XModuleIdentity implements Serializable
{
   private static final long serialVersionUID = -6096312558786598132L;

   private String name;
   private String version;
   private String revision;

   public static XModuleIdentity create(OSGiMetaData osgiMetaData, String revision)
   {
      String name = osgiMetaData.getBundleSymbolicName();
      String version = osgiMetaData.getBundleVersion().toString();
      return new XModuleIdentity(name, version, revision);
   }

   public static XModuleIdentity create(String name, String version, String revision)
   {
      return new XModuleIdentity(name, version, revision);
   }

   private XModuleIdentity(String name, String version, String revision)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name part");

      this.version = "0.0.0";
      if (version != null)
      {
         Version.parseVersion(version);
         this.version = version;
      }

      this.name = name;
      this.revision = revision;
   }

   public String getName()
   {
      return name;
   }

   public String getVersion()
   {
      return version;
   }

   public String getRevision()
   {
      return revision;
   }

   public static XModuleIdentity parse(String string)
   {
      if (string == null)
         throw new IllegalArgumentException("Null string");

      String[] parts = string.split("-");
      if (parts.length < 2 || parts.length > 3)
         throw new IllegalArgumentException("Invalid string: " + string);

      return new XModuleIdentity(parts[0], parts[1], parts[2]);
   }

   @Override
   public int hashCode()
   {
      return toString().hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj instanceof XModuleIdentity == false)
         return false;
      return toString().equals(obj.toString());
   }

   @Override
   public String toString()
   {
      String string = name + "-" + version;
      if (revision != null)
         string += "-" + revision;
      return string;
   }

}