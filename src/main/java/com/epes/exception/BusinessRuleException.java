package com.epes.exception;

/**
 * Unchecked exception thrown when input is structurally valid but violates
 * a domain business rule or policy.
 */
public class BusinessRuleException extends ServiceException {

    /**
     * Constructs a new BusinessRuleException with the given message.
     *
     * @param message explanation of the business rule violation
     */
    public BusinessRuleException(String message) {
        super(message);
    }

    /**
     * Constructs a new BusinessRuleException with the given message and cause.
     *
     * @param message explanation of the business rule violation
     * @param cause   underlying cause
     */
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
