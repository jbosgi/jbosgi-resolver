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
/*
 * Copyright (c) OSGi Alliance (2011, 2012). All Rights Reserved.
 *
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
 */

package org.osgi.resource;

import java.util.List;

/**
 * A resource is the representation of a uniquely identified and typed data. A
 * resource declares requirements that need to be satisfied by capabilities
 * before it can provide its capabilities.
 * 
 * <p>
 * Instances of this type must be <i>effectively immutable</i>. That is, for a
 * given instance of this interface, the methods defined by this interface must
 * always return the same result.
 * 
 * @ThreadSafe
 * @version $Id: 40958d5777ee269d27d58e9f646a4c91bcc6daa4 $
 */
public interface Resource {
	/**
	 * Returns the capabilities declared by this resource.
	 * 
	 * @param namespace The namespace of the declared capabilities to return or
	 *        {@code null} to return the declared capabilities from all
	 *        namespaces.
	 * @return An unmodifiable list containing the declared {@link Capability}s
	 *         from the specified namespace. The returned list will be empty if
	 *         this resource declares no capabilities in the specified
	 *         namespace.
	 */
	List<Capability> getCapabilities(String namespace);

	/**
	 * Returns the requirements declared by this bundle resource.
	 * 
	 * @param namespace The namespace of the declared requirements to return or
	 *        {@code null} to return the declared requirements from all
	 *        namespaces.
	 * @return An unmodifiable list containing the declared {@link Requirement}
	 *         s from the specified namespace. The returned list will be empty
	 *         if this resource declares no requirements in the specified
	 *         namespace.
	 */
	List<Requirement> getRequirements(String namespace);

	/**
	 * Compares this {@code Resource} to another {@code Resource}.
	 * 
	 * <p>
	 * This {@code Resource} is equal to another {@code Resource} if both have
	 * the same content and come from the same location. Location may be defined
	 * as the bundle location if the resource is an installed bundle or the
	 * repository location if the resource is in a repository.
	 * 
	 * @param obj The object to compare against this {@code Resource}.
	 * @return {@code true} if this {@code Resource} is equal to the other
	 *         object; {@code false} otherwise.
	 */
	boolean equals(Object obj);

	/**
	 * Returns the hashCode of this {@code Resource}.
	 * 
	 * @return The hashCode of this {@code Resource}.
	 */
	int hashCode();
}
