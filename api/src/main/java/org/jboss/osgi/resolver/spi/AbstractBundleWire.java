package org.jboss.osgi.resolver.spi;
/*
 * #%L
 * JBossOSGi Framework
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

import static org.jboss.osgi.resolver.ResolverMessages.MESSAGES;

import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XBundleRequirement;
import org.jboss.osgi.resolver.XBundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Wire;

/**
 * The {@link BundleWire} implementation.
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2012
 */
public class AbstractBundleWire extends AbstractWire implements BundleWire {

    private BundleWiring providerWiring;
    private BundleWiring requirerWiring;

    public AbstractBundleWire(Wire wire) {
        super(wire.getCapability(), wire.getRequirement(), wire.getProvider(), wire.getRequirer());
    }

    @Override
    public BundleWiring getProviderWiring() {
        return getProviderWiring(true);
    }

    @Override
    public BundleWiring getRequirerWiring() {
        return getRequirerWiring(true);
    }

    public BundleWiring getProviderWiring(boolean checkInUse) {
        if (providerWiring != null) {
            return !checkInUse || providerWiring.isInUse() ? providerWiring : null;
        }
        return null;
    }

    public BundleWiring getRequirerWiring(boolean checkInUse) {
        if (requirerWiring != null) {
            return !checkInUse || requirerWiring.isInUse() ? requirerWiring : null;
        }
        return null;
    }

    public void setProviderWiring(BundleWiring providerWiring) {
        if (providerWiring == null)
            throw MESSAGES.illegalArgumentNull("providerWiring");
        this.providerWiring = providerWiring;
    }

    public void setRequirerWiring(BundleWiring requirerWiring) {
        if (providerWiring == null)
            throw MESSAGES.illegalArgumentNull("requirerWiring");
        this.requirerWiring = requirerWiring;
    }

    public XBundleRevision getProvider() {
        return (XBundleRevision) super.getProvider();
    }

    public XBundleRevision getRequirer() {
        return (XBundleRevision) super.getRequirer();
    }

    @Override
    public XBundleCapability getCapability() {
        return (XBundleCapability) super.getCapability();
    }

    @Override
    public XBundleRequirement getRequirement() {
        return (XBundleRequirement) super.getRequirement();
    }
}
