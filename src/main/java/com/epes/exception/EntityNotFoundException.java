package com.epes.exception;

/**
 * Checked exception thrown when an entity requested by ID, email,
 * or unique property does not exist in the database.
 */
public class EntityNotFoundException extends DataAccessException {

    /**
     * Creates a new EntityNotFoundException with the given detail message.
     *
     * @param message description of the entity that was not found
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new EntityNotFoundException with the given detail message and cause.
     *
     * @param message description of the entity that was not found
     * @param cause   the underlying cause
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
