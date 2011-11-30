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

import org.jboss.osgi.resolver.XAttachmentSupport;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Requirement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The abstract implementation of an {@link org.jboss.osgi.resolver.XResource}.
 * 
 * This is the resolver representation of a {@link Bundle}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractResource extends AbstractElement implements XResource {

    private final Map<String, List<Capability>> capabilities;
    private final Map<String, List<Requirement>> requirements;
    private XAttachmentSupport attachments;

    public AbstractResource(Map<String, List<Capability>> capabilities, Map<String, List<Requirement>> requirements) {
        this.capabilities = capabilities;
        this.requirements = requirements;
    }

    @Override
    public List<Capability> getCapabilities(String namespace) {
        List<Capability> result = capabilities.get(namespace);
        return result == null ? Collections.<Capability>emptyList() : Collections.unmodifiableList(result);
    }

    @Override
    public List<Requirement> getRequirements(String namespace) {
        List<Requirement> result = requirements.get(namespace);
        return result == null ? Collections.<Requirement>emptyList() : Collections.unmodifiableList(result);
    }

    @Override
    public <T> T addAttachment(Class<T> clazz, T value) {
        if (attachments == null)
            attachments = new AttachmentSupporter();

        return attachments.addAttachment(clazz, value);
    }

    @Override
    public <T> T getAttachment(Class<T> clazz) {
        if (attachments == null)
            return null;

        return attachments.getAttachment(clazz);
    }

    @Override
    public <T> T removeAttachment(Class<T> clazz) {
        if (attachments == null)
            return null;

        return attachments.removeAttachment(clazz);
    }
}