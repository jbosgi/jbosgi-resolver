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

import java.util.Map;

import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XFragmentHostRequirement;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequireBundleRequirement;
import org.osgi.framework.Version;

/**
 * A builder for resolver modules
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XModuleBuilder
{
   /**
    * Get a new module and associate it with this builder
    * @param symbolicName The module symbolic name
    * @param version The module version
    */
   XModule createModule(long moduleId, String symbolicName, Version version);

   /**
    * Add a bundle capability
    * @param symbolicName The bundle symbolic name
    * @param version The bundle version
    */
   XBundleCapability addBundleCapability(String symbolicName, Version version);

   /**
    * Add a bundle requirement
    * @param symbolicName The bundle symbolic name
    * @param dirs The directives
    * @param atts The attributes
    */
   XRequireBundleRequirement addBundleRequirement(String symbolicName, Map<String, String> dirs, Map<String, Object> atts);

   /**
    * Add a fragment host requirement
    * @param symbolicName The bundle symbolic name
    * @param dirs The directives
    * @param atts The attributes
    */
   XFragmentHostRequirement addFragmentHostRequirement(String symbolicName, Map<String, String> dirs, Map<String, Object> atts);

   /**
    * Add a package capability
    * @param name The package name
    * @param dirs The directives
    * @param atts The attributes
    */
   XPackageCapability addPackageCapability(String name, Map<String, String> dirs, Map<String, Object> atts);

   /**
    * Add a package requirement
    * @param name The package name
    * @param dirs The directives
    * @param atts The attributes
    */
   XPackageRequirement addPackageRequirement(String name, Map<String, String> dirs, Map<String, Object> atts);

   /**
    * Add a dynamic package requirement
    * @param name The package name
    * @param atts The attributes
    */
   XPackageRequirement addDynamicPackageRequirement(String name, Map<String, Object> atts);

   /**
    * Get the final module from the builder
    */
   XModule getModule();
}