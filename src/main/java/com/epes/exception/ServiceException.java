package com.epes.exception;

/**
 * Base abstract unchecked exception for all service and business layer errors.
 *
 * <p><b>Architecture Note on Exception Strategy:</b>
 * Service exceptions extend {@link RuntimeException} (unchecked) because business rule
 * violations, validation failures, and authorization denials represent logical domain outcomes
 * or client-side request faults that are caught uniformly by the presentation/web layer
 * (filters/servlets) to render user-facing error messages or redirects.
 * In contrast, DAO/infrastructure exceptions (e.g. {@link DataAccessException}) are checked
 * because low-level I/O, SQL failures, and resource rollbacks require explicit handling
 * or transaction management at boundary layers.</p>
 */
public abstract class ServiceException extends RuntimeException {

    /**
     * Constructs a new ServiceException with a detail message.
     *
     * @param message explanation of the service error
     */
    protected ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new ServiceException with a detail message and cause.
     *
     * @param message explanation of the service error
     * @param cause   underlying cause
     */
    protected ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
