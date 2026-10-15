package com.epes.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ChartJson}.
 */
class ChartJsonTest {

    @Test
    @DisplayName("Escape special characters, quotes, HTML tags and backslashes")
    void testEscaping() {
        assertEquals("", ChartJson.escapeString(null));
        assertEquals("", ChartJson.escapeString(""));
        assertEquals("Hello \\\"World\\\"", ChartJson.escapeString("Hello \"World\""));
        assertEquals("Path\\\\to\\\\file", ChartJson.escapeString("Path\\to\\file"));
        assertEquals("\\u003c/script\\u003e", ChartJson.escapeString("</script>"));
        assertEquals("Tom \\u0026 Jerry", ChartJson.escapeString("Tom & Jerry"));
        assertEquals("Line1\\nLine2", ChartJson.escapeString("Line1\nLine2"));
        assertEquals("Tab\\tSpace", ChartJson.escapeString("Tab\tSpace"));
    }

    @Test
    @DisplayName("Line chart JSON structure and escaping")
    void testLineChart() {
        List<String> labels = new ArrayList<>();
        labels.add("Q1 <2026>");
        labels.add(null);
        labels.add("Q3 \"Final\"");

        List<Double> scores = List.of(65.5, 75.0, 90.25);
        String json = ChartJson.lineChart(labels, scores, "Overall & Trend", 0, 100);

        assertNotNull(json);
        assertTrue(json.startsWith("{\"type\":\"line\""));
        assertTrue(json.contains("\"labels\":[\"Q1 \\u003c2026\\u003e\",\"\",\"Q3 \\\"Final\\\"\"]"));
        assertTrue(json.contains("\"label\":\"Overall \\u0026 Trend\""));
        assertTrue(json.contains("\"data\":[65.50,75,90.25]"));
        assertTrue(json.contains("\"min\":0"));
        assertTrue(json.contains("\"max\":100"));
        assertFalse(json.contains("<"));
        assertFalse(json.contains(">"));
    }

    @Test
    @DisplayName("Bar chart structure and empty lists")
    void testBarChart() {
        String json = ChartJson.barChart(List.of(), List.of(), "Empty Bar", 0, 5);
        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"bar\""));
        assertTrue(json.contains("\"labels\":[]"));
        assertTrue(json.contains("\"data\":[]"));
        assertTrue(json.contains("\"min\":0"));
        assertTrue(json.contains("\"max\":5"));

        // Custom colors
        String coloredJson = ChartJson.barChart(
                List.of("Launchpad", "Liftoff"),
                List.of(1, 2),
                "Stages",
                0, 5,
                List.of("#64748b", "#2563eb")
        );
        assertTrue(coloredJson.contains("\"backgroundColor\":[\"#64748b\",\"#2563eb\"]"));
    }

    @Test
    @DisplayName("Doughnut chart structure")
    void testDoughnutChart() {
        List<String> labels = List.of("Not Started", "In Progress", "Completed", "Missed");
        List<Long> values = List.of(1L, 2L, 5L, 0L);

        String json = ChartJson.doughnutChart(labels, values, "Goal Status");
        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"doughnut\""));
        assertTrue(json.contains("\"labels\":[\"Not Started\",\"In Progress\",\"Completed\",\"Missed\"]"));
        assertTrue(json.contains("\"data\":[1,2,5,0]"));
    }
}
