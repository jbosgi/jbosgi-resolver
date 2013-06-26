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
import static org.osgi.framework.namespace.IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE;
import static org.osgi.framework.namespace.IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

import java.util.Collections;
import java.util.Map;

import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XHostCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
import org.jboss.osgi.resolver.XProvidedCapability;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceCapability;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;

/**
 * The abstract implementation of a {@link XCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractCapability extends AbstractElement implements XIdentityCapability, XHostCapability, XPackageCapability, XResourceCapability, XProvidedCapability {

    private final String namespace;
    private final XResource resource;
    private XAttributeSupport attributes;
    private XDirectiveSupport directives;
    private String namespaceValue;
    private String canonicalName;
    private Version version;
    private boolean valid;

    public AbstractCapability(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        if (resource == null)
            throw MESSAGES.illegalArgumentNull("resource");
        if (namespace == null)
            throw MESSAGES.illegalArgumentNull("namespace");
        if (atts == null)
            throw MESSAGES.illegalArgumentNull("attributes");
        if (dirs == null)
            throw MESSAGES.illegalArgumentNull("directives");

        this.resource = resource;
        this.namespace = namespace;
        this.attributes = new AttributeSupporter(atts);
        this.directives = new DirectiveSupporter(dirs);
    }

    @Override
    public XResource getResource() {
        return resource;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public Map<String, String> getDirectives() {
        return isMutable() ? directives.getDirectives() : Collections.unmodifiableMap(directives.getDirectives());
    }

    @Override
    public String getDirective(String key) {
        return directives.getDirective(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return isMutable() ? attributes.getAttributes() : Collections.unmodifiableMap(attributes.getAttributes());
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.getAttribute(key);
    }

    private boolean isMutable() {
        return resource.isMutable();
    }
    
    @Override
    public void validate() {
        if (valid == false) {
            if (AbstractResource.identityNamespaces.contains(getNamespace())) {
                version = getVersion(this, CAPABILITY_VERSION_ATTRIBUTE);
                namespaceValue = (String) getAttribute(getNamespace());
                if (namespaceValue == null)
                    throw MESSAGES.illegalStateCannotObtainAttribute(getNamespace());
            } else if (BUNDLE_NAMESPACE.equals(getNamespace())) {
                version = getVersion(this, CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
                namespaceValue = (String) getAttribute(getNamespace());
                if (namespaceValue == null)
                    throw MESSAGES.illegalStateCannotObtainAttribute(getNamespace());
            } else if (HOST_NAMESPACE.equals(getNamespace())) {
                version = getVersion(this, CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
                namespaceValue = (String) getAttribute(getNamespace());
                if (namespaceValue == null)
                    throw MESSAGES.illegalStateCannotObtainAttribute(getNamespace());
            } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
                version = getVersion(this, CAPABILITY_VERSION_ATTRIBUTE);
                attributes.getAttributes().put(CAPABILITY_VERSION_ATTRIBUTE, version);
                namespaceValue = (String) getAttribute(getNamespace());
                if (namespaceValue == null)
                    throw MESSAGES.illegalStateCannotObtainAttribute(getNamespace());
            }
            canonicalName = toString();
            valid = true;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends XCapability> T adapt(Class<T> clazz) {
        T result = null;
        if (XIdentityCapability.class == clazz && AbstractResource.identityNamespaces.contains(getNamespace())) {
            result = (T) this;
        } else if (XResourceCapability.class == clazz && BUNDLE_NAMESPACE.equals(getNamespace())) {
            result = (T) this;
        } else if (XHostCapability.class == clazz && HOST_NAMESPACE.equals(getNamespace())) {
            result = (T) this;
        } else if (XPackageCapability.class == clazz && PACKAGE_NAMESPACE.equals(getNamespace())) {
            result = (T) this;
        }
        return result;
    }

    @Override
    public String getPackageName() {
        return namespaceValue;
    }

    @Override
    public String getName() {
        return namespaceValue;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    static Version getVersion(Capability cap, String attname) {
        Object attval = cap.getAttributes().get(attname);
        if (attval != null && !(attval instanceof Version)) {
            attval = new Version(attval.toString());
            cap.getAttributes().put(attname, attval);
        }
        return attval != null ? (Version)attval : Version.emptyVersion;
    }

    @Override
    public String getType() {
        String typeval = (String) getAttribute(CAPABILITY_TYPE_ATTRIBUTE);
        return typeval != null ? typeval : IdentityNamespace.TYPE_UNKNOWN;
    }

    @Override
    public boolean isSingleton() {
        return Boolean.parseBoolean(getDirective(IdentityNamespace.CAPABILITY_SINGLETON_DIRECTIVE));
    }

    @Override
    public String toString() {
        String result = canonicalName;
        if (result == null) {
            String type;
            String nsval = null;
            if (AbstractResource.identityNamespaces.contains(getNamespace())) {
                type = XIdentityCapability.class.getSimpleName();
            } else if (BUNDLE_NAMESPACE.equals(getNamespace())) {
                type = XResourceCapability.class.getSimpleName();
            } else if (HOST_NAMESPACE.equals(getNamespace())) {
                type = XHostCapability.class.getSimpleName();
            } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
                type = XPackageCapability.class.getSimpleName();
            } else {
                type = XProvidedCapability.class.getSimpleName();
                nsval = namespace;
            }
            StringBuffer buffer = new StringBuffer(type + "[");
            boolean addcomma = false;
            if (nsval != null) {
                buffer.append(nsval);
                addcomma = true;
            }
            if (!getAttributes().isEmpty()) {
                buffer.append(addcomma ? "," : "");
                buffer.append("atts=" + attributes);
                addcomma = true;
            }
            if (!getDirectives().isEmpty()) {
                buffer.append(addcomma ? "," : "");
                buffer.append("dirs=" + directives);
                addcomma = true;
            }
            XIdentityCapability icap = resource.getIdentityCapability();
            if (icap != null) {
                buffer.append(addcomma ? "," : "");
                buffer.append("[" + icap.getName() + ":" + icap.getVersion() + "]");
                addcomma = true;
            } else {
                buffer.append(addcomma ? "," : "");
                buffer.append("[anonymous]");
                addcomma = true;
            }
            buffer.append("]");
            result = buffer.toString();
        }
        return result;
    }
}
