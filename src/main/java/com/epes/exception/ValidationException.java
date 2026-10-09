package com.epes.exception;

import java.util.Collections;
import java.util.List;

/**
 * Unchecked exception thrown when input parameters fail one or more validation constraints.
 * Holds an immutable collection of error messages collected during validation.
 */
public class ValidationException extends ServiceException {

    private final List<String> errors;

    /**
     * Constructs a ValidationException with a list of error messages.
     *
     * @param errors list of validation error descriptions
     */
    public ValidationException(List<String> errors) {
        super(errors != null && !errors.isEmpty() ? String.join("; ", errors) : "Validation failed");
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }

    /**
     * Constructs a ValidationException with a single error message.
     *
     * @param singleError single validation error description
     */
    public ValidationException(String singleError) {
        super(singleError != null ? singleError : "Validation failed");
        this.errors = singleError != null ? List.of(singleError) : Collections.emptyList();
    }

    /**
     * Returns the unmodifiable list of collected validation error messages.
     *
     * @return list of error messages
     */
    public List<String> getErrors() {
        return errors;
    }
}
