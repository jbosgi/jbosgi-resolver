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

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;

/**
 * The abstract implementation of a {@link Wire}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractWire implements Wire {

    private final Capability capability;
    private final Requirement requirement;
    private final Resource provider;
    private final Resource requirer;

    protected AbstractWire(Capability capability, Requirement requirement, Resource provider, Resource requirer) {
        this.capability = capability;
        this.requirement = requirement;
        this.provider = provider;
        this.requirer = requirer;
    }

    public Capability getCapability() {
        return capability;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public Resource getProvider() {
        return provider;
    }

    public Resource getRequirer() {
        return requirer;
    }

    @Override
    public String toString() {
        return "Wire[" + requirer + "{" + requirement + "} => " + provider + "{" + capability + "}]";
    }
}
