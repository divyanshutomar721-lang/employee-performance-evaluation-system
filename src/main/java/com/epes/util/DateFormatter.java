package com.epes.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * Utility for formatting Java date/time objects into user-friendly strings for JSP rendering.
 * Used via custom EL functions in JSPs.
 */
public final class DateFormatter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    private DateFormatter() {}

    /**
     * Formats a {@link LocalDate} or {@link LocalDateTime} as "dd MMM yyyy" (e.g. "15 Oct 2026").
     *
     * @param temporal date or datetime object, may be null
     * @return formatted date string, or "-" if null
     */
    public static String formatDate(TemporalAccessor temporal) {
        if (temporal == null) {
            return "-";
        }
        if (temporal instanceof LocalDateTime ldt) {
            return DATE_FMT.format(ldt.toLocalDate());
        }
        return DATE_FMT.format(temporal);
    }

    /**
     * Formats a {@link LocalDateTime} as "dd MMM yyyy HH:mm" (e.g. "15 Oct 2026 14:30").
     * If passed a {@link LocalDate}, formats as "dd MMM yyyy".
     *
     * @param temporal date or datetime object, may be null
     * @return formatted datetime string, or "-" if null
     */
    public static String formatDateTime(TemporalAccessor temporal) {
        if (temporal == null) {
            return "-";
        }
        if (temporal instanceof LocalDate ld) {
            return DATE_FMT.format(ld);
        }
        return DATE_TIME_FMT.format(temporal);
    }
}
