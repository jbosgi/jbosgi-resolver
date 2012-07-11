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

package org.jboss.osgi.resolver;


/**
 * Adds attachment support
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface XAttachmentSupport  {
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
