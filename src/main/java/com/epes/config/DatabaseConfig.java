package com.epes.config;

import com.epes.exception.ConfigurationException;

/**
 * Holds database connection settings read from environment variables.
 *
 * <ul>
 *   <li>{@code DB_URL}      — JDBC URL (default {@code jdbc:postgresql://localhost:5432/epes})</li>
 *   <li>{@code DB_USER}     — database user (default {@code postgres})</li>
 *   <li>{@code DB_PASSWORD} — database password (<b>required</b>, no default)</li>
 * </ul>
 *
 * An instance is immutable once created.
 */
public class DatabaseConfig {

    private static final String DEFAULT_URL  = "jdbc:postgresql://localhost:5432/epes";
    private static final String DEFAULT_USER = "postgres";

    private final String url;
    private final String user;
    private final String password;

    /**
     * Reads configuration from the process environment.
     *
     * @throws ConfigurationException if {@code DB_PASSWORD} is not set
     */
    public DatabaseConfig() {
        this.url  = envOrDefault("DB_URL",  DEFAULT_URL);
        this.user = envOrDefault("DB_USER", DEFAULT_USER);

        String pw = System.getenv("DB_PASSWORD");
        if (pw == null || pw.isBlank()) {
            throw new ConfigurationException(
                    "Environment variable DB_PASSWORD is not set. "
                  + "Set it before starting the application.");
        }
        this.password = pw;
    }

    /** Returns the JDBC URL. */
    public String getUrl()      { return url; }

    /** Returns the database user name. */
    public String getUser()     { return user; }

    /** Returns the database password. */
    public String getPassword() { return password; }

    /**
     * Returns the environment variable value, or {@code defaultValue} if unset/blank.
     */
    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
