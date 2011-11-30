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

import org.apache.felix.framework.Logger;
import org.apache.felix.framework.resolver.Resolver;
import org.apache.felix.framework.resolver.ResolverImpl;
import org.apache.felix.framework.resolver.ResolverWire;
import org.osgi.framework.wiring.BundleRevision;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An extension to the Apache Felix Resolver.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
class ResolverExt implements Resolver {
    private Resolver delegate;

    public ResolverExt(Logger logger) {
        this.delegate = new ResolverImpl(logger);
    }

    @Override
    public Map<BundleRevision, List<ResolverWire>> resolve(ResolverState state, Set<BundleRevision> mandatoryRevisions, Set<BundleRevision> optionalRevisions, Set<BundleRevision> ondemandFragments) {
        return delegate.resolve(state, mandatoryRevisions, optionalRevisions, ondemandFragments);
    }

    @Override
    public Map<BundleRevision, List<ResolverWire>> resolve(ResolverState state, BundleRevision revision, String pkgName, Set<BundleRevision> ondemandFragments) {
        return delegate.resolve(state, revision, pkgName, ondemandFragments);
    }
}