/*
 * #%L
 * JBossOSGi Resolver Felix
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
package org.jboss.osgi.resolver.felix;

import static org.jboss.osgi.resolver.ResolverLogger.LOGGER;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.felix.resolver.ResolverImpl;
import org.osgi.resource.Resource;
import org.osgi.resource.Wire;
import org.osgi.service.resolver.ResolutionException;
import org.osgi.service.resolver.ResolveContext;

/**
 * An extension to the Felix Resolver that does some logging.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public final class LoggingResolver extends ResolverImpl {

    public LoggingResolver() {
        super(new LoggerDelegate());
    }

    @Override
    public Map<Resource, List<Wire>> resolve(ResolveContext context) throws ResolutionException {
        Collection<Resource> mandatory = context.getMandatoryResources();
        Collection<Resource> optional = context.getOptionalResources();
        LOGGER.debugf("Resolve: %s, %s", mandatory, optional);
        Map<Resource, List<Wire>> result = super.resolve(context);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Resolution result: %d", result.size());
            for (Map.Entry<Resource, List<Wire>> entry : result.entrySet()) {
                Resource res = entry.getKey();
                List<Wire> wires = entry.getValue();
                LOGGER.debugf("   %s: %d wires", res, wires.size());
                for (Wire wire : wires) {
                    LOGGER.debugf("      %s", wire);
                }
            }
        }
        return result;
    }
}
