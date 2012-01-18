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
package org.jboss.osgi.resolver.v2.spi;

import org.jboss.osgi.resolver.v2.XIdentityCapability;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.osgi.framework.resource.ResourceConstants.IDENTITY_TYPE_FRAGMENT;

/**
 * The abstract implementation of a {@link BundleRevision}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractBundleRevision extends AbstractResource implements BundleRevision {

    @Override
    public String getSymbolicName() {
        XIdentityCapability cap = getIdentityCapability();
        return cap != null ? cap.getSymbolicName() : null;
    }

    @Override
    public Version getVersion() {
        XIdentityCapability cap = getIdentityCapability();
        return cap != null ? cap.getVersion() : null;
    }

    @Override
    public List<BundleCapability> getDeclaredCapabilities(String namespace) {
        List<Capability> capabilities = getCapabilities(namespace);
        List<BundleCapability> result = new ArrayList<BundleCapability>(capabilities.size());
        for (Capability cap : capabilities) {
            result.add((BundleCapability) cap);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<BundleRequirement> getDeclaredRequirements(String namespace) {
        List<Requirement> requirements = getRequirements(namespace);
        List<BundleRequirement> result = new ArrayList<BundleRequirement>(requirements.size());
        for (Requirement req : requirements) {
            result.add((BundleRequirement) req);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public int getTypes() {
        XIdentityCapability idcap = getIdentityCapability();
        boolean isfragment = IDENTITY_TYPE_FRAGMENT.equals(idcap.getType());
        return isfragment ? TYPE_FRAGMENT : 0;
    }

    @Override
    public BundleWiring getWiring() {
        return getAttachment(BundleWiring.class);
    }

    @Override
    public Bundle getBundle() {
        return getAttachment(Bundle.class);
    }

    @Override
    public InputStream getContent() {
        return null;
    }
}