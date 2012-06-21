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
import static org.osgi.framework.namespace.BundleNamespace.BUNDLE_NAMESPACE;
import static org.osgi.framework.namespace.HostNamespace.HOST_NAMESPACE;
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
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

/**
 * The abstract implementation of a {@link XCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractCapability extends AbstractElement implements XCapability {

    private final String namespace;
    private final XResource resource;
    private XAttributeSupport attributes;
    private XDirectiveSupport directives;

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
    public Resource getResource() {
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
        if (IDENTITY_NAMESPACE.equals(getNamespace())) {
            addAttachment(XIdentityCapability.class, new AbstractIdentityCapability(this));
        } else if (BUNDLE_NAMESPACE.equals(getNamespace())) {
            addAttachment(XResourceCapability.class, new AbstractResourceCapability(this));
        } else if (HOST_NAMESPACE.equals(getNamespace())) {
            addAttachment(XHostCapability.class, new AbstractHostCapability(this));
        } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
            addAttachment(XPackageCapability.class, new AbstractPackageCapability(this));
        }
    }

    @Override
    public <T extends XCapability> T adapt(Class<T> clazz) {
        return getAttachment(clazz);
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
    public String toString() {
        String attstr = "atts=" + attributes;
        String dirstr = !getDirectives().isEmpty() ? ",dirs=" + directives : "";
        XIdentityCapability icap = resource.getIdentityCapability();
        String resname = ",[" + (icap != null ? icap.getSymbolicName() + ":" + icap.getVersion() : "anonymous") + "]";
        return getClass().getSimpleName() + "[" + attstr + dirstr + resname + "]";
    }
}