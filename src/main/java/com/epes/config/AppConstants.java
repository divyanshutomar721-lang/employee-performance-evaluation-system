package com.epes.config;

/**
 * Application-wide constants used across the EPES project.
 * All fields are compile-time constants or static finals.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class — prevent instantiation
    }

    /** Application display name. */
    public static final String APP_NAME = "Employee Performance Evaluation System";

    /** Context path under which the WAR is deployed. */
    public static final String CONTEXT_PATH = "/epes";

    /** HTTP session timeout in minutes. */
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    /** Default page encoding. */
    public static final String ENCODING = "UTF-8";

    /** ServletContext attribute key for the central application context. */
    public static final String APP_CONTEXT = "com.epes.APP_CONTEXT";
}
