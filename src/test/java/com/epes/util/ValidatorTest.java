package com.epes.util;

import com.epes.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Validator} and {@link Validator.ErrorCollector}.
 */
class ValidatorTest {

    @Test
    @DisplayName("ErrorCollector accumulates multiple errors and throws ValidationException")
    void testErrorAccumulation() {
        var collector = Validator.newCollector();
        collector.checkNotBlank("", "Name")
                .checkEmail("invalid-email", "Email")
                .checkRange(150, 1, 100, "Weight");

        assertTrue(collector.hasErrors());
        assertEquals(3, collector.getErrors().size());

        ValidationException ex = assertThrows(ValidationException.class, collector::throwIfAny);
        assertEquals(3, ex.getErrors().size());
        assertTrue(ex.getMessage().contains("Name cannot be blank"));
        assertTrue(ex.getMessage().contains("Email must be a valid email"));
        assertTrue(ex.getMessage().contains("Weight must be between 1 and 100"));
    }

    @Test
    @DisplayName("Email validation normalizes to lowercase and trims whitespace")
    void testEmailValidation() {
        List<String> errors = new ArrayList<>();
        String normalized = Validator.requireEmail("  USER.NAME@Domain.COM  ", "Email", errors);
        assertTrue(errors.isEmpty());
        assertEquals("user.name@domain.com", normalized);

        Validator.requireEmail("not-an-email", "Email", errors);
        assertFalse(errors.isEmpty());
    }

    @Test
    @DisplayName("Date validation ensures date is today or future")
    void testFutureOrToday() {
        List<String> errors = new ArrayList<>();
        Validator.requireFutureOrToday(LocalDate.now(), "Target date", errors);
        Validator.requireFutureOrToday(LocalDate.now().plusDays(5), "Target date", errors);
        assertTrue(errors.isEmpty());

        Validator.requireFutureOrToday(LocalDate.now().minusDays(1), "Target date", errors);
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Range check validates boundary conditions")
    void testRangeValidation() {
        List<String> errors = new ArrayList<>();
        Validator.requireRange(1, 1, 5, "Rating", errors);
        Validator.requireRange(5, 1, 5, "Rating", errors);
        Validator.requireRange(3, 1, 5, "Rating", errors);
        assertTrue(errors.isEmpty());

        Validator.requireRange(0, 1, 5, "Rating", errors);
        Validator.requireRange(6, 1, 5, "Rating", errors);
        assertEquals(2, errors.size());
    }
}
