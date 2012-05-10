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

import java.util.Map;

/**
 * A capability that has been declared from a {@link Resource}.
 * 
 * <p>
 * Instances of this type must be <i>effectively immutable</i>. That is, for a
 * given instance of this interface, the methods defined by this interface must
 * always return the same result.
 * 
 * @ThreadSafe
 * @version $Id: 5f40514f7bf45f6dce59651e8812b0922580e77e $
 */
public interface Capability {

	/**
	 * Returns the namespace of this capability.
	 * 
	 * @return The namespace of this capability.
	 */
	String getNamespace();

	/**
	 * Returns the directives of this capability.
	 * 
	 * @return An unmodifiable map of directive names to directive values for
	 *         this capability, or an empty map if this capability has no
	 *         directives.
	 */
	Map<String, String> getDirectives();

	/**
	 * Returns the attributes of this capability.
	 * 
	 * @return An unmodifiable map of attribute names to attribute values for
	 *         this capability, or an empty map if this capability has no
	 *         attributes.
	 */
	Map<String, Object> getAttributes();

	/**
	 * Returns the resource declaring this capability.
	 * 
	 * @return The resource declaring this capability.
	 */
	Resource getResource();

	/**
	 * Compares this {@code Capability} to another {@code Capability}.
	 * 
	 * <p>
	 * This {@code Capability} is equal to another {@code Capability} if they
	 * have the same namespace, directives and attributes and are declared by
	 * the same resource.
	 * 
	 * @param obj The object to compare against this {@code Capability}.
	 * @return {@code true} if this {@code Capability} is equal to the other
	 *         object; {@code false} otherwise.
	 */
	boolean equals(Object obj);

	/**
	 * Returns the hashCode of this {@code Capability}.
	 * 
	 * @return The hashCode of this {@code Capability}.
	 */
	int hashCode();
}
