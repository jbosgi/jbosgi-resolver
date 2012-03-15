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
