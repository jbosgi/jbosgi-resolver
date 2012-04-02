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

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @since 31-May-2010
 */
public class PreferencesResolverTest extends AbstractResolverTest {

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
        installResources(resourceA, resourceC, resourceB);

        // resolve B
        List<Resource> mandatory = Collections.singletonList(resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        // resolve A and C
        mandatory = Arrays.asList(resourceA, resourceC);
        map = resolver.resolve(getResolveContext(mandatory, null));
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
        installResources(resourceA, resourceB, resourceC);

        // resolve B
        List<Resource> mandatory = Collections.singletonList(resourceB);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        // resolve A and C
        mandatory = Arrays.asList(resourceA, resourceC);
        map = resolver.resolve(getResolveContext(mandatory, null));
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
        installResources(resourceA, resourceB, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceC, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());

        Wiring wiringC = getWiring(resourceC);
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
        installResources(resourceB, resourceA, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(0, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(1, wiringB.getProvidedResourceWires(null).size());
        Wire wireB = wiringB.getProvidedResourceWires(null).get(0);
        assertSame(resourceC, wireB.getRequirer());
        assertSame(resourceB, wireB.getProvider());

        Wiring wiringC = getWiring(resourceC);
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
        installResources(resourceA, resourceB, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);

        Wiring wiringA = getWiring(resourceA);
        assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        assertEquals(1, wiringA.getProvidedResourceWires(null).size());

        Wiring wiringB = getWiring(resourceB);
        assertEquals(0, wiringB.getRequiredResourceWires(null).size());
        assertEquals(0, wiringB.getProvidedResourceWires(null).size());

        Wiring wiringC = getWiring(resourceC);
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
        installResources(resourceB, resourceA, resourceC);

        List<Resource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
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
}