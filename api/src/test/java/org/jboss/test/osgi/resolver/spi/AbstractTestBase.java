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

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes.Name;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.spi.AbstractOSGiMetaData;
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
