package com.epes.exception;

/**
 * Unchecked exception thrown when a required configuration value
 * (e.g. an environment variable) is missing or invalid.
 */
public class ConfigurationException extends RuntimeException {

    /**
     * Creates a new ConfigurationException with the given detail message.
     *
     * @param message explanation of what is misconfigured
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a new ConfigurationException with the given detail message and cause.
     *
     * @param message explanation of what is misconfigured
     * @param cause   the underlying cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
