/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.resolver.v2.spi;

import org.jboss.osgi.resolver.v2.XAttributeSupport;
import org.jboss.osgi.resolver.v2.XCapability;
import org.jboss.osgi.resolver.v2.XDirectiveSupport;
import org.jboss.osgi.resolver.v2.XRequirement;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.resource.Capability;
import org.osgi.framework.resource.Resource;
import org.osgi.framework.resource.ResourceConstants;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.osgi.framework.resource.ResourceConstants.REQUIREMENT_RESOLUTION_DIRECTIVE;
import static org.osgi.framework.resource.ResourceConstants.REQUIREMENT_RESOLUTION_OPTIONAL;

/**
 * The abstract implementation of a {@link XRequirement}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractRequirement extends AbstractElement implements XRequirement {

    private final Resource resource;
    private final String namespace;
    private final XAttributeSupport attributes;
    private final XDirectiveSupport directives;
    private final boolean optional;
    private final Filter filter;

    protected AbstractRequirement(Resource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        if (resource == null)
            throw new IllegalArgumentException("Null resource");
        if (namespace == null)
            throw new IllegalArgumentException("Null namespace");
        if (atts == null)
            throw new IllegalArgumentException("Null attributes");
        if (dirs == null)
            throw new IllegalArgumentException("Null directives");

        this.resource = resource;
        this.namespace = namespace;
        this.attributes = new AttributeSupporter(atts);
        this.directives = new DirectiveSupporter(dirs);

        String resdir = dirs.get(REQUIREMENT_RESOLUTION_DIRECTIVE);
        optional = REQUIREMENT_RESOLUTION_OPTIONAL.equals(resdir);

        String filterdir = getDirective(ResourceConstants.REQUIREMENT_FILTER_DIRECTIVE);
        if (filterdir != null) {
            try {
                filter = FrameworkUtil.createFilter(filterdir);
            } catch (InvalidSyntaxException e) {
                throw new IllegalArgumentException("Invalid filter directive: " + filterdir);
            }
        } else {
            filter = null;
        }
        
        
        validateAttributes(atts);
    }

    protected void validateAttributes(Map<String, Object> atts) {
        for (String name : getMandatoryAttributes()) {
            if (atts.get(name) == null)
                throw new IllegalArgumentException("Cannot obtain attribute: " + name);
        }
    }

    protected abstract List<String> getMandatoryAttributes();

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
    public boolean matches(Capability cap) {
        String namespace = getNamespace();
        boolean matches = namespace.equals(cap.getNamespace());

        // match namespace value
        if (matches) {
            XCapability xcap = (XCapability) cap;
            Object thisatt = getAttribute(namespace);
            Object otheratt = xcap.getAttribute(namespace);
            matches = thisatt.equals(otheratt);
        }

        // match filter
        if (matches && filter != null) {
            Dictionary dict = new Hashtable(cap.getAttributes());
            matches = filter.match(dict);
        }

        return matches;
    }
    
    public String toString() {
        String attstr = !getAttributes().isEmpty() ? ",attributes=" + attributes : "";
        String dirstr = !getDirectives().isEmpty() ? ",directives=" + directives : "";
        return getClass().getSimpleName() + "[" + namespace + attstr + dirstr + "]";
    }
}