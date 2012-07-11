/*
 * #%L
 * JBossOSGi Resolver API
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
import org.jboss.osgi.resolver.XRequirement;
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
public class XEnvironmentTestCase extends AbstractTestBase {

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
        XRequirement req = (XRequirement) reqs.get(0);

        List<Capability> providers = env.findProviders(req);
        assertEquals(2, providers.size());
        assertSame(resourceB, providers.get(0).getResource());
        assertSame(resourceC, providers.get(1).getResource());
    }
}
