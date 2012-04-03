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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

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
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;

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
        for (Wire wire : wires) {
            Requirement req = wire.getRequirement();
            String pkgname = (String) req.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
            if ("javax.servlet".equals(pkgname)) {
                assertSame(resourceA, wire.getProvider());
            } else {
                fail("Unexpected package name: " + pkgname);
            }
        }
        
        // Verify that javax.servlet wires to the API bundle 
        wires = map.get(resourceC);
        assertEquals(1, wires.size());
        for (Wire wire : wires) {
            Requirement req = wire.getRequirement();
            String pkgname = (String) req.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
            if ("javax.servlet".equals(pkgname)) {
                assertSame(resourceA, wire.getProvider());
            } else {
                fail("Unexpected package name: " + pkgname);
            }
        }
        
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
        for (Wire wire : wires) {
            Requirement req = wire.getRequirement();
            String pkgname = (String) req.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
            if ("org.ops4j.pax.web.service".equals(pkgname)) {
                assertSame(resourceC, wire.getProvider());
            } else if ("javax.servlet".equals(pkgname)) {
                assertSame(resourceA, wire.getProvider());
            } else {
                fail("Unexpected package name: " + pkgname);
            }
        }
    }
}