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

import org.jboss.osgi.spi.AttachmentKey;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Resource;

/**
 * An extension to {@link Resource}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XResource extends XElement, XAttributeSupport, Resource {

    /** The id attachment key */
    AttachmentKey<Long> RESOURCE_IDENTIFIER_KEY = AttachmentKey.create(Long.class);
    
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
     * Some predefined resource types
     */
    String TYPE_BUNDLE = IdentityNamespace.TYPE_BUNDLE;
    String TYPE_FRAGMENT = IdentityNamespace.TYPE_FRAGMENT;
    String TYPE_UNKNOWN = IdentityNamespace.TYPE_UNKNOWN;
    String TYPE_ABSTRACT = "abstract";
    String TYPE_MODULE = "module";
    String TYPE_MAVEN = "maven";
    
    /**
     * Get the identity capability for this resource
     */
    XIdentityCapability getIdentityCapability();

    /**
     * Validate the resource
     */
    void validate();

    /**
     * Get the current resource state
     */
    State getState();

    /**
     * True if the resource is mutable
     */
    boolean isMutable();

    /**
     * Make the resource immutable
     */
    void setMutable(boolean mutable);

    /**
     * Get the {@link XWiringSupport} associated with this resource
     */
    XWiringSupport getWiringSupport();

    /**
     * The resource state in the {@link XEnvironment}
     */
    enum State {
        INSTALLED, UNINSTALLED
    }
}
