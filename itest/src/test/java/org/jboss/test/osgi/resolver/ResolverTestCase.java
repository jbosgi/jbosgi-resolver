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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.osgi.resolver.XFragmentHostRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResolverException;
import org.jboss.osgi.resolver.XWire;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @since 31-May-2010
 */
public class ResolverTestCase extends AbstractResolverTestCase {
    @Test
    public void testSimpleImport() throws Exception {
        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        Set<XResource> resources = new HashSet<XResource>();
        resources.add(resourceA);
        resources.add(resourceB);

        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));

        assertTrue(resolver.resolveAll(resources));

        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceB, wiresA.get(0).getExporter());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testSimpleImportPackageFails() throws Exception {
        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        XResource resourceA = installModule(assemblyA);

        try {
            resolver.resolve(resourceA);
            fail("XResolverException expected");
        } catch (XResolverException ex) {
            // expected;
        }
    }

    @Test
    public void testExplicitBundleResolve() throws Exception {
        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Only resolve resourceB
        resolver.resolve(resourceB);

        // Verify bundle states
        assertFalse("resourceA INSTALLED", resourceA.isResolved());
        assertNull("resourceA null wires", resourceA.getWires());
        assertTrue("resourceB RESOLVED", resourceB.isResolved());
        assertNotNull("resourceB wires", resourceB.getWires());
    }

    @Test
    public void testSelfImportPackage() throws Exception {
        // Bundle-SymbolicName: selfimport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/selfimport");
        XResource resourceA = installModule(assemblyA);

        resolver.resolve(resourceA);

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceA, wiresA.get(0).getExporter());
        assertEquals(resourceA, wiresA.get(0).getImporter());
    }

    @Test
    public void testVersionImportPackage() throws Exception {
        // Bundle-SymbolicName: packageimportversion
        // Import-Package: org.jboss.test.osgi.classloader.support.a;version="[0.0.0,1.0.0]"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportversion");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion100");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceB, wiresA.get(0).getExporter());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testVersionImportPackageFails() throws Exception {
        // Bundle-SymbolicName: packageimportversionfails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;version="[3.0,4.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportversionfails");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion100");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertFalse(resolver.resolveAll(null));

        assertEquals(1, resolved.size());
        assertFalse(resourceA.isResolved());
        assertTrue(resourceB.isResolved());
    }

    @Test
    public void testOptionalImportPackage() throws Exception {
        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        XResource resourceA = installModule(assemblyA);

        resolver.resolve(resourceA);
        assertTrue(resourceA.isResolved());
        assertEquals(0, resourceA.getWires().size());
    }

    @Test
    public void testOptionalImportPackageWired() throws Exception {
        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceB, wiresA.get(0).getExporter());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testOptionalImportPackageNotWired() throws Exception {
        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        XResource resourceA = installModule(assemblyA);

        resolver.resolve(resourceA);
        assertTrue(resourceA.isResolved());

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Verify that the class cannot be loaded from resourceA
        // because the wire could not be established when resourceA was resolved
        resolver.resolve(resourceB);
        assertTrue(resourceB.isResolved());

        assertEquals(0, resourceA.getWires().size());
        assertEquals(0, resourceB.getWires().size());
    }

    @Test
    public void testBundleNameImportPackage() throws Exception {
        // Bundle-SymbolicName: bundlenameimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundlenameimport");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceB, wiresA.get(0).getExporter());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testBundleNameImportPackageFails() throws Exception {
        // Bundle-SymbolicName: bundlenameimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundlenameimport");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: sigleton;singleton:=true
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/singleton");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertFalse(resolver.resolveAll(null));

        assertEquals(1, resolved.size());
        assertFalse(resourceA.isResolved());
        assertTrue(resourceB.isResolved());
    }

    @Test
    public void testBundleVersionImportPackage() throws Exception {
        // Bundle-SymbolicName: bundleversionimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[0.0.0,1.0.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundleversionimport");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceB, wiresA.get(0).getExporter());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testBundleVersionImportPackageFails() throws Exception {
        // Bundle-SymbolicName: bundleversionimportfails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[1.0.0,2.0.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundleversionimportfails");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertFalse(resolver.resolveAll(null));

        assertEquals(1, resolved.size());
        assertFalse(resourceA.isResolved());
        assertTrue(resourceB.isResolved());
    }

    @Test
    public void testRequireBundle() throws Exception {
        // [TODO] require bundle visibility

        // Bundle-SymbolicName: requirebundle
        // Require-Bundle: simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundle");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceB, wiresA.get(0).getExporter());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testRequireBundleFails() throws Exception {
        // Bundle-SymbolicName: requirebundle
        // Require-Bundle: simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundle");
        XResource resourceA = installModule(assemblyA);

        try {
            resolver.resolve(resourceA);
            fail("XResolverException expected");
        } catch (XResolverException ex) {
            // expected;
        }
    }

    @Test
    public void testRequireBundleOptional() throws Exception {
        // Bundle-SymbolicName: requirebundleoptional
        // Require-Bundle: simpleexport;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleoptional");
        XResource resourceA = installModule(assemblyA);

        resolver.resolve(resourceA);
        assertTrue(resourceA.isResolved());
    }

    @Test
    public void testRequireBundleVersion() throws Exception {
        // Bundle-SymbolicName: requirebundleversion
        // Require-Bundle: simpleexport;bundle-version="[0.0.0,1.0.0]"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleversion");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresA = resourceA.getWires();
        assertEquals(1, wiresA.size());
        assertEquals(resourceB, wiresA.get(0).getExporter());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testRequireBundleVersionFails() throws Exception {
        // Bundle-SymbolicName: versionrequirebundlefails
        // Require-Bundle: simpleexport;bundle-version="[1.0.0,2.0.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleversionfails");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertFalse(resolver.resolveAll(null));

        assertEquals(1, resolved.size());
        assertFalse(resourceA.isResolved());
        assertTrue(resourceB.isResolved());
    }

    @Test
    public void testPreferredExporterResolved() throws Exception {
        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleexport");

        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexportother");

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");

        XResource resourceA = installModule(assemblyA);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(1, resolved.size());
        assertTrue(resourceA.isResolved());

        XResource resourceB = installModule(assemblyB);

        XResource resourceC = installModule(assemblyC);

        // Resolve all modules
        resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(2, resolved.size());
        assertTrue(resourceB.isResolved());
        assertTrue(resourceC.isResolved());

        List<XWire> wiresC = resourceC.getWires();
        assertEquals(1, wiresC.size());

        System.out.println("FIXME [JBOSGI-368] testPreferredExporterResolved fails occasionally");
        // assertEquals(resourceA, wiresC.get(0).getExporter());
    }

    @Test
    public void testPreferredExporterResolvedReverse() throws Exception {
        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleexport");

        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexportother");

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");

        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(1, resolved.size());
        assertTrue(resourceB.isResolved());

        XResource resourceA = installModule(assemblyA);

        XResource resourceC = installModule(assemblyC);

        // Resolve all modules
        resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceC.isResolved());

        List<XWire> wiresC = resourceC.getWires();
        assertEquals(1, wiresC.size());

        System.out.println("FIXME [JBOSGI-368] testPreferredExporterResolvedReverse fails occasionally");
        // assertEquals(resourceB, wiresC.get(0).getExporter());
    }

    @Test
    public void testPreferredExporterHigherVersion() throws Exception {
        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportversion100");

        // Bundle-SymbolicName: packageexportversion200
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion200");

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");

        XResource resourceA = installModule(assemblyA);
        XResource resourceB = installModule(assemblyB);
        XResource resourceC = installModule(assemblyC);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(3, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());
        assertTrue(resourceC.isResolved());

        List<XWire> wiresC = resourceC.getWires();
        assertEquals(1, wiresC.size());
        assertEquals(resourceB, wiresC.get(0).getExporter());
    }

    @Test
    public void testPreferredExporterHigherVersionReverse() throws Exception {
        // Bundle-SymbolicName: packageexportversion200
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportversion200");

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion100");

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");

        XResource resourceA = installModule(assemblyA);
        XResource resourceB = installModule(assemblyB);
        XResource resourceC = installModule(assemblyC);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(3, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());
        assertTrue(resourceC.isResolved());

        List<XWire> wiresC = resourceC.getWires();
        assertEquals(1, wiresC.size());
        assertEquals(resourceA, wiresC.get(0).getExporter());
    }

    @Test
    public void testPreferredExporterLowerId() throws Exception {
        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleexport");

        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexportother");

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");

        XResource resourceA = installModule(assemblyA);
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        XResource resourceC = installModule(assemblyC);

        // Resolve all modules
        resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(1, resolved.size());
        assertTrue(resourceC.isResolved());

        List<XWire> wiresC = resourceC.getWires();
        assertEquals(1, wiresC.size());

        System.out.println("FIXME [JBOSGI-368] testPreferredExporterLowerId fails occasionally");
        // assertEquals(resourceA, wiresC.get(0).getExporter());
    }

    @Test
    public void testPreferredExporterLowerIdReverse() throws Exception {
        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceA", "/resolver/simpleexportother");

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceB", "/resolver/simpleexport");

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");

        XResource resourceB = installModule(assemblyB);
        XResource resourceA = installModule(assemblyA);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(2, resolved.size());
        assertTrue(resourceB.isResolved());
        assertTrue(resourceA.isResolved());

        XResource resourceC = installModule(assemblyC);

        // Resolve all modules
        resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(1, resolved.size());
        assertTrue(resourceC.isResolved());

        List<XWire> wiresC = resourceC.getWires();
        assertEquals(1, wiresC.size());

        System.out.println("FIXME [JBOSGI-368] testPreferredExporterLowerIdReverse fails occasionally");
        // assertEquals(resourceB, wiresC.get(0).getExporter());
    }

    @Test
    public void testPackageAttribute() throws Exception {
        // Bundle-SymbolicName: packageexportattribute
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattribute");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleimport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(1, wiresB.size());
        assertEquals(resourceA, wiresB.get(0).getExporter());

        // Bundle-SymbolicName: packageimportattribute
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/packageimportattribute");
        XResource resourceC = installModule(assemblyC);

        resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(1, resolved.size());
        assertTrue(resourceC.isResolved());

        List<XWire> wiresC = resourceC.getWires();
        assertEquals(1, wiresC.size());
        assertEquals(resourceA, wiresC.get(0).getExporter());
    }

    @Test
    public void testPackageAttributeFails() throws Exception {
        // Bundle-SymbolicName: packageexportattribute
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattribute");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: packageimportattributefails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=y
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageimportattributefails");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertFalse(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(1, resolved.size());
        assertTrue(resourceA.isResolved());
        assertFalse(resourceB.isResolved());
    }

    @Test
    public void testPackageAttributeMandatory() throws Exception {
        // Bundle-SymbolicName: packageexportattributemandatory
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattributemandatory");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: packageimportattribute
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageimportattribute");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(2, resolved.size());
        assertTrue(resourceA.isResolved());
        assertTrue(resourceB.isResolved());

        List<XWire> wiresB = resourceB.getWires();
        assertEquals(1, wiresB.size());
        assertEquals(resourceA, wiresB.get(0).getExporter());
    }

    @Test
    public void testPackageAttributeMandatoryFails() throws Exception {
        // Bundle-SymbolicName: packageexportattributemandatory
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattributemandatory");
        XResource resourceA = installModule(assemblyA);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleimport");
        XResource resourceB = installModule(assemblyB);

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertFalse(resolver.resolveAll(null));

        // Verify bundle states
        assertEquals(1, resolved.size());
        assertTrue(resourceA.isResolved());
        assertFalse(resourceB.isResolved());
    }

    @Test
    public void testFragmentAddsExport() throws Exception {
        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyH = assembleArchive("host", "/resolver/bundlefragmenthost");
        XResource resourceH = installModule(assemblyH);
        assertFalse(resourceH.isFragment());

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyF = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        XResource resourceF = installModule(assemblyF);
        assertTrue(resourceF.isFragment());

        // Bundle-SymbolicName: bundleimportfragmentpkg
        // Import-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("bundle", "/resolver/bundleimportfragmentpkg");
        XResource resourceB = installModule(assemblyB);
        assertFalse(resourceB.isFragment());

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertTrue(resourceH.isResolved());
        assertTrue(resourceF.isResolved());
        assertTrue(resourceB.isResolved());

        assertEquals(0, resourceH.getWires().size());
        assertEquals(1, resourceF.getWires().size());
        XWire fragWire = resourceF.getWires().get(0);
        assertTrue(fragWire.getRequirement() instanceof XFragmentHostRequirement);
        assertEquals(resourceH, fragWire.getExporter());

        assertEquals(1, resourceB.getWires().size());
        XWire bundleWire = resourceB.getWires().get(0);
        assertTrue(bundleWire.getRequirement() instanceof XPackageRequirement);
        assertEquals(resourceH, bundleWire.getExporter());
    }

    @Test
    public void testHostDependsOnFragmentPackage() throws Exception {
        // Bundle-SymbolicName: bundledependsfragment
        // Export-Package: org.jboss.osgi.test.host.export
        // Import-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyH = assembleArchive("host", "/resolver/bundledependsfragment");
        XResource resourceH = installModule(assemblyH);
        assertFalse(resourceH.isFragment());

        // Bundle-SymbolicName: fragmentsdependshostexport
        // Export-Package: org.jboss.osgi.test.fragment.export
        // Import-Package: org.jboss.osgi.test.host.export
        // Fragment-Host: bundledependsfragment
        Archive<?> assemblyF = assembleArchive("fragment", "/resolver/fragmentdependshostexport");
        XResource resourceF = installModule(assemblyF);
        assertTrue(resourceF.isFragment());

        // Resolve all modules
        List<XResource> resolved = new ArrayList<XResource>();
        resolver.setCallbackHandler(new ResolverCallback(resolved));
        assertTrue(resolver.resolveAll(null));

        assertTrue(resourceH.isResolved());
        assertTrue(resourceF.isResolved());

        assertEquals(1, resourceH.getWires().size());
        XWire hostWire = resourceH.getWires().get(0);
        assertTrue(hostWire.getRequirement() instanceof XPackageRequirement);
        assertEquals(resourceF, hostWire.getCapability().getModuleRevision());
        assertEquals("Fragment wire is exported by module itself", resourceH, hostWire.getExporter());
        assertEquals(2, resourceF.getWires().size());
        List<XWire> wires = new ArrayList<XWire>(resourceF.getWires());
        for (Iterator<XWire> it = wires.iterator(); it.hasNext();) {
            XWire wire = it.next();
            if (wire.getRequirement() instanceof XFragmentHostRequirement) {
                assertEquals(resourceH, wire.getExporter());
                it.remove();
            }
        }

        assertEquals(1, wires.size());
        XWire fragWire = wires.get(0);
        assertTrue(fragWire.getRequirement() instanceof XPackageRequirement);
        assertEquals(resourceH, fragWire.getCapability().getModuleRevision());
        assertEquals(resourceH, fragWire.getExporter());
    }
}