package com.epes.exception;

/**
 * Checked exception thrown when a database operation fails.
 * Wraps the underlying {@link java.sql.SQLException} so that
 * callers do not depend directly on JDBC types.
 */
public class DataAccessException extends Exception {

    /**
     * Creates a new DataAccessException with the given detail message and cause.
     *
     * @param message description of the failed operation
     * @param cause   the underlying SQL exception
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new DataAccessException with the given detail message.
     *
     * @param message description of the failed operation
     */
    public DataAccessException(String message) {
        super(message);
    }
}
