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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

/**
 * Logging Id ranges: 10900-10999
 *
 * https://docs.jboss.org/author/display/JBOSGI/JBossOSGi+Logging
 *
 * @author Thomas.Diesler@jboss.com
 */
@MessageBundle(projectCode = "JBOSGI")
public interface ResolverMessages {

    ResolverMessages MESSAGES = Messages.getBundle(ResolverMessages.class);

    @Message(id = 10900, value = "%s is null")
    IllegalArgumentException illegalArgumentNull(String name);

    //@Message(id = 10901, value = "Cannot obtain attribute: %s")
    //IllegalArgumentException illegalArgumentCannotObtainAttribute(String name);

    @Message(id = 10902, value = "Invalid filter directive: %s")
    IllegalArgumentException illegalArgumentInvalidFilterDirective(String filter);

    @Message(id = 10903, value = "Invalid coordinates: %s")
    IllegalArgumentException illegalArgumentInvalidCoordinates(String coordinates);

    @Message(id = 10904, value = "Resource already installed: %s")
    IllegalStateException illegalStateResourceAlreadyInstalled(Resource resource);

    @Message(id = 10905, value = "Multiple identities detected: %s")
    IllegalStateException illegalStateMultipleIdentities(List<Capability> caps);

    @Message(id = 10906, value = "Resource not created")
    IllegalStateException illegalStateResourceNotCreated();

    @Message(id = 10907, value = "Invalid artifact URL: %s")
    IllegalStateException illegalStateInvalidArtifactURL(String urlspec);

    @Message(id = 10908, value = "Cannot initialize resource from: %s")
    ResourceBuilderException resourceBuilderCannotInitializeResource(@Cause Throwable cause, String input);

    @Message(id = 10909, value = "Invalid namespace: %s")
    IllegalArgumentException illegalArgumentInvalidNamespace(String namespace);

    @Message(id = 10910, value = "Cannot obtain attribute: %s")
    IllegalStateException illegalStateCannotObtainAttribute(String name);

    //@Message(id = 10911, value = "Cannot obtain namespace value for: %s")
    //IllegalStateException illegalStateCannotObtainNamespaceValue(String name);

    @Message(id = 10912, value = "Invalid access to mutable resource")
    IllegalStateException illegalStateInvalidAccessToMutableResource();

    @Message(id = 10913, value = "Invalid access to immutable resource")
    IllegalStateException illegalStateInvalidAccessToImmutableResource();

    @Message(id = 10914, value = "Invalid capability: %s")
    String validationInvalidCapability(Capability cap);

    @Message(id = 10915, value = "Invalid requirement: %s")
    String validationInvalidRequirement(Requirement req);

    @Message(id = 10916, value = "A requirement in namespace '%s' cannot have attributes: %s")
    IllegalArgumentException illegalArgumentRequirementCannotHaveAttributes(String namespace, Map<String, Object> atts);

    @Message(id = 10917, value = "A requirement in namespace '%s' must have a filter directive: %s")
    IllegalArgumentException illegalArgumentRequirementMustHaveFilterDirective(String namespace, Map<String, String> dirs);

    @Message(id = 10918, value = "Resolver hook unregistered while in resolve operation: %s")
    IllegalStateException illegalStateResolverHookUnregistered(ServiceReference<ResolverHookFactory> sref);

    @Message(id = 10919, value = "Resolver Hooks are not allowed to start another resolve operation")
    IllegalStateException illegalStateResolverHookCannotTriggerResolveOperation();

//    @Message(id = 10920, value = "Singleton capability %s collides with: %s")
//    IllegalStateException illegalStateSingletonCapabilityCollision(BundleCapability cap, Collection<BundleCapability> collisions);
}
