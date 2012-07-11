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
 * Indicates failure to build a resource.
 */
public class ResourceBuilderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception of type {@code ResourceBuilderException}.
     * 
     * <p>
     * This method creates an {@code ResourceBuilderException} object with the
     * specified message and cause.
     * 
     * @param message The message.
     * @param cause The cause of this exception.
     */
    public ResourceBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception of type {@code ResourceBuilderException}.
     * 
     * <p>
     * This method creates an {@code ResourceBuilderException} object with the
     * specified message.
     * 
     * @param message The message.
     */
    public ResourceBuilderException(String message) {
        super(message);
    }

    /**
     * Creates an exception of type {@code ResourceBuilderException}.
     * 
     * <p>
     * This method creates an {@code ResourceBuilderException} object with the
     * specified cause.
     * 
     * @param cause The cause of this exception.
     */
    public ResourceBuilderException(Throwable cause) {
        super(cause);
    }
}
