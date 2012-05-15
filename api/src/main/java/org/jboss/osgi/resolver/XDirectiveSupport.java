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

package org.jboss.osgi.resolver;

import java.util.Map;

/**
 * Adds support for directives
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XDirectiveSupport {
    /**
     * Get the directives
     */
    Map<String, String> getDirectives();

    /**
     * Get the value of the given directive
     * 
     * @return null if no such directive is associated with this capability
     */
    String getDirective(String key);
}