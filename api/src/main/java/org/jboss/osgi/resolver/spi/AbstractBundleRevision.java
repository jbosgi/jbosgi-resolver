/*
 * #%L
 * JBossOSGi Resolver API
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

package org.jboss.osgi.resolver.spi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XBundleRevision;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * The abstract implementation of an {@link XBundleRevision}.
 *
 * @author thomas.diesler@jboss.com
 * @since 30-May-2012
 */
public class AbstractBundleRevision extends AbstractResource implements XBundleRevision {

    private List<BundleCapability> bundleCapabilities;
    private List<BundleRequirement> bundleRequirements;

    @Override
    public String getSymbolicName() {
        return getIdentityCapability().getSymbolicName();
    }

    @Override
    public Version getVersion() {
        return getIdentityCapability().getVersion();
    }

    @Override
    public List<BundleCapability> getDeclaredCapabilities(String namespace) {
        if (bundleCapabilities == null) {
            bundleCapabilities = new ArrayList<BundleCapability>();
            for (Capability cap : getCapabilities(namespace)) {
                bundleCapabilities.add((BundleCapability) cap);
            }
        }
        return bundleCapabilities;
    }

    @Override
    public List<BundleRequirement> getDeclaredRequirements(String namespace) {
        if (bundleRequirements == null) {
            bundleRequirements = new ArrayList<BundleRequirement>();
            for (Requirement cap : getRequirements(namespace)) {
                bundleRequirements.add((BundleRequirement) cap);
            }
        }
        return bundleRequirements;
    }

    @Override
    public int getRevisionId() {
        return 0;
    }

    @Override
    public int getTypes() {
        return isFragment() ? TYPE_FRAGMENT : 0;
    }

    @Override
    public BundleWiring getWiring() {
        return getAttachment(BundleWiring.class);
    }

    @Override
    public XBundle getBundle() {
        return (XBundle) getAttachment(Bundle.class);
    }
    
    @Override
    public ModuleIdentifier getModuleIdentifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModuleClassLoader getModuleClassLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getResource(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getEntry(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getEntryPaths(String path) {
        throw new UnsupportedOperationException();
    }
}