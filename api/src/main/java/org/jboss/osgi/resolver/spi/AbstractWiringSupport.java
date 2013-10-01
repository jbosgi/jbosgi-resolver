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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.osgi.resolver.XWiring;
import org.jboss.osgi.resolver.XWiringSupport;

/**
 * The abstract implementation of an {@link XWiringSupport}.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-Feb-2013
 * 
 * @ThreadSafe
 */
public class AbstractWiringSupport extends AbstractElement implements XWiringSupport {

    private final AtomicReference<XWiring> wiring = new AtomicReference<XWiring>();
    private final AtomicBoolean effective = new AtomicBoolean(true);

    @Override
    public boolean isEffective() {
        return effective.get();
    }

    @Override
    public void makeUneffective() {
        effective.set(false);
    }

    @Override
    public XWiring getWiring(boolean checkEffective) {
        XWiring effectiveWiring = checkEffective ? (isEffective() ? wiring.get() : null) : wiring.get();
        return effectiveWiring;
    }

    @Override
    public void setWiring(XWiring wiring) {
        this.wiring.set(wiring);
    }

    @Override
    public void refresh() {
        wiring.set(null);
    }
}
