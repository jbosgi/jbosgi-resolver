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

import java.util.List;

import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XHostCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.osgi.resolver.spi.AbstractResourceBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

/**
 * Unit tests for the {@link AbstractResourceBuilder} class
 *
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @author Thomas.Diesler@jboss.com
 */
public class XResourceBuilderTestCase extends AbstractTestBase {

    @Test
    public void testAttributDirectiveTrimming() throws Exception {
        OSGiMetaDataBuilder builder = OSGiMetaDataBuilder.createBuilder("test1");
        builder.addImportPackages("value1", "value2; version= 1.0.1", "value3;resolution:= optional", "value4;version = 3 ; resolution := optional ");
        Resource resource = XResourceBuilderFactory.create().loadFrom(builder.getOSGiMetaData()).getResource();
        validateRequirements(resource);
        validateCapabilities(resource);
    }

    @Test
    public void testAttributDirectiveNoTrimming() throws Exception {
        OSGiMetaDataBuilder builder = OSGiMetaDataBuilder.createBuilder("test1");
        builder.addImportPackages("value1", "value2;version=1.0.1", "value3;resolution:=optional", "value4;version=3;resolution:=optional");
        Resource resource = XResourceBuilderFactory.create().loadFrom(builder.getOSGiMetaData()).getResource();
        validateRequirements(resource);
        validateCapabilities(resource);
    }

    @Test
    public void testAttributMutability() throws Exception {
        XResourceBuilder builder = XResourceBuilderFactory.create();
        XCapability cap = builder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "test1");
        cap.getAttributes().put(BundleNamespace.CAPABILITY_EFFECTIVE_DIRECTIVE, "meta");
        XResource res = builder.getResource();
        XIdentityCapability icap = res.getIdentityCapability();
        Assert.assertEquals("test1", icap.getSymbolicName());
        Assert.assertEquals("meta", icap.getAttribute(BundleNamespace.CAPABILITY_EFFECTIVE_DIRECTIVE));
        try {
            icap.getAttributes().remove(BundleNamespace.CAPABILITY_EFFECTIVE_DIRECTIVE);
            Assert.fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    private void validateRequirements(Resource resource) throws BundleException {
        List<Requirement> reqs = resource.getRequirements(PackageNamespace.PACKAGE_NAMESPACE);
        Assert.assertNotNull("Requirements not null", reqs);
        Assert.assertEquals(4, reqs.size());
        for (Requirement req : reqs) {
            XPackageRequirement xreq = ((XRequirement) req).adapt(XPackageRequirement.class);
            String packageName = xreq.getPackageName();
            if ("value1".equals(packageName)) {
                Assert.assertEquals("(osgi.wiring.package=value1)", xreq.getFilter().toString());
                Assert.assertFalse(xreq.isOptional());
            } else if ("value2".equals(packageName)) {
                Assert.assertEquals("(&(osgi.wiring.package=value2)(version>=1.0.1))", xreq.getFilter().toString());
                Assert.assertFalse(xreq.isOptional());
            } else if ("value3".equals(packageName)) {
                Assert.assertEquals("(osgi.wiring.package=value3)", xreq.getFilter().toString());
                Assert.assertTrue(xreq.isOptional());
            } else if ("value4".equals(packageName)) {
                Assert.assertEquals("(&(osgi.wiring.package=value4)(version>=3.0.0))", xreq.getFilter().toString());
                Assert.assertTrue(xreq.isOptional());
            } else {
                Assert.fail("Incorrect package name: " + req);
            }
        }
    }

    private void validateCapabilities(Resource resource) {
        List<Capability> caps = resource.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        XCapability cap = (XCapability) caps.get(0);
        XIdentityCapability icap = cap.adapt(XIdentityCapability.class);
        Assert.assertEquals("test1", icap.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, icap.getVersion());

        caps = resource.getCapabilities(HostNamespace.HOST_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        cap = (XCapability) caps.get(0);
        XHostCapability hcap = cap.adapt(XHostCapability.class);
        Assert.assertEquals("test1", hcap.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, hcap.getVersion());
    }
}
