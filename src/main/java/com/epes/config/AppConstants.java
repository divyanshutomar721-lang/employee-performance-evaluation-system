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

    // ── Session attribute names ──────────────────────────────────────────

    /** Session attribute: the authenticated {@code User} object. */
    public static final String SESSION_USER = "currentUser";

    /** Session attribute: CSRF token string. */
    public static final String SESSION_CSRF = "csrfToken";

    /** Session attribute: list of {@link com.epes.util.FlashMessage}s. */
    public static final String SESSION_FLASH = "flashMessages";

    // ── Request attribute names ──────────────────────────────────────────

    /** Request attribute: CSRF token exposed to JSP forms. */
    public static final String REQ_CSRF_TOKEN = "csrfToken";

    /** Request attribute: error status code (Integer). */
    public static final String REQ_ERROR_STATUS = "errorStatus";

    /** Request attribute: error display message (String). */
    public static final String REQ_ERROR_MESSAGE = "errorMessage";

    /** Request attribute: validation error messages (List&lt;String&gt;). */
    public static final String REQ_VALIDATION_ERRORS = "validationErrors";

    /** Request attribute: consumed flash messages (List&lt;FlashMessage&gt;). */
    public static final String REQ_FLASH_MESSAGES = "flashMessages";
}
