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

import org.jboss.osgi.resolver.v2.XEnvironment;
import org.jboss.osgi.resolver.v2.XPackageCapability;
import org.jboss.osgi.resolver.v2.XPackageRequirement;
import org.jboss.osgi.resolver.v2.XRequirement;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;
import org.osgi.service.resolver.Environment;
import org.osgi.service.resolver.ResolutionException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.osgi.framework.resource.ResourceConstants.WIRING_HOST_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

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
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        assertNotNull("Wire map not null", map);
        assertEquals(2, map.size());

        List<Wire> wiresA = map.get(resourceA);
        assertEquals(1, wiresA.size());
        Wire wireA = wiresA.get(0);
        assertEquals(resourceA, wireA.getRequirer());
        assertEquals(resourceB, wireA.getProvider());
        XPackageRequirement reqA = (XPackageRequirement) wireA.getRequirement();
        assertEquals("org.jboss.test.osgi.classloader.support.a", reqA.getPackageName());
        assertNull("Version range is null", reqA.getVersionRange());
        XPackageCapability capA = (XPackageCapability) wireA.getCapability();
        assertEquals("org.jboss.test.osgi.classloader.support.a", capA.getPackageName());
        assertEquals(Version.emptyVersion, capA.getVersion());

        List<Wire> wiresB = map.get(resourceB);
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testSimpleImportPackageFails() throws Exception {

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: somepackage
        // Export-Package: org.jboss.test.osgi.somepackage
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/someexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testExplicitBundleResolve() throws Exception {
        
        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceB);

        List<Resource> mandatory = Collections.singletonList(resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringB = env.getWiring(resourceB);
        assertNotNull("Wiring not null", wiringB);
        assertTrue(wiringB.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringB.getProvidedResourceWires(null).isEmpty());

        installResources(resourceA);

        mandatory = Collections.singletonList(resourceA);
        map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertNotNull("Wiring not null", wiringA);
        assertTrue(wiringA.getProvidedResourceWires(null).isEmpty());
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceA, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());
    }

    @Test
    public void testSelfImportPackage() throws Exception {

        // Bundle-SymbolicName: selfimport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/selfimport");
        Resource resourceA = createResource(assemblyA);

        Environment env = installResources(resourceA);

        List<Resource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        assertEquals(1, map.size());

        List<Wire> wiresA = map.get(resourceA);
        assertEquals(0, wiresA.size());
    }

    @Test
    public void testVersionImportPackage() throws Exception {

        // Bundle-SymbolicName: packageimportversion
        // Import-Package: org.jboss.test.osgi.classloader.support.a;version="[0.0.0,1.0.0]"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportversion");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion100");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        assertEquals(2, map.size());
    }

    @Test
    public void testVersionImportPackageFails() throws Exception {

        // Bundle-SymbolicName: packageimportversionfails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;version="[3.0,4.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportversionfails");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion100");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testOptionalImportPackage() throws Exception {

        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        Resource resourceA = createResource(assemblyA);

        Environment env = installResources(resourceA);

        List<Resource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        assertEquals(1, map.size());
    }

    @Test
    public void testOptionalImportPackageWired() throws Exception {

        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        List<Resource> optional = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, null, optional);
        assertNotNull("Wire map not null", map);
        assertEquals(2, map.size());

        List<Wire> wiresA = map.get(resourceA);
        assertEquals(1, wiresA.size());
        Wire wireA = wiresA.get(0);
        assertEquals(resourceA, wireA.getRequirer());
        assertEquals(resourceB, wireA.getProvider());
        assertTrue(((XRequirement)wireA.getRequirement()).isOptional());
    }

    @Test
    public void testOptionalImportPackageNotWired() throws Exception {

        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        Resource resourceA = createResource(assemblyA);

        XEnvironment env = installResources(resourceA);

        List<Resource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        assertNotNull("Wire map not null", map);
        assertEquals(1, map.size());
        
        env.applyResolverResults(map);
        Wiring wiringA = env.getWiring(resourceA);
        assertNotNull("Wiring not null", wiringA);
        assertTrue(wiringA.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringA.getProvidedResourceWires(null).isEmpty());

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        installResources(resourceB);

        mandatory = Arrays.asList(resourceB);
        map = resolver.resolve(env, mandatory, null);
        assertNotNull("Wire map not null", map);
        assertEquals(1, map.size());
        env.applyResolverResults(map);

        // Verify that there is no wire to resourceB
        Wiring wiringB = env.getWiring(resourceB);
        assertNotNull("Wiring not null", wiringB);
        assertTrue(wiringB.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringB.getProvidedResourceWires(null).isEmpty());
    }

    @Test
    public void testBundleNameImportPackage() throws Exception {
        
        // Bundle-SymbolicName: bundlenameimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundlenameimport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceA, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());
    }

    @Test
    public void testBundleNameImportPackageFails() throws Exception {

        // Bundle-SymbolicName: bundlenameimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundlenameimport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: sigleton;singleton:=true
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/singleton");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testBundleVersionImportPackage() throws Exception {

        // Bundle-SymbolicName: bundleversionimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[0.0.0,1.0.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundleversionimport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceA, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());
    }

    @Test
    public void testBundleVersionImportPackageFails() throws Exception {
        // Bundle-SymbolicName: bundleversionimportfails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[1.0.0,2.0.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundleversionimportfails");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testRequireBundle() throws Exception {

        // Bundle-SymbolicName: requirebundle
        // Require-Bundle: simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundle");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceA, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());
    }

    @Test
    public void testRequireBundleFails() throws Exception {

        // Bundle-SymbolicName: requirebundle
        // Require-Bundle: simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundle");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: somepackage
        // Export-Package: org.jboss.test.osgi.somepackage
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/someexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testRequireBundleOptional() throws Exception {

        // Bundle-SymbolicName: requirebundleoptional
        // Require-Bundle: simpleexport;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleoptional");
        Resource resourceA = createResource(assemblyA);

        XEnvironment env = installResources(resourceA);

        List<Resource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertTrue(wiringA.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringA.getProvidedResourceWires(null).isEmpty());
    }

    @Test
    public void testRequireBundleVersion() throws Exception {

        // Bundle-SymbolicName: requirebundleversion
        // Require-Bundle: simpleexport;bundle-version="[0.0.0,1.0.0]"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleversion");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceA, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());
    }

    @Test
    public void testRequireBundleVersionFails() throws Exception {

        // Bundle-SymbolicName: versionrequirebundlefails
        // Require-Bundle: simpleexport;bundle-version="[1.0.0,2.0.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleversionfails");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testPreferredExporterResolved() throws Exception {

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleexportother");
        Resource resourceC = createResource(assemblyC);

        // install C before B
        XEnvironment env = installResources(resourceA, resourceC, resourceB);

        // resolve B
        List<Resource> mandatory = Collections.singletonList(resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        // resolve A and C
        mandatory = Arrays.asList(resourceA, resourceC);
        map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceA, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());
    }

    @Test
    public void testPreferredExporterResolvedReverse() throws Exception {
        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleexportother");
        Resource resourceC = createResource(assemblyC);

        // install B before C
        XEnvironment env = installResources(resourceA, resourceB, resourceC);

        // resolve B
        List<Resource> mandatory = Collections.singletonList(resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        // resolve A and C
        mandatory = Arrays.asList(resourceA, resourceC);
        map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceA, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());
    }

    @Test
    public void testPreferredExporterHigherVersion() throws Exception {

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportversion100");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageexportversion200
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion200");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");
        Resource resourceC = createResource(assemblyC);

        // install A before B
        XEnvironment env = installResources(resourceA, resourceB, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceC, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());

        Wiring wiringC = env.getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(null).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceB, wireC.getProvider());
    }

    @Test
    public void testPreferredExporterHigherVersionReverse() throws Exception {
        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportversion100");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageexportversion200
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion200");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");
        Resource resourceC = createResource(assemblyC);

        // install B before A
        XEnvironment env = installResources(resourceB, resourceA, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceC, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());

        Wiring wiringC = env.getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(null).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceB, wireC.getProvider());
    }

    @Test
    public void testPreferredExporterLowerId() throws Exception {
        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleexport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexportother");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");
        Resource resourceC = createResource(assemblyC);

        // install A before B
        XEnvironment env = installResources(resourceA, resourceB, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());

        Wiring wiringC = env.getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(null).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceA, wireC.getProvider());
    }

    @Test
    public void testPreferredExporterLowerIdReverse() throws Exception {
        // Bundle-SymbolicName: simpleexportother
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceA", "/resolver/simpleexportother");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/simpleimport");
        Resource resourceC = createResource(assemblyC);

        // install B before A
        XEnvironment env = installResources(resourceB, resourceA, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());

        Wiring wiringC = env.getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(null).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceB, wireC.getProvider());
    }

    @Test
    public void testPackageAttribute() throws Exception {

        // Bundle-SymbolicName: packageexportattribute
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattribute");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleimport");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: packageimportattribute
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/packageimportattribute");
        Resource resourceC = createResource(assemblyC);

        XEnvironment env = installResources(resourceA, resourceB, resourceC);
        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(2, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(null).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());

        Wiring wiringC = env.getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(null).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceA, wireC.getProvider());
    }

    @Test
    public void testPackageAttributeFails() throws Exception {

        // Bundle-SymbolicName: packageexportattribute
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattribute");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageimportattributefails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=y
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageimportattributefails");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);
        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testPackageAttributeMandatory() throws Exception {

        // Bundle-SymbolicName: packageexportattributemandatory
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattributemandatory");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageimportattribute
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageimportattribute");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);
        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(null).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());
    }

    @Test
    public void testPackageAttributeMandatoryFails() throws Exception {

        // Bundle-SymbolicName: packageexportattributemandatory
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattributemandatory");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleimport");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);
        try {
            List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    @Ignore
    public void testFragmentAddsExport() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: bundleimportfragmentpkg
        // Import-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyC = assembleArchive("bundle", "/resolver/bundleimportfragmentpkg");
        Resource resourceC = createResource(assemblyC);

        XEnvironment env = installResources(resourceA, resourceB, resourceC);
        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(2, wiringA.getProvidedResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).size());
        Wire pwireA = wiringA.getProvidedResourceWires(WIRING_PACKAGE_NAMESPACE).get(0);
        assertSame(resourceA, pwireA.getProvider());
        assertSame(resourceC, pwireA.getRequirer());
        Wire hwireA = wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());

        Wiring wiringC = env.getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(1, wiringC.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceA, wireC.getProvider());
    }

    @Test
    @Ignore
    public void testFragmentDependsOnHostExport() throws Exception {

        // Bundle-SymbolicName: bundledependsfragment
        // Export-Package: org.jboss.osgi.test.host.export
        // Import-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundledependsfragment");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentsdependshostexport
        // Export-Package: org.jboss.osgi.test.fragment.export
        // Import-Package: org.jboss.osgi.test.host.export
        // Fragment-Host: bundledependsfragment
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentdependshostexport");
        Resource resourceB = createResource(assemblyB);

        XEnvironment env = installResources(resourceA, resourceB);
        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        env.applyResolverResults(map);

        Wiring wiringA = env.getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).size());
        Wire hwireA = wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = env.getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire hwireB = wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceB, hwireB.getRequirer());
        assertSame(resourceA, hwireB.getProvider());
    }
}