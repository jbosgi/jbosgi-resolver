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
package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XResource;
import org.osgi.resource.Resource;

/**
 * The abstract implementation of a {@link XCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractCapability extends AbstractElement implements XCapability {

    private final String namespace;
    private final Resource resource;
    private final XAttributeSupport attributes;
    private final XDirectiveSupport directives;
    private String toString;

    protected AbstractCapability(Resource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
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

        validateAttributes(atts);
    }

    protected void validateAttributes(Map<String, Object> atts) {
        for (String name : getMandatoryAttributes()) {
            if (atts.get(name) == null)
                throw MESSAGES.illegalArgumentCannotObtainAttribute(name);
        }
    }

    protected Set<String> getMandatoryAttributes() {
        return Collections.emptySet();
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

    public String toString() {
    	if (toString == null) {
            String attstr = "atts=" + attributes;
            String dirstr = !getDirectives().isEmpty() ? ",dirs=" + directives : "";
        	XIdentityCapability icap = ((XResource)getResource()).getIdentityCapability();
    		String resname = ",[" + (icap != null ? icap.getSymbolicName() + ":" + icap.getVersion() : "anonymous") + "]";
            toString = getClass().getSimpleName() + "[" + attstr + dirstr + resname + "]";
    	}
        return toString;
    }
}