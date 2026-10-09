package com.epes.dao.impl;

import com.epes.dao.GoalDao;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.model.Goal;
import com.epes.model.GoalStatus;
import com.epes.model.GoalType;
import com.epes.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link GoalDao}.
 */
public class GoalDaoImpl implements GoalDao {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private final ConnectionFactory connectionFactory;

    /**
     * Default constructor.
     *
     * @throws DataAccessException if connection factory initialization fails
     */
    public GoalDaoImpl() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Parameterized constructor.
     *
     * @param connectionFactory connection factory
     */
    public GoalDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Goal create(Goal goal) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return create(conn, goal);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for creating goal", e);
        }
    }

    @Override
    public Goal create(Connection conn, Goal goal) throws DataAccessException {
        String sql = "INSERT INTO goals (employee_id, set_by, goal_type, description, target_date, status, progress_percent, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
        LocalDateTime now = goal.getCreatedAt() != null ? goal.getCreatedAt() : LocalDateTime.now();
        GoalStatus status = goal.getStatus() != null ? goal.getStatus() : GoalStatus.NOT_STARTED;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, goal.getEmployeeId());
            ps.setLong(2, goal.getSetBy());
            ps.setString(3, goal.getGoalType().name());
            ps.setString(4, goal.getDescription());
            ps.setDate(5, Date.valueOf(goal.getTargetDate()));
            ps.setString(6, status.name());
            ps.setInt(7, goal.getProgressPercent());
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    goal.setId(rs.getLong("id"));
                    goal.setStatus(status);
                    goal.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    goal.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return goal;
                } else {
                    throw new DataAccessException("Creating goal failed, no ID returned.");
                }
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to create goal", e);
        }
    }

    @Override
    public Optional<Goal> findById(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findById goal", e);
        }
    }

    @Override
    public Optional<Goal> findById(Connection conn, Long id) throws DataAccessException {
        String sql = "SELECT id, employee_id, set_by, goal_type, description, target_date, status, progress_percent, created_at, updated_at " +
                "FROM goals WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find goal by ID: " + id, e);
        }
    }

    @Override
    public List<Goal> findByEmployee(Long employeeId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByEmployee(conn, employeeId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByEmployee goals", e);
        }
    }

    @Override
    public List<Goal> findByEmployee(Connection conn, Long employeeId) throws DataAccessException {
        String sql = "SELECT id, employee_id, set_by, goal_type, description, target_date, status, progress_percent, created_at, updated_at " +
                "FROM goals WHERE employee_id = ? ORDER BY target_date ASC, id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Goal> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find goals for employee ID: " + employeeId, e);
        }
    }

    @Override
    public List<Goal> findBySetBy(Long setById) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findBySetBy(conn, setById);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findBySetBy goals", e);
        }
    }

    @Override
    public List<Goal> findBySetBy(Connection conn, Long setById) throws DataAccessException {
        String sql = "SELECT id, employee_id, set_by, goal_type, description, target_date, status, progress_percent, created_at, updated_at " +
                "FROM goals WHERE set_by = ? ORDER BY target_date ASC, id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, setById);
            try (ResultSet rs = ps.executeQuery()) {
                List<Goal> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find goals set by user ID: " + setById, e);
        }
    }

    @Override
    public Goal update(Goal goal) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return update(conn, goal);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for update goal", e);
        }
    }

    @Override
    public Goal update(Connection conn, Goal goal) throws DataAccessException {
        String sql = "UPDATE goals SET description = ?, target_date = ?, status = ?, progress_percent = ?, updated_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getDescription());
            ps.setDate(2, Date.valueOf(goal.getTargetDate()));
            ps.setString(3, goal.getStatus().name());
            ps.setInt(4, goal.getProgressPercent());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setLong(6, goal.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Goal not found for update with ID: " + goal.getId());
            }
            goal.setUpdatedAt(now);
            return goal;
        } catch (SQLException e) {
            throw handleSqlException("Failed to update goal ID: " + goal.getId(), e);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            delete(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for delete goal", e);
        }
    }

    @Override
    public void delete(Connection conn, Long id) throws DataAccessException {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Goal not found for delete with ID: " + id);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to delete goal ID: " + id, e);
        }
    }

    @Override
    public List<Goal> findDueBefore(LocalDate date) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findDueBefore(conn, date);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findDueBefore goals", e);
        }
    }

    @Override
    public List<Goal> findDueBefore(Connection conn, LocalDate date) throws DataAccessException {
        String sql = "SELECT id, employee_id, set_by, goal_type, description, target_date, status, progress_percent, created_at, updated_at " +
                "FROM goals WHERE status != 'COMPLETED' AND target_date <= ? ORDER BY target_date ASC, id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                List<Goal> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find goals due before: " + date, e);
        }
    }

    private Goal mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long employeeId = rs.getLong("employee_id");
        Long setBy = rs.getLong("set_by");
        GoalType goalType = GoalType.valueOf(rs.getString("goal_type"));
        String description = rs.getString("description");
        LocalDate targetDate = rs.getDate("target_date").toLocalDate();
        GoalStatus status = GoalStatus.valueOf(rs.getString("status"));
        int progressPercent = rs.getInt("progress_percent");
        Timestamp createdTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedTs != null ? updatedTs.toLocalDateTime() : null;

        return new Goal(id, employeeId, setBy, goalType, description, targetDate, status, progressPercent, createdAt, updatedAt);
    }

    private DataAccessException handleSqlException(String message, SQLException e) {
        if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
            return new DuplicateEntityException(message + " (Duplicate entity / unique constraint violation)", e);
        }
        return new DataAccessException(message + ": " + e.getMessage(), e);
    }
}
