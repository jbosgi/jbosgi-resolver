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

import static org.jboss.osgi.resolver.ResolverMessages.MESSAGES;
import static org.osgi.framework.namespace.IdentityNamespace.IDENTITY_NAMESPACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XWiring;
import org.jboss.osgi.resolver.XWire;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.resource.Wiring;
import org.osgi.service.resolver.HostedCapability;

/**
 * The abstract implementation of a {@link Wiring}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractWiring implements XWiring {

    private final XResource resource;
    private final List<Wire> required = new ArrayList<Wire>();
    private final List<Wire> provided = new ArrayList<Wire>();

    public AbstractWiring(XResource resource, List<Wire> reqwires, List<Wire> provwires) {
        if (resource == null)
            throw MESSAGES.illegalArgumentNull("resource");
        this.resource = resource;
        if (reqwires != null) {
            for (Wire wire : reqwires) {
                addRequiredWire(wire);
            }
        }
        if (provwires != null) {
            for (Wire wire : provwires) {
                addProvidedWire(wire);
            }
        }
    }

    @Override
    public void addRequiredWire(Wire wire) {
        if (wire instanceof AbstractWire) {
            ((XWire) wire).setRequirerWiring(this);
        }
        required.add(wire);
    }

    @Override
    public void addProvidedWire(Wire wire) {
        if (wire instanceof AbstractWire) {
            ((XWire) wire).setProviderWiring(this);
        }
        provided.add(wire);
    }

    @Override
    public List<Capability> getResourceCapabilities(String namespace) {
        List<Capability> caps = new ArrayList<Capability>(resource.getCapabilities(namespace));
        // Add capabilities from attached fragments
        for (Wire wire : getProvidedResourceWires(HostNamespace.HOST_NAMESPACE)) {
            for (Capability cap : wire.getRequirer().getCapabilities(null)) {
                if (IDENTITY_NAMESPACE.equals(cap.getNamespace()))
                    continue;
                if (namespace == null || namespace.equals(cap.getNamespace()))
                    caps.add(cap);
            }
        }

        // Remove unwanted caps
        Iterator<Capability> capit = caps.iterator();
        while (capit.hasNext()) {
            boolean removed = false;
            XCapability cap = (XCapability) capit.next();
            XResource res = (XResource) cap.getResource();

            // Capabilities with {@link Namespace#CAPABILITY_EFFECTIVE_DIRECTIVE}
            // not equal to {@link Namespace#EFFECTIVE_RESOLVE} are not returned
            String effdir = cap.getDirectives().get(Namespace.CAPABILITY_EFFECTIVE_DIRECTIVE);
            if (effdir != null && !effdir.equals(Namespace.EFFECTIVE_RESOLVE)) {
                capit.remove();
                removed = true;
            }

            // A package declared to be both exported and imported,
            // only one is selected and the other is discarded
            if (!removed) {
                String capns = cap.getNamespace();
                Object capval = cap.getAttributes().get(capns);
                for (Wire wire : required) {
                    Capability wirecap = wire.getCapability();
                    Object wirecapval = wirecap.getAttributes().get(wirecap.getNamespace());
                    if (capns.equals(wirecap.getNamespace()) && capval.equals(wirecapval)) {
                        capit.remove();
                        removed = true;
                        break;
                    }
                }
            }

            // Remove identity capability for fragments
            if (!removed && res.isFragment() && IDENTITY_NAMESPACE.equals(cap.getNamespace())) {
                capit.remove();
                removed = true;
            }
        }
        return Collections.unmodifiableList(caps);
    }

    protected HostedCapability getHostedCapability(XCapability cap) {
        return new AbstractHostedCapability(resource, cap);
    }

    @Override
    public List<Requirement> getResourceRequirements(String namespace) {
        List<Requirement> reqs = new ArrayList<Requirement>(resource.getRequirements(namespace));
        Iterator<Requirement> reqit = reqs.iterator();
        while (reqit.hasNext()) {
            XRequirement req = (XRequirement) reqit.next();
            // Requirements with {@link Namespace#CAPABILITY_EFFECTIVE_DIRECTIVE}
            // not equal to {@link Namespace#EFFECTIVE_RESOLVE} are not returned
            String effdir = req.getDirectives().get(Namespace.CAPABILITY_EFFECTIVE_DIRECTIVE);
            if (effdir != null && !Namespace.EFFECTIVE_RESOLVE.equals(effdir)) {
                reqit.remove();
            }
            // A package declared to be optionally imported and is not
            // actually imported, the requirement must be discarded
            String resdir = req.getDirectives().get(Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
            if (req.adapt(XPackageRequirement.class) != null && Namespace.RESOLUTION_OPTIONAL.equals(resdir)) {
                String packageName = ((XPackageRequirement) req).getPackageName();
                boolean packageWireFound = false;
                for (Wire wire : required) {
                    XPackageCapability wirecap = (XPackageCapability) wire.getCapability();
                    if (packageName.equals(wirecap.getPackageName())) {
                        packageWireFound = true;
                        break;
                    }
                }
                if (!packageWireFound) {
                    reqit.remove();
                }
            }
        }
        return Collections.unmodifiableList(reqs);
    }

    @Override
    public List<Wire> getProvidedResourceWires(String namespace) {
        List<Wire> result = new ArrayList<Wire>();
        for (Wire wire : provided) {
            Capability cap = wire.getCapability();
            if (namespace == null || namespace.equals(cap.getNamespace())) {
                result.add(wire);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Wire> getRequiredResourceWires(String namespace) {
        List<Wire> result = new ArrayList<Wire>();
        for (Wire wire : required) {
            Requirement req = wire.getRequirement();
            if (namespace == null || namespace.equals(req.getNamespace())) {
                result.add(wire);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "Wiring[" + resource + "]";
    }
}
