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

import org.jboss.osgi.resolver.XWiring;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.Wire;
import org.osgi.framework.resource.Wiring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The abstract implementation of a {@link Wiring}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractWiring implements XWiring {

    private final Resource resource;
    private final List<Wire> required;
    private List<Wire> provided;

    public AbstractWiring(Resource resource, List<Wire> wires) {
        this.resource = resource;
        this.required = wires;
    }

    @Override
    public void addProvidedWire(Wire wire) {
        if (provided == null) {
            provided = new ArrayList<Wire>();
        }
        provided.add(wire);
    }

    @Override
    public List<Capability> getResourceCapabilities(String namespace) {
        return resource.getCapabilities(namespace);
    }

    @Override
    public List<Requirement> getResourceRequirements(String namespace) {
        return resource.getRequirements(namespace);
    }

    @Override
    public List<Wire> getProvidedResourceWires(String namespace) {
        List<Wire> result = new ArrayList<Wire>();
        if (provided != null) {
            for (Wire wire : provided) {
                Capability cap = wire.getCapability();
                if (namespace == null || namespace.equals(cap.getNamespace())) {
                    result.add(wire);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Wire> getRequiredResourceWires(String namespace) {
        List<Wire> result = new ArrayList<Wire>();
        if (required != null) {
            for (Wire wire : required) {
                Requirement req = wire.getRequirement();
                if (namespace == null || namespace.equals(req.getNamespace())) {
                    result.add(wire);
                }
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