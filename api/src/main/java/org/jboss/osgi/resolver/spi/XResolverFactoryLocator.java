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

import java.util.ServiceLoader;

import org.jboss.osgi.resolver.XResolverFactory;

/**
 * The implementation of a {@link XResolverFactory}.
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Mar-2013
 */
public class XResolverFactoryLocator {

    // Hide ctor
    private XResolverFactoryLocator() {
    }

    public static XResolverFactory getResolverFactory() {
        ClassLoader classLoader = XResolverFactoryLocator.class.getClassLoader();
        ServiceLoader<XResolverFactory> loader = ServiceLoader.load(XResolverFactory.class, classLoader);
        return loader.iterator().next();
    }
}
