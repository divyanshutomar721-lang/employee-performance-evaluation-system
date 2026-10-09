package com.epes.util;

import com.epes.config.DatabaseConfig;
import com.epes.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory that produces {@link Connection} instances using settings
 * from {@link DatabaseConfig}.
 *
 * <p>Usage:
 * <pre>
 *   ConnectionFactory cf = new ConnectionFactory();
 *   try (Connection c = cf.getConnection()) { … }
 * </pre>
 */
public class ConnectionFactory {

    private static final Logger LOG = Logger.getLogger(ConnectionFactory.class.getName());

    private final DatabaseConfig config;

    /**
     * Creates a factory that reads DB settings from the environment.
     * Explicitly loads the PostgreSQL JDBC driver.
     *
     * @throws DataAccessException if the driver class cannot be loaded
     */
    public ConnectionFactory() throws DataAccessException {
        this.config = new DatabaseConfig();
        loadDriver();
    }

    /**
     * Creates a factory with an externally supplied configuration.
     *
     * @param config database configuration to use
     * @throws DataAccessException if the driver class cannot be loaded
     */
    public ConnectionFactory(DatabaseConfig config) throws DataAccessException {
        this.config = config;
        loadDriver();
    }

    /**
     * Opens and returns a new database connection.
     *
     * @return an open {@link Connection}
     * @throws DataAccessException if the connection cannot be established
     */
    public Connection getConnection() throws DataAccessException {
        try {
            Connection conn = DriverManager.getConnection(
                    config.getUrl(),
                    config.getUser(),
                    config.getPassword());
            LOG.fine("Database connection obtained successfully.");
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to obtain database connection.", e);
        }
    }

    /**
     * Explicitly loads the PostgreSQL JDBC driver class.
     */
    private void loadDriver() throws DataAccessException {
        try {
            Class.forName("org.postgresql.Driver");
            LOG.fine("PostgreSQL JDBC driver loaded.");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException(
                    "PostgreSQL JDBC driver not found on the classpath.", e);
        }
    }
}
