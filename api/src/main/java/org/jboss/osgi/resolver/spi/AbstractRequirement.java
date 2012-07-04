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

package org.jboss.osgi.resolver.spi;

import static org.jboss.osgi.resolver.internal.ResolverMessages.MESSAGES;
import static org.osgi.framework.namespace.AbstractWiringNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.BundleNamespace.BUNDLE_NAMESPACE;
import static org.osgi.framework.namespace.HostNamespace.HOST_NAMESPACE;
import static org.osgi.framework.namespace.PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;
import static org.osgi.framework.namespace.PackageNamespace.RESOLUTION_DYNAMIC;
import static org.osgi.resource.Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.jboss.osgi.metadata.VersionRange;
import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XHostRequirement;
import org.jboss.osgi.resolver.XIdentityCapability;
import org.jboss.osgi.resolver.XPackageRequirement;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResource;
import org.jboss.osgi.resolver.XResourceRequirement;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.AbstractWiringNamespace;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;

/**
 * The abstract implementation of a {@link XRequirement}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractRequirement extends AbstractElement implements XHostRequirement, XPackageRequirement, XResourceRequirement {

    private final XResource resource;
    private final String namespace;
    private XAttributeSupport attributes;
    private XDirectiveSupport directives;
    private String canonicalName;
    private boolean optional;
    private Filter filter;

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
    public Resource getResource() {
        return resource;
    }

    @Override
    boolean isMutable() {
        return resource.isMutable();
    }

    static String getNamespaceValue(XRequirement req) {
        String namespaceValue = (String) req.getAttribute(req.getNamespace());
        if (namespaceValue == null) {
            namespaceValue = namespaceValueFromFilter(getFilterFromDirective(req), req.getNamespace());
        }
        if (namespaceValue == null) {
            throw MESSAGES.illegalStateCannotObtainNamespaceValue(req.getNamespace());
        }
        return namespaceValue;
    }

    @Override
    public void validate() {
        filter = getFilterFromDirective(this);
        attributes = new AttributeSupporter(Collections.unmodifiableMap(attributes.getAttributes()));
        directives = new DirectiveSupporter(Collections.unmodifiableMap(directives.getDirectives()));
        String resdir = getDirective(AbstractWiringNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE);
        optional = AbstractWiringNamespace.RESOLUTION_OPTIONAL.equals(resdir);
        getNamespaceValue(this);
        canonicalName = toString();
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

    public static String namespaceValueFromFilter(Filter filter, String namespace) {
        String result = null;
        if (filter != null) {
            String filterstr = filter.toString();
            int index = filterstr.indexOf("(" + namespace + "=");
            if (index >= 0) {
                result = filterstr.substring(index + namespace.length() + 2);
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
        return directives.getDirectives();
    }

    @Override
    public String getDirective(String key) {
        return directives.getDirective(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes.getAttributes();
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.getAttribute(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends XRequirement> T adapt(Class<T> clazz) {
        T result = null;
        if (XResourceRequirement.class == clazz && BUNDLE_NAMESPACE.equals(getNamespace())) {
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
        ensureImmutable();

        // The requirement matches the capability if their namespaces match and the requirement's
        // filter is absent or matches the attributes.
        boolean matches = namespace.equals(cap.getNamespace()) && matchFilter(cap);

        if (matches == true) {
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

        // match the namespace value
        String nsvalue = (String) getAttribute(getNamespace());
        if (nsvalue != null && !nsvalue.equals(cap.getAttributes().get(getNamespace())))
            return false;

        // cannot require itself
        if (getResource() == cap.getResource())
            return false;

        // match the bundle version range
        if (getVersionRange() != null) {
            Version version = AbstractCapability.getVersion(cap, BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
            if (getVersionRange().isInRange(version) == false)
                return false;
        }

        return true;
    }
    
    private boolean matchesHostRequirement(Capability cap) {

        // match the namespace value
        String nsvalue = (String) getAttribute(getNamespace());
        if (nsvalue != null && !nsvalue.equals(cap.getAttributes().get(getNamespace())))
            return false;

        // match the bundle version range
        if (getVersionRange() != null) {
            Version version = AbstractCapability.getVersion(cap, HostNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
            if (getVersionRange().isInRange(version) == false)
                return false;
        }

        return true;
    }
    
    @SuppressWarnings("deprecation")
    private boolean matchesPackageRequirement(Capability cap) {

        // match the namespace value
        if (!matchPackageName(cap))
            return false;

        // match the package version range
        if (getVersionRange() != null) {
            Version version = AbstractCapability.getVersion(cap, PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            if (getVersionRange().isInRange(version) == false)
                return false;
        }

        Map<String, Object> reqatts = new HashMap<String, Object> (getAttributes());
        reqatts.remove(PackageNamespace.PACKAGE_NAMESPACE);
        reqatts.remove(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        reqatts.remove(Constants.PACKAGE_SPECIFICATION_VERSION);

        Map<String, Object> capatts = new HashMap<String, Object> (cap.getAttributes());
        capatts.remove(PackageNamespace.PACKAGE_NAMESPACE);
        capatts.remove(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
        capatts.remove(Constants.PACKAGE_SPECIFICATION_VERSION);


        // match package's bundle-symbolic-name
        String symbolicName = (String) reqatts.remove(PackageNamespace.CAPABILITY_BUNDLE_SYMBOLICNAME_ATTRIBUTE);
        if (symbolicName != null) {
            XResource capres = (XResource) cap.getResource();
            XIdentityCapability idcap = capres.getIdentityCapability();
            String targetSymbolicName = idcap != null ? idcap.getSymbolicName() : null;
            if (symbolicName.equals(targetSymbolicName) == false)
                return false;
        }

        // match package's bundle-version
        String versionstr = (String) reqatts.remove(PackageNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE);
        if (versionstr != null) {
            XResource capres = (XResource) cap.getResource();
            XIdentityCapability idcap = capres.getIdentityCapability();
            Version targetVersion = idcap != null ? idcap.getVersion() : null;
            VersionRange versionRange = VersionRange.parse(versionstr);
            if (targetVersion != null && versionRange.isInRange(targetVersion) == false)
                return false;
        }

        // match mandatory attributes on the capability
        String dirstr = ((XCapability) cap).getDirective(PackageNamespace.CAPABILITY_MANDATORY_DIRECTIVE);
        if (dirstr != null) {
            for (String att : dirstr.split(",")) {
                Object capval = capatts.remove(att);
                if (capval != null) {
                    Object reqval = reqatts.remove(att);
                    if (!capval.equals(reqval))
                        return false;
                }
            }
        }

        // match package attributes on the requirement
        for (Map.Entry<String,Object> entry : reqatts.entrySet()) {
            String att = entry.getKey();
            Object reqval = entry.getValue();
            Object capval = capatts.remove(att);
            if (!reqval.equals(capval))
                return false;
        }

        return true;
    }

    private boolean matchPackageName(Capability cap) {

        String packageName = getPackageName();
        if (packageName.equals("*"))
            return true;

        String capvalue = (String) cap.getAttributes().get(getNamespace());
        if (packageName.endsWith(".*")) {
            packageName = packageName.substring(0, packageName.length() - 2);
            return capvalue.startsWith(packageName);
        }
        else
        {
            return packageName.equals(capvalue);
        }
    }
    
    static VersionRange getVersionRange(XRequirement req, String attr) {
        Object value = req.getAttribute(attr);
        return (value instanceof String) ? VersionRange.parse((String) value) : (VersionRange) value;
    }

    private boolean matchFilter(Capability cap) {
        return filter != null ? filter.match(new Hashtable<String, Object>(cap.getAttributes())) : true;
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
        }
        else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
            result = AbstractRequirement.getVersionRange(this, CAPABILITY_VERSION_ATTRIBUTE);
        }
        return result;
    }

    @Override
    public boolean isDynamic() {
        return RESOLUTION_DYNAMIC.equals(getDirective(REQUIREMENT_RESOLUTION_DIRECTIVE));
    }

    @Override
    public String toString() {
        String result = canonicalName;
        if (result == null) {
            String type = getClass().getSimpleName();
            if (BUNDLE_NAMESPACE.equals(getNamespace())) {
                type = XResourceRequirement.class.getSimpleName();
            } else if (HOST_NAMESPACE.equals(getNamespace())) {
                type = XHostRequirement.class.getSimpleName();
            } else if (PACKAGE_NAMESPACE.equals(getNamespace())) {
                type = XPackageRequirement.class.getSimpleName();
            }
            String attstr = "atts=" + attributes;
            String dirstr = !getDirectives().isEmpty() ? ",dirs=" + directives : "";
            XIdentityCapability icap = resource.getIdentityCapability();
            String resname = ",[" + (icap != null ? icap.getSymbolicName() + ":" + icap.getVersion() : "anonymous") + "]";
            result = type + "[" + attstr + dirstr + resname + "]";
        }
        return result;
    }
}