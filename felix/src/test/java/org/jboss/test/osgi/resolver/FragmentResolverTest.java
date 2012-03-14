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

package org.jboss.test.osgi.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.osgi.framework.resource.ResourceConstants.WIRING_HOST_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.shrinkwrap.api.Archive;
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
 * @since 31-May-2010
 */
public class FragmentResolverTest extends AbstractResolverTest {

    @Test
    public void testFragmentAttach() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);
        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).size());
        Wire hwireA = wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = getWiring(env, resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());
    }

    @Test
    public void testResolveFragmentOnly() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);
        List<Resource> mandatory = Arrays.asList(resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).size());
        Wire hwireA = wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = getWiring(env, resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());
    }

    @Test
    public void testResolveHostOnly() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        Resource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        Resource resourceB = createResource(assemblyB);

        Environment env = installResources(resourceA, resourceB);
        List<Resource> mandatory = Arrays.asList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(0, wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).size());

        Wiring wiringB = getWiring(env, resourceB);
        assertNull(wiringB);
    }

    @Test
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

        Environment env = installResources(resourceA, resourceB, resourceC);
        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
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

        Wiring wiringB = getWiring(env, resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());

        Wiring wiringC = getWiring(env, resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(1, wiringC.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceA, wireC.getProvider());
    }

    @Test
    public void testFragmentCannotAddExport() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        Resource resourceA = createResource(assemblyA);

        Environment env = installResources(resourceA);
        List<Resource> mandatory = Arrays.asList(resourceA);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);
        
        // Bundle-SymbolicName: bundleimportfragmentpkg
        // Import-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("bundle", "/resolver/bundleimportfragmentpkg");
        Resource resourceB = createResource(assemblyB);

        installResources(resourceB);
        try {
            mandatory = Arrays.asList(resourceB);
            resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected");
        } catch (ResolutionException ex) {
            // expected
        }

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyC = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        Resource resourceC = createResource(assemblyC);
        
        installResources(resourceC);
        try {
            mandatory = Arrays.asList(resourceB, resourceC);
            resolver.resolve(env, mandatory, null);
            fail("ResolutionException expected");
        } catch (ResolutionException ex) {
            // expected
        }
    }

    @Test
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

        Environment env = installResources(resourceA, resourceB);
        List<Resource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(env, mandatory, null);
        applyResolverResults(map);

        Wiring wiringA = getWiring(env, resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).size());
        Wire hwireA = wiringA.getProvidedResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = getWiring(env, resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getRequiredResourceWires(WIRING_PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire hwireB = wiringB.getRequiredResourceWires(WIRING_HOST_NAMESPACE).get(0);
        assertSame(resourceB, hwireB.getRequirer());
        assertSame(resourceA, hwireB.getProvider());
    }

}