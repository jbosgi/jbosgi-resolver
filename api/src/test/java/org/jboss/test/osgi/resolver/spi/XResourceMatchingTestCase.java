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

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.IdentityNamespace;

/**
 * Unit tests for resource matching
 *
 * @author Thomas.Diesler@jboss.com
 */
public class XResourceMatchingTestCase extends AbstractTestBase {

    @Test
    public void testAttributOnSymbolicNameNoMatch() throws Exception {
        XResourceBuilder<XResource> cbuilder = XResourceBuilderFactory.create();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        XCapability cap = cbuilder.addCapability(BundleNamespace.BUNDLE_NAMESPACE, "org.jboss.test.cases.repository.tb1");
        cbuilder.getResource();

        XResourceBuilder<XResource> rbuilder = XResourceBuilderFactory.create();
        rbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res2");
        Filter filter = FrameworkUtil.createFilter("(&(osgi.wiring.bundle=org.jboss.test.cases.repository.tb1)(foo=bar))");
        XRequirement req = rbuilder.addRequirement("osgi.wiring.bundle", filter);
        rbuilder.getResource();

        Assert.assertFalse("No match", req.matches(cap));
    }

    @Test
    public void testAttributOnSymbolicNameMatch() throws Exception {
        XResourceBuilder<XResource> cbuilder = XResourceBuilderFactory.create();
        cbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res1");
        XCapability cap = cbuilder.addCapability(BundleNamespace.BUNDLE_NAMESPACE, "org.jboss.test.cases.repository.tb1");
        cap.getAttributes().put("foo", "bar");
        cbuilder.getResource();

        XResourceBuilder<XResource> rbuilder = XResourceBuilderFactory.create();
        rbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "res2");
        Filter filter = FrameworkUtil.createFilter("(&(osgi.wiring.bundle=org.jboss.test.cases.repository.tb1)(foo=bar))");
        XRequirement req = rbuilder.addRequirement("osgi.wiring.bundle", filter);
        rbuilder.getResource();

        Assert.assertTrue("Match", req.matches(cap));
    }
}
