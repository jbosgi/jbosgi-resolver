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

package org.jboss.osgi.resolver;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleIdentifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;

/**
 * An extension to {@link BundleRevision}
 *
 * @author thomas.diesler@jboss.com
 * @since 15-Mar-2012
 */
public interface XBundleRevision extends XResource, BundleRevision {

    /**
     * Get the associated {@link XBundle}
     */
    @Override
    XBundle getBundle();

    /**
     * Get the associated {@link ModuleIdentifier}
     * or null if the revision is not resolved
     */
    ModuleIdentifier getModuleIdentifier();

    /**
     * Get the associated {@link ModuleIdentifier}
     * or null if the revision is not resolved
     */
    ModuleClassLoader getModuleClassLoader();

    /**
     * @see {@link Bundle#getResource(String)}
     */
    URL getResource(String name);

    /**
     * @see {@link Bundle#getResources(String)}
     */
    Enumeration<URL> getResources(String name) throws IOException;

    /**
     * @see {@link Bundle#findEntries(String, String, boolean)}
     */
    Enumeration<URL> findEntries(String path, String filePattern, boolean recursive);

    /**
     * @see {@link Bundle#getEntry(String)}
     */
    URL getEntry(String path);

    /**
     * @see {@link Bundle#getEntryPaths(String)}
     */
    Enumeration<String> getEntryPaths(String path);

    /**
     * True if this revision is a fragment
     */
    boolean isFragment();
}
