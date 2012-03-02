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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;
import org.osgi.service.resolver.Environment;
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
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: simpleexport
        // Export-Package: org.jboss.test.osgi.classloader.support.a
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/simpleexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = getWiring(env, resourceB);
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

        Environment env = installResources(resourceA);

        List<Resource> mandatory = Collections.singletonList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
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

        Environment env = installResources(resourceA, resourceB);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
        assertEquals(1, wiringA.getRequiredResourceWires(null).size());
        Wire wireA = wiringA.getRequiredResourceWires(null).get(0);
        assertSame(resourceA, wireA.getRequirer());
        assertSame(resourceB, wireA.getProvider());

        Wiring wiringB = getWiring(env, resourceB);
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
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: bundleversion
        // Bundle-Version: 1.1.0
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/bundleversion110");
        Resource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: requirebundlehighestversion
        // Require-Bundle: bundleversion
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/requirebundlehighestversion");
        Resource resourceC = createResource(assemblyC);

        Environment env = installResources(resourceA, resourceB, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = getWiring(env, resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        
        Wiring wiringC = getWiring(env, resourceC);
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
    @Ignore("[FELIX-3370] Wires E to B instead of C") 
    // https://issues.apache.org/jira/browse/FELIX-3370
    public void testImportBySymbolicName() throws Exception {
        // Bundle-SymbolicName: requirebundleB
        // Export-Package: resources
        Archive<?> assemblyB = assembleArchive("resourceB", "/resolver/requirebundleB");
        Resource resourceB = createResource(assemblyB);
        
        // Bundle-SymbolicName: requirebundleC
        // Export-Package: resources
        Archive<?> assemblyC = assembleArchive("resourceC", "/resolver/requirebundleC");
        Resource resourceC = createResource(assemblyC);
        
        Environment env = installResources(resourceB, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);
        
        Wiring wiringB = getWiring(env, resourceB);
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        
        Wiring wiringC = getWiring(env, resourceC);
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        assertEquals(0, wiringC.getRequiredResourceWires(null).size());
        
        // Bundle-SymbolicName: requirebundleD
        // Import-Package: resources;bundle-symbolic-name=requirebundleC
        Archive<?> assemblyD = assembleArchive("resourceD", "/resolver/requirebundleD");
        Resource resourceD = createResource(assemblyD);
        
        installResources(resourceD);
        
        mandatory = Arrays.asList(resourceD);
        map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);
        
        Wiring wiringD = getWiring(env, resourceD);
        assertEquals(0, wiringD.getProvidedResourceWires(null).size());
        assertEquals(1, wiringD.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        Wire wire = wiringD.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).get(0);
        assertEquals(resourceC, wire.getProvider());
        
        // Bundle-SymbolicName: requirebundleD
        // Require-Bundle: requirebundleD
        // Import-Package: resources
        Archive<?> assemblyE = assembleArchive("resourceE", "/resolver/requirebundleE");
        Resource resourceE = createResource(assemblyE);
        
        installResources(resourceE);
        
        mandatory = Arrays.asList(resourceE);
        map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringE = getWiring(env, resourceE);
        assertEquals(0, wiringE.getProvidedResourceWires(null).size());
        assertEquals(1, wiringE.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        wire = wiringE.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).get(0);
        assertEquals(resourceC, wire.getProvider());
    }
}