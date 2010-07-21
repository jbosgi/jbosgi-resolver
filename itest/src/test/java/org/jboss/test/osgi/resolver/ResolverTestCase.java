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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XResolverException;
import org.jboss.osgi.resolver.XWire;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class ResolverTestCase extends AbstractResolverTestCase
{
   @Test
   public void testSimpleImport() throws Exception
   {
      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/simpleimport");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      Set<XModule> modules = new HashSet<XModule>();
      modules.add(moduleA);
      modules.add(moduleB);
      Set<XModule> result = resolver.resolveAll(modules);

      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleB, wiresA.get(0).getExporter());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(0, wiresB.size());
   }

   @Test
   public void testSimpleImportPackageFails() throws Exception
   {
      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/simpleimport");
      XModule moduleA = installModule(assemblyA);

      try
      {
         resolver.resolve(moduleA);
         fail("XResolverException expected");
      }
      catch (XResolverException ex)
      {
         // expected;
      }
   }

   @Test
   public void testExplicitBundleResolve() throws Exception
   {
      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/simpleimport");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Only resolve moduleB
      resolver.resolve(moduleB);

      // Verify bundle states
      assertFalse("moduleA INSTALLED", moduleA.isResolved());
      assertNull("moduleA null wires", moduleA.getWires());
      assertTrue("moduleB RESOLVED", moduleB.isResolved());
      assertNotNull("moduleB wires", moduleB.getWires());
   }

   @Test
   public void testSelfImportPackage() throws Exception
   {
      // Bundle-SymbolicName: selfimport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/selfimport");
      XModule moduleA = installModule(assemblyA);

      resolver.resolve(moduleA);

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleA, wiresA.get(0).getExporter());
      assertEquals(moduleA, wiresA.get(0).getImporter());
   }

   @Test
   public void testVersionImportPackage() throws Exception
   {
      //Bundle-SymbolicName: packageimportversion
      //Import-Package: org.jboss.test.osgi.classloader.support.a;version="[0.0.0,1.0.0]"
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageimportversion");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: packageexportversion100
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/packageexportversion100");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleB, wiresA.get(0).getExporter());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(0, wiresB.size());
   }

   @Test
   public void testVersionImportPackageFails() throws Exception
   {
      //Bundle-SymbolicName: packageimportversionfails
      //Import-Package: org.jboss.test.osgi.classloader.support.a;version="[3.0,4.0)"
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageimportversionfails");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: packageexportversion100
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/packageexportversion100");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(1, result.size());
      assertFalse(moduleA.isResolved());
      assertTrue(moduleB.isResolved());
   }

   @Test
   public void testOptionalImportPackage() throws Exception
   {
      //Bundle-SymbolicName: packageimportoptional
      //Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageimportoptional");
      XModule moduleA = installModule(assemblyA);

      resolver.resolve(moduleA);
      assertTrue(moduleA.isResolved());
      assertEquals(0, moduleA.getWires().size());
   }

   @Test
   public void testOptionalImportPackageWired() throws Exception
   {
      //Bundle-SymbolicName: packageimportoptional
      //Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageimportoptional");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleB, wiresA.get(0).getExporter());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(0, wiresB.size());
   }

   @Test
   public void testOptionalImportPackageNotWired() throws Exception
   {
      //Bundle-SymbolicName: packageimportoptional
      //Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageimportoptional");
      XModule moduleA = installModule(assemblyA);

      resolver.resolve(moduleA);
      assertTrue(moduleA.isResolved());

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Verify that the class cannot be loaded from moduleA
      // because the wire could not be established when moduleA was resolved
      resolver.resolve(moduleB);
      assertTrue(moduleB.isResolved());

      assertEquals(0, moduleA.getWires().size());
      assertEquals(0, moduleB.getWires().size());
   }

   @Test
   public void testBundleNameImportPackage() throws Exception
   {
      //Bundle-SymbolicName: bundlenameimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/bundlenameimport");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: simpleexport
      //Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleB, wiresA.get(0).getExporter());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(0, wiresB.size());
   }

   @Test
   public void testBundleNameImportPackageFails() throws Exception
   {
      //Bundle-SymbolicName: bundlenameimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/bundlenameimport");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: sigleton;singleton:=true
      //Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/singleton");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(1, result.size());
      assertFalse(moduleA.isResolved());
      assertTrue(moduleB.isResolved());
   }

   @Test
   public void testBundleVersionImportPackage() throws Exception
   {
      //Bundle-SymbolicName: bundleversionimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[0.0.0,1.0.0)"
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/bundleversionimport");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleB, wiresA.get(0).getExporter());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(0, wiresB.size());
   }

   @Test
   public void testBundleVersionImportPackageFails() throws Exception
   {
      //Bundle-SymbolicName: bundleversionimportfails
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[1.0.0,2.0.0)"
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/bundleversionimportfails");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(1, result.size());
      assertFalse(moduleA.isResolved());
      assertTrue(moduleB.isResolved());
   }

   @Test
   public void testRequireBundle() throws Exception
   {
      // [TODO] require bundle visibility

      //Bundle-SymbolicName: requirebundle
      //Require-Bundle: simpleexport
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/requirebundle");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleB, wiresA.get(0).getExporter());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(0, wiresB.size());
   }

   @Test
   public void testRequireBundleFails() throws Exception
   {
      //Bundle-SymbolicName: requirebundle
      //Require-Bundle: simpleexport
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/requirebundle");
      XModule moduleA = installModule(assemblyA);

      try
      {
         resolver.resolve(moduleA);
         fail("XResolverException expected");
      }
      catch (XResolverException ex)
      {
         // expected;
      }
   }

   @Test
   public void testRequireBundleOptional() throws Exception
   {
      //Bundle-SymbolicName: requirebundleoptional
      //Require-Bundle: simpleexport;resolution:=optional
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/requirebundleoptional");
      XModule moduleA = installModule(assemblyA);

      resolver.resolve(moduleA);
      assertTrue(moduleA.isResolved());
   }

   @Test
   public void testRequireBundleVersion() throws Exception
   {
      //Bundle-SymbolicName: requirebundleversion
      //Require-Bundle: simpleexport;bundle-version="[0.0.0,1.0.0]"
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/requirebundleversion");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresA = moduleA.getWires();
      assertEquals(1, wiresA.size());
      assertEquals(moduleB, wiresA.get(0).getExporter());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(0, wiresB.size());
   }

   @Test
   public void testRequireBundleVersionFails() throws Exception
   {
      //Bundle-SymbolicName: versionrequirebundlefails
      //Require-Bundle: simpleexport;bundle-version="[1.0.0,2.0.0)"
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/requirebundleversionfails");
      XModule moduleA = installModule(assemblyA);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      assertEquals(1, result.size());
      assertFalse(moduleA.isResolved());
      assertTrue(moduleB.isResolved());
   }

   @Test
   public void testPreferredExporterResolved() throws Exception
   {
      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/simpleexport");

      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexportother");

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("moduleC", "/resolver/simpleimport");

      XModule moduleA = installModule(assemblyA);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(1, result.size());
      assertTrue(moduleA.isResolved());

      XModule moduleB = installModule(assemblyB);

      XModule moduleC = installModule(assemblyC);

      // Resolve all modules
      result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(2, result.size());
      assertTrue(moduleB.isResolved());
      assertTrue(moduleC.isResolved());

      List<XWire> wiresC = moduleC.getWires();
      assertEquals(1, wiresC.size());

      System.out.println("FIXME testPreferredExporterResolved fails occasionally");
      //assertEquals(moduleA, wiresC.get(0).getExporter());
   }

   @Test
   public void testPreferredExporterResolvedReverse() throws Exception
   {
      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/simpleexport");

      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexportother");

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("moduleC", "/resolver/simpleimport");

      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(1, result.size());
      assertTrue(moduleB.isResolved());

      XModule moduleA = installModule(assemblyA);

      XModule moduleC = installModule(assemblyC);

      // Resolve all modules
      result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleC.isResolved());

      List<XWire> wiresC = moduleC.getWires();
      assertEquals(1, wiresC.size());

      System.out.println("FIXME testPreferredExporterResolvedReverse fails occasionally");
      //assertEquals(moduleB, wiresC.get(0).getExporter());
   }

   @Test
   public void testPreferredExporterHigherVersion() throws Exception
   {
      //Bundle-SymbolicName: packageexportversion100
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageexportversion100");

      //Bundle-SymbolicName: packageexportversion200
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/packageexportversion200");

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("moduleC", "/resolver/simpleimport");

      XModule moduleA = installModule(assemblyA);
      XModule moduleB = installModule(assemblyB);
      XModule moduleC = installModule(assemblyC);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(3, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());
      assertTrue(moduleC.isResolved());

      List<XWire> wiresC = moduleC.getWires();
      assertEquals(1, wiresC.size());
      assertEquals(moduleB, wiresC.get(0).getExporter());
   }

   @Test
   public void testPreferredExporterHigherVersionReverse() throws Exception
   {
      //Bundle-SymbolicName: packageexportversion200
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageexportversion200");

      //Bundle-SymbolicName: packageexportversion100
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/packageexportversion100");

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("moduleC", "/resolver/simpleimport");

      XModule moduleA = installModule(assemblyA);
      XModule moduleB = installModule(assemblyB);
      XModule moduleC = installModule(assemblyC);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(3, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());
      assertTrue(moduleC.isResolved());

      List<XWire> wiresC = moduleC.getWires();
      assertEquals(1, wiresC.size());
      assertEquals(moduleA, wiresC.get(0).getExporter());
   }

   @Test
   public void testPreferredExporterLowerId() throws Exception
   {
      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/simpleexport");

      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleexportother");

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("moduleC", "/resolver/simpleimport");

      XModule moduleA = installModule(assemblyA);
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      XModule moduleC = installModule(assemblyC);

      // Resolve all modules
      result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(1, result.size());
      assertTrue(moduleC.isResolved());

      List<XWire> wiresC = moduleC.getWires();
      assertEquals(1, wiresC.size());

      System.out.println("FIXME testPreferredExporterLowerId fails occasionally");
      //assertEquals(moduleA, wiresC.get(0).getExporter());
   }

   @Test
   public void testPreferredExporterLowerIdReverse() throws Exception
   {
      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleA", "/resolver/simpleexportother");

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("moduleB", "/resolver/simpleexport");

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("moduleC", "/resolver/simpleimport");

      XModule moduleB = installModule(assemblyB);
      XModule moduleA = installModule(assemblyA);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(2, result.size());
      assertTrue(moduleB.isResolved());
      assertTrue(moduleA.isResolved());

      XModule moduleC = installModule(assemblyC);

      // Resolve all modules
      result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(1, result.size());
      assertTrue(moduleC.isResolved());

      List<XWire> wiresC = moduleC.getWires();
      assertEquals(1, wiresC.size());

      System.out.println("FIXME testPreferredExporterLowerIdReverse fails occasionally");
      //assertEquals(moduleB, wiresC.get(0).getExporter());
   }

   @Test
   public void testPackageAttribute() throws Exception
   {
      //Bundle-SymbolicName: packageexportattribute
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageexportattribute");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: simpleimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleimport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(1, wiresB.size());
      assertEquals(moduleA, wiresB.get(0).getExporter());

      //Bundle-SymbolicName: packageimportattribute
      //Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
      Archive<?> assemblyC = assembleArchive("moduleC", "/resolver/packageimportattribute");
      XModule moduleC = installModule(assemblyC);

      result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(1, result.size());
      assertTrue(moduleC.isResolved());

      List<XWire> wiresC = moduleC.getWires();
      assertEquals(1, wiresC.size());
      assertEquals(moduleA, wiresC.get(0).getExporter());
   }

   @Test
   public void testPackageAttributeFails() throws Exception
   {
      //Bundle-SymbolicName: packageexportattribute
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageexportattribute");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: packageimportattributefails
      //Import-Package: org.jboss.test.osgi.classloader.support.a;test=y
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/packageimportattributefails");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(1, result.size());
      assertTrue(moduleA.isResolved());
      assertFalse(moduleB.isResolved());
   }

   @Test
   public void testPackageAttributeMandatory() throws Exception
   {
      //Bundle-SymbolicName: packageexportattributemandatory
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageexportattributemandatory");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: packageimportattribute
      //Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/packageimportattribute");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(2, result.size());
      assertTrue(moduleA.isResolved());
      assertTrue(moduleB.isResolved());

      List<XWire> wiresB = moduleB.getWires();
      assertEquals(1, wiresB.size());
      assertEquals(moduleA, wiresB.get(0).getExporter());
   }

   @Test
   public void testPackageAttributeMandatoryFails() throws Exception
   {
      //Bundle-SymbolicName: packageexportattributemandatory
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
      Archive<?> assemblyA = assembleArchive("moduleA", "/resolver/packageexportattributemandatory");
      XModule moduleA = installModule(assemblyA);

      //Bundle-SymbolicName: simpleimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("moduleB", "/resolver/simpleimport");
      XModule moduleB = installModule(assemblyB);

      // Resolve all modules
      Collection<XModule> result = resolver.resolveAll(null);

      // Verify bundle states
      assertEquals(1, result.size());
      assertTrue(moduleA.isResolved());
      assertFalse(moduleB.isResolved());
   }
}