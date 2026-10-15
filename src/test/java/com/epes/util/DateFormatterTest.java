package com.epes.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateFormatterTest {

    @Test
    @DisplayName("formatDate formats LocalDate as 'dd MMM yyyy'")
    void testFormatDateLocalDate() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        assertEquals("15 Oct 2026", DateFormatter.formatDate(date));
    }

    @Test
    @DisplayName("formatDate handles LocalDateTime by extracting date")
    void testFormatDateLocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 3, 5, 14, 30);
        assertEquals("05 Mar 2026", DateFormatter.formatDate(dt));
    }

    @Test
    @DisplayName("formatDate returns '-' for null")
    void testFormatDateNull() {
        assertEquals("-", DateFormatter.formatDate(null));
    }

    @Test
    @DisplayName("formatDateTime formats LocalDateTime as 'dd MMM yyyy HH:mm'")
    void testFormatDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 10, 15, 9, 5);
        assertEquals("15 Oct 2026 09:05", DateFormatter.formatDateTime(dt));
    }

    @Test
    @DisplayName("formatDateTime returns '-' for null")
    void testFormatDateTimeNull() {
        assertEquals("-", DateFormatter.formatDateTime(null));
    }

    @Test
    @DisplayName("formatDecimal formats number to two decimal places")
    void testFormatDecimal() {
        assertEquals("88.00", DateFormatter.formatDecimal(88.0));
        assertEquals("48.50", DateFormatter.formatDecimal(48.5));
        assertEquals("3.14", DateFormatter.formatDecimal(3.14159));
        assertEquals("0.00", DateFormatter.formatDecimal(0));
        assertEquals("-", DateFormatter.formatDecimal(null));
    }
}
