/*
 * #%L
 * JBossOSGi Resolver Felix
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
package org.jboss.test.osgi.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.XBundleRevisionBuilderFactory;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResolveContext;
import org.jboss.osgi.resolver.XResolver;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.osgi.resolver.spi.AbstractEnvironment;
import org.jboss.osgi.resolver.spi.AbstractResolver;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.junit.Before;
import org.osgi.framework.Constants;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;

/**
 * The abstract resolver test.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolverTest extends OSGiTest {

    XResolver resolver;
    XEnvironment environment;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        resolver = new AbstractResolver();
        environment = new AbstractEnvironment();
        XResource sysres = createSystemResource();
        environment.installResources(sysres);
    }

    protected XResource createSystemResource() {
        XResourceBuilder<XResource> builder = XResourceBuilderFactory.create();
        builder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, Constants.SYSTEM_BUNDLE_SYMBOLICNAME);
        return builder.getResource();
    }

    XResource createResource(Archive<?> archive) throws Exception {
        Node node = archive.get(JarFile.MANIFEST_NAME);
        Manifest manifest = new Manifest(node.getAsset().openStream());
        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest);
        return XBundleRevisionBuilderFactory.create().loadFrom(metadata).getResource();
    }

    XEnvironment installResources(XResource... resources) {
        environment.installResources(resources);
        return environment;
    }

    XResolveContext getResolveContext(final List<XResource> mandatory, final List<XResource> optional) {
        Collection<? extends Resource> manres = mandatory != null ? new ArrayList<Resource>(mandatory) : null;
        Collection<? extends Resource> optres = optional != null ? new ArrayList<Resource>(optional) : null;
        return resolver.createResolveContext(environment, manres, optres);
    }

    void applyResolverResults(Map<Resource, List<Wire>> wiremap) {
        environment.updateWiring(wiremap);
    }

    Wiring getWiring(Resource resource) {
        return environment.getWirings().get(resource);
    }
}
