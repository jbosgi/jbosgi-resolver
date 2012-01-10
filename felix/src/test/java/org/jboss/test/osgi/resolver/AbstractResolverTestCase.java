package org.jboss.test.osgi.resolver;

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

import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.metadata.OSGiMetaDataBuilder;
import org.jboss.osgi.resolver.XCapabilityComparator;
import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.felix.FelixResolver;
import org.jboss.osgi.resolver.spi.AbstractEnvironment;
import org.jboss.osgi.resolver.spi.AbstractResourceBuilder;
import org.jboss.osgi.resolver.spi.FrameworkPreferencesComparator;
import org.jboss.osgi.testing.OSGiTest;
import org.jboss.osgi.vfs.VFSUtils;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;
import org.osgi.service.resolver.Resolver;

import java.util.Comparator;
import java.util.List;
import java.util.jar.Manifest;

/**
 * The abstract resolver test.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public abstract class AbstractResolverTestCase extends OSGiTest {

    Resolver resolver;
    AbstractEnvironment environment;

    @Before
    public void setUp() {
        resolver = new FelixResolver();
        environment = new AbstractEnvironment(new FrameworkPreferencesComparator());
    }

    XEnvironment createEnvironment(XCapabilityComparator comparator) {
        environment = new AbstractEnvironment(comparator);
        return environment;
    }
    
    XResource createResource(Archive<?> archive) throws Exception {
        VirtualFile virtualFile = toVirtualFile(archive);
        try {
            Manifest manifest = VFSUtils.getManifest(virtualFile);
            OSGiMetaData metadata = OSGiMetaDataBuilder.load(manifest);
            XResourceBuilder builder = new AbstractResourceBuilder();
            return builder.createResource(metadata).getResource();
        } finally {
            virtualFile.close();
        }
    }
    
    XEnvironment installResources(Resource... resources) {
        environment.installResources(resources);
        return environment;
    }
}