/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;
import static org.osgi.framework.namespace.AbstractWiringNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.BundleNamespace.BUNDLE_NAMESPACE;
import static org.osgi.framework.namespace.HostNamespace.HOST_NAMESPACE;
import static org.osgi.framework.namespace.IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE;
import static org.osgi.framework.namespace.IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.IdentityNamespace.IDENTITY_NAMESPACE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

import java.util.Collections;
import java.util.Map;

import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XHostCapability;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageCapability;
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
public class AbstractCapability extends AbstractElement implements XIdentityCapability, XHostCapability, XPackageCapability, XResourceCapability {

    private final String namespace;
    private final XResource resource;
    private XAttributeSupport attributes;
    private XDirectiveSupport directives;
    private String namespaceValue;
    private String canonicalName;
    private Version version;

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
        return directives.getDirectives();
    }

    @Override
    public String getDirective(String key) {
        return directives.getDirective(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes.getAttributes();
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.getAttribute(key);
    }

    @Override
    boolean isMutable() {
        return resource.isMutable();
    }

    @Override
    public void validate() {
        attributes = new AttributeSupporter(Collections.unmodifiableMap(attributes.getAttributes()));
        directives = new DirectiveSupporter(Collections.unmodifiableMap(directives.getDirectives()));
        namespaceValue = (String)getAttribute(getNamespace());
        if (namespaceValue == null) 
            throw MESSAGES.illegalStateCannotObtainAttribute(getNamespace());
        if (IDENTITY_NAMESPACE.equals(getNamespace())) {
            version = getVersion(this, CAPABILITY_VERSION_ATTRIBUTE);
        } else if (BUNDLE_NAMESPACE.equals(getNamespace())) {
            version = getVersion(this, CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        } else if (HOST_NAMESPACE.equals(getNamespace())) {
            version = getVersion(this, CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
            version = getVersion(this, CAPABILITY_VERSION_ATTRIBUTE);
        }
        canonicalName = toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends XCapability> T adapt(Class<T> clazz) {
        T result = null;
        if (XIdentityCapability.class == clazz && IDENTITY_NAMESPACE.equals(getNamespace())) {
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
    public String getSymbolicName() {
        return namespaceValue;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    static Version getVersion(Capability cap, String attr) {
        Object versionatt = cap.getAttributes().get(attr);
        if (versionatt instanceof Version)
            return (Version) versionatt;
        else if (versionatt instanceof String)
            return Version.parseVersion((String) versionatt);
        else
            return Version.emptyVersion;
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
            String type = getClass().getSimpleName();
            if (IDENTITY_NAMESPACE.equals(getNamespace())) {
                type = XIdentityCapability.class.getSimpleName();
            } else if (BUNDLE_NAMESPACE.equals(getNamespace())) {
                type = XResourceCapability.class.getSimpleName();
            } else if (HOST_NAMESPACE.equals(getNamespace())) {
                type = XHostCapability.class.getSimpleName();
            } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
                type = XPackageCapability.class.getSimpleName();
            }
            String attstr = "atts=" + attributes;
            String dirstr = !getDirectives().isEmpty() ? ",dirs=" + directives : "";
            XIdentityCapability icap = resource.getIdentityCapability();
            String resname = ",[" + (icap != null ? icap.getSymbolicName() + ":" + icap.getVersion() : "anonymous") + "]";
            result = type + "[" + attstr + dirstr + resname + "]";
        }
        return result;
    }
}