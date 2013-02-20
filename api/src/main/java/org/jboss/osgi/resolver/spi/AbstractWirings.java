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

import org.jboss.osgi.resolver.XWirings;
import org.osgi.resource.Wiring;

/**
 * The abstract implementation of an {@link XWirings}.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-Feb-2013
 */
public class AbstractWirings extends AbstractElement implements XWirings {

    private Wiring current;
    private Wiring unresolved;

    @Override
    public synchronized Wiring getCurrent() {
        return current;
    }

    @Override
    public synchronized void setCurrent(Wiring wiring) {
        current = wiring;
    }

    @Override
    public synchronized void unresolve() {
        unresolved = current;
        current = null;
    }

    @Override
    public synchronized Wiring getUnresolved() {
        return unresolved;
    }

    @Override
    public synchronized void refresh() {
        unresolved = null;
        current = null;
    }
}
