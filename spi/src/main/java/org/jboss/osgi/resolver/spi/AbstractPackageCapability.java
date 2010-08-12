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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * The abstract implementation of a {@link XBundleCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractPackageCapability extends AbstractCapability implements XPackageCapability
{
   private Version version = Version.emptyVersion;

   public AbstractPackageCapability(AbstractModule module, String name, Map<String, String> dirs, Map<String, Object> atts)
   {
      super(module, name, dirs, atts);
      
      Object att = getAttribute(Constants.VERSION_ATTRIBUTE);
      if (att != null)
         version = Version.parseVersion(att.toString());
   }

   @Override
   public Version getVersion()
   {
      return version;
   }

   @Override
   public List<String> getUses()
   {
      String dir = getDirective(Constants.USES_DIRECTIVE);
      if (dir == null)
         return Collections.emptyList();
      
      String[] split = dir.split(",");
      return Arrays.asList(split);
   }

   @Override
   public List<String> getMandatory()
   {
      String dir = getDirective(Constants.MANDATORY_DIRECTIVE);
      if (dir == null)
         return Collections.emptyList();
      
      String[] split = dir.split(",");
      return Arrays.asList(split);
   }

   @Override
   public List<String> getInclude()
   {
      String dir = getDirective(Constants.INCLUDE_DIRECTIVE);
      if (dir == null)
         return Collections.emptyList();
      
      String[] split = dir.split(",");
      return Arrays.asList(split);
   }

   @Override
   public List<String> getExclude()
   {
      String dir = getDirective(Constants.EXCLUDE_DIRECTIVE);
      if (dir == null)
         return Collections.emptyList();
      
      String[] split = dir.split(",");
      return Arrays.asList(split);
   }

   @Override
   public String toString()
   {
      return Constants.EXPORT_PACKAGE + "[" + getName() + ":" + version + "]";
   }
}