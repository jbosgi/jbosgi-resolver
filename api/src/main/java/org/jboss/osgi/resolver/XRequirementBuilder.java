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

import java.util.Map;

import org.jboss.modules.ModuleIdentifier;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;

/**
 * A builder for resource requirements
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public final class XRequirementBuilder {

    private final XResourceBuilder<XResource> resbuilder;
    private final XRequirement requirement;

    public static XRequirementBuilder create(ModuleIdentifier modid) {
        XRequirementBuilder reqbuilder = createInternal(XResource.MODULE_IDENTITY_NAMESPACE, modid.toString(), false);
        XCapability icap = reqbuilder.resbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, modid.getName());
        try {
            Version version = Version.parseVersion(modid.getSlot());
            icap.getAttributes().put("version", version);
        } catch (RuntimeException ex) {
            icap.getAttributes().put("slot", modid.getSlot());
        }
        return reqbuilder;
    }

    public static XRequirementBuilder create(MavenCoordinates mvnid) {
        XRequirementBuilder reqbuilder = createInternal(XResource.MAVEN_IDENTITY_NAMESPACE, mvnid.toExternalForm(), false);
        String nsvalue;
        Version version = null;
        try {
            version = Version.parseVersion(mvnid.getVersion());
            nsvalue = mvnid.getGroupId() + ":" + mvnid.getArtifactId();
        } catch (RuntimeException ex) {
            nsvalue = mvnid.getGroupId() + ":" + mvnid.getArtifactId() + ":" + mvnid.getVersion();
        }
        XCapability icap = reqbuilder.resbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, nsvalue);
        icap.getAttributes().put("type", mvnid.getType());
        if (mvnid.getClassifier() != null)
            icap.getAttributes().put("classifier", mvnid.getClassifier());
        if (version != null) 
            icap.getAttributes().put("version", version);
        return reqbuilder;
    }

    public static XRequirementBuilder create(String namespace) {
        return createInternal(namespace, null, true);
    }

    public static XRequirementBuilder create(String namespace, String nsvalue) {
        return createInternal(namespace, nsvalue, true);
    }

    private static XRequirementBuilder createInternal(String namespace, String nsvalue, boolean addIdentity) {
        XResourceBuilder<XResource> resbuilder = XResourceBuilderFactory.create();
        XRequirement req = resbuilder.addRequirement(namespace, nsvalue);
        if (addIdentity) {
            resbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "anonymous");
        }
        return new XRequirementBuilder(resbuilder, req);
    }
    
    private XRequirementBuilder(XResourceBuilder<XResource> resbuilder, XRequirement requirement) {
        this.resbuilder = resbuilder;
        this.requirement = requirement;
    }

    public Map<String, Object> getAttributes() {
        return requirement.getAttributes();
    }

    public Map<String, String> getDirectives() {
        return requirement.getDirectives();
    }

    public XRequirement getRequirement() {
        XResource resource = resbuilder.getResource();
        String namespace = requirement.getNamespace();
        return (XRequirement) resource.getRequirements(namespace).get(0);
    }

}
