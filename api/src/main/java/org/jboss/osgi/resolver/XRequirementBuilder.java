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
package org.jboss.osgi.resolver;

import org.jboss.modules.ModuleIdentifier;
import org.osgi.resource.Requirement;

import java.util.HashMap;
import java.util.Map;

import static org.jboss.osgi.resolver.XResourceBuilder.EMPTY_DIRECTIVES;
import static org.jboss.osgi.resolver.XResourceConstants.MAVEN_IDENTITY_NAMESPACE;
import static org.jboss.osgi.resolver.XResourceConstants.MODULE_IDENTITY_NAMESPACE;

/**
 * A builder for resource requirements
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class XRequirementBuilder {

    public static Requirement createArtifactRequirement(ModuleIdentifier moduleId) {
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put(MODULE_IDENTITY_NAMESPACE, moduleId.toString());
        return createRequirement(MODULE_IDENTITY_NAMESPACE, atts, EMPTY_DIRECTIVES);
    }

    public static Requirement createArtifactRequirement(MavenCoordinates coordinates) {
        Map<String, Object> atts = new HashMap<String, Object>();
        atts.put(MAVEN_IDENTITY_NAMESPACE, coordinates.toExternalForm());
        return createRequirement(MAVEN_IDENTITY_NAMESPACE, atts, EMPTY_DIRECTIVES);
    }

    public static Requirement createRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        XResourceBuilder builder = XResourceBuilderFactory.create();
        return builder.addGenericRequirement(namespace, atts, dirs);
    }
}