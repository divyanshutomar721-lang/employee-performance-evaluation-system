package com.epes.exception;

/**
 * Unchecked exception thrown when an authenticated actor attempts an action
 * for which they lack appropriate permissions or role.
 */
public class AuthorizationException extends ServiceException {

    /**
     * Constructs a new AuthorizationException with the given message.
     *
     * @param message explanation of authorization denial
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthorizationException with the given message and cause.
     *
     * @param message explanation of authorization denial
     * @param cause   underlying cause
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
