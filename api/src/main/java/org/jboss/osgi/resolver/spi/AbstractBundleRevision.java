/*
 * #%L
 * JBossOSGi Resolver API
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

package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.metadata.OSGiMetaData.ANONYMOUS_BUNDLE_SYMBOLIC_NAME;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.osgi.resolver.XBundle;
import org.jboss.osgi.resolver.XBundleRevision;
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

    private final Map<String, List<BundleCapability>> capabilities = new HashMap<String, List<BundleCapability>>();
    private final Map<String, List<BundleRequirement>> requirements = new HashMap<String, List<BundleRequirement>>();

    @Override
    public String getSymbolicName() {
        String symbolicName = getIdentityCapability().getSymbolicName();
        return ANONYMOUS_BUNDLE_SYMBOLIC_NAME.equals(symbolicName) ? null : symbolicName;
    }

    @Override
    public Version getVersion() {
        return getIdentityCapability().getVersion();
    }

    @Override
    public List<BundleCapability> getDeclaredCapabilities(String namespace) {
        List<BundleCapability> result = capabilities.get(namespace);
        if (result == null) {
            result = new ArrayList<BundleCapability>();
            for (Capability cap : getCapabilities(namespace)) {
                result.add((BundleCapability) cap);
            }
            capabilities.put(namespace, result);
        }
        return result;
    }

    @Override
    public List<BundleRequirement> getDeclaredRequirements(String namespace) {
        List<BundleRequirement> result = requirements.get(namespace);
        if (result == null) {
            result = new ArrayList<BundleRequirement>();
            for (Requirement req : getRequirements(namespace)) {
                result.add((BundleRequirement) req);
            }
            requirements.put(namespace, result);
        }
        return result;
    }

    @Override
    public int getTypes() {
        return isFragment() ? TYPE_FRAGMENT : 0;
    }

    @Override
    public BundleWiring getWiring() {
        return (BundleWiring) getResourceWiring();
    }

    @Override
    public XBundle getBundle() {
        throw new UnsupportedOperationException();
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
