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
package org.jboss.test.osgi.resolver.spi;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.junit.Test;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * Unit tests for the {@link XEnvironment} class
 *
 * @author Thomas.Diesler@jboss.com
 */
public class AbstractEnvironmentTestCase extends AbstractTestBase {

    @Test
    public void testFindProviders() throws Exception {
        
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testA");
        attrs.put(IMPORT_PACKAGE, "org.jboss.foo");
        Resource resourceA = createResource(attrs);

        attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testB");
        attrs.put(EXPORT_PACKAGE, "org.jboss.foo");
        Resource resourceB = createResource(attrs);

        attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testC");
        attrs.put(EXPORT_PACKAGE, "org.jboss.foo");
        Resource resourceC = createResource(attrs);

        List<Resource> resources = Arrays.asList(resourceA, resourceB, resourceC);
        XEnvironment env = installResources(resources);

        List<Requirement> reqs = resourceA.getRequirements(WIRING_PACKAGE_NAMESPACE);
        assertEquals(1, reqs.size());
        XPackageRequirement req = (XPackageRequirement) reqs.get(0);

        Collection<Capability> providers = env.findProviders(req);
        assertEquals(2, providers.size());
        Capability[] caparr = providers.toArray(new Capability[2]);
        assertSame(resourceB, caparr[0].getResource());
        assertSame(resourceC, caparr[1].getResource());
    }

    @Test
    public void testFindProvidersReverse() throws Exception {

        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testA");
        attrs.put(IMPORT_PACKAGE, "org.jboss.foo");
        Resource resourceA = createResource(attrs);

        attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testB");
        attrs.put(EXPORT_PACKAGE, "org.jboss.foo");
        Resource resourceB = createResource(attrs);

        attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testC");
        attrs.put(EXPORT_PACKAGE, "org.jboss.foo");
        Resource resourceC = createResource(attrs);

        List<Resource> resources = Arrays.asList(resourceA, resourceC, resourceB);
        XEnvironment env = installResources(resources);

        List<Requirement> reqs = resourceA.getRequirements(WIRING_PACKAGE_NAMESPACE);
        assertEquals(1, reqs.size());
        XPackageRequirement req = (XPackageRequirement) reqs.get(0);

        Collection<Capability> providers = env.findProviders(req);
        assertEquals(2, providers.size());
        Capability[] caparr = providers.toArray(new Capability[2]);
        assertSame(resourceC, caparr[0].getResource());
        assertSame(resourceB, caparr[1].getResource());
    }
}
