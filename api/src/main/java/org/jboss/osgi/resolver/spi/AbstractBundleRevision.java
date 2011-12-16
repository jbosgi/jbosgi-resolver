/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.resolver.spi;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_FRAGMENT;

/**
 * The abstract implementation of a {@link BundleRevision}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractBundleRevision extends AbstractElement implements XResource, BundleRevision {

    private final XResource delegate;
    private final Map<String, List<BundleCapability>> capabilities = new HashMap<String, List<BundleCapability>>();
    private final Map<String, List<BundleRequirement>> requirements = new HashMap<String, List<BundleRequirement>>();
    private final int types;

    public AbstractBundleRevision(XResource resource) {
        if (resource == null)
            throw new IllegalArgumentException("Null resource");
        
        delegate = resource;

        resource.addAttachment(BundleRevision.class, this);
        addAttachment(Resource.class, resource);
        
        for(Capability cap : resource.getCapabilities(null)) {
            BundleCapability bcap = new AbstractBundleCapability((XCapability) cap);
            getCaplist(cap.getNamespace()).add(bcap);
            getCaplist(null).add(bcap);
        } 
        for(Requirement req : resource.getRequirements(null)) {
            BundleRequirement breq = new AbstractBundleRequirement((XRequirement) req);
            getReqlist(req.getNamespace()).add(breq);
            getReqlist(null).add(breq);
        }

        XIdentityCapability idcap = delegate.getIdentityCapability();
        boolean isfragment = IDENTITY_TYPE_FRAGMENT.equals(idcap.getType());
        types = isfragment ? TYPE_FRAGMENT : 0;
    }

    @Override
    public XIdentityCapability getIdentityCapability() {
        return delegate.getIdentityCapability();
    }

    @Override
    public List<Capability> getCapabilities(String namespace) {
        return delegate.getCapabilities(namespace);
    }

    @Override
    public List<Requirement> getRequirements(String namespace) {
        return delegate.getRequirements(namespace);
    }

    @Override
    public String getSymbolicName() {
        XIdentityCapability cap = delegate.getIdentityCapability();
        return cap != null ? cap.getSymbolicName() : null;
    }

    @Override
    public Version getVersion() {
        XIdentityCapability cap = delegate.getIdentityCapability();
        return cap != null ? cap.getVersion() : null;
    }

    @Override
    public List<BundleCapability> getDeclaredCapabilities(String namespace) {
        return Collections.unmodifiableList(getCaplist(namespace));
    }

    @Override
    public List<BundleRequirement> getDeclaredRequirements(String namespace) {
        return Collections.unmodifiableList(getReqlist(namespace));
    }

    @Override
    public int getTypes() {
        return types;
    }

    @Override
    public BundleWiring getWiring() {
        return getAttachment(BundleWiring.class);
    }

    @Override
    public Bundle getBundle() {
        return adapt(Bundle.class);
    }

    private List<BundleCapability> getCaplist(String namespace) {
        List<BundleCapability> caplist = capabilities.get(namespace);
        if (caplist == null) {
            caplist = new ArrayList<BundleCapability>();
            capabilities.put(namespace, caplist);
        }
        return caplist;
    }

    private List<BundleRequirement> getReqlist(String namespace) {
        List<BundleRequirement> reqlist = requirements.get(namespace);
        if (reqlist == null) {
            reqlist = new ArrayList<BundleRequirement>();
            requirements.put(namespace, reqlist);
        }
        return reqlist;
    }
    
    public String toString() {
        return getClass().getSimpleName() + ":" + delegate;
    }
}