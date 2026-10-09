package com.epes.dao.impl;

import com.epes.dao.FeedbackDao;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.model.Feedback;
import com.epes.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link FeedbackDao}.
 */
public class FeedbackDaoImpl implements FeedbackDao {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private final ConnectionFactory connectionFactory;

    /**
     * Default constructor.
     *
     * @throws DataAccessException if connection factory initialization fails
     */
    public FeedbackDaoImpl() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Parameterized constructor.
     *
     * @param connectionFactory connection factory
     */
    public FeedbackDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Feedback create(Feedback feedback) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return create(conn, feedback);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for creating feedback", e);
        }
    }

    @Override
    public Feedback create(Connection conn, Feedback feedback) throws DataAccessException {
        String sql = "INSERT INTO feedback (employee_id, manager_id, evaluation_id, comments, rating, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        LocalDateTime now = feedback.getCreatedAt() != null ? feedback.getCreatedAt() : LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, feedback.getEmployeeId());
            ps.setLong(2, feedback.getManagerId());
            if (feedback.getEvaluationId() != null) {
                ps.setLong(3, feedback.getEvaluationId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setString(4, feedback.getComments());
            ps.setInt(5, feedback.getRating());
            ps.setTimestamp(6, Timestamp.valueOf(now));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    feedback.setId(rs.getLong("id"));
                    feedback.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return feedback;
                } else {
                    throw new DataAccessException("Creating feedback failed, no ID returned.");
                }
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to create feedback", e);
        }
    }

    @Override
    public Optional<Feedback> findById(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findById feedback", e);
        }
    }

    @Override
    public Optional<Feedback> findById(Connection conn, Long id) throws DataAccessException {
        String sql = "SELECT id, employee_id, manager_id, evaluation_id, comments, rating, created_at FROM feedback WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find feedback by ID: " + id, e);
        }
    }

    @Override
    public List<Feedback> findByEmployee(Long employeeId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByEmployee(conn, employeeId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByEmployee feedback", e);
        }
    }

    @Override
    public List<Feedback> findByEmployee(Connection conn, Long employeeId) throws DataAccessException {
        String sql = "SELECT id, employee_id, manager_id, evaluation_id, comments, rating, created_at " +
                "FROM feedback WHERE employee_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Feedback> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find feedback for employee ID: " + employeeId, e);
        }
    }

    @Override
    public List<Feedback> findByManager(Long managerId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByManager(conn, managerId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByManager feedback", e);
        }
    }

    @Override
    public List<Feedback> findByManager(Connection conn, Long managerId) throws DataAccessException {
        String sql = "SELECT id, employee_id, manager_id, evaluation_id, comments, rating, created_at " +
                "FROM feedback WHERE manager_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Feedback> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find feedback for manager ID: " + managerId, e);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            delete(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for delete feedback", e);
        }
    }

    @Override
    public void delete(Connection conn, Long id) throws DataAccessException {
        String sql = "DELETE FROM feedback WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Feedback not found for delete with ID: " + id);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to delete feedback ID: " + id, e);
        }
    }

    private Feedback mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long employeeId = rs.getLong("employee_id");
        Long managerId = rs.getLong("manager_id");
        Long evalId = rs.getObject("evaluation_id", Long.class);
        String comments = rs.getString("comments");
        int rating = rs.getInt("rating");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        return new Feedback(id, employeeId, managerId, evalId, comments, rating, createdAt);
    }

    private DataAccessException handleSqlException(String message, SQLException e) {
        if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
            return new DuplicateEntityException(message + " (Duplicate entity / unique constraint violation)", e);
        }
        return new DataAccessException(message + ": " + e.getMessage(), e);
    }
}
