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

import org.jboss.osgi.spi.Attachable;
import org.osgi.framework.Bundle;

/**
 * An extension to {@link Bundle}
 *
 * @author thomas.diesler@jboss.com
 * @since 11-Jun-2012
 */
public interface XBundle extends Attachable, Bundle {

    /**
     * Get the current {@link XBundleRevision}
     */
    XBundleRevision getBundleRevision();

    /**
     * Get the bundle canonical name
     *
     * [symbolicName:version]
     */
    String getCanonicalName();

    /**
     * True if the bundle is resolved.
     */
    boolean isResolved();

    /**
     * True if the bundle is a fragment.
     */
    boolean isFragment();
}
