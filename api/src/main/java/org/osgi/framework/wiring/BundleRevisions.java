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

package org.osgi.framework.wiring;

import java.util.List;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * The {@link BundleRevision bundle revisions} of a bundle. When a bundle is
 * installed and each time a bundle is updated, a new bundle revision of the
 * bundle is created. For a bundle that has not been uninstalled, the most
 * recent bundle revision is defined to be the current bundle revision. A bundle
 * in the UNINSTALLED state does not have a current revision. An in use bundle
 * revision is associated with an {@link BundleWiring#isInUse() in use}
 * {@link BundleWiring}. The current bundle revision, if there is one, and all
 * in use bundle revisions are returned.
 * 
 * <p>
 * The bundle revisions for a bundle can be obtained by calling
 * {@link Bundle#adapt(Class) bundle.adapt}({@link BundleRevisions}.class).
 * {@link #getRevisions()} on the bundle.
 * 
 * @ThreadSafe
 * @noimplement
 * @version $Id: 8423242078417873faf0f8979e153e3c1f3a0e4b $
 */
public interface BundleRevisions extends BundleReference {
	/**
	 * Return the bundle revisions for the {@link BundleReference#getBundle()
	 * referenced} bundle.
	 * 
	 * <p>
	 * The result is a list containing the current bundle revision, if there is
	 * one, and all in use bundle revisions. The list may also contain
	 * intermediate bundle revisions which are not in use.
	 * 
	 * <p>
	 * The list is ordered in reverse chronological order such that the first
	 * item is the most recent bundle revision and last item is the oldest
	 * bundle revision.
	 * 
	 * <p>
	 * Generally the list will have at least one bundle revision for the bundle:
	 * the current bundle revision. However, for an uninstalled bundle with no
	 * in use bundle revisions, the list may be empty.
	 * 
	 * @return A list containing a snapshot of the {@link BundleRevision}s for
	 *         the referenced bundle.
	 */
	List<BundleRevision> getRevisions();
}
