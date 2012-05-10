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
 * Copyright (c) OSGi Alliance (2012). All Rights Reserved.
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

package org.osgi.service.resolver;

import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

/**
 * A capability hosted by a resource.
 * 
 * <p>
 * A HostedCapability is a Capability where the {@link #getResource()} method
 * returns a Resource that hosts this Capability instead of declaring it. This
 * is necessary for cases where the declaring Resource of a Capability does not
 * match the runtime state. For example, this is the case for fragments attached
 * to a host. Most fragment declared capabilities and requirements become hosted
 * by the host resource. Since a fragment can attach to multiple hosts, a single
 * capability can actually be hosted multiple times.
 * 
 * @ThreadSafe
 * @noimplement
 * @version $Id: db698baa07e2ee8b5467871239adb5f0806dc183 $
 */
public interface HostedCapability extends Capability {

	/**
	 * Return the Resource that hosts this Capability.
	 * 
	 * @return The Resource that hosts this Capability.
	 */
	Resource getResource();

	/**
	 * Return the Capability hosted by the Resource.
	 * 
	 * @return The Capability hosted by the Resource.
	 */
	Capability getDeclaredCapability();
}
