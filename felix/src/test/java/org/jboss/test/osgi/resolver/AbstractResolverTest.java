/*
 * #%L
 * JBossOSGi Resolver Felix
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
import org.jboss.osgi.resolver.felix.StatelessResolver;
import org.jboss.osgi.resolver.spi.AbstractEnvironment;
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
        resolver = new StatelessResolver();
        environment = new AbstractEnvironment();
        XResource sysres = createSystemResource();
        environment.installResources(sysres);
    }

    protected XResource createSystemResource() {
        XResourceBuilder builder = XResourceBuilderFactory.create();
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