/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.resolver.felix;

import org.apache.felix.framework.capabilityset.CapabilitySet;
import org.apache.felix.framework.capabilityset.SimpleFilter;
import org.apache.felix.framework.resolver.CandidateComparator;
import org.apache.felix.framework.resolver.ResolveException;
import org.apache.felix.framework.resolver.Resolver.ResolverState;
import org.apache.felix.framework.util.Util;
import org.apache.felix.framework.wiring.BundleRequirementImpl;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * An extension to the Apache Felix ResolverState.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class ResolverStateExt implements ResolverState {

    // Set of all revisions.
    private final Set<BundleRevision> m_revisions = new HashSet<BundleRevision>();
    // Set of all fragments.
    private final Set<BundleRevision> m_fragments = new HashSet<BundleRevision>();
    // Capability sets.
    private final Map<String, CapabilitySet> m_capSets = new HashMap<String, CapabilitySet>();
    // Maps singleton symbolic names to list of bundle revisions sorted by version.
    private final Map<String, List<BundleRevision>> m_singletons = new HashMap<String, List<BundleRevision>>();
    // Execution environment.
    private final String m_fwkExecEnvStr;
    // Parsed framework environments
    private final Set<String> m_fwkExecEnvSet;

    public ResolverStateExt(String fwkExecEnvStr) {
        m_fwkExecEnvStr = (fwkExecEnvStr != null) ? fwkExecEnvStr.trim() : null;
        m_fwkExecEnvSet = parseExecutionEnvironments(fwkExecEnvStr);

        List<String> indices = new ArrayList<String>();
        indices.add(BundleRevision.BUNDLE_NAMESPACE);
        m_capSets.put(BundleRevision.BUNDLE_NAMESPACE, new CapabilitySet(indices, true));

        indices = new ArrayList<String>();
        indices.add(BundleRevision.PACKAGE_NAMESPACE);
        m_capSets.put(BundleRevision.PACKAGE_NAMESPACE, new CapabilitySet(indices, true));

        indices = new ArrayList<String>();
        indices.add(BundleRevision.HOST_NAMESPACE);
        m_capSets.put(BundleRevision.HOST_NAMESPACE, new CapabilitySet(indices, true));
    }

    synchronized void addRevision(BundleRevision br) {
        // Always attempt to remove the revision, since
        // this method can be used for re-indexing a revision
        // after it has been resolved.
        removeRevision(br);

        m_revisions.add(br);

        // Add singletons to the singleton map.
        boolean isSingleton = Util.isSingleton(br);
        if (isSingleton) {
            // Index the new singleton.
            addToSingletonMap(m_singletons, br);
        }

        // We always need to index non-singleton bundle capabilities, but
        // singleton bundles only need to be index if they are resolved.
        // Unresolved singleton capabilities are only indexed before a
        // resolve operation when singleton selection is performed.
        if (!isSingleton || (br.getWiring() != null)) {
            if (Util.isFragment(br)) {
                m_fragments.add(br);
            }
            indexCapabilities(br);
        }
    }

    synchronized void removeRevision(BundleRevision br) {
        if (m_revisions.remove(br)) {
            m_fragments.remove(br);
            deindexCapabilities(br);

            // If this module is a singleton, then remove it from the
            // singleton map.
            List<BundleRevision> revisions = m_singletons.get(br.getSymbolicName());
            if (revisions != null) {
                revisions.remove(br);
                if (revisions.isEmpty()) {
                    m_singletons.remove(br.getSymbolicName());
                }
            }
        }
    }

    @Override
    public boolean isEffective(BundleRequirement req) {
        String effective = req.getDirectives().get(Constants.EFFECTIVE_DIRECTIVE);
        return ((effective == null) || effective.equals(Constants.EFFECTIVE_RESOLVE));
    }

    @Override
    public synchronized SortedSet<BundleCapability> getCandidates(BundleRequirement req, boolean obeyMandatory) {

        SortedSet<BundleCapability> result = new TreeSet<BundleCapability>(new CandidateComparator());
        CapabilitySet capSet = m_capSets.get(req.getNamespace());
        if (capSet != null) {
            // Get the requirement's filter; if this is our own impl we
            // have a shortcut to get the already parsed filter, otherwise
            // we must parse it from the directive.
            SimpleFilter sf = null;
            if (req instanceof BundleRequirementImpl) {
                sf = ((BundleRequirementImpl) req).getFilter();
            } else {
                String filter = req.getDirectives().get(Constants.FILTER_DIRECTIVE);
                if (filter == null) {
                    sf = new SimpleFilter(null, null, SimpleFilter.MATCH_ALL);
                } else {
                    sf = SimpleFilter.parse(filter);
                }
            }

            // Find the matching candidates.
            Set<BundleCapability> matches = capSet.match(sf, obeyMandatory);

            // Filter matching candidates.
            for (BundleCapability cap : matches) {

                /* Filter according to security.
                if (filteredBySecurity(req, cap))
                {
                    continue;
                }
                */

                // Filter already resolved hosts, since we don't support
                // dynamic attachment of fragments.
                if (req.getNamespace().equals(BundleRevision.HOST_NAMESPACE) && (cap.getRevision().getWiring() != null)) {
                    continue;
                }

                result.add(cap);
            }
        }

        // If we have resolver hooks, then we may need to filter our results
        // based on a whitelist and/or fine-grained candidate filtering.
        /*
        if (!result.isEmpty() && !m_hooks.isEmpty())
        {
            // It we have a whitelist, then first filter out candidates
            // from disallowed revisions.
            if (m_whitelist != null)
            {
                for (Iterator<BundleCapability> it = result.iterator(); it.hasNext(); )
                {
                    if (!m_whitelist.contains(it.next().getRevision()))
                    {
                        it.remove();
                    }
                }
            }

            // Now give the hooks a chance to do fine-grained filtering.
            ShrinkableCollection<BundleCapability> shrinkable =
                new ShrinkableCollection<BundleCapability>(result);
            for (ResolverHook hook : m_hooks)
            {
                try
                {
                    Felix.m_secureAction
                        .invokeResolverHookMatches(hook, req, shrinkable);
                }
                catch (Throwable th)
                {
                    m_logger.log(Logger.LOG_WARNING, "Resolver hook exception.", th);
                }
            }
        }
        */

        return result;
    }

    @Override
    public void checkExecutionEnvironment(BundleRevision revision) throws ResolveException {
        String bundleExecEnvStr = revision.getBundle().getHeaders().get(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
        if (bundleExecEnvStr != null) {
            bundleExecEnvStr = bundleExecEnvStr.trim();
            // If the bundle has specified an execution environment and the
            // framework has an execution environment specified, then we must
            // check for a match.
            if (!bundleExecEnvStr.equals("") && (m_fwkExecEnvStr != null) && (m_fwkExecEnvStr.length() > 0)) {
                StringTokenizer tokens = new StringTokenizer(bundleExecEnvStr, ",");
                boolean found = false;
                while (tokens.hasMoreTokens() && !found) {
                    if (m_fwkExecEnvSet.contains(tokens.nextToken().trim())) {
                        found = true;
                    }
                }
                if (!found) {
                    throw new ResolveException("Execution environment not supported: " + bundleExecEnvStr, revision, null);
                }
            }
        }
    }

    @Override
    public void checkNativeLibraries(BundleRevision revision) throws ResolveException {
    }

    private static void addToSingletonMap(
            Map<String, List<BundleRevision>> singletons, BundleRevision br) {
        List<BundleRevision> revisions = singletons.get(br.getSymbolicName());
        if (revisions == null) {
            revisions = new ArrayList<BundleRevision>();
        }
        revisions.add(br);
        singletons.put(br.getSymbolicName(), revisions);
    }

    private synchronized void indexCapabilities(BundleRevision br) {
        List<BundleCapability> caps =
                (Util.isFragment(br) || (br.getWiring() == null))
                        ? br.getDeclaredCapabilities(null)
                        : br.getWiring().getCapabilities(null);
        if (caps != null) {
            for (BundleCapability cap : caps) {
                // If the capability is from a different revision, then
                // don't index it since it is a capability from a fragment.
                // In that case, the fragment capability is still indexed.
                // It will be the resolver's responsibility to find all
                // attached hosts for fragments.
                if (cap.getRevision() == br) {
                    CapabilitySet capSet = m_capSets.get(cap.getNamespace());
                    if (capSet == null) {
                        capSet = new CapabilitySet(null, true);
                        m_capSets.put(cap.getNamespace(), capSet);
                    }
                    capSet.addCapability(cap);
                }
            }
        }
    }

    private synchronized void deindexCapabilities(BundleRevision br) {
        // We only need be concerned with declared capabilities here,
        // because resolved capabilities will be a subset, since fragment
        // capabilities are not considered to be part of the host.
        List<BundleCapability> caps = br.getDeclaredCapabilities(null);
        if (caps != null) {
            for (BundleCapability cap : caps) {
                CapabilitySet capSet = m_capSets.get(cap.getNamespace());
                if (capSet != null) {
                    capSet.removeCapability(cap);
                }
            }
        }
    }

    /**
     * Updates the framework wide execution environment string and a cached Set of
     * execution environment tokens from the comma delimited list specified by the
     * system variable 'org.osgi.framework.executionenvironment'.
     *
     * @param fwkExecEnvStr Comma delimited string of provided execution environments
     * @return the parsed set of execution environments
     */
    private static Set<String> parseExecutionEnvironments(String fwkExecEnvStr) {
        Set<String> newSet = new HashSet<String>();
        if (fwkExecEnvStr != null) {
            StringTokenizer tokens = new StringTokenizer(fwkExecEnvStr, ",");
            while (tokens.hasMoreTokens()) {
                newSet.add(tokens.nextToken().trim());
            }
        }
        return newSet;
    }
}