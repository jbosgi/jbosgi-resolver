/*
 * #%L
 * JBossOSGi Resolver Felix
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


package org.jboss.test.osgi.resolver;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.osgi.framework.namespace.AbstractWiringNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.ResolutionException;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @since 31-May-2010
 */
public class PackageImportResolverTest extends AbstractResolverTest {

    @Test
    public void testSimpleImport() throws Exception {
        
        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        assertNotNull("Wire map not null", map);
        assertEquals(2, map.size());

        List<Wire> wiresA = map.get(resourceA);
        assertEquals(1, wiresA.size());
        Wire wireA = wiresA.get(0);
        assertEquals(resourceA, wireA.getRequirer());
        assertEquals(resourceB, wireA.getProvider());
        Requirement reqA = wireA.getRequirement();
        String reqApack = (String) reqA.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
        assertEquals("org.jboss.test.osgi.classloader.support.a", reqApack);
        Capability capA = wireA.getCapability();
        String capApack = (String) capA.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
        assertEquals("org.jboss.test.osgi.classloader.support.a", capApack);

        List<Wire> wiresB = map.get(resourceB);
        assertEquals(0, wiresB.size());
    }

    @Test
    public void testSimpleImportPackageFails() throws Exception {

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/simpleimport");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: somepackage
        // Export-Package: org.jboss.test.osgi.somepackage
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/someexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        try {
            List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceB);

        List<XResource> mandatory = Collections.singletonList(resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringB = getWiring(resourceB);
        assertNotNull("Wiring not null", wiringB);
        assertTrue(wiringB.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringB.getProvidedResourceWires(null).isEmpty());

        installResources(resourceA);

        mandatory = Collections.singletonList(resourceA);
        map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertNotNull("Wiring not null", wiringA);
        assertTrue(wiringA.getProvidedResourceWires(null).isEmpty());
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        wiringB = getWiring(resourceB);
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
        XResource resourceA = createResource(assemblyA);

        installResources(resourceA);

        List<XResource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        assertEquals(1, map.size());

        List<Wire> wiresA = map.get(resourceA);
        assertEquals(0, wiresA.size());
    }

    @Test
    public void testVersionImportPackage() throws Exception {

        // Bundle-SymbolicName: packageimportversion
        // Import-Package: org.jboss.test.osgi.classloader.support.a;version="[0.0.0,1.0.0]"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportversion");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion100");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        assertEquals(2, map.size());
    }

    @Test
    public void testVersionImportPackageFails() throws Exception {

        // Bundle-SymbolicName: packageimportversionfails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;version="[3.0,4.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportversionfails");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageexportversion100
        // Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageexportversion100");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        try {
            List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
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
        XResource resourceA = createResource(assemblyA);

        installResources(resourceA);

        List<XResource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        assertEquals(1, map.size());
    }

    @Test
    public void testOptionalImportPackageWired() throws Exception {

        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> optional = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(null, optional));
        assertNotNull("Wire map not null", map);
        assertEquals(2, map.size());

        List<Wire> wiresA = map.get(resourceA);
        assertEquals(1, wiresA.size());
        Wire wireA = wiresA.get(0);
        assertEquals(resourceA, wireA.getRequirer());
        assertEquals(resourceB, wireA.getProvider());
        Map<String, String> reqdirs = wireA.getRequirement().getDirectives();
        String resdir = reqdirs.get(AbstractWiringNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
        assertEquals(AbstractWiringNamespace.RESOLUTION_OPTIONAL, resdir);
    }

    @Test
    public void testOptionalImportPackageNotWired() throws Exception {

        // Bundle-SymbolicName: packageimportoptional
        // Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageimportoptional");
        XResource resourceA = createResource(assemblyA);

        installResources(resourceA);

        List<XResource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        assertNotNull("Wire map not null", map);
        assertEquals(1, map.size());
        
        applyResolverResults(map);
        Wiring wiringA = getWiring(resourceA);
        assertNotNull("Wiring not null", wiringA);
        assertTrue(wiringA.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringA.getProvidedResourceWires(null).isEmpty());

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceB);

        mandatory = Arrays.asList(resourceB);
        map = resolver.resolve(getResolveContext(mandatory, null));
        assertNotNull("Wire map not null", map);
        assertEquals(1, map.size());
        applyResolverResults(map);

        // Verify that there is no wire to resourceB
        Wiring wiringB = getWiring(resourceB);
        assertNotNull("Wiring not null", wiringB);
        assertTrue(wiringB.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringB.getProvidedResourceWires(null).isEmpty());
    }

    @Test
    public void testBundleNameImportPackage() throws Exception {
        
        // Bundle-SymbolicName: bundlenameimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundlenameimport");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = getWiring(resourceB);
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: sigleton;singleton:=true
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/singleton");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        try {
            List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = getWiring(resourceB);
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        try {
            List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }

    @Test
    public void testPackageAttribute() throws Exception {

        // Bundle-SymbolicName: packageexportattribute
        // Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/packageexportattribute");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleimport");
        XResource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: packageimportattribute
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/packageimportattribute");
        XResource resourceC = createResource(assemblyC);

        installResources(resourceA, resourceB, resourceC);
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(2, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(null).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());

        Wiring wiringC = getWiring(resourceC);
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageimportattributefails
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=y
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageimportattributefails");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);
        try {
            List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: packageimportattribute
        // Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/packageimportattribute");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = getWiring(resourceB);
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleimport
        // Import-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleimport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);
        try {
            List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
            Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
            fail("ResolutionException expected, was: " + map);
        } catch (ResolutionException ex) {
            // expected;
        }
    }
}
