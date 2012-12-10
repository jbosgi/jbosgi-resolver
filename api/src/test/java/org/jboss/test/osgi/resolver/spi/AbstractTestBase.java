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

package org.jboss.test.osgi.resolver.spi;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.spi.AbstractEnvironment;
import org.junit.Before;

/**
 * @author Thomas.Diesler@jboss.com
 */
public abstract class AbstractTestBase {

    private AbstractEnvironment environment;

    @Before
    public void setUp() {
        environment = new AbstractEnvironment();
    }

    XEnvironment installResources(XResource... resources) {
        environment.installResources(resources);
        return environment;
    }
}
