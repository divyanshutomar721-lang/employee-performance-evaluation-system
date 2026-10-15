package com.epes;

import com.epes.exception.ConfigurationException;
import com.epes.exception.DataAccessException;
import com.epes.util.ConnectionFactory;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit test that purges all smoke-test artifacts matching '%@epes.test' and test teams in FK-safe order,
 * then asserts zero rows remain.
 */
public class SmokeDataCleanupTest {

    @Test
    @DisplayName("Clean up all smoke test artifacts and assert zero rows remain")
    void cleanupSmokeTestData() throws Exception {
        String dbPw = System.getenv("DB_PASSWORD");
        Assumptions.assumeTrue(dbPw != null && !dbPw.isBlank(), "DB_PASSWORD must be configured");

        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection conn = connectionFactory.getConnection()) {
            // Delete in foreign-key safe order
            executeUpdate(conn, "DELETE FROM feedback WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM evaluation_scores WHERE evaluation_id IN (SELECT id FROM evaluations WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test'))");
            executeUpdate(conn, "DELETE FROM evaluations WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM goals WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR set_by IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM criteria WHERE manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test')");
            executeUpdate(conn, "DELETE FROM team_members WHERE employee_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR team_id IN (SELECT id FROM teams WHERE name LIKE '%Smoke%' OR name LIKE 'T-%' OR manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test'))");
            executeUpdate(conn, "DELETE FROM teams WHERE manager_id IN (SELECT id FROM users WHERE email LIKE '%@epes.test') OR name LIKE '%Smoke%' OR name LIKE 'T-%'");
            executeUpdate(conn, "DELETE FROM users WHERE email LIKE '%@epes.test'");

            // Assert zero rows remain
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email LIKE '%@epes.test'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(0L, rs.getLong(1), "Expected 0 users with email like '%@epes.test'");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM teams WHERE name LIKE '%Smoke%' OR name LIKE 'T-%'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(0L, rs.getLong(1), "Expected 0 teams with smoke/test name");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM evaluations WHERE period_label LIKE 'T-%'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(0L, rs.getLong(1), "Expected 0 evaluations with test period label");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM goals WHERE description LIKE '%Smoke%' OR description LIKE '%test%'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(0L, rs.getLong(1), "Expected 0 goals with smoke/test description");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM feedback WHERE comments LIKE '%Smoke%' OR comments LIKE '%test%'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(0L, rs.getLong(1), "Expected 0 feedback with smoke/test comments");
                }
            }
        }
    }

    private static void executeUpdate(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ignored) {
            // Table may be empty or error ignored
        }
    }
}
