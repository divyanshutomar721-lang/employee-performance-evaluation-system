package com.epes.dao.impl;

import com.epes.dao.UserDao;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import com.epes.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link UserDao}.
 */
public class UserDaoImpl implements UserDao {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private final ConnectionFactory connectionFactory;

    /**
     * Constructs a UserDaoImpl using the default ConnectionFactory.
     *
     * @throws DataAccessException if connection factory initialization fails
     */
    public UserDaoImpl() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Constructs a UserDaoImpl with an explicit ConnectionFactory.
     *
     * @param connectionFactory connection factory
     */
    public UserDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public User create(User user) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return create(conn, user);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for creating user", e);
        }
    }

    @Override
    public User create(Connection conn, User user) throws DataAccessException {
        String sql = "INSERT INTO users (email, full_name, role, active, created_at) VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";
        String normalizedEmail = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : null;
        LocalDateTime now = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.setTimestamp(5, Timestamp.valueOf(now));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    user.setEmail(normalizedEmail);
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return user;
                } else {
                    throw new DataAccessException("Creating user failed, no ID returned.");
                }
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to create user with email: " + normalizedEmail, e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findById", e);
        }
    }

    @Override
    public Optional<User> findById(Connection conn, Long id) throws DataAccessException {
        String sql = "SELECT id, email, full_name, role, active, created_at FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find user by ID: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByEmail(conn, email);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByEmail", e);
        }
    }

    @Override
    public Optional<User> findByEmail(Connection conn, String email) throws DataAccessException {
        String sql = "SELECT id, email, full_name, role, active, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email != null ? email.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find user by email: " + email, e);
        }
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findAll users", e);
        }
    }

    @Override
    public List<User> findAll(Connection conn) throws DataAccessException {
        String sql = "SELECT id, email, full_name, role, active, created_at FROM users ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw handleSqlException("Failed to retrieve all users", e);
        }
    }

    @Override
    public List<User> findByRole(Role role) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByRole(conn, role);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByRole", e);
        }
    }

    @Override
    public List<User> findByRole(Connection conn, Role role) throws DataAccessException {
        String sql = "SELECT id, email, full_name, role, active, created_at FROM users WHERE role = ? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find users by role: " + role, e);
        }
    }

    @Override
    public User update(User user) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return update(conn, user);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for update user", e);
        }
    }

    @Override
    public User update(Connection conn, User user) throws DataAccessException {
        String sql = "UPDATE users SET email = ?, full_name = ?, role = ?, active = ? WHERE id = ?";
        String normalizedEmail = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedEmail);
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.setLong(5, user.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("User not found for update with ID: " + user.getId());
            }
            user.setEmail(normalizedEmail);
            return user;
        } catch (SQLException e) {
            throw handleSqlException("Failed to update user ID: " + user.getId(), e);
        }
    }

    @Override
    public void deactivate(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            deactivate(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for deactivate user", e);
        }
    }

    @Override
    public void deactivate(Connection conn, Long id) throws DataAccessException {
        String sql = "UPDATE users SET active = FALSE WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("User not found for deactivation with ID: " + id);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to deactivate user ID: " + id, e);
        }
    }

    @Override
    public long countByRole(Role role) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return countByRole(conn, role);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for countByRole", e);
        }
    }

    @Override
    public long countByRole(Connection conn, Role role) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to count users by role: " + role, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String email = rs.getString("email");
        String fullName = rs.getString("full_name");
        Role role = Role.valueOf(rs.getString("role"));
        boolean active = rs.getBoolean("active");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        return UserFactory.create(id, email, fullName, role, active, createdAt);
    }

    private DataAccessException handleSqlException(String message, SQLException e) {
        if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
            return new DuplicateEntityException(message + " (Duplicate entity / unique constraint violation)", e);
        }
        return new DataAccessException(message + ": " + e.getMessage(), e);
    }
}
