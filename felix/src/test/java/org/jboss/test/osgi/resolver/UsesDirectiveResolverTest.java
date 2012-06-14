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

import static org.junit.Assert.assertEquals;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.spi.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 12-Mar-2012
 */
public class UsesDirectiveResolverTest extends AbstractResolverTest {

    @Test
    public void testMultipleProviders() throws Exception {

        // Bundle-SymbolicName: javax.servlet.api
        // ExportPackage: javax.servlet;version=2.5
        final JavaArchive archiveA = ShrinkWrap.create(JavaArchive.class, "javax.servlet.api");
        archiveA.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveA.getName());
                builder.addExportPackages("javax.servlet;version=2.5");
                return builder.openStream();
            }
        });
        XResource resourceA = createResource(archiveA);

        // Bundle-SymbolicName: enterprise.jar
        // ExportPackage: org.osgi.service.http;version=1.2.1;uses:=javax.servlet
        // ImportPackage: javax.servlet;resolution:=optional
        final JavaArchive archiveB = ShrinkWrap.create(JavaArchive.class, "enterprise.jar");
        archiveB.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveB.getName());
                builder.addExportPackages("org.osgi.service.http;version=1.2.1;uses:=javax.servlet");
                builder.addImportPackages("javax.servlet;resolution:=optional");
                return builder.openStream();
            }
        });
        XResource resourceB = createResource(archiveB);

        // Bundle-SymbolicName: http.service.provider
        // ExportPackage: javax.servlet;version=2.5
        // ExportPackage: org.ops4j.pax.web.service;resolution:=optional;uses:=javax.servlet
        // ExportPackage: org.osgi.service.http;version=1.2.0;uses:=javax.servlet
        // ImportPackage: javax.servlet;version="[2.3.0,2.6.0)";resolution:=optional
        final JavaArchive archiveC = ShrinkWrap.create(JavaArchive.class, "http.service.provider");
        archiveC.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveC.getName());
                builder.addExportPackages("javax.servlet;version=2.5");
                builder.addExportPackages("org.ops4j.pax.web.service;uses:=javax.servlet");
                builder.addExportPackages("org.osgi.service.http;version=1.2.0;uses:=javax.servlet");
                builder.addImportPackages("javax.servlet;version=\"[2.3.0,2.6.0)\";resolution:=optional");
                return builder.openStream();
            }
        });
        XResource resourceC = createResource(archiveC);

        // Install and resolve A, B, C
        installResources(resourceA, resourceB, resourceC);
        List<XResource> mandatory = Arrays.asList(resourceA, resourceB, resourceC);
        Map<Resource, List<Wire>> map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        // Verify that javax.servlet wires to the API bundle 
        List<Wire> wires = map.get(resourceB);
        assertEquals(1, wires.size());
        Assert.assertEquals("javax.servlet", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceA, wires.get(0).getProvider());
        
        // Verify that javax.servlet wires to the API bundle 
        wires = map.get(resourceC);
        assertEquals(1, wires.size());
        Assert.assertEquals("javax.servlet", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceA, wires.get(0).getProvider());
        
        // Bundle-SymbolicName: war.extender.jar
        // ImportPackage: org.ops4j.pax.web.service
        // ImportPackage: javax.servlet;version="[2.3,3.0)"
        final JavaArchive archiveD = ShrinkWrap.create(JavaArchive.class, "war.extender.jar");
        archiveD.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleManifestVersion(2);
                builder.addBundleSymbolicName(archiveD.getName());
                builder.addImportPackages("org.ops4j.pax.web.service");
                builder.addImportPackages("javax.servlet;version=\"[2.3,3.0)\"");
                return builder.openStream();
            }
        });
        XResource resourceD = createResource(archiveD);

        // Install and resolve D
        installResources(resourceD);
        mandatory = Arrays.asList(resourceD);
        map = resolver.resolve(getResolveContext(mandatory, null));
        applyResolverResults(map);
        
        // Verify that javax.servlet wires to the API bundle 
        wires = map.get(resourceD);
        assertEquals(2, wires.size());
        Assert.assertEquals("org.ops4j.pax.web.service", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceC, wires.get(0).getProvider());
        Assert.assertEquals("javax.servlet", getNamespaceValue(wires.get(1).getCapability()));
        Assert.assertSame(resourceA, wires.get(1).getProvider());
        
        // Verify wiring of A
        Wiring wiringA = getWiring(resourceA);
        Assert.assertEquals(0, wiringA.getRequiredResourceWires(null).size());
        wires = wiringA.getProvidedResourceWires(null);
        Assert.assertEquals(3, wires.size());
        List<XResource> requierers = new ArrayList<XResource>(Arrays.asList(resourceB, resourceC, resourceD));
        for (Wire wire : wires) {
            Assert.assertEquals("javax.servlet", getNamespaceValue(wire.getCapability()));
            requierers.remove(wire.getRequirer());
        }
        Assert.assertTrue(requierers.isEmpty());
        Assert.assertEquals(0, wiringA.getResourceRequirements(PACKAGE_NAMESPACE).size());
        List<Capability> caps = wiringA.getResourceCapabilities(PACKAGE_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        Assert.assertEquals("javax.servlet", getNamespaceValue(caps.get(0)));
        
        // Verify wiring of B
        Wiring wiringB = getWiring(resourceB);
        wires = wiringB.getRequiredResourceWires(null);
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals("javax.servlet", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceA, wires.get(0).getProvider());
        Assert.assertEquals(0, wiringB.getProvidedResourceWires(null).size());
        List<Requirement> reqs = wiringB.getResourceRequirements(PACKAGE_NAMESPACE);
        Assert.assertEquals(1, reqs.size());
        Assert.assertEquals("javax.servlet", getNamespaceValue(reqs.get(0)));
        caps = wiringB.getResourceCapabilities(PACKAGE_NAMESPACE);
        Assert.assertEquals(1, caps.size());
        Assert.assertEquals("org.osgi.service.http", getNamespaceValue(caps.get(0)));
        
        // Verify wiring of C
        Wiring wiringC = getWiring(resourceC);
        wires = wiringC.getRequiredResourceWires(null);
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals("javax.servlet", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceA, wires.get(0).getProvider());
        wires = wiringC.getProvidedResourceWires(null);
        Assert.assertEquals(1, wires.size());
        Assert.assertEquals("org.ops4j.pax.web.service", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceD, wires.get(0).getRequirer());
        reqs = wiringC.getResourceRequirements(PACKAGE_NAMESPACE);
        Assert.assertEquals(1, reqs.size());
        Assert.assertEquals("javax.servlet", getNamespaceValue(reqs.get(0)));
        caps = wiringC.getResourceCapabilities(PACKAGE_NAMESPACE);
        Assert.assertEquals(2, caps.size());
        Assert.assertEquals("org.ops4j.pax.web.service", getNamespaceValue(caps.get(0)));
        Assert.assertEquals("org.osgi.service.http", getNamespaceValue(caps.get(1)));
        
        // Verify wiring of D
        Wiring wiringD = getWiring(resourceD);
        wires = wiringD.getRequiredResourceWires(null);
        Assert.assertEquals(2, wires.size());
        Assert.assertEquals("org.ops4j.pax.web.service", getNamespaceValue(wires.get(0).getCapability()));
        Assert.assertSame(resourceC, wires.get(0).getProvider());
        Assert.assertEquals("javax.servlet", getNamespaceValue(wires.get(1).getCapability()));
        Assert.assertSame(resourceA, wires.get(1).getProvider());
        Assert.assertEquals(0, wiringD.getProvidedResourceWires(null).size());
        reqs = wiringD.getResourceRequirements(PACKAGE_NAMESPACE);
        Assert.assertEquals(2, reqs.size());
        Assert.assertEquals("org.ops4j.pax.web.service", getNamespaceValue(reqs.get(0)));
        Assert.assertEquals("javax.servlet", getNamespaceValue(reqs.get(1)));
        Assert.assertEquals(0, wiringD.getResourceCapabilities(PACKAGE_NAMESPACE).size());
    }

    private String getNamespaceValue(Capability cap) {
        return (String) cap.getAttributes().get(cap.getNamespace());
    }
    
    private String getNamespaceValue(Requirement req) {
        return (String) req.getAttributes().get(req.getNamespace());
    }
}