package com.epes.exception;

/**
 * Unchecked exception thrown when user authentication fails.
 */
public class AuthenticationException extends ServiceException {

    /**
     * Constructs a new AuthenticationException with the given message.
     *
     * @param message explanation of authentication failure
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthenticationException with the given message and cause.
     *
     * @param message explanation of authentication failure
     * @param cause   underlying cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
