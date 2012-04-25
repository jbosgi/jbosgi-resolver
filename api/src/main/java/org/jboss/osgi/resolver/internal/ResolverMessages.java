/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.osgi.resolver.internal;

import java.util.List;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.jboss.osgi.metadata.OSGiMetaData;
import org.jboss.osgi.resolver.ResourceBuilderException;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

/**
 * Logging Id ranges: 10900-10999
 *
 * https://docs.jboss.org/author/display/JBOSGI/JBossOSGi+Logging
 *
 * @author Thomas.Diesler@jboss.com
 */
@MessageBundle(projectCode = "JBOSGI")
public interface ResolverMessages {

    ResolverMessages MESSAGES = Messages.getBundle(ResolverMessages.class);

    @Message(id = 10900, value = "%s is null")
    IllegalArgumentException illegalArgumentNull(String name);

    @Message(id = 10901, value = "Cannot obtain attribute: %s")
    IllegalArgumentException illegalArgumentCannotObtainAttribute(String name);

    @Message(id = 10902, value = "Invalid filter directive: %s")
    IllegalArgumentException illegalArgumentInvalidFilterDirective(String filter);

    @Message(id = 10903, value = "Invalid coordinates: %s")
    IllegalArgumentException illegalArgumentInvalidCoordinates(String coordinates);

    @Message(id = 10904, value = "Resource already installed: %s")
    IllegalStateException illegalStateResourceAlreadyInstalled(Resource resource);

    @Message(id = 10905, value = "Multiple identities detected: %s")
    IllegalStateException illegalStateMultipleIdentities(List<Capability> caps);

    @Message(id = 10906, value = "Resource not created")
    IllegalStateException illegalStateResourceNotCreated();

    @Message(id = 10907, value = "Invalid artifact URL: %s")
    IllegalStateException illegalStateInvalidArtifactURL(String urlspec);

    @Message(id = 10908, value = "Cannot initialize resource from: %s")
    ResourceBuilderException resourceBuilderCannotInitializeResource(@Cause Throwable cause, String attributes);
}
