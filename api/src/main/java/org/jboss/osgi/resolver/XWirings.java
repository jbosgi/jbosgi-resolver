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

import org.osgi.resource.Wiring;

/**
 * An extension to {@link Wiring}
 *
 * @author thomas.diesler@jboss.com
 * @since 18-Feb-2013
 */
public interface XWirings extends XElement {

    /**
     * Get the {@link Wiring} associated with this resource
     */
    Wiring getCurrent();

    /**
     * Set the {@link Wiring} associated with this resource
     */
    void setCurrent(Wiring wiring);

    /**
     * Remove the {@link Wiring} associated with this resource
     */
    void unresolve();

    /**
     * Get the list of all {@link Wiring}s associated with this resource
     */
    Wiring getUnresolved();

    /**
     * Refresh all {@link Wiring}s associated with this resource
     */
    void refresh();
}
