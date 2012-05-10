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
