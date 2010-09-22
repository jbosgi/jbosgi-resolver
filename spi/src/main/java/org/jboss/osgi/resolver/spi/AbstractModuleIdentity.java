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

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XModuleIdentity;
import org.osgi.framework.Version;

/**
 * An {@link XModule} identity.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Sep-2010
 */
final class AbstractModuleIdentity implements XModuleIdentity
{
   private String name;
   private Version version;
   private int revision;

   AbstractModuleIdentity (OSGiMetaData metadata, int revision)
   {
      if (metadata == null)
         throw new IllegalArgumentException("Null metadata");

      this.name = metadata.getBundleSymbolicName();
      this.version = metadata.getBundleVersion();
      this.revision = revision;
   }

   AbstractModuleIdentity (String name, Version version, int revision)
   {
       if (name == null)
           throw new IllegalArgumentException("Null name part");

       if (version == null)
           version = Version.emptyVersion;

      this.name = name;
      this.version = version;
      this.revision = revision;
   }

   public String getName()
   {
      return name;
   }

   public Version getVersion()
   {
      return version;
   }

   public int getRevision()
   {
      return revision;
   }

   public static AbstractModuleIdentity parse(String string)
   {
      if (string == null)
         throw new IllegalArgumentException("Null string");

      String[] parts = string.split(":");
      if (parts.length < 2 || parts.length > 3)
         throw new IllegalArgumentException("Invalid string: " + string);


      int revision = 0;
      if (parts.length > 2)
      {
          if (parts[2].startsWith("rev") == false)
              throw new IllegalArgumentException("Invalid revision part: " + parts[2]);
          revision = Integer.parseInt(parts[2].substring(3));
      }

      return new AbstractModuleIdentity(parts[0], Version.parseVersion(parts[1]), revision);
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
      if (obj instanceof AbstractModuleIdentity == false)
         return false;
      return toString().equals(obj.toString());
   }

   @Override
   public String toString()
   {
      String string = name + ":" + version;
      if (revision > 0)
         string += ":rev" + revision;
      return string;
   }

}