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

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.internal.AbstractOSGiMetaData;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.spi.AbstractEnvironment;
import org.jboss.osgi.resolver.spi.AbstractResourceBuilder;
import org.jboss.osgi.resolver.spi.ResourceIndexComparator;
import org.junit.Before;
import org.osgi.framework.BundleException;
import org.osgi.framework.resource.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes.Name;

/**
 * @author Thomas.Diesler@jboss.com
 */
public abstract class AbstractTestBase {

    private AbstractEnvironment environment;

    @Before
    public void setUp() {
        environment = new AbstractEnvironment();
    }

    XEnvironment installResources(List<Resource> resources) {
        for(Resource res : resources) {
            environment.installResources(res);
        }
        return environment;
    }

    Resource createResource(Map<String, String> attrs) throws BundleException {
        XResourceBuilder amb = XResourceBuilder.INSTANCE.createResource();
        OSGiMetaData metaData = new TestOSGiMetaData(attrs);
        XResourceBuilder builder = amb.load(metaData);
        return builder.getResource();
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
