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

package org.jboss.test.osgi.resolver.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.XHostCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.osgi.resolver.spi.AbstractResourceBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
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
public class AbstractResourceBuilderTestCase extends AbstractTestBase {

    @Test
    public void testAttributDirectiveTrimming() throws Exception {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("Bundle-SymbolicName", "test1");
        attrs.put("Import-Package", "value1,value2; version= 1.0.1,value3;resolution:= optional,value4;version = 3 ; resolution := optional ");
        Resource resource = createResource(attrs);
        validateRequirements(resource);
        validateCapabilities(resource);
    }

    @Test
    public void testAttributDirectiveNoTrimming() throws Exception {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("Bundle-SymbolicName", "test1");
        attrs.put("Import-Package", "value1,value2;version=1.0.1,value3;resolution:=optional,value4;version=3;resolution:=optional");
        Resource resource = createResource(attrs);
        validateRequirements(resource);
        validateCapabilities(resource);
    }

    @Test
    public void testAttributMutability() throws Exception {
        XResourceBuilder builder = XResourceBuilderFactory.create();
        XIdentityCapability icap = builder.addIdentityCapability("test1");
        icap.getAttributes().put(BundleNamespace.CAPABILITY_EFFECTIVE_DIRECTIVE, "meta");
        XResource res = builder.getResource();
        icap = res.getIdentityCapability();
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
            XPackageRequirement xreq = (XPackageRequirement) req;
            String packageName = xreq.getPackageName();
            if ("value1".equals(packageName)) {
                Assert.assertNull(xreq.getVersionRange());
                Assert.assertFalse(xreq.isOptional());
            } else if ("value2".equals(packageName)) {
                Assert.assertEquals(VersionRange.parse("1.0.1"), xreq.getVersionRange());
                Assert.assertFalse(xreq.isOptional());
            } else if ("value3".equals(packageName)) {
                Assert.assertNull(xreq.getVersionRange());
                Assert.assertTrue(xreq.isOptional());
            } else if ("value4".equals(packageName)) {
                Assert.assertEquals(VersionRange.parse("3"), xreq.getVersionRange());
                Assert.assertTrue(xreq.isOptional());
            } else {
                Assert.fail("Incorrect package name: " + req);
            }
        }
    }

    private void validateCapabilities(Resource resource) {
        List<Capability> caps = resource.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        XIdentityCapability icap = (XIdentityCapability) caps.get(0);
        Assert.assertEquals("test1", icap.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, icap.getVersion());

        caps = resource.getCapabilities(HostNamespace.HOST_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        XHostCapability hcap = (XHostCapability) caps.get(0);
        Assert.assertEquals("test1", hcap.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, hcap.getVersion());
    }
}
