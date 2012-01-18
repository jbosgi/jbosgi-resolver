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

import org.jboss.osgi.resolver.v2.VersionRange;
import org.jboss.osgi.resolver.v2.XFragmentHostCapability;
import org.jboss.osgi.resolver.v2.XIdentityCapability;
import org.jboss.osgi.resolver.v2.XPackageRequirement;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.osgi.framework.resource.ResourceConstants.IDENTITY_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_HOST_NAMESPACE;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * Unit tests for the {@link org.jboss.osgi.resolver.v2.spi.AbstractResourceBuilder} class
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

    private void validateRequirements(Resource resource) throws BundleException {
        List<Requirement> reqs = resource.getRequirements(WIRING_PACKAGE_NAMESPACE);
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
        List<Capability> caps = resource.getCapabilities(IDENTITY_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        XIdentityCapability icap = (XIdentityCapability) caps.get(0);
        Assert.assertEquals("test1", icap.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, icap.getVersion());

        caps = resource.getCapabilities(WIRING_HOST_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        XFragmentHostCapability hcap = (XFragmentHostCapability) caps.get(0);
        Assert.assertEquals("test1", hcap.getSymbolicName());
        Assert.assertEquals(Version.emptyVersion, hcap.getVersion());
    }
}
