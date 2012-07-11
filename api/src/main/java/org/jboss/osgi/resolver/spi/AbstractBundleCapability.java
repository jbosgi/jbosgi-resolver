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

package org.jboss.osgi.resolver.spi;

import java.util.Map;

import org.jboss.osgi.resolver.XBundleCapability;
import org.jboss.osgi.resolver.XBundleRevision;
import org.jboss.osgi.resolver.XResource;

/**
 * The abstract implementation of an {@link XBundleCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 30-May-2012
 */
public class AbstractBundleCapability extends AbstractCapability implements XBundleCapability {

    public AbstractBundleCapability(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        super(resource, namespace, atts, dirs);
    }

    @Override
    public XBundleRevision getResource() {
        return (XBundleRevision) super.getResource();
    }

    @Override
    public XBundleRevision getRevision() {
        return (XBundleRevision) super.getResource();
    }
}
