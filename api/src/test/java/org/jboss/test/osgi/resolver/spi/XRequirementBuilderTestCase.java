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


import org.jboss.modules.ModuleIdentifier;
import org.jboss.osgi.resolver.MavenCoordinates;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XRequirementBuilder;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the {@link XRequirementBuilder} class
 *
 * @author Thomas.Diesler@jboss.com
 */
public class XRequirementBuilderTestCase extends AbstractTestBase {

    @Test
    public void testMavenCoordinates() throws Exception {
        MavenCoordinates mavenId = MavenCoordinates.parse("org.jboss.spec.javax.transaction:jboss-transaction-api_1.1_spec:1.0.1.Final");
        XRequirement req = XRequirementBuilder.create(mavenId).getRequirement();
        Assert.assertNotNull("Filter not null", req.getFilter());
        XResource res = req.getResource();
        XIdentityCapability icap = (XIdentityCapability) res.getCapabilities(XResource.MAVEN_IDENTITY_NAMESPACE).iterator().next();
        Assert.assertEquals("jboss-transaction-api_1.1_spec", icap.getName());

        XIdentityCapability cap = XResourceBuilderFactory.<XResource>create().addIdentityCapability(mavenId);
        Assert.assertTrue("Requirement matches", req.matches(cap));
    }

    @Test
    public void testModuleIdentifier() throws Exception {
        ModuleIdentifier moduleId = ModuleIdentifier.fromString("org.jboss.spec.javax.transaction:1.0.1.Final");
        XRequirement req = XRequirementBuilder.create(moduleId).getRequirement();
        Assert.assertNotNull("Filter not null", req.getFilter());
        XResource res = req.getResource();
        XIdentityCapability icap = (XIdentityCapability) res.getCapabilities(XResource.MODULE_IDENTITY_NAMESPACE).iterator().next();
        Assert.assertEquals("org.jboss.spec.javax.transaction", icap.getName());

        XIdentityCapability cap = XResourceBuilderFactory.<XResource>create().addIdentityCapability(moduleId);
        Assert.assertTrue("Requirement matches", req.matches(cap));
    }
}
