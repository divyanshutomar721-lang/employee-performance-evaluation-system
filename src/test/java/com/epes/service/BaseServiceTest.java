package com.epes.service;

import com.epes.config.AppContext;
import com.epes.exception.ConfigurationException;
import com.epes.exception.DataAccessException;
import com.epes.util.ConnectionFactory;
import com.epes.util.SchemaInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base class for service integration tests.
 *
 * <p>Initializes {@link AppContext} and provides FK-safe test data teardown
 * guaranteeing no test records remain in the database after test execution.</p>
 */
public abstract class BaseServiceTest {

    private static final Logger LOG = Logger.getLogger(BaseServiceTest.class.getName());

    protected static ConnectionFactory connectionFactory;
    protected static AppContext appContext;

    @BeforeAll
    static void initServiceSuite() {
        String dbPw = System.getenv("DB_PASSWORD");
        Assumptions.assumeTrue(dbPw != null && !dbPw.isBlank(), "Skipping service tests: DB_PASSWORD not configured.");

        try {
            connectionFactory = new ConnectionFactory();
            try (Connection c = connectionFactory.getConnection()) {
                Assumptions.assumeTrue(c.isValid(2), "Database connection invalid.");
            }
            new SchemaInitializer(connectionFactory).initialize();
            appContext = new AppContext(connectionFactory);
        } catch (ConfigurationException | DataAccessException | SQLException e) {
            LOG.log(Level.WARNING, "Database unavailable, skipping service integration tests: " + e.getMessage());
            Assumptions.assumeTrue(false, "Database unavailable: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanAfterEach() {
        cleanTestData();
    }

    @AfterAll
    static void verifyNoTestArtifactsRemain() {
        if (connectionFactory != null) {
            cleanTestData();
            try (Connection conn = connectionFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email LIKE '%@epes.test'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    assertEquals(0L, rs.getLong(1), "Database must contain ZERO test rows after test completion");
                }
            } catch (SQLException | DataAccessException e) {
                LOG.log(Level.WARNING, "Failed verifying zero test rows: " + e.getMessage());
            }
        }
    }

    protected static String randomEmail() {
        return "t-" + UUID.randomUUID().toString().substring(0, 8) + "@epes.test";
    }

    protected static String randomTeamName() {
        return "T-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String randomPeriod() {
        return "T-Period-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private static void cleanTestData() {
        if (connectionFactory == null) return;
        try (Connection conn = connectionFactory.getConnection()) {
            // Delete in foreign-key safe order
            executeUpdate(conn, "DELETE FROM feedback WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM evaluations WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM goals WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR set_by IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM criteria WHERE manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM team_members WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR team_id IN (SELECT id FROM teams WHERE name LIKE 'T-%')");
            executeUpdate(conn, "DELETE FROM teams WHERE manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR name LIKE 'T-%'");
            executeUpdate(conn, "DELETE FROM users WHERE email LIKE '%@epes.test'");
        } catch (SQLException | DataAccessException e) {
            LOG.log(Level.WARNING, "Error during test data cleanup: " + e.getMessage());
        }
    }

    private static void executeUpdate(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
