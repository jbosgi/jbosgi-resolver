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
package org.jboss.osgi.resolver;

import org.osgi.resource.Capability;

/**
 * An extension to the {@link Capability}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XCapability extends XElement, XAttributeSupport, XDirectiveSupport, Capability {

    /**
     * Get the resource declaring this capability.
     */
    XResource getResource();
    
    /**
     * Validate the capability
     */
    void validate();

    /**
     * Adapt this capability to another type
     */
    <T extends XCapability> T adapt(Class<T> clazz);
}
