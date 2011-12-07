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

import junit.framework.Assert;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.internal.AbstractOSGiMetaData;
import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.spi.AbstractResourceBuilder;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.wiring.BundleRevision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes.Name;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.osgi.framework.resource.ResourceConstants.WIRING_PACKAGE_NAMESPACE;

/**
 * Unit tests for the {@link AbstractResourceBuilder} class
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 * @author Thomas.Diesler@jboss.com
 */
public class AbstractResourceBuilderTestCase {

    @Test
    public void testAttributDirectiveTrimming() throws Exception {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("Bundle-SymbolicName", "test1");
        attrs.put("Import-Package", "value1,value2; version= 1.0.1,value3;resolution:= optional,value4;version = 3 ; resolution := optional ");

        XResource resource = createResource(attrs);
        validateRequirements(resource);
        validateCapabilities(resource);
    }

    @Test
    public void testAttributDirectiveNoTrimming() throws Exception {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("Bundle-SymbolicName", "test1");
        attrs.put("Import-Package", "value1,value2;version=1.0.1,value3;resolution:=optional,value4;version=3;resolution:=optional");

        XResource resource = createResource(attrs);
        validateRequirements(resource);
        validateCapabilities(resource);
    }

    private XResource createResource(Map<String, String> attrs) throws BundleException {
        XResourceBuilder amb = new AbstractResourceBuilder();
        OSGiMetaData metaData = new TestOSGiMetaData(attrs);
        XResourceBuilder builder = amb.createResource(metaData);
        return builder.getResource();
    }

    private void validateRequirements(XResource resource) throws BundleException {
        List<Requirement> reqs = resource.getRequirements(WIRING_PACKAGE_NAMESPACE);
        assertNotNull("Requirements not null", reqs);
        assertEquals(4, reqs.size());
        for (Requirement req : reqs) {
            XPackageRequirement xreq = (XPackageRequirement) req;
            String packageName = xreq.getPackageName();
            if ("value1".equals(packageName)) {
                assertNull(xreq.getVersionRange());
                assertFalse(xreq.isOptional());
            } else if ("value2".equals(packageName)) {
                assertEquals(new VersionRange("1.0.1"), xreq.getVersionRange());
                assertFalse(xreq.isOptional());
            } else if ("value3".equals(packageName)) {
                assertNull(xreq.getVersionRange());
                assertTrue(xreq.isOptional());
            } else if ("value4".equals(packageName)) {
                assertEquals(new VersionRange("3"), xreq.getVersionRange());
                assertTrue(xreq.isOptional());
            } else {
                Assert.fail("Incorrect package name: " + req);
            }
        }
    }

    private void validateCapabilities(XResource resource) {
        List<Capability> caps = resource.getCapabilities(null);
        assertNotNull("Capabilities not null", caps);
        assertEquals(1, caps.size());
        XBundleCapability cap = (XBundleCapability) caps.get(0);
        BundleRevision rev = cap.getRevision();
        assertEquals("test1", rev.getSymbolicName());
        assertEquals(Version.emptyVersion, rev.getVersion());
    }

    private static class TestOSGiMetaData extends AbstractOSGiMetaData {
        private final HashMap<Name, String> attributes;

        TestOSGiMetaData(Map<String, String> attrMap) {
            attributes = new HashMap<Name, String>();
            for (Map.Entry<String, String> entry : attrMap.entrySet()) {
                attributes.put(new Name(entry.getKey()), entry.getValue());
            }
        }

        @Override
        protected Map<Name, String> getMainAttributes() {
            return attributes;
        }

        @Override
        protected String getMainAttribute(String key) {
            return attributes.get(new Name(key));
        }
    }
}
