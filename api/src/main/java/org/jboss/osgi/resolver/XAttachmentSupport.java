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

/**
 * Adds attachment support
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XAttachmentSupport {
    /**
     * Attach an arbirtary object with this element.
     * 
     * @param clazz key for the attachment
     * @return The previously attachment object or null
     */
    <T> T addAttachment(Class<T> clazz, T value);

    /**
     * Get the attached object for a given key
     * 
     * @param clazz key for the attachment
     * @return The attached object or null
     */
    <T> T getAttachment(Class<T> clazz);

    /**
     * Remove an attached object for a given key
     * 
     * @param clazz key for the attachment
     * @return The attached object or null
     */
    <T> T removeAttachment(Class<T> clazz);
}