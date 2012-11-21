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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.osgi.resolver.XResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 20-Nov-2012
 */
public class RequireCapabilityResolverTest extends AbstractResolverTest {

    @Test
    public void testNamespaceValueInFilter() throws Exception {
        
        XResource resourceA = createResource(getArchiveA());
        verifyResouceA(resourceA);

        // Bundle-SymbolicName: org.osgi.test.cases.framework.resolver.tb5
        // Require-Capability: test; filter:="(&(test=aName)(version>=1.1.0))"
        final JavaArchive archiveB = ShrinkWrap.create(JavaArchive.class, "org.osgi.test.cases.framework.resolver.tb5");
        archiveB.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveB.getName());
                builder.addRequiredCapabilities("test;filter:=\"(&(test=aName)(version>=1.1.0))\"");
                return builder.openStream();
            }
        });
        XResource resourceB = createResource(archiveB);

        // Install and resolve A, B
        installResources(resourceA, resourceB);
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        List<Wire> wires = map.get(resourceB);
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals("test", wires.get(0).getCapability().getNamespace());
        Assert.assertEquals("aName", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceA, wires.get(0).getProvider());
    }


    @Test
    public void testStringValueProximity() throws Exception {
        
        XResource resourceA = createResource(getArchiveA());
        verifyResouceA(resourceA);

        // Bundle-SymbolicName: org.osgi.test.cases.framework.resolver.tb5
        // Require-Capability: test; filter:="(&(version>=1.1)(string~=astring))"
        final JavaArchive archiveB = ShrinkWrap.create(JavaArchive.class, "org.osgi.test.cases.framework.resolver.tb5");
        archiveB.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveB.getName());
                builder.addRequiredCapabilities("test;filter:=\"(&(version>=1.1)(string~=astring))\"");
                return builder.openStream();
            }
        });
        XResource resourceB = createResource(archiveB);

        // Install and resolve A, B
        installResources(resourceA, resourceB);
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        List<Wire> wires = map.get(resourceB);
        Assert.assertEquals(1, wires.size());
    }

    @Test
    public void testLongValueMatch() throws Exception {
        
        XResource resourceA = createResource(getArchiveA());
        verifyResouceA(resourceA);

        // Bundle-SymbolicName: org.osgi.test.cases.framework.resolver.tb5
        // Require-Capability: test; filter:="(&(version>=1.1)(long>=99))"
        final JavaArchive archiveB = ShrinkWrap.create(JavaArchive.class, "org.osgi.test.cases.framework.resolver.tb5");
        archiveB.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveB.getName());
                builder.addRequiredCapabilities("test;filter:=\"(&(version>=1.1)(long>=99))\"");
                return builder.openStream();
            }
        });
        XResource resourceB = createResource(archiveB);

        // Install and resolve A, B
        installResources(resourceA, resourceB);
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        List<Wire> wires = map.get(resourceB);
        Assert.assertEquals(1, wires.size());
    }

    @Test
    public void testStringListMatch() throws Exception {
        
        XResource resourceA = createResource(getArchiveA());
        verifyResouceA(resourceA);

        // Bundle-SymbolicName: org.osgi.test.cases.framework.resolver.tb5
        // Require-Capability: test; filter:="(&(version>=1.1)(string.list2=a\"quote)(string.list2=a\,comma)(string.list2= aSpace )(string.list2=\"start)(string.list2=\,start)(string.list2=end\")(string.list2=end\,))"
        final JavaArchive archiveB = ShrinkWrap.create(JavaArchive.class, "org.osgi.test.cases.framework.resolver.tb5");
        archiveB.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveB.getName());
                Map<String, String> dirs = new LinkedHashMap<String, String>();
                dirs.put("filter", "(&(version>=1.1)(string.list2=a\"quote)(string.list2=a\\,comma)(string.list2= aSpace )(string.list2=\"start)(string.list2=\\,start)(string.list2=end\")(string.list2=end\\,))");
                builder.addRequiredCapability("test", null, dirs);
                return builder.openStream();
            }
        });
        XResource resourceB = createResource(archiveB);

        // Install and resolve A, B
        installResources(resourceA, resourceB);
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        List<Wire> wires = map.get(resourceB);
        Assert.assertEquals(1, wires.size());
    }


    private void verifyResouceA(XResource resourceA) {
        List<Capability> caps = resourceA.getCapabilities("test");
        Assert.assertEquals(1, caps.size());
        Map<String, Object> atts = caps.get(0).getAttributes();
        List<String> keys = new ArrayList<String>(atts.keySet());
        Assert.assertEquals(6, keys.size());
        Assert.assertEquals("test", keys.get(0));
        Assert.assertEquals("aName", atts.get(keys.get(0)));
        Assert.assertEquals("version", keys.get(1));
        Assert.assertEquals(Version.parseVersion("1.1"), atts.get(keys.get(1)));
        Assert.assertEquals("long", keys.get(2));
        Assert.assertEquals(Long.valueOf("100"), atts.get(keys.get(2)));
        Assert.assertEquals("string", keys.get(3));
        Assert.assertEquals("aString", atts.get(keys.get(3)));
        Assert.assertEquals("version.list", keys.get(4));
        List<Version> versions = Arrays.asList(Version.parseVersion("1.0"), Version.parseVersion("1.1"), Version.parseVersion("1.2"));
        Assert.assertEquals(versions, atts.get(keys.get(4)));
        Assert.assertEquals("string.list2", keys.get(5));
        List<String> strings = Arrays.asList("a\"quote", "a,comma", " aSpace ", "\"start", ",start", "end\"", "end,");
        Assert.assertEquals(strings, atts.get(keys.get(5)));
    }

    private String getNamespaceValue(Capability cap) {
        return (String) cap.getAttributes().get(cap.getNamespace());
    }
    
    private JavaArchive getArchiveA() {
        final JavaArchive archiveA = ShrinkWrap.create(JavaArchive.class, "org.osgi.test.cases.framework.resolver.tb1");
        archiveA.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveA.getName());
                Map<String, String> atts = new LinkedHashMap<String, String>();
                atts.put("test", "aName");
                atts.put("version:Version", "1.1");
                atts.put("long:Long", "100");
                atts.put("string:String", "aString");
                atts.put("version.list:List<Version>", "1.0, 1.1, 1.2");
                atts.put("string.list2:List", "a\\\"quote,a\\,comma, aSpace ,\\\"start,\\,start,end\\\",end\\,");
                builder.addProvidedCapability("test", atts, null);
                return builder.openStream();
            }
        });
        return archiveA;
    }
}
