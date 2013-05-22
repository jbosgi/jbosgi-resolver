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

    public static XRequirementBuilder create(ModuleIdentifier moduleId) {
        XRequirementBuilder reqbuilder = createInternal(XResource.MODULE_IDENTITY_NAMESPACE, moduleId.getName());
        reqbuilder.resbuilder.addCapability(XResource.MODULE_IDENTITY_NAMESPACE, moduleId.getName());
        String npart = "(name=" + moduleId.getName() + ")";
        String spart = "(slot=" + moduleId.getSlot() + ")";
        String filter = "(&" + npart + spart + ")";
        reqbuilder.requirement.getDirectives().put("filter", filter);
        return reqbuilder;
    }

    public static XRequirementBuilder create(MavenCoordinates mavenId) {
        XRequirementBuilder reqbuilder = createInternal(XResource.MAVEN_IDENTITY_NAMESPACE, mavenId.getArtifactId());
        reqbuilder.resbuilder.addCapability(XResource.MAVEN_IDENTITY_NAMESPACE, mavenId.getArtifactId());
        String gpart = "(groupId=" + mavenId.getGroupId() + ")";
        String apart = "(artifactId=" + mavenId.getArtifactId() + ")";
        String tpart = "(type=" + mavenId.getType() + ")";
        // TODO support version ranges
        String vpart = "(version=" + mavenId.getVersion() + ")";
        String cpart = mavenId.getClassifier() != null ? "(classifier=" + mavenId.getClassifier() + ")" : "";
        String filter = "(&" + gpart + apart + tpart + vpart + cpart + ")";
        reqbuilder.requirement.getDirectives().put("filter", filter);
        return reqbuilder;
    }

    public static XRequirementBuilder create(String namespace) {
        XRequirementBuilder reqbuilder = createInternal(namespace, null);
        reqbuilder.resbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "anonymous");
        return reqbuilder;
    }

    public static XRequirementBuilder create(String namespace, String nsvalue) {
        XRequirementBuilder reqbuilder = createInternal(namespace, nsvalue);
        reqbuilder.resbuilder.addCapability(IdentityNamespace.IDENTITY_NAMESPACE, "anonymous");
        return reqbuilder;
    }

    private static XRequirementBuilder createInternal(String namespace, String nsvalue) {
        XResourceBuilder<XResource> resbuilder = XResourceBuilderFactory.create();
        XRequirement req = resbuilder.addRequirement(namespace, nsvalue);
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
