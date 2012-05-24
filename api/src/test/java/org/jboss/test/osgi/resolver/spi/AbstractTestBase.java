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
import java.util.Map;
import java.util.jar.Attributes.Name;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.internal.AbstractOSGiMetaData;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.osgi.resolver.spi.AbstractEnvironment;
import org.junit.Before;
import org.osgi.framework.BundleException;

/**
 * @author Thomas.Diesler@jboss.com
 */
public abstract class AbstractTestBase {

    private AbstractEnvironment environment;

    @Before
    public void setUp() {
        environment = new AbstractEnvironment();
    }

    XEnvironment installResources(XResource... resources) {
        environment.installResources(resources);
        return environment;
    }

    XResource createResource(Map<String, String> attrs) throws BundleException {
        XResourceBuilder builder = buildResource(attrs);
        return builder.getResource();
    }

    XResourceBuilder buildResource(Map<String, String> attrs) throws BundleException {
        OSGiMetaData metaData = new TestOSGiMetaData(attrs);
        XResourceBuilder builder = XResourceBuilderFactory.create();
        return builder.loadFrom(metaData);
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
