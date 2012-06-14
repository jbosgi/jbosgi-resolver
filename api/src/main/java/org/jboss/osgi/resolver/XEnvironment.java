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

import static org.osgi.framework.namespace.IdentityNamespace.TYPE_BUNDLE;
import static org.osgi.framework.namespace.IdentityNamespace.TYPE_FRAGMENT;
import static org.osgi.framework.namespace.IdentityNamespace.TYPE_UNKNOWN;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;

/**
 * An environment that hosts the resources applicable for resource resolution.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XEnvironment {

    String[] ALL_IDENTITY_TYPES = new String[] { TYPE_BUNDLE, TYPE_FRAGMENT, TYPE_UNKNOWN };
    
    void installResources(XResource... resources);

    void uninstallResources(XResource... resources);

    void refreshResources(XResource... resources);

    Collection<XResource> getResources(String... types);

    Long nextResourceIdentifier(Long value, String symbolicName);

    List<Capability> findProviders(Requirement req);

    Map<Resource, Wiring> updateWiring(Map<Resource, List<Wire>> delta);

    Wiring createWiring(XResource res, List<Wire> required, List<Wire> provided);
    
    Map<Resource, Wiring> getWirings();
}