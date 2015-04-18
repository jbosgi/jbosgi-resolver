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

import static org.jboss.osgi.resolver.ResolverMessages.MESSAGES;
import static org.osgi.framework.namespace.AbstractWiringNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.BundleNamespace.BUNDLE_NAMESPACE;
import static org.osgi.framework.namespace.HostNamespace.HOST_NAMESPACE;
import static org.osgi.framework.namespace.PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;
import static org.osgi.framework.namespace.PackageNamespace.RESOLUTION_DYNAMIC;
import static org.osgi.resource.Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XCapabilityRequirement;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XHostRequirement;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XIdentityRequirement;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.VersionRange;
import org.osgi.framework.namespace.AbstractWiringNamespace;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

/**
 * The abstract implementation of a {@link XRequirement}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractRequirement extends AbstractElement implements XHostRequirement, XPackageRequirement, XIdentityRequirement, XCapabilityRequirement {

    private final XResource resource;
    private final String namespace;
    private XAttributeSupport attributes;
    private XDirectiveSupport directives;
    private String canonicalName;
    private boolean optional;
    private Filter filter;
    private boolean valid;

    public AbstractRequirement(XResource resource, String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        if (resource == null)
            throw MESSAGES.illegalArgumentNull("resource");
        if (namespace == null)
            throw MESSAGES.illegalArgumentNull("namespace");
        if (atts == null)
            throw MESSAGES.illegalArgumentNull("attributes");
        if (dirs == null)
            throw MESSAGES.illegalArgumentNull("directives");

        this.resource = resource;
        this.namespace = namespace;
        this.attributes = new AttributeSupporter(atts);
        this.directives = new DirectiveSupporter(dirs);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public XResource getResource() {
        return resource;
    }

    static String getNamespaceValue(Requirement req) {
        return getNamespaceValue(req, null);
    }

    static String getNamespaceValue(Requirement req, StringBuffer operator) {
        return getValueFromFilter(getFilterFromDirective(req), req.getNamespace(), operator);
    }

    @Override
    public void validate() {
        if (!valid) {
            Map<String, Object> atts = attributes.getAttributes();
            Map<String, String> dirs = directives.getDirectives();

            // Attributes declared on Require-Capability will be visible in getAttributes, but attributes declared on
            // other manifest entries which map to osgi.wiring.* namespace requirements will not be visible in getAttributes.
            // There are instead used to form a generated filter directive which will be visible in getDirectives.
            if (namespace.startsWith("osgi.wiring.")) {
                if (!atts.isEmpty()) {
                    generateFilterDirective(namespace, atts, dirs);
                }
                if (!dirs.containsKey(Constants.FILTER_DIRECTIVE))
                    throw MESSAGES.illegalArgumentRequirementMustHaveFilterDirective(namespace, dirs);
            }

            filter = getFilterFromDirective(this);
            String resdir = getDirective(AbstractWiringNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
            optional = AbstractWiringNamespace.RESOLUTION_OPTIONAL.equals(resdir);
            canonicalName = toString();
            valid = true;
        }
    }

    public static Filter getFilterFromDirective(Requirement req) {
        String filterdir = req.getDirectives().get(AbstractWiringNamespace.REQUIREMENT_FILTER_DIRECTIVE);
        if (filterdir != null) {
            try {
                return FrameworkUtil.createFilter(filterdir);
            } catch (InvalidSyntaxException e) {
                throw MESSAGES.illegalArgumentInvalidFilterDirective(filterdir);
            }
        }
        return null;
    }

    public static String getValueFromFilter(Filter filter, String attrname, StringBuffer operator) {
        String result = null;
        if (filter != null) {
            String filterstr = filter.toString();
            int index = filterstr.indexOf("(" + attrname);
            if (index >= 0) {
                index += attrname.length() + 1;
                char ch = filterstr.charAt(index);
                while ("~<=>".indexOf(ch) >= 0) {
                    if (operator != null) {
                        operator.append(ch);
                    }
                    ch = filterstr.charAt(++index);
                }
                result = filterstr.substring(index);
                result = result.substring(0, result.indexOf(")"));
            }
        }
        return result;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public Map<String, String> getDirectives() {
        return isMutable() ? directives.getDirectives() : Collections.unmodifiableMap(directives.getDirectives());
    }

    @Override
    public String getDirective(String key) {
        return directives.getDirective(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return isMutable() ? attributes.getAttributes() : Collections.unmodifiableMap(attributes.getAttributes());
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.getAttribute(key);
    }

    private boolean isMutable() {
        return resource.isMutable();
    }

    private void assertImmutable() {
        if (isMutable())
            throw MESSAGES.illegalStateInvalidAccessToMutableResource();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends XRequirement> T adapt(Class<T> clazz) {
        T result = null;
        if (XIdentityRequirement.class == clazz && BUNDLE_NAMESPACE.equals(getNamespace())) {
            result = (T) this;
        } else if (XHostRequirement.class == clazz && HOST_NAMESPACE.equals(getNamespace())) {
            result = (T) this;
        } else if (XPackageRequirement.class == clazz && PACKAGE_NAMESPACE.equals(getNamespace())) {
            result = (T) this;
        }
        return result;
    }

    @Override
    public boolean matches(Capability cap) {
        assertImmutable();

        // The requirement matches the capability if their namespaces match and the filter is absent or matches the attributes.
        boolean matches = namespace.equals(cap.getNamespace()) && matchFilter(cap);

        if (matches) {
            if (BUNDLE_NAMESPACE.equals(getNamespace())) {
                matches = matchesResourceRequirement(cap);
            } else if (HOST_NAMESPACE.equals(getNamespace())) {
                matches = matchesHostRequirement(cap);
            } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
                matches = matchesPackageRequirement(cap);
            } else {
                Object reqval = getAttribute(getNamespace());
                Object capval = cap.getAttributes().get(getNamespace());
                matches = (reqval == null || reqval.equals(capval));
            }
        }

        return matches;
    }

    private boolean matchesResourceRequirement(Capability cap) {
        // cannot require itself
        if (getResource() == cap.getResource())
            return false;

        return matchesMandatoryDirective(cap);
    }

    private boolean matchesHostRequirement(Capability cap) {
        return matchesMandatoryDirective(cap);
    }

    private boolean matchesPackageRequirement(Capability cap) {
        return matchesMandatoryDirective(cap);
    }

    private boolean matchesMandatoryDirective(Capability cap) {
        // match mandatory attributes on the capability
        String dirstr = ((XCapability) cap).getDirective(Constants.MANDATORY_DIRECTIVE);
        if (dirstr != null) {
            for (String attname : dirstr.split("[,\\s]")) {
                String attval = getValueFromFilter(filter, attname, null);
                if (attval == null) {
                    return false;
                }
            }
        }
        return true;
    }

    static VersionRange getVersionRange(XRequirement req, String attr) {
        Object value = req.getAttribute(attr);
        return (value instanceof String) ? new VersionRange((String) value) : (VersionRange) value;
    }

    private boolean matchFilter(Capability cap) {
        Map<String, Object> capatts = cap.getAttributes();
        return filter != null ? filter.matches(capatts) : true;
    }

    @Override
    public String getVisibility() {
        return getDirective(BundleNamespace.REQUIREMENT_VISIBILITY_DIRECTIVE);
    }

    @Override
    public String getSymbolicName() {
        String result = null;
        if (HOST_NAMESPACE.equals(getNamespace())) {
            result = getNamespaceValue(this);
        }
        return result;
    }

    @Override
    public String getPackageName() {
        String result = null;
        if (PACKAGE_NAMESPACE.equals(getNamespace())) {
            result = getNamespaceValue(this);
        }
        return result;
    }

    @Override
    public VersionRange getVersionRange() {
        VersionRange result = null;
        if (HOST_NAMESPACE.equals(getNamespace()) || BUNDLE_NAMESPACE.equals(getNamespace())) {
            result = AbstractRequirement.getVersionRange(this, CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
            result = AbstractRequirement.getVersionRange(this, CAPABILITY_VERSION_ATTRIBUTE);
        }
        return result;
    }

    @Override
    public boolean isDynamic() {
        return RESOLUTION_DYNAMIC.equals(getDirective(REQUIREMENT_RESOLUTION_DIRECTIVE));
    }

    private void generateFilterDirective(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        List<String> parts = new ArrayList<String>();
        if (atts.containsKey(namespace)) {
            addAttributePart(atts, namespace, parts);
            addVersionRangePart(atts, Constants.BUNDLE_VERSION_ATTRIBUTE, parts);
            addVersionRangePart(atts, Constants.VERSION_ATTRIBUTE, parts);
            for (String key : new ArrayList<String>(atts.keySet())) {
                addAttributePart(atts, key, parts);
            }
            StringBuffer filterSpec = new StringBuffer(parts.remove(0));
            for (String part : parts) {
                filterSpec.insert(0, "(&");
                filterSpec.append(part + ")");
            }
            try {
                Filter filter = FrameworkUtil.createFilter(filterSpec.toString());
                dirs.put(Constants.FILTER_DIRECTIVE, filter.toString());
            } catch (InvalidSyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    private void addAttributePart(Map<String, Object> atts, String attrname, List<String> parts) {
        Object attrval = atts.remove(attrname);
        if (attrval instanceof String) {
            parts.add("(" + attrname + "=" + attrval + ")");
        }
    }

    private void addVersionRangePart(Map<String, Object> atts, String attrname, List<String> parts) {
        Object versionAtt = atts.remove(attrname);
        if (versionAtt instanceof VersionRange) {
            VersionRange versionRange = (VersionRange) versionAtt;
            parts.add(versionRange.toFilterString(attrname));
        } else if (versionAtt instanceof String) {
            VersionRange versionRange = new VersionRange((String) versionAtt);
            parts.add(versionRange.toFilterString(attrname));
        }
    }

    @Override
    public String toString() {
        String result = canonicalName;
        if (result == null) {
            String type;
            String nsval = null;
            if (BUNDLE_NAMESPACE.equals(getNamespace())) {
                type = XIdentityRequirement.class.getSimpleName();
            } else if (HOST_NAMESPACE.equals(getNamespace())) {
                type = XHostRequirement.class.getSimpleName();
            } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
                type = XPackageRequirement.class.getSimpleName();
            } else {
                type = XCapabilityRequirement.class.getSimpleName();
                nsval = namespace;
            }
            StringBuffer buffer = new StringBuffer(type + "[");
            boolean addcomma = false;
            if (nsval != null) {
                buffer.append(nsval);
                addcomma = true;
            }
            if (!getAttributes().isEmpty()) {
                buffer.append(addcomma ? "," : "");
                buffer.append("atts=" + attributes);
                addcomma = true;
            }
            if (!getDirectives().isEmpty()) {
                buffer.append(addcomma ? "," : "");
                buffer.append("dirs=" + directives);
                addcomma = true;
            }
            XIdentityCapability icap = resource.getIdentityCapability();
            if (icap != null) {
                buffer.append(addcomma ? "," : "");
                buffer.append("[" + icap.getName() + ":" + icap.getVersion() + "]");
                addcomma = true;
            } else {
                buffer.append(addcomma ? "," : "");
                buffer.append("[anonymous]");
                addcomma = true;
            }
            buffer.append("]");
            result = buffer.toString();
        }
        return result;
    }
}
