/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
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
 * #L%
 */

package org.jboss.osgi.resolver;

import org.osgi.framework.VersionRange;

/**
 * A package requirement
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XPackageRequirement extends XRequirement {

    /**
     * The package name
     */
    String getPackageName();

    /**
     * The package version range.
     */
    VersionRange getVersionRange();

    /**
     * A flag indicating that this is a dynamic package requirement
     */
    boolean isDynamic();
}
