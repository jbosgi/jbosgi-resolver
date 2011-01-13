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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.framework.Logger;
import org.apache.felix.framework.capabilityset.Requirement;
import org.apache.felix.framework.resolver.FragmentRequirement;
import org.apache.felix.framework.resolver.Module;
import org.apache.felix.framework.resolver.ResolveException;
import org.apache.felix.framework.resolver.Wire;
import org.apache.felix.framework.util.Util;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResolver;
import org.jboss.osgi.resolver.XResolverException;
import org.jboss.osgi.resolver.XWire;
import org.jboss.osgi.resolver.spi.AbstractModule;
import org.jboss.osgi.resolver.spi.AbstractResolver;

/**
 * An implementation of the Resolver.
 * 
 * This implemantion should use no framework specific API. It is the extension point for a framework specific Resolver.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class FelixResolver extends AbstractResolver implements XResolver {
    private Logger logger;

    private ResolverExt resolver;
    private ResolverStateExt resolverState;
    private ResultProcessor resultProcessor;

    public FelixResolver() {
        logger = new LoggerDelegate();
        resolver = new ResolverExt(logger);
        resolverState = new ResolverStateExt(logger);
        resultProcessor = new ResultProcessor(this);
    }

    @Override
    public void addModule(XModule module) {
        super.addModule(module);
        ModuleExt fmod = new ModuleExt((AbstractModule) module);
        module.addAttachment(ModuleExt.class, fmod);
        resolverState.addModule(fmod);
    }

    @Override
    public void removeModule(XModule module) {
        super.removeModule(module);
        ModuleExt fmod = module.getAttachment(ModuleExt.class);
        resolverState.removeModule(fmod);
    }

    public ModuleExt findHost(ModuleExt fragModule) {
        ModuleExt hostModule = (ModuleExt) resolverState.findHost(fragModule);
        return hostModule;
    }

    public List<ModuleExt> findFragments(ModuleExt hostModule) {
        List<ModuleExt> frags = new ArrayList<ModuleExt>();
        for (Module m : resolverState.findFragments(hostModule))
            frags.add((ModuleExt) m);

        return frags;
    }

    @Override
    protected void setResolved(AbstractModule module) {
        super.setResolved(module);
    }

    @Override
    protected void resolveInternal(XModule module) throws XResolverException {
        if (module == null)
            throw new IllegalArgumentException("Null module");

        ModuleExt rootModule = module.getAttachment(ModuleExt.class);
        try {
            resolveInternal(rootModule);
        } catch (ResolveException ex) {
            String msg = ex.getMessage();
            ModuleExt exmod = (ModuleExt) ex.getModule();
            Requirement exreq = ex.getRequirement();
            Throwable cause = ex.getCause();

            XResolverException resex = new XResolverException(msg, exmod.getModule(), exreq);
            resex.initCause(cause);
            throw resex;
        }
    }

    private void resolveInternal(ModuleExt rootModule) {
        if (!rootModule.isResolved()) {
            synchronized (resolverState) {
                // If the root module to resolve is a fragment, then we
                // must find a host to attach it to and resolve the host
                // instead, since the underlying resolver doesn't know
                // how to deal with fragments.
                Module newRootModule = resolverState.findHost(rootModule);
                if (!Util.isFragment(newRootModule)) {
                    // Check singleton status.
                    resolverState.checkSingleton(newRootModule);

                    boolean repeat;
                    do {
                        repeat = false;
                        try {
                            // Resolve the module.
                            Map<Module, List<Wire>> wireMap = resolver.resolve(resolverState, newRootModule);

                            // Mark all modules as resolved.
                            markResolvedModules(wireMap);
                        } catch (ResolveException ex) {
                            Requirement req = ex.getRequirement();
                            if (req != null && (req instanceof FragmentRequirement) && (rootModule != ((FragmentRequirement) req).getFragment())) {
                                resolverState.detachFragment(newRootModule, ((FragmentRequirement) req).getFragment());
                                repeat = true;
                            } else {
                                throw ex;
                            }
                        }
                    } while (repeat);
                }
            }
        }
    }

    private void markResolvedModules(Map<Module, List<Wire>> wireMap) {
        if (wireMap != null) {
            Iterator<Entry<Module, List<Wire>>> iter = wireMap.entrySet().iterator();
            // Iterate over the map to mark the modules as resolved and
            // update our resolver data structures.
            while (iter.hasNext()) {
                Entry<Module, List<Wire>> entry = iter.next();
                ModuleExt moduleExt = (ModuleExt) entry.getKey();
                List<Wire> wires = entry.getValue();

                // Only add wires attribute if some exist; export
                // only modules may not have wires.
                for (int wireIdx = 0; wireIdx < wires.size(); wireIdx++) {
                    logger.log(Logger.LOG_DEBUG, "WIRE: " + wires.get(wireIdx));
                }

                resultProcessor.setModuleWires(moduleExt, wires);

                // Resolve all attached fragments.
                List<Module> fragments = moduleExt.getFragments();
                for (int i = 0; (fragments != null) && (i < fragments.size()); i++) {
                    ModuleExt frag = (ModuleExt) fragments.get(i);
                    frag.setResolved();

                    resultProcessor.setModuleWires(frag, null);
                    resultProcessor.setResolved(frag);
                    logger.log(Logger.LOG_DEBUG, "FRAGMENT WIRE: " + frag + " -> hosted by -> " + moduleExt);
                }
                // Update the resolver state to show the module as resolved.
                resultProcessor.setResolved(moduleExt);
                resolverState.moduleResolved(moduleExt);
            }
        }
    }

    protected XWire addWire(AbstractModule importer, XRequirement requirement, XModule exporter, XCapability capability) {
        return super.addWire(importer, requirement, exporter, capability);
    }
}