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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    void installResources(XResource... resources);

    void uninstallResources(XResource... resources);

    void refreshResources(XResource... resources);

    Collection<XResource> getResources(Set<String> types);

    Long getResourceIndex(XResource res);

    Long nextResourceIndex(Long min);

    List<Capability> findProviders(Requirement req);

    Map<Resource, Wiring> updateWiring(Map<Resource, List<Wire>> delta);

    Map<Resource, Wiring> getWirings();
}