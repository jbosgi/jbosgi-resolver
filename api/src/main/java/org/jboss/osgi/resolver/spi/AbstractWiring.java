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

import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;
import static org.osgi.framework.namespace.IdentityNamespace.IDENTITY_NAMESPACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XResource;
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
public class AbstractWiring implements Wiring {

    private final XResource resource;
    private final List<Wire> required;
    private final List<Wire> provided;

    public AbstractWiring(XResource resource, List<Wire> required, List<Wire> provided) {
        if (resource == null)
            throw MESSAGES.illegalArgumentNull("resource");
        this.resource = resource;
        List<Wire> emptywires = Collections.emptyList();
        this.required = required != null ? required : emptywires;
        this.provided = provided != null ? provided : emptywires;
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
                    caps.add(getHostedCapability((XCapability) cap));
            }
        }
        Iterator<Capability> capit = caps.iterator();
        while(capit.hasNext()) {
            Capability cap = capit.next();
            // Capabilities with {@link Namespace#CAPABILITY_EFFECTIVE_DIRECTIVE} 
            // not equal to {@link Namespace#EFFECTIVE_RESOLVE} are not returned
            String effdir = cap.getDirectives().get(Namespace.CAPABILITY_EFFECTIVE_DIRECTIVE);
            if (effdir != null && !effdir.equals(Namespace.EFFECTIVE_RESOLVE)) {
                capit.remove();
            }
            // A package declared to be both exported and imported, 
            // only one is selected and the other is discarded
            String capns = cap.getNamespace();
            Object capval = cap.getAttributes().get(capns);
            for (Wire wire : required) {
                Capability wirecap = wire.getCapability();
                Object wirecapval = wirecap.getAttributes().get(wirecap.getNamespace());
                if (capns.equals(wirecap.getNamespace()) && capval.equals(wirecapval)) {
                    capit.remove();
                }
            }
        }
        return caps;
    }
    
    protected HostedCapability getHostedCapability(XCapability cap) {
        return new AbstractHostedCapability(resource, cap);
    }

    @Override
    public List<Requirement> getResourceRequirements(String namespace) {
        List<Requirement> reqs = new ArrayList<Requirement>(resource.getRequirements(namespace));
        Iterator<Requirement> reqit = reqs.iterator();
        while(reqit.hasNext()) {
            Requirement req = reqit.next();
            // Requirements with {@link Namespace#CAPABILITY_EFFECTIVE_DIRECTIVE} 
            // not equal to {@link Namespace#EFFECTIVE_RESOLVE} are not returned
            String effdir = req.getDirectives().get(Namespace.CAPABILITY_EFFECTIVE_DIRECTIVE);
            if (effdir != null && !Namespace.EFFECTIVE_RESOLVE.equals(effdir)) {
                reqit.remove();
            }
            // A package declared to be optionally imported and is not
            // actually imported, the requirement must be discarded
            String resdir = req.getDirectives().get(Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
            if (Namespace.RESOLUTION_OPTIONAL.equals(resdir)) {
                String reqns = req.getNamespace();
                Object reqval = req.getAttributes().get(reqns);
                boolean packageWireFound = false;
                for (Wire wire : required) {
                    Capability wirecap = wire.getCapability();
                    Object wirecapval = wirecap.getAttributes().get(wirecap.getNamespace());
                    if (reqns.equals(wirecap.getNamespace()) && reqval.equals(wirecapval)) {
                        packageWireFound = true;
                        break;
                    }
                }
                if (!packageWireFound) {
                    reqit.remove();
                }
            }
        }
        return reqs;
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
