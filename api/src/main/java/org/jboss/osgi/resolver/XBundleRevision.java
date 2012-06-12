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

    XBundle getBundle();
    
    ModuleIdentifier getModuleIdentifier();
    
    ModuleClassLoader getModuleClassLoader();
    
    // [TODO] remove
    int getRevisionId();

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
}