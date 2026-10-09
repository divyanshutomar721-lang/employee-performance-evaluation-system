package com.epes.util;

import com.epes.config.DatabaseConfig;
import com.epes.dao.EvaluationDao;
import com.epes.dao.UserDao;
import com.epes.dao.impl.EvaluationDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.ConfigurationException;
import com.epes.exception.DataAccessException;
import com.epes.model.Employee;
import com.epes.model.Evaluation;
import com.epes.model.EvaluationScore;
import com.epes.model.Manager;
import com.epes.model.RocketStage;
import com.epes.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test proving atomic rollback behavior in {@link TransactionManager}.
 *
 * <p>Fixture Strategy (Cleanest Approach):
 * Prerequisite User rows (Manager and Employee) are created for the test and tracked by ID.
 * The test invokes {@link TransactionManager#executeInTransaction(TransactionalWork)} to insert
 * an Evaluation and deliberately throw an exception before completion.
 * After verifying that no evaluation was committed, the {@link #cleanupFixture()} method runs
 * in {@code @AfterEach} to delete the prerequisite user rows, ensuring the database is left completely clean.</p>
 */
class TransactionRollbackTest {

    private static ConnectionFactory connectionFactory;
    private Long testManagerId;
    private Long testEmployeeId;

    @BeforeAll
    static void init() {
        String dbPw = System.getenv("DB_PASSWORD");
        Assumptions.assumeTrue(dbPw != null && !dbPw.isBlank(), "Skipping test: DB_PASSWORD not configured.");

        try {
            connectionFactory = new ConnectionFactory();
            try (Connection c = connectionFactory.getConnection()) {
                Assumptions.assumeTrue(c.isValid(2), "Database connection invalid.");
            }
            new SchemaInitializer(connectionFactory).initialize();
        } catch (ConfigurationException | DataAccessException | SQLException e) {
            Assumptions.assumeTrue(false, "Database unavailable: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanupFixture() {
        // Clean up prerequisite fixture rows so the database remains pristine
        if (connectionFactory != null) {
            try (Connection conn = connectionFactory.getConnection()) {
                if (testEmployeeId != null) {
                    try (var ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        ps.setLong(1, testEmployeeId);
                        ps.executeUpdate();
                    }
                }
                if (testManagerId != null) {
                    try (var ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        ps.setLong(1, testManagerId);
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException | DataAccessException e) {
                // Ignore cleanup errors during teardown
            }
        }
    }

    @Test
    @DisplayName("TransactionManager rolls back all changes when an exception is thrown inside work block")
    void testTransactionRollbackOnException() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);
        EvaluationDao evaluationDao = new EvaluationDaoImpl(connectionFactory);
        TransactionManager tm = new TransactionManager(connectionFactory);

        // 1. Create prerequisite users
        Manager manager = new Manager();
        manager.setEmail("tx.manager@epes.local");
        manager.setFullName("Tx Manager");
        User savedManager = userDao.create(manager);
        testManagerId = savedManager.getId();

        Employee employee = new Employee();
        employee.setEmail("tx.employee@epes.local");
        employee.setFullName("Tx Employee");
        User savedEmployee = userDao.create(employee);
        testEmployeeId = savedEmployee.getId();

        // 2. Execute transactional work that inserts an evaluation then throws an exception
        assertThrows(DataAccessException.class, () -> {
            tm.executeInTransaction(conn -> {
                Evaluation eval = new Evaluation();
                eval.setEmployeeId(savedEmployee.getId());
                eval.setManagerId(savedManager.getId());
                eval.setPeriodLabel("Q4 2026");
                eval.setOverallScore(new BigDecimal("92.00"));
                eval.setOverallStage(RocketStage.SUMMIT);
                eval.setSummaryComment("Top performer");
                eval.setCreatedAt(LocalDateTime.now());

                List<EvaluationScore> scores = new ArrayList<>();
                EvaluationScore s = new EvaluationScore();
                s.setCriterionName("Leadership");
                s.setWeight(100);
                s.setRating(5);
                scores.add(s);

                // This insert happens inside the transaction
                evaluationDao.saveWithScores(conn, eval, scores);

                // Deliberately trigger failure after insert
                throw new IllegalStateException("Simulated operational failure to trigger transaction rollback");
            });
        });

        // 3. Assert that NO evaluation row exists in the database
        List<Evaluation> evals = evaluationDao.findByEmployee(savedEmployee.getId());
        assertTrue(evals.isEmpty(), "Evaluation row must not exist due to transactional rollback.");
    }
}
