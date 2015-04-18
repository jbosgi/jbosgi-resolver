/*
 * #%L
 * JBossOSGi Framework
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

import static org.jboss.osgi.resolver.ResolverMessages.MESSAGES;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.Resource;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;
import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XBundleRevision;
import org.jboss.osgi.resolver.XBundleWiring;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Wire;
import org.osgi.service.resolver.HostedCapability;

/**
 * The {@link BundleWiring} implementation.
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2012
 */
public class AbstractBundleWiring extends AbstractWiring implements XBundleWiring {

    public AbstractBundleWiring(XBundleRevision brev, List<Wire> required, List<Wire> provided) {
        super(brev, required, provided);
    }

    @Override
    protected HostedCapability getHostedCapability(XCapability cap) {
        return new AbstractHostedBundleCapability(getResource(), cap);
    }

    @Override
    public boolean isCurrent() {
        XBundleRevision brev = getRevision();
        return brev.getWiringSupport().getWiring(true) == this;
    }

    @Override
    public boolean isInUse() {
        return transistiveInUse(this, true, new HashSet<BundleWiring>());
    }

    public boolean isInUseForUninstall() {
        return transistiveInUse(this, false, new HashSet<BundleWiring>());
    }

    private boolean transistiveInUse(AbstractBundleWiring wiring, boolean checkCurrent, Set<BundleWiring> visited) {
        if (wiring != null && !visited.contains(wiring)) {
            visited.add(wiring);

            if (checkCurrent && wiring.isCurrent()) {
                return true;
            }

            XBundle bundle = (XBundle) wiring.getBundle();
            if (bundle.isFragment()) {
                for (Wire wire : wiring.getRequiredResourceWires(HostNamespace.HOST_NAMESPACE)) {
                    AbstractBundleWire bwire = (AbstractBundleWire) wire;
                    AbstractBundleWiring auxwiring = (AbstractBundleWiring) bwire.getProviderWiring(false);
                    if (auxwiring != null && transistiveInUse(auxwiring, true, visited)) {
                        return true;
                    }
                }
            } else {
                for (Wire wire : wiring.getProvidedResourceWires(null)) {
                    AbstractBundleWire bwire = (AbstractBundleWire) wire;
                    AbstractBundleWiring auxwiring = (AbstractBundleWiring) bwire.getRequirerWiring(false);
                    if (auxwiring != null && transistiveInUse(auxwiring, true, visited)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<BundleCapability> getCapabilities(String namespace) {
        // If this bundle wiring is not in use, null will be returned
        if (!isInUse()) {
            return null;
        }
        List<BundleCapability> result = new ArrayList<BundleCapability>();
        for (Capability cap : getResourceCapabilities(namespace)) {
            result.add((BundleCapability) cap);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<BundleRequirement> getRequirements(String namespace) {
        // If this bundle wiring is not in use, null will be returned
        if (!isInUse()) {
            return null;
        }
        List<BundleRequirement> result = new ArrayList<BundleRequirement>();
        for (Requirement req : getResourceRequirements(namespace)) {
            result.add((BundleRequirement) req);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<BundleWire> getProvidedWires(String namespace) {
        // If this bundle wiring is not in use, null will be returned
        if (!isInUse()) {
            return null;
        }
        List<BundleWire> providedWires = new ArrayList<BundleWire>();
        for (Wire wire : super.getProvidedResourceWires(namespace)) {
            providedWires.add((BundleWire) wire);
        }
        return Collections.unmodifiableList(providedWires);
    }

    @Override
    public List<BundleWire> getRequiredWires(String namespace) {
        // If this bundle wiring is not in use, null will be returned
        if (!isInUse()) {
            return null;
        }
        List<BundleWire> requiredWires = new ArrayList<BundleWire>();
        for (Wire wire : super.getRequiredResourceWires(namespace)) {
            requiredWires.add((BundleWire) wire);
        }
        return Collections.unmodifiableList(requiredWires);
    }

    @Override
    public XBundleRevision getRevision() {
        return getResource();
    }

    @Override
    public XBundleRevision getResource() {
        return (XBundleRevision) super.getResource();
    }

    @Override
    public ClassLoader getClassLoader() {
        // If this bundle wiring is not in use, null will be returned
        if (!isInUse()) {
            return null;
        }
        XBundleRevision brev = getRevision();
        return brev.getModuleClassLoader();
    }

    @Override
    public List<URL> findEntries(String path, String filePattern, int options) {
        // If this bundle wiring is not in use, null will be returned
        if (!isInUse()) {
            return null;
        }
        List<URL> result = new ArrayList<URL>();
        XBundleRevision brev = getRevision();
        Enumeration<URL> entries = brev.findEntries(path, filePattern, (options & FINDENTRIES_RECURSE) != 0);
        while (entries != null && entries.hasMoreElements()) {
            result.add(entries.nextElement());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<String> listResources(String startPath, String filePattern, int options) {
        if (startPath == null)
            throw new IllegalArgumentException("Null rootPath");
        if (startPath.startsWith("/"))
            startPath = startPath.substring(1);
        if (startPath.endsWith("/"))
            startPath = startPath.substring(0, startPath.length() - 1);
        if (filePattern == null)
            filePattern = "*";

        // If this bundle wiring is not in use, null will be returned
        if (!isInUse() || getRevision().isFragment()) {
            return null;
        }

        boolean local = (options & BundleWiring.LISTRESOURCES_LOCAL) != 0;
        boolean recurse = (options & BundleWiring.LISTRESOURCES_RECURSE) != 0;

        Set<String> result = new LinkedHashSet<String>();
        ModuleClassLoader moduleClassLoader = getRevision().getModuleClassLoader();
        Pattern pattern = convertToPattern(filePattern);

        Iterator<Resource> itResources = moduleClassLoader.iterateResources(startPath, recurse);
        addResourceNames(itResources, pattern, result);

        if (!local) {
            try {
                PathFilter pathFilter;
                if (recurse && startPath.length() == 0) {
                    pathFilter = PathFilters.acceptAll();
                } else {
                    pathFilter = PathFilters.is(startPath);
                    if (recurse) {
                        pathFilter = PathFilters.any(pathFilter, PathFilters.match(startPath + "/**"));
                    }
                }
                itResources = moduleClassLoader.getModule().iterateResources(pathFilter);
            } catch (ModuleLoadException ex) {
                throw MESSAGES.illegalStateCannotIterateOverModuleResources(ex, getRevision());
            }
            addResourceNames(itResources, pattern, result);
        }

        return Collections.unmodifiableSet(result);
    }

    // Convert file pattern (RFC 1960-based Filter) into a RegEx pattern
    private static Pattern convertToPattern(String filePattern) {
        filePattern = filePattern.replace("*", ".*");
        return Pattern.compile("^" + filePattern + "$");
    }

    private void addResourceNames(Iterator<Resource> itResources, Pattern pattern, Set<String> result) {
        while(itResources.hasNext()) {
            String resname = itResources.next().getName();
            if (resname.startsWith("/")) {
                resname = resname.substring(1);
            }
            int lastIndex = resname.lastIndexOf('/');
            String filename = lastIndex > 0 ? resname.substring(lastIndex + 1) : resname;
            if (pattern.matcher(filename).matches()) {
                result.add(resname);
            }
        }
    }

    @Override
    public Bundle getBundle() {
        return getRevision().getBundle();
    }

    static class AbstractHostedBundleCapability extends AbstractHostedCapability implements BundleCapability {

        AbstractHostedBundleCapability(XResource resource, XCapability capability) {
            super(resource, capability);
        }

        @Override
        public XBundleRevision getRevision() {
            return (XBundleRevision) super.getResource();
        }

        @Override
        public XBundleRevision getResource() {
            return (XBundleRevision) super.getResource();
        }
    }
}
