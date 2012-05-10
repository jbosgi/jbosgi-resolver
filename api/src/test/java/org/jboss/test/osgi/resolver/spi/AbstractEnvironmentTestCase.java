/*
 * #%L
 * JBossOSGi Resolver API
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResource;
import org.junit.Test;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * Unit tests for the {@link org.osgi.service.resolver.Environment} class
 *
 * @author Thomas.Diesler@jboss.com
 */
public class AbstractEnvironmentTestCase extends AbstractTestBase {

    @Test
    public void testFindProviders() throws Exception {
        
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testA");
        attrs.put(IMPORT_PACKAGE, "org.jboss.foo");
        XResource resourceA = createResource(attrs);

        attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testB");
        attrs.put(EXPORT_PACKAGE, "org.jboss.foo");
        XResource resourceB = createResource(attrs);

        attrs = new HashMap<String, String>();
        attrs.put(BUNDLE_SYMBOLICNAME, "testC");
        attrs.put(EXPORT_PACKAGE, "org.jboss.foo");
        XResource resourceC = createResource(attrs);

        XEnvironment env = installResources(resourceA, resourceB, resourceC);

        List<Requirement> reqs = resourceA.getRequirements(PackageNamespace.PACKAGE_NAMESPACE);
        assertEquals(1, reqs.size());
        XPackageRequirement req = (XPackageRequirement) reqs.get(0);

        List<Capability> providers = env.findProviders(req);
        assertEquals(2, providers.size());
        assertSame(resourceB, providers.get(0).getResource());
        assertSame(resourceC, providers.get(1).getResource());
    }
}
