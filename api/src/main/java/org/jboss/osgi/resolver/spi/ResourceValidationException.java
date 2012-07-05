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

import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;


/**
 * A resource validation exceptionbuilder for resources.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Jun-2012
 */
public class ResourceValidationException extends RuntimeException {

    private static final long serialVersionUID = 6996504110476446968L;
    
    private transient Capability invalidCapability;
    private transient Requirement invalidRequirement;

    ResourceValidationException(String message, Throwable cause, Capability cap) {
        super(message, cause);
        invalidCapability = cap;
    }

    ResourceValidationException(String message, Throwable cause, Requirement req) {
        super(message, cause);
        invalidRequirement = req;
    }

    public Capability getInvalidCapability() {
        return invalidCapability;
    }

    public Requirement getInvalidRequirement() {
        return invalidRequirement;
    }

    public String getOffendingInput() {
        String message = null;
        if (invalidCapability != null)
            message = invalidCapability.toString();
        else if (invalidRequirement != null)
            message = invalidRequirement.toString();
        return message;
    }
}