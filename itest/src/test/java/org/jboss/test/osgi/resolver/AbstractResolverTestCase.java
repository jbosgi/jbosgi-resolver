package org.jboss.test.osgi.resolver;

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

import java.util.Hashtable;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.internal.OSGiManifestMetaData;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XModuleBuilder;
import org.jboss.osgi.resolver.XModuleIdentity;
import org.jboss.osgi.resolver.XResolver;
import org.jboss.osgi.resolver.XResolverCallback;
import org.jboss.osgi.resolver.XResolverFactory;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.osgi.vfs.VFSUtils;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

/**
 * The abstract resolver test.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolverTestCase extends OSGiTest
{
   XResolver resolver;

   @Before
   public void setUp()
   {
      resolver = XResolverFactory.getResolver();
   }

   XModule installModule(Archive<?> archive) throws Exception
   {
      VirtualFile virtualFile = toVirtualFile(archive);
      Manifest manifest = VFSUtils.getManifest(virtualFile);
      OSGiManifestMetaData osgiMetaData = new OSGiManifestMetaData(manifest);

      // Setup the headers
      Hashtable<String, String> headers = new Hashtable<String, String>();
      Attributes attributes = manifest.getMainAttributes();
      for (Object key : attributes.keySet())
      {
         String value = attributes.getValue(key.toString());
         headers.put(key.toString(), value);
      }

      XModuleBuilder builder = XResolverFactory.getModuleBuilder();
      XModuleIdentity moduleId = XModuleIdentity.create(osgiMetaData, null);
      XModule module = builder.createModule(moduleId, manifest);

      Bundle bundle = Mockito.mock(Bundle.class);
      Mockito.when(bundle.getHeaders()).thenReturn(headers);
      module.addAttachment(Bundle.class, bundle);

      resolver.addModule(module);
      return module;
   }

   class ResolverCallback implements XResolverCallback
   {
      private List<XModule> resolved;

      ResolverCallback(List<XModule> resolved)
      {
         this.resolved = resolved;
      }

      @Override
      public void markResolved(XModule resModule)
      {
         resolved.add(resModule);
      }
   }
}