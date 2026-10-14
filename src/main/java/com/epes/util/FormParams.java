package com.epes.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * Utility containing safe HTTP form parameter parsing helpers.
 *
 * <p>Never throws {@link NumberFormatException} or {@link DateTimeParseException}.
 * Instead, parsing failures and blank values append a user-friendly validation error
 * to the provided error list and return {@code null}.</p>
 */
public final class FormParams {

    private FormParams() {
        // Utility class — prevent instantiation
    }

    /**
     * Safely parses a string into a {@link Long}.
     *
     * @param value      raw form input string
     * @param fieldLabel friendly display label for error messages
     * @param errors     list to collect validation errors
     * @return parsed Long value, or null if invalid or blank
     */
    public static Long parseLong(String value, String fieldLabel, List<String> errors) {
        Objects.requireNonNull(errors, "errors list must not be null");
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldLabel + " cannot be blank");
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            errors.add(fieldLabel + " must be a valid number");
            return null;
        }
    }

    /**
     * Safely parses a string into an {@link Integer}.
     *
     * @param value      raw form input string
     * @param fieldLabel friendly display label for error messages
     * @param errors     list to collect validation errors
     * @return parsed Integer value, or null if invalid or blank
     */
    public static Integer parseInt(String value, String fieldLabel, List<String> errors) {
        Objects.requireNonNull(errors, "errors list must not be null");
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldLabel + " cannot be blank");
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            errors.add(fieldLabel + " must be a valid whole number");
            return null;
        }
    }

    /**
     * Safely parses an ISO-8601 date string (YYYY-MM-DD) into a {@link LocalDate}.
     *
     * @param value      raw form input string
     * @param fieldLabel friendly display label for error messages
     * @param errors     list to collect validation errors
     * @return parsed LocalDate value, or null if invalid or blank
     */
    public static LocalDate parseLocalDate(String value, String fieldLabel, List<String> errors) {
        Objects.requireNonNull(errors, "errors list must not be null");
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldLabel + " cannot be blank");
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            errors.add(fieldLabel + " must be a valid date (YYYY-MM-DD)");
            return null;
        }
    }
}
