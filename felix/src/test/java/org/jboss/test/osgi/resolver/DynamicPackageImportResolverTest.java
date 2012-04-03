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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 25-Feb-2012
 */
public class DynamicPackageImportResolverTest extends AbstractResolverTest {

    @Test
    public void testBundleSymbolicNameDirective() throws Exception {

        final JavaArchive archiveA = ShrinkWrap.create(JavaArchive.class, "tb8a");
        archiveA.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveA.getName());
                builder.addExportPackages("org.jboss.test.osgi.framework.classloader.support.a");
                return builder.openStream();
            }
        });
        XResource resourceA = createResource(archiveA);

        final JavaArchive archiveB = ShrinkWrap.create(JavaArchive.class, "tb8b");
        archiveB.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveB.getName());
                builder.addExportPackages("org.jboss.test.osgi.framework.classloader.support.a");
                return builder.openStream();
            }
        });
        XResource resourceB = createResource(archiveB);

        final JavaArchive archiveC = ShrinkWrap.create(JavaArchive.class, "tb17c");
        archiveC.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveC.getName());
                String packageA = "org.jboss.test.osgi.framework.classloader.support.a";
                builder.addDynamicImportPackages(packageA + ";bundle-symbolic-name=tb8b," + packageA + ";bundle-symbolic-name=tb8a");
                return builder.openStream();
            }
        });
        XResource resourceC = createResource(archiveC);

        installResources(resourceA, resourceB, resourceC);

        List<XResource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource,List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        assertNotNull("Wire map not null", map);
        assertEquals(3, map.size());
        
        assertTrue("No wires", map.get(resourceA).isEmpty());
        assertTrue("No wires", map.get(resourceB).isEmpty());
        assertTrue("No wires", map.get(resourceC).isEmpty());
    }
}