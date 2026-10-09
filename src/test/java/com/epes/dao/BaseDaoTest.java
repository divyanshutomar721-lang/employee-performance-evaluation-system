package com.epes.dao;

import com.epes.config.DatabaseConfig;
import com.epes.exception.ConfigurationException;
import com.epes.exception.DataAccessException;
import com.epes.util.ConnectionFactory;
import com.epes.util.SchemaInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for database integration tests.
 *
 * <p>Ensures that tests only run when DB_PASSWORD is set and database is reachable.
 * Each test runs within a transaction that is rolled back in {@link #tearDown()},
 * ensuring no test data is committed to the database.</p>
 */
public abstract class BaseDaoTest {

    private static final Logger LOG = Logger.getLogger(BaseDaoTest.class.getName());
    protected static ConnectionFactory connectionFactory;
    protected Connection connection;

    @BeforeAll
    static void initDatabase() {
        String dbPw = System.getenv("DB_PASSWORD");
        Assumptions.assumeTrue(dbPw != null && !dbPw.isBlank(), "Skipping DB tests: DB_PASSWORD not configured.");

        try {
            connectionFactory = new ConnectionFactory();
            // Verify connection is working before running tests
            try (Connection c = connectionFactory.getConnection()) {
                Assumptions.assumeTrue(c.isValid(2), "Database connection is not valid.");
            }
            // Run schema initialization (idempotent)
            new SchemaInitializer(connectionFactory).initialize();
        } catch (ConfigurationException | DataAccessException e) {
            LOG.log(Level.WARNING, "Database unavailable, skipping DB integration tests: " + e.getMessage());
            Assumptions.assumeTrue(false, "Database unavailable: " + e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Database connection validation failed: " + e.getMessage());
            Assumptions.assumeTrue(false, "Database connection validation failed: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUpConnection() throws Exception {
        Assumptions.assumeTrue(connectionFactory != null, "ConnectionFactory is not initialized.");
        connection = connectionFactory.getConnection();
        connection.setAutoCommit(false);
    }

    @AfterEach
    void tearDown() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Error rolling back test transaction", e);
            }
        }
    }
}
