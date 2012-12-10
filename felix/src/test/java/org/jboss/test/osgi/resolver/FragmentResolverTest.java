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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XResolveContext;
import org.jboss.osgi.resolver.XResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
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

        assertEquals(4, wiringA.getResourceCapabilities(null).size());
        assertEquals(1, wiringA.getResourceCapabilities(IdentityNamespace.IDENTITY_NAMESPACE).size());
        assertEquals(1, wiringA.getResourceCapabilities(HostNamespace.HOST_NAMESPACE).size());
        assertEquals(1, wiringA.getResourceCapabilities(BundleNamespace.BUNDLE_NAMESPACE).size());
        assertEquals(1, wiringA.getResourceCapabilities(PackageNamespace.PACKAGE_NAMESPACE).size());
        XPackageCapability pcap = (XPackageCapability) wiringA.getResourceCapabilities(PackageNamespace.PACKAGE_NAMESPACE).get(0);
        assertEquals("org.jboss.osgi.test.fragment.export", pcap.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE));
        assertSame(resourceB, pcap.getResource());

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
