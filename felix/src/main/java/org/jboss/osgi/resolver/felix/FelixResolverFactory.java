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

import org.jboss.osgi.resolver.XResolver;
import org.jboss.osgi.resolver.XResolverFactory;

/**
 * An implementation of the XResolverFactory.
 *
 * @author thomas.diesler@jboss.com
 * @since 13-Mar-2013
 */
public class FelixResolverFactory implements XResolverFactory {

    @Override
    public XResolver createResolver() {
        return new FelixResolver();
    }

}
