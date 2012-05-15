/*
 * #%L
 * JBossOSGi Resolver Felix
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


package org.jboss.test.osgi.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XResolveContext;
import org.jboss.osgi.resolver.XResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).size());
        Wire hwireA = wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());
    }

    @Test
    public void testResolveFragmentOnly() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> mandatory = Arrays.asList(resourceB);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).size());
        Wire hwireA = wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());
    }

    @Test
    public void testResolveHostOnly() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);

        List<XResource> mandatory = Arrays.asList(resourceA);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        assertEquals(0, wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).size());

        Wiring wiringB = getWiring(resourceB);
        assertNull(wiringB);
    }

    @Test
    public void testFragmentAddsExport() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        XResource resourceB = createResource(assemblyB);

        // Bundle-SymbolicName: bundleimportfragmentpkg
        // Import-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyC = assembleArchive("bundle", "/resolver/bundleimportfragmentpkg");
        XResource resourceC = createResource(assemblyC);

        installResources(resourceA, resourceB, resourceC);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        XResolveContext context = getResolveContext(mandatory, null);
        Map<Resource, List<Wire>> map = resolver.resolve(context);
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(2, wiringA.getProvidedResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).size());
        Wire pwireA = wiringA.getProvidedResourceWires(PackageNamespace.PACKAGE_NAMESPACE).get(0);
        assertSame(resourceA, pwireA.getProvider());
        assertSame(resourceC, pwireA.getRequirer());
        Wire hwireA = wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceB, wireB.getRequirer());
        assertSame(resourceA, wireB.getProvider());

        Wiring wiringC = getWiring(resourceC);
        assertEquals(1, wiringC.getRequiredResourceWires(null).size());
        assertEquals(1, wiringC.getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        assertEquals(0, wiringC.getProvidedResourceWires(null).size());
        Wire wireC = wiringC.getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE).get(0);
        assertSame(resourceC, wireC.getRequirer());
        assertSame(resourceA, wireC.getProvider());
    }

    @Test
    public void testFragmentCannotAddExport() throws Exception {

        // Bundle-SymbolicName: bundlefragmenthost
        Archive<?> assemblyA = assembleArchive("host", "/resolver/bundlefragmenthost");
        XResource resourceA = createResource(assemblyA);

        installResources(resourceA);
        
        List<XResource> mandatory = Arrays.asList(resourceA);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        // Bundle-SymbolicName: bundleimportfragmentpkg
        // Import-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyB = assembleArchive("bundle", "/resolver/bundleimportfragmentpkg");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceB);
        try {
            mandatory = Arrays.asList(resourceB);
            resolver.resolve(getResolveContext(mandatory, null));
            fail("ResolutionException expected");
        } catch (ResolutionException ex) {
            // expected
        }

        // Bundle-SymbolicName: fragmentaddsexport
        // Fragment-Host: bundlefragmenthost
        // Export-Package: org.jboss.osgi.test.fragment.export
        Archive<?> assemblyC = assembleArchive("fragment", "/resolver/fragmentaddsexport");
        XResource resourceC = createResource(assemblyC);

        installResources(resourceC);
        try {
            mandatory = Arrays.asList(resourceB, resourceC);
            resolver.resolve(getResolveContext(mandatory, null));
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
        XResource resourceA = createResource(assemblyA);

        // Bundle-SymbolicName: fragmentsdependshostexport
        // Export-Package: org.jboss.osgi.test.fragment.export
        // Import-Package: org.jboss.osgi.test.host.export
        // Fragment-Host: bundledependsfragment
        Archive<?> assemblyB = assembleArchive("fragment", "/resolver/fragmentdependshostexport");
        XResource resourceB = createResource(assemblyB);

        installResources(resourceA, resourceB);
        
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).size());
        Wire hwireA = wiringA.getProvidedResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceA, hwireA.getProvider());
        assertSame(resourceB, hwireA.getRequirer());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(1, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getRequiredResourceWires(PackageNamespace.PACKAGE_NAMESPACE).size());
        assertEquals(1, wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        Wire hwireB = wiringB.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE).get(0);
        assertSame(resourceB, hwireB.getRequirer());
        assertSame(resourceA, hwireB.getProvider());
    }

}