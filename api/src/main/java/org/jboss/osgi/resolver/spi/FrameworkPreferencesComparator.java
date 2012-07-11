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

import static org.osgi.framework.namespace.BundleNamespace.BUNDLE_NAMESPACE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

import java.util.Map;

import org.jboss.osgi.resolver.XEnvironment;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.resource.Wiring;

/**
 * A comparator based on defined framework preferences.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class FrameworkPreferencesComparator extends ResourceIndexComparator {

    public FrameworkPreferencesComparator(XEnvironment environment) {
        super(environment);
    }

    @Override
    public int compare(Capability o1, Capability o2) {
        Resource res1 = o1.getResource();
        Resource res2 = o2.getResource();

        // prefer system bundle
        Long in1 = getResourceIndex((XResource) o1.getResource());
        Long in2 = getResourceIndex((XResource) o2.getResource());
        if (in1 == 0 || in2 == 0) {
            return (int)(in1 - in2);
        }

        Map<Resource, Wiring> wirings = getEnvironment().getWirings();
        Wiring w1 = wirings.get(res1);
        Wiring w2 = wirings.get(res2);

        // prefer wired
        if (w1 != null && w2 == null)
            return -1;
        if (w1 == null && w2 != null)
            return +1;

        // prefer higher package version
        String ns1 = o1.getNamespace();
        String ns2 = o1.getNamespace();
        if (PACKAGE_NAMESPACE.equals(ns1) && PACKAGE_NAMESPACE.equals(ns2)) {
            Version v1 = AbstractCapability.getVersion(o1, PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            Version v2 = AbstractCapability.getVersion(o2, PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            if (!v1.equals(v2))
                return v2.compareTo(v1);
        }

        // prefer higher resource version
        if (BUNDLE_NAMESPACE.equals(ns1) && BUNDLE_NAMESPACE.equals(ns2)) {
            Version v1 = AbstractCapability.getVersion(o1, BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
            Version v2 = AbstractCapability.getVersion(o2, BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
            if (!v1.equals(v2))
                return v2.compareTo(v1);
        }

        // prefer lower index
        return in1.compareTo(in2);
    }
}
