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

import java.util.Comparator;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResource;
import org.osgi.resource.Capability;

/**
 * A comparator that uses the provided resource index.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class ResourceIndexComparator implements Comparator<Capability> {

    private final XEnvironment environment;

    ResourceIndexComparator(XEnvironment environment) {
        this.environment = environment;
    }

    XEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public int compare(Capability o1, Capability o2) {
        Long in1 = getResourceIndex((XResource) o1.getResource());
        Long in2 = getResourceIndex((XResource) o2.getResource());
        return in1.compareTo(in2);
    }

    Long getResourceIndex(XResource res) {
        return res.getAttachment(Long.class);
    }
}
