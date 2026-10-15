package com.epes.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure utility class to construct safely escaped JSON strings for Chart.js canvas configurations.
 * Prevents XSS when rendered inside HTML attributes or scripts by escaping quotes, control characters,
 * and HTML entities (&lt;, &gt;, &amp;).
 */
public final class ChartJson {

    /** Default Rocket Stage color palette. */
    public static final List<String> STAGE_COLORS = List.of(
            "#64748b", // Launchpad (grey)
            "#2563eb", // Liftoff (blue)
            "#0891b2", // Orbit (teal)
            "#16a34a", // Cruise (green)
            "#d97706"  // Summit (gold)
    );

    /** Default Goal status color palette. */
    public static final List<String> GOAL_STATUS_COLORS = List.of(
            "#64748b", // Not Started
            "#2563eb", // In Progress
            "#16a34a", // Completed
            "#dc2626"  // Missed
    );

    private ChartJson() {
        // Pure utility class
    }

    /**
     * Builds a Chart.js line chart configuration.
     *
     * @param labels       category labels
     * @param values       numeric data series
     * @param datasetLabel dataset legend title
     * @param yMin         y-axis lower bound
     * @param yMax         y-axis upper bound
     * @return escaped JSON string
     */
    public static String lineChart(List<String> labels, List<? extends Number> values,
                                   String datasetLabel, double yMin, double yMax) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"line\",\"data\":{");
        appendLabels(sb, labels);
        sb.append(",\"datasets\":[{");
        sb.append("\"label\":\"").append(escapeString(datasetLabel)).append("\",");
        sb.append("\"borderColor\":\"#2563eb\",");
        sb.append("\"backgroundColor\":\"rgba(37, 99, 235, 0.1)\",");
        sb.append("\"fill\":true,");
        sb.append("\"tension\":0.2,");
        appendData(sb, values);
        sb.append("}]},\"options\":{\"responsive\":true,\"maintainAspectRatio\":false,");
        sb.append("\"scales\":{\"y\":{\"min\":").append(formatNumber(yMin))
                .append(",\"max\":").append(formatNumber(yMax)).append("}}}}");
        return sb.toString();
    }

    /**
     * Builds a Chart.js bar chart configuration with a single color.
     */
    public static String barChart(List<String> labels, List<? extends Number> values,
                                  String datasetLabel, double yMin, double yMax) {
        return barChart(labels, values, datasetLabel, yMin, yMax, null);
    }

    /**
     * Builds a Chart.js bar chart configuration with optional custom bar colors.
     */
    public static String barChart(List<String> labels, List<? extends Number> values,
                                  String datasetLabel, double yMin, double yMax,
                                  List<String> backgroundColors) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"bar\",\"data\":{");
        appendLabels(sb, labels);
        sb.append(",\"datasets\":[{");
        sb.append("\"label\":\"").append(escapeString(datasetLabel)).append("\",");
        if (backgroundColors != null && !backgroundColors.isEmpty()) {
            sb.append("\"backgroundColor\":");
            appendStringArray(sb, backgroundColors);
            sb.append(",");
        } else {
            sb.append("\"backgroundColor\":\"#2563eb\",");
        }
        appendData(sb, values);
        sb.append("}]},\"options\":{\"responsive\":true,\"maintainAspectRatio\":false,");
        sb.append("\"scales\":{\"y\":{\"min\":").append(formatNumber(yMin))
                .append(",\"max\":").append(formatNumber(yMax)).append("}}}}");
        return sb.toString();
    }

    /**
     * Builds a Chart.js doughnut chart configuration using default goal colors.
     */
    public static String doughnutChart(List<String> labels, List<? extends Number> values, String datasetLabel) {
        return doughnutChart(labels, values, datasetLabel, GOAL_STATUS_COLORS);
    }

    /**
     * Builds a Chart.js doughnut chart configuration with custom slice colors.
     */
    public static String doughnutChart(List<String> labels, List<? extends Number> values,
                                       String datasetLabel, List<String> backgroundColors) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"doughnut\",\"data\":{");
        appendLabels(sb, labels);
        sb.append(",\"datasets\":[{");
        sb.append("\"label\":\"").append(escapeString(datasetLabel)).append("\",");
        if (backgroundColors != null && !backgroundColors.isEmpty()) {
            sb.append("\"backgroundColor\":");
            appendStringArray(sb, backgroundColors);
            sb.append(",");
        }
        appendData(sb, values);
        sb.append("}]},\"options\":{\"responsive\":true,\"maintainAspectRatio\":false}}");
        return sb.toString();
    }

    /**
     * Escapes a string for safe inclusion in JSON strings and HTML attributes.
     * Escapes quotes, backslashes, ASCII control characters, and &lt;, &gt;, &amp;.
     *
     * @param input the input string
     * @return safely escaped string, or empty string if input is null
     */
    public static String escapeString(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '<':
                    sb.append("\\u003c");
                    break;
                case '>':
                    sb.append("\\u003e");
                    break;
                case '&':
                    sb.append("\\u0026");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20 || c == 0x7F) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    private static void appendLabels(StringBuilder sb, List<String> labels) {
        sb.append("\"labels\":[");
        if (labels != null && !labels.isEmpty()) {
            for (int i = 0; i < labels.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeString(labels.get(i))).append("\"");
            }
        }
        sb.append("]");
    }

    private static void appendData(StringBuilder sb, List<? extends Number> values) {
        sb.append("\"data\":[");
        if (values != null && !values.isEmpty()) {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) sb.append(",");
                Number val = values.get(i);
                sb.append(val != null ? formatNumber(val.doubleValue()) : "0");
            }
        }
        sb.append("]");
    }

    private static void appendStringArray(StringBuilder sb, List<String> items) {
        sb.append("[");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeString(items.get(i))).append("\"");
            }
        }
        sb.append("]");
    }

    private static String formatNumber(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format(java.util.Locale.US, "%.2f", d);
        }
    }
}
