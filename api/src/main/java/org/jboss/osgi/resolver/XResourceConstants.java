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

package org.jboss.osgi.resolver;

/**
 * Defines names for the attributes, directives and name spaces for
 * resources, capabilities and requirements in the context of the
 * JBoss Repository.
 * <p/>
 * The values associated with these keys are of type {@code String}, unless
 * otherwise indicated.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public interface XResourceConstants {

    /**
     * Artifact coordinates may be given in simple groupId:artifactId:version form,
     * or they may be fully qualified in the form groupId:artifactId:type:version[:classifier]
     */
    String MAVEN_IDENTITY_NAMESPACE = "maven.identity";

    /**
     * Artifact coordinates may be given by {@link org.jboss.modules.ModuleIdentifier}
     */
    String MODULE_IDENTITY_NAMESPACE = "module.identity";

    /**
     * An attribute on the content capability that represents the location of the resource
     * relative to the base url of the repository.
     */
    String CAPABILITY_PATH_ATTRIBUTE = "path";
}
