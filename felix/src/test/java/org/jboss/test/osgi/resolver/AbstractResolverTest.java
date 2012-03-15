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
package org.jboss.test.osgi.resolver;

import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.jboss.osgi.resolver.felix.FelixResolver;
import org.jboss.osgi.resolver.spi.AbstractEnvironment;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.junit.Before;
import org.osgi.framework.Constants;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;
import org.osgi.service.resolver.Environment;
import org.osgi.service.resolver.Resolver;

/**
 * The abstract resolver test.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolverTest extends OSGiTest {

    Resolver resolver;
    XEnvironment environment;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        resolver = new FelixResolver();
        environment = new AbstractEnvironment();
        Resource sysres = createSystemResource();
        environment.installResources(sysres);
    }

	protected Resource createSystemResource() {
		XResourceBuilder builder = XResourceBuilderFactory.create();
		builder.addIdentityCapability(Constants.SYSTEM_BUNDLE_SYMBOLICNAME, null, null, null, null);
		return builder.getResource();
	}

    Resource createResource(Archive<?> archive) throws Exception {
        Node node = archive.get(JarFile.MANIFEST_NAME);
        Manifest manifest = new Manifest(node.getAsset().openStream());
        OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest);
        return XResourceBuilderFactory.create().loadFrom(metadata).getResource();
    }

    Environment installResources(Resource... resources) {
        environment.installResources(resources);
        return environment;
    }

    void applyResolverResults(Map<Resource,List<Wire>> wiremap) {
    	environment.updateWiring(wiremap);
    }

    Wiring getWiring(Environment env, Resource resource) {
        return env.getWirings().get(resource);
    }
}