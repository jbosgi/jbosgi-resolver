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

package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;

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
    private XIdentityCapability identityCapability;
    private Boolean fragment;

    protected void addCapability(Capability cap) {
        ensureMutable();
        String namespace = cap.getNamespace();
        getCaplist(namespace).add(cap);
        getCaplist(null).add(cap);
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
            try {
                ((XCapability) cap).validate();
            } catch (RuntimeException ex) {
                throw new ResourceValidationException("Invalid capability", ex, cap);
            }
        }

        // Validate the requirements
        for (Requirement req : getReqlist(null)) {
            try {
                ((XRequirement) req).validate();
            } catch (RuntimeException ex) {
                throw new ResourceValidationException("Invalid requirement", ex, req);
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