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
