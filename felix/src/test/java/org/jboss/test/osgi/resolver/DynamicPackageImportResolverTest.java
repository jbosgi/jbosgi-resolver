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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.spi.OSGiManifestBuilder;
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
