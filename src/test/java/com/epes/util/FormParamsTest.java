package com.epes.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FormParams} parser utility.
 */
class FormParamsTest {

    @Test
    void parseLongSuccess() {
        List<String> errors = new ArrayList<>();
        Long result = FormParams.parseLong("123456", "User ID", errors);
        assertEquals(123456L, result);
        assertTrue(errors.isEmpty());

        result = FormParams.parseLong("  987  ", "User ID", errors);
        assertEquals(987L, result);
        assertTrue(errors.isEmpty());
    }

    @Test
    void parseLongFailureAppendsErrorWithoutException() {
        List<String> errors = new ArrayList<>();
        Long result = FormParams.parseLong("not-a-number", "User ID", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("User ID must be a valid number", errors.get(0));

        errors.clear();
        result = FormParams.parseLong("", "User ID", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("User ID cannot be blank", errors.get(0));

        errors.clear();
        result = FormParams.parseLong(null, "User ID", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("User ID cannot be blank", errors.get(0));
    }

    @Test
    void parseIntSuccess() {
        List<String> errors = new ArrayList<>();
        Integer result = FormParams.parseInt("75", "Weight", errors);
        assertEquals(75, result);
        assertTrue(errors.isEmpty());

        result = FormParams.parseInt("  100  ", "Weight", errors);
        assertEquals(100, result);
        assertTrue(errors.isEmpty());
    }

    @Test
    void parseIntFailureAppendsErrorWithoutException() {
        List<String> errors = new ArrayList<>();
        Integer result = FormParams.parseInt("invalid", "Weight", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("Weight must be a valid whole number", errors.get(0));

        errors.clear();
        result = FormParams.parseInt("", "Weight", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("Weight cannot be blank", errors.get(0));

        errors.clear();
        result = FormParams.parseInt(null, "Weight", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("Weight cannot be blank", errors.get(0));
    }

    @Test
    void parseLocalDateSuccess() {
        List<String> errors = new ArrayList<>();
        LocalDate result = FormParams.parseLocalDate("2026-10-14", "Start Date", errors);
        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 10, 14), result);
        assertTrue(errors.isEmpty());
    }

    @Test
    void parseLocalDateFailureAppendsErrorWithoutException() {
        List<String> errors = new ArrayList<>();
        LocalDate result = FormParams.parseLocalDate("2026-13-45", "Target Date", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("Target Date must be a valid date (YYYY-MM-DD)", errors.get(0));

        errors.clear();
        result = FormParams.parseLocalDate("invalid-date", "Target Date", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("Target Date must be a valid date (YYYY-MM-DD)", errors.get(0));

        errors.clear();
        result = FormParams.parseLocalDate("", "Target Date", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("Target Date cannot be blank", errors.get(0));

        errors.clear();
        result = FormParams.parseLocalDate(null, "Target Date", errors);
        assertNull(result);
        assertEquals(1, errors.size());
        assertEquals("Target Date cannot be blank", errors.get(0));
    }
}
