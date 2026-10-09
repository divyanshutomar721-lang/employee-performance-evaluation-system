package com.epes.util;

import com.epes.exception.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility containing validation rules and an {@link ErrorCollector} for accumulating
 * multiple validation errors before throwing a single {@link ValidationException}.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validator() {
        // Prevent instantiation
    }

    /**
     * Checks that a string is not null, not empty, and not only whitespace.
     *
     * @param value     string to validate
     * @param fieldName name of the field for error reporting
     * @param errors    collection to append error to if invalid
     */
    public static void requireNotBlank(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " cannot be blank");
        }
    }

    /**
     * Checks that a string does not exceed a maximum length if non-null.
     *
     * @param value     string to validate
     * @param max       maximum allowed character length
     * @param fieldName name of the field for error reporting
     * @param errors    collection to append error to if invalid
     */
    public static void maxLength(String value, int max, String fieldName, List<String> errors) {
        if (value != null && value.length() > max) {
            errors.add(fieldName + " exceeds maximum allowed length of " + max + " characters");
        }
    }

    /**
     * Checks that a string conforms to a valid email format.
     *
     * @param email     email address string
     * @param fieldName name of the field for error reporting
     * @param errors    collection to append error to if invalid
     * @return normalized lowercase trimmed email if valid, or original trimmed email
     */
    public static String requireEmail(String email, String fieldName, List<String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.add(fieldName + " cannot be blank");
            return email;
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            errors.add(fieldName + " must be a valid email address");
        }
        return trimmed.toLowerCase();
    }

    /**
     * Checks that an integer value falls within a specified inclusive range [min, max].
     *
     * @param value     value to check
     * @param min       minimum inclusive
     * @param max       maximum inclusive
     * @param fieldName name of the field for error reporting
     * @param errors    collection to append error to if invalid
     */
    public static void requireRange(int value, int min, int max, String fieldName, List<String> errors) {
        if (value < min || value > max) {
            errors.add(fieldName + " must be between " + min + " and " + max);
        }
    }

    /**
     * Checks that a date is today or in the future.
     *
     * @param date      date to validate
     * @param fieldName name of the field for error reporting
     * @param errors    collection to append error to if invalid
     */
    public static void requireFutureOrToday(LocalDate date, String fieldName, List<String> errors) {
        if (date == null) {
            errors.add(fieldName + " cannot be null");
        } else if (date.isBefore(LocalDate.now())) {
            errors.add(fieldName + " must be today or in the future");
        }
    }

    /**
     * Checks that an object is not null.
     *
     * @param value     object to validate
     * @param fieldName name of the field for error reporting
     * @param errors    collection to append error to if invalid
     */
    public static void requireNotNull(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " cannot be null");
        }
    }

    /**
     * Creates a new, empty {@link ErrorCollector}.
     *
     * @return a fresh error collector
     */
    public static ErrorCollector newCollector() {
        return new ErrorCollector();
    }

    /**
     * Stateful error accumulator for fluent validation flows.
     */
    public static class ErrorCollector {
        private final List<String> errors = new ArrayList<>();

        public ErrorCollector checkNotBlank(String value, String fieldName) {
            requireNotBlank(value, fieldName, errors);
            return this;
        }

        public ErrorCollector checkMaxLength(String value, int max, String fieldName) {
            maxLength(value, max, fieldName, errors);
            return this;
        }

        public ErrorCollector checkEmail(String email, String fieldName) {
            requireEmail(email, fieldName, errors);
            return this;
        }

        public ErrorCollector checkRange(int value, int min, int max, String fieldName) {
            requireRange(value, min, max, fieldName, errors);
            return this;
        }

        public ErrorCollector checkFutureOrToday(LocalDate date, String fieldName) {
            requireFutureOrToday(date, fieldName, errors);
            return this;
        }

        public ErrorCollector checkNotNull(Object value, String fieldName) {
            requireNotNull(value, fieldName, errors);
            return this;
        }

        public ErrorCollector addError(String message) {
            if (message != null && !message.isBlank()) {
                errors.add(message);
            }
            return this;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        /**
         * Throws a {@link ValidationException} containing all accumulated error messages
         * if one or more validation errors have occurred.
         *
         * @throws ValidationException if errors are present
         */
        public void throwIfAny() throws ValidationException {
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }
}
