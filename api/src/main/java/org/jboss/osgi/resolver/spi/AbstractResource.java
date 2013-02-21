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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XWiringSupport;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * The abstract implementation of an {@link XResource}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResource extends AbstractElement implements XResource {

    private final Map<String, List<Capability>> capabilities = new HashMap<String, List<Capability>>();
    private final Map<String, List<Requirement>> requirements = new HashMap<String, List<Requirement>>();
    private final AtomicBoolean mutable = new AtomicBoolean(true);
    private final XWiringSupport wirings = new AbstractWirings();
    private XIdentityCapability identityCapability;
    private Boolean fragment;

    protected void addCapability(Capability cap) {
        ensureMutable();
        String namespace = cap.getNamespace();
        getCaplist(namespace).add(cap);
        getCaplist(null).add(cap);
        if (IdentityNamespace.IDENTITY_NAMESPACE.equals(namespace)) {
            identityCapability = (XIdentityCapability) cap;
        }
    }

    protected void addRequirement(Requirement req) {
        ensureMutable();
        String namespace = req.getNamespace();
        getReqlist(namespace).add(req);
        getReqlist(null).add(req);
    }

    @Override
    public void makeImmutable() {
        mutable.set(false);
    }

    @Override
    public boolean isMutable() {
        return mutable.get();
    }

    public void ensureImmutable() {
        if (isMutable() == true)
            throw MESSAGES.illegalStateInvalidAccessToMutableResource();
    }

    public void ensureMutable() {
        if (isMutable() == false)
            throw MESSAGES.illegalStateInvalidAccessToImmutableResource();
    }

    @Override
    public XWiringSupport getWiringSupport() {
        return wirings;
    }

    @Override
    public void validate() {

        // identity
        List<Capability> caps = getCaplist(IdentityNamespace.IDENTITY_NAMESPACE);
        if (caps.size() > 1)
            throw MESSAGES.illegalStateMultipleIdentities(caps);
        if (caps.size() == 1) {
            XCapability cap = (XCapability) caps.get(0);
            cap.validate();
            identityCapability = cap.adapt(XIdentityCapability.class);
        }

        // Validate the capabilities
        for (Capability cap : getCaplist(null)) {
            if (cap != identityCapability) {
                try {
                    ((XCapability) cap).validate();
                } catch (RuntimeException ex) {
                    throw new ResourceValidationException(MESSAGES.validationInvalidCapability(cap), ex, cap);
                }
            }
        }

        // Validate the requirements
        for (Requirement req : getReqlist(null)) {
            try {
                ((XRequirement) req).validate();
            } catch (RuntimeException ex) {
                throw new ResourceValidationException(MESSAGES.validationInvalidRequirement(req), ex, req);
            }
        }

        // fragment
        List<Requirement> reqs = getReqlist(HostNamespace.HOST_NAMESPACE);
        fragment = new Boolean(reqs.size() > 0);
    }

    @Override
    public List<Capability> getCapabilities(String namespace) {
        ensureImmutable();
        return Collections.unmodifiableList(getCaplist(namespace));
    }

    @Override
    public List<Requirement> getRequirements(String namespace) {
        ensureImmutable();
        return Collections.unmodifiableList(getReqlist(namespace));
    }

    @Override
    public XIdentityCapability getIdentityCapability() {
        return identityCapability;
    }

    @Override
    public boolean isFragment() {
        return fragment.booleanValue();
    }

    private List<Capability> getCaplist(String namespace) {
        List<Capability> caplist = capabilities.get(namespace);
        if (caplist == null) {
            caplist = new ArrayList<Capability>();
            capabilities.put(namespace, caplist);
        }
        return caplist;
    }

    private List<Requirement> getReqlist(String namespace) {
        List<Requirement> reqlist = requirements.get(namespace);
        if (reqlist == null) {
            reqlist = new ArrayList<Requirement>();
            requirements.put(namespace, reqlist);
        }
        return reqlist;
    }

    @Override
    public String toString() {
        XIdentityCapability id = identityCapability;
        String idstr = (id != null ? id.getSymbolicName() + ":" + id.getVersion() : "anonymous");
        return getClass().getSimpleName() + "[" + idstr + "]";
    }
}
