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

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.namespace.AbstractWiringNamespace;
import org.osgi.resource.Resource;

/**
 * The abstract implementation of a {@link XRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractRequirement extends AbstractElement implements XRequirement {

    private final XResource resource;
    private final String namespace;
    private XAttributeSupport attributes;
    private XDirectiveSupport directives;
    private boolean optional;
    private Filter filter;

    protected AbstractRequirement(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
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
    public void validate() {
        String filterdir = getDirective(AbstractWiringNamespace.REQUIREMENT_FILTER_DIRECTIVE);
        if (filterdir != null) {
            try {
                filter = FrameworkUtil.createFilter(filterdir);
            } catch (InvalidSyntaxException e) {
                throw MESSAGES.illegalArgumentInvalidFilterDirective(filterdir);
            }
        }
        for (String name : getMandatoryAttributes()) {
            if (getAttribute(name) == null) {
                if (!name.equals(namespace) || namespaceValueFromFilter(filter, namespace) == null)
                throw MESSAGES.illegalArgumentCannotObtainAttribute(name);
            }
        }
        attributes = new AttributeSupporter(Collections.unmodifiableMap(attributes.getAttributes()));
        directives = new DirectiveSupporter(Collections.unmodifiableMap(directives.getDirectives()));
        String resdir = getDirective(AbstractWiringNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
        optional = AbstractWiringNamespace.RESOLUTION_OPTIONAL.equals(resdir);
    }

    public static String namespaceValueFromFilter(Filter filter, String namespace) {
        String result = null;
        if (filter != null) {
            String filterstr = filter.toString();
            int index = filterstr.indexOf("(" + namespace + "=");
            if (index >= 0) {
                result = filterstr.substring(index + namespace.length() + 2);
                result = result.substring(0, result.indexOf(")"));
            }
        }
        return result;
    }

    protected List<String> getMandatoryAttributes() {
        return Collections.emptyList();
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
    public boolean isOptional() {
        return optional;
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
    public boolean matches(XCapability cap) {
        boolean matches = namespace.equals(cap.getNamespace());

        // match namespace value
        matches &= matchNamespaceValue(cap);

        // match filter
        matches &= matchFilterValue(cap);

        return matches;
    }

    VersionRange getVersionRange(String attr) {
        Object value = getAttribute(attr);
        return (value instanceof String) ? VersionRange.parse((String) value) : (VersionRange) value;
    }

    protected boolean matchNamespaceValue(XCapability cap) {
        Object thisatt = getAttribute(namespace);
        Object otheratt = cap.getAttribute(namespace);
        return thisatt.equals(otheratt);
    }

    protected boolean matchFilterValue(XCapability cap) {
        return filter != null ? filter.match(new Hashtable<String, Object>(cap.getAttributes())) : true;
    }

    public String toString() {
        String attstr = "atts=" + attributes;
        String dirstr = !getDirectives().isEmpty() ? ",dirs=" + directives : "";
        XIdentityCapability icap = ((XResource) getResource()).getIdentityCapability();
        String resname = ",[" + (icap != null ? icap.getSymbolicName() + ":" + icap.getVersion() : "anonymous") + "]";
        return getClass().getSimpleName() + "[" + attstr + dirstr + resname + "]";
    }
}