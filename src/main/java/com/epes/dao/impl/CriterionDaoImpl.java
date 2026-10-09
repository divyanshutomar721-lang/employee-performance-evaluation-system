package com.epes.dao.impl;

import com.epes.dao.CriterionDao;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.model.Criterion;
import com.epes.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link CriterionDao}.
 */
public class CriterionDaoImpl implements CriterionDao {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private final ConnectionFactory connectionFactory;

    /**
     * Default constructor.
     *
     * @throws DataAccessException if connection factory initialization fails
     */
    public CriterionDaoImpl() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Parameterized constructor.
     *
     * @param connectionFactory connection factory
     */
    public CriterionDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Criterion create(Criterion criterion) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return create(conn, criterion);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for creating criterion", e);
        }
    }

    @Override
    public Criterion create(Connection conn, Criterion criterion) throws DataAccessException {
        String sql = "INSERT INTO criteria (manager_id, name, description, weight, active) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, criterion.getManagerId());
            ps.setString(2, criterion.getName());
            ps.setString(3, criterion.getDescription());
            ps.setInt(4, criterion.getWeight());
            ps.setBoolean(5, criterion.isActive());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    criterion.setId(rs.getLong("id"));
                    return criterion;
                } else {
                    throw new DataAccessException("Creating criterion failed, no ID returned.");
                }
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to create criterion: " + criterion.getName(), e);
        }
    }

    @Override
    public Optional<Criterion> findById(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findById criterion", e);
        }
    }

    @Override
    public Optional<Criterion> findById(Connection conn, Long id) throws DataAccessException {
        String sql = "SELECT id, manager_id, name, description, weight, active FROM criteria WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find criterion by ID: " + id, e);
        }
    }

    @Override
    public List<Criterion> findByManager(Long managerId, boolean activeOnly) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByManager(conn, managerId, activeOnly);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByManager criterion", e);
        }
    }

    @Override
    public List<Criterion> findByManager(Connection conn, Long managerId, boolean activeOnly) throws DataAccessException {
        String sql = activeOnly
                ? "SELECT id, manager_id, name, description, weight, active FROM criteria WHERE manager_id = ? AND active = TRUE ORDER BY id ASC"
                : "SELECT id, manager_id, name, description, weight, active FROM criteria WHERE manager_id = ? ORDER BY id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Criterion> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find criteria for manager ID: " + managerId, e);
        }
    }

    @Override
    public Criterion update(Criterion criterion) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return update(conn, criterion);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for update criterion", e);
        }
    }

    @Override
    public Criterion update(Connection conn, Criterion criterion) throws DataAccessException {
        String sql = "UPDATE criteria SET name = ?, description = ?, weight = ?, active = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, criterion.getName());
            ps.setString(2, criterion.getDescription());
            ps.setInt(3, criterion.getWeight());
            ps.setBoolean(4, criterion.isActive());
            ps.setLong(5, criterion.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Criterion not found for update with ID: " + criterion.getId());
            }
            return criterion;
        } catch (SQLException e) {
            throw handleSqlException("Failed to update criterion ID: " + criterion.getId(), e);
        }
    }

    @Override
    public void deactivate(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            deactivate(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for deactivate criterion", e);
        }
    }

    @Override
    public void deactivate(Connection conn, Long id) throws DataAccessException {
        String sql = "UPDATE criteria SET active = FALSE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Criterion not found for deactivation with ID: " + id);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to deactivate criterion ID: " + id, e);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            delete(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for delete criterion", e);
        }
    }

    @Override
    public void delete(Connection conn, Long id) throws DataAccessException {
        String sql = "DELETE FROM criteria WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Criterion not found for delete with ID: " + id);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to delete criterion ID: " + id, e);
        }
    }

    private Criterion mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long managerId = rs.getLong("manager_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int weight = rs.getInt("weight");
        boolean active = rs.getBoolean("active");

        return new Criterion(id, managerId, name, description, weight, active);
    }

    private DataAccessException handleSqlException(String message, SQLException e) {
        if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
            return new DuplicateEntityException(message + " (Duplicate entity / unique constraint violation)", e);
        }
        return new DataAccessException(message + ": " + e.getMessage(), e);
    }
}
