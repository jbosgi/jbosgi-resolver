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
import org.osgi.framework.namespace.PackageNamespace;
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
public class RequireBundleResolverTest extends AbstractResolverTest {

    @Test
    public void testRequireBundle() throws Exception {

        // Bundle-SymbolicName: requirebundle
        // Require-Bundle: simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundle");
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
    public void testRequireBundleFails() throws Exception {

        // Bundle-SymbolicName: requirebundle
        // Require-Bundle: simpleexport
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundle");
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
    public void testRequireBundleOptional() throws Exception {

        // Bundle-SymbolicName: requirebundleoptional
        // Require-Bundle: simpleexport;resolution:=optional
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleoptional");
        XResource resourceA = createResource(assemblyA);

        installResources(resourceA);

        List<XResource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertTrue(wiringA.getRequiredResourceWires(null).isEmpty());
        assertTrue(wiringA.getProvidedResourceWires(null).isEmpty());
    }

    @Test
    public void testRequireBundleVersion() throws Exception {

        // Bundle-SymbolicName: requirebundleversion
        // Require-Bundle: simpleexport;bundle-version="[0.0.0,1.0.0]"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleversion");
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
    public void testRequireBundleHighestVersion() throws Exception {

        // Bundle-SymbolicName: bundleversion
        // Bundle-Version: 1.0.0
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/bundleversion100");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: bundleversion
        // Bundle-Version: 1.1.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/bundleversion110");
        XResource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: requirebundlehighestversion
        // Require-Bundle: bundleversion
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/requirebundlehighestversion");
        XResource resourceC = createResource(assemblyC);

        installResources(resourceA, resourceB, resourceC);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        
        Wiring wiringC = getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(null).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceB, wireC.getProvider());
    }

    @Test
    public void testRequireBundleVersionFails() throws Exception {

        // Bundle-SymbolicName: versionrequirebundlefails
        // Require-Bundle: simpleexport;bundle-version="[1.0.0,2.0.0)"
        Archive<?> assemblyA = assembleArchive("resourceA", "/resolver/requirebundleversionfails");
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
    public void testImportBySymbolicName() throws Exception {
        // Bundle-SymbolicName: requirebundleB
        // Export-Package: resources
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/requirebundleB");
        XResource resourceB = createResource(assemblyB);
        
        // Bundle-SymbolicName: requirebundleC
        // Export-Package: resources
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/requirebundleC");
        XResource resourceC = createResource(assemblyC);
        
        installResources(resourceB, resourceC);

        List<XResource> mandatory = Arrays.asList(resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        Wiring wiringB = getWiring(resourceB);
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        
        Wiring wiringC = getWiring(resourceC);
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        assertEquals(0, wiringC.getRequiredResourceWires(null).size());
        
        // Bundle-SymbolicName: requirebundleD
        // Export-Package: org.jboss.osgi.test.classloading.export;uses:=resources
        // Import-Package: resources;bundle-symbolic-name=requirebundleC
        Archive<?> assemblyD = assembleArchive("resourceD", "/resolver/requirebundleD");
        XResource resourceD = createResource(assemblyD);
        
        installResources(resourceD);
        
        mandatory = Arrays.asList(resourceD);
        map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        Wiring wiringD = getWiring(resourceD);
        assertEquals(0, wiringD.getProvidedResourceWires(null).size());
        assertEquals(1, wiringD.getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        Wire wire = wiringD.getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE).get(0);
        assertEquals(resourceC, wire.getProvider());
        
        // Bundle-SymbolicName: requirebundleE
        // Require-Bundle: requirebundleD
        // Import-Package: resources
        Archive<?> assemblyE = assembleArchive("resourceE", "/resolver/requirebundleE");
        XResource resourceE = createResource(assemblyE);
        
        installResources(resourceE);
        
        mandatory = Arrays.asList(resourceE);
        map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringE = getWiring(resourceE);
        assertEquals(0, wiringE.getProvidedResourceWires(null).size());
        assertEquals(1, wiringE.getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        wire = wiringE.getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE).get(0);
        assertEquals(resourceC, wire.getProvider());
    }
}
