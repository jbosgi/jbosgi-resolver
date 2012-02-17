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

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
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
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

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
}