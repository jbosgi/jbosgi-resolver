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
