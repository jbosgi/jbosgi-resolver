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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes.Name;

import junit.framework.Assert;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.internal.AbstractOSGiMetaData;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XModuleBuilder;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XVersionRange;
import org.jboss.osgi.resolver.spi.AbstractModuleBuilder;
import org.junit.Test;

/**
 * Unit tests for the {@link AbstractModuleBuilder} class
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class AbstractModuleBuilderTestCase {
    @Test
    public void testAttributDirectiveTrimming() throws Exception {
        AbstractModuleBuilder amb = new AbstractModuleBuilder();
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("Bundle-SymbolicName", "test1");
        attrs.put("Import-Package", "value1," + "value2; version= 1.0.1," + "value3;resolution:= optional," + "value4;version = 3 ; resolution := optional ");
        OSGiMetaData md = new TestOSGiMetaData(attrs);
        XModuleBuilder builder = amb.createModule(md, 1);
        XModule m = builder.getModule();

        for (XPackageRequirement preq : m.getPackageRequirements()) {
            if (preq.getName().equals("value1")) {
                assertEquals(XVersionRange.infiniteRange, preq.getVersionRange());
                assertFalse(preq.isOptional());
            } else if (preq.getName().equals("value2")) {
                assertEquals(XVersionRange.parse("1.0.1"), preq.getVersionRange());
                assertFalse(preq.isOptional());
            } else if (preq.getName().equals("value3")) {
                assertEquals(XVersionRange.infiniteRange, preq.getVersionRange());
                assertTrue(preq.isOptional());
            } else if (preq.getName().equals("value4")) {
                assertEquals(XVersionRange.parse("3"), preq.getVersionRange());
                assertTrue(preq.isOptional());
            } else {
                Assert.fail("Incorrect package name:" + preq.getName());
            }
        }
    }

    @Test
    public void testAttributDirectiveNoTrimming() throws Exception {
        AbstractModuleBuilder amb = new AbstractModuleBuilder();
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("Bundle-SymbolicName", "test1");
        attrs.put("Import-Package", "value1," + "value2;version=1.0.1," + "value3;resolution:=optional," + "value4;version=3;resolution:=optional");
        OSGiMetaData md = new TestOSGiMetaData(attrs);
        XModuleBuilder builder = amb.createModule(md, 1);
        XModule m = builder.getModule();

        for (XPackageRequirement preq : m.getPackageRequirements()) {
            if (preq.getName().equals("value1")) {
                assertEquals(XVersionRange.infiniteRange, preq.getVersionRange());
                assertFalse(preq.isOptional());
            } else if (preq.getName().equals("value2")) {
                assertEquals(XVersionRange.parse("1.0.1"), preq.getVersionRange());
                assertFalse(preq.isOptional());
            } else if (preq.getName().equals("value3")) {
                assertEquals(XVersionRange.infiniteRange, preq.getVersionRange());
                assertTrue(preq.isOptional());
            } else if (preq.getName().equals("value4")) {
                assertEquals(XVersionRange.parse("3"), preq.getVersionRange());
                assertTrue(preq.isOptional());
            } else {
                Assert.fail("Incorrect package name:" + preq.getName());
            }
        }
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
