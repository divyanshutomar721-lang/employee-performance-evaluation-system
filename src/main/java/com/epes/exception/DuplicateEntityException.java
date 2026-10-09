package com.epes.exception;

/**
 * Checked exception thrown when an entity creation or update violates
 * a unique constraint (PostgreSQL SQLState 23505).
 */
public class DuplicateEntityException extends DataAccessException {

    /**
     * Creates a new DuplicateEntityException with the given detail message.
     *
     * @param message description of the duplicated resource/key
     */
    public DuplicateEntityException(String message) {
        super(message);
    }

    /**
     * Creates a new DuplicateEntityException with the given detail message and cause.
     *
     * @param message description of the duplicated resource/key
     * @param cause   the underlying SQL exception
     */
    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
