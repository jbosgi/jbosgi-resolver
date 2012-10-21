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

import static org.jboss.osgi.resolver.ResolverMessages.MESSAGES;
import static org.osgi.framework.namespace.AbstractWiringNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.BundleNamespace.BUNDLE_NAMESPACE;
import static org.osgi.framework.namespace.HostNamespace.HOST_NAMESPACE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

import java.util.Map;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Version;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;
import org.osgi.service.resolver.HostedCapability;

/**
 * The abstract implementation of a {@link HostedCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 29-Jun-2012
 */
public class AbstractHostedCapability extends AbstractElement implements HostedCapability, XPackageCapability {

    private final XResource resource;
    private final XCapability capability;

    public AbstractHostedCapability(XResource resource, XCapability capability) {
        if (capability == null)
            throw MESSAGES.illegalArgumentNull("capability");
        this.resource = resource;
        this.capability = capability;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    public boolean isMutable() {
        return false;
    }

    @Override
    public Capability getDeclaredCapability() {
        return capability;
    }

    @Override
    public String getNamespace() {
        return capability.getNamespace();
    }


    @Override
    public Map<String, String> getDirectives() {
        return capability.getDirectives();
    }


    @Override
    public Map<String, Object> getAttributes() {
        return capability.getAttributes();
    }

    @Override
    public Object getAttribute(String key) {
        return capability.getAttribute(key);
    }

    @Override
    public String getDirective(String key) {
        return capability.getDirective(key);
    }

    @Override
    public void validate() {
        capability.validate();
    }

    @Override
    public String getPackageName() {
        String result = null;
        if (PACKAGE_NAMESPACE.equals(getNamespace())) {
            result = (String) getAttribute(PACKAGE_NAMESPACE);
        }
        return result;
    }

    @Override
    public Version getVersion() {
        Version result = null;
        if (HOST_NAMESPACE.equals(getNamespace())) {
            result = AbstractCapability.getVersion(capability, CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        } else if (BUNDLE_NAMESPACE.equals(getNamespace())) {
            result = AbstractCapability.getVersion(capability, CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends XCapability> T adapt(Class<T> clazz) {
        T result = null;
        if (XPackageCapability.class == clazz && PACKAGE_NAMESPACE.equals(getNamespace())) {
            result = (T) this;
        }
        return result;
    }

    @Override
    public String toString() {
        return "HostedCapability[" + resource + "," + capability + "]";
    }
}
