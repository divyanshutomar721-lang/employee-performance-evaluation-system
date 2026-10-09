package com.epes.dao.impl;

import com.epes.dao.TeamDao;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.model.Team;
import com.epes.model.TeamMember;
import com.epes.util.ConnectionFactory;
import com.epes.util.TransactionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link TeamDao}.
 */
public class TeamDaoImpl implements TeamDao {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private final ConnectionFactory connectionFactory;

    /**
     * Default constructor.
     *
     * @throws DataAccessException if connection factory initialization fails
     */
    public TeamDaoImpl() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Parameterized constructor.
     *
     * @param connectionFactory connection factory
     */
    public TeamDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Team create(Team team) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return create(conn, team);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for creating team", e);
        }
    }

    @Override
    public Team create(Connection conn, Team team) throws DataAccessException {
        String sql = "INSERT INTO teams (name, description, manager_id, created_at) VALUES (?, ?, ?, ?) RETURNING id, created_at";
        LocalDateTime now = team.getCreatedAt() != null ? team.getCreatedAt() : LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, team.getName());
            ps.setString(2, team.getDescription());
            ps.setLong(3, team.getManagerId());
            ps.setTimestamp(4, Timestamp.valueOf(now));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    team.setId(rs.getLong("id"));
                    team.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return team;
                } else {
                    throw new DataAccessException("Creating team failed, no ID returned.");
                }
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to create team: " + team.getName(), e);
        }
    }

    @Override
    public Optional<Team> findById(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findById team", e);
        }
    }

    @Override
    public Optional<Team> findById(Connection conn, Long id) throws DataAccessException {
        String sql = "SELECT id, name, description, manager_id, created_at FROM teams WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Team team = mapRow(rs);
                    team.setMembers(findMembers(conn, id));
                    return Optional.of(team);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find team by ID: " + id, e);
        }
    }

    @Override
    public List<Team> findByManager(Long managerId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByManager(conn, managerId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByManager", e);
        }
    }

    @Override
    public List<Team> findByManager(Connection conn, Long managerId) throws DataAccessException {
        String sql = "SELECT id, name, description, manager_id, created_at FROM teams WHERE manager_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Team> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find teams by manager ID: " + managerId, e);
        }
    }

    @Override
    public List<Team> findByEmployee(Long employeeId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByEmployee(conn, employeeId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByEmployee", e);
        }
    }

    @Override
    public List<Team> findByEmployee(Connection conn, Long employeeId) throws DataAccessException {
        String sql = "SELECT t.id, t.name, t.description, t.manager_id, t.created_at " +
                "FROM teams t " +
                "INNER JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE tm.employee_id = ? ORDER BY t.id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Team> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find teams for employee ID: " + employeeId, e);
        }
    }

    @Override
    public Team update(Team team) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return update(conn, team);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for update team", e);
        }
    }

    @Override
    public Team update(Connection conn, Team team) throws DataAccessException {
        String sql = "UPDATE teams SET name = ?, description = ?, manager_id = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, team.getName());
            ps.setString(2, team.getDescription());
            ps.setLong(3, team.getManagerId());
            ps.setLong(4, team.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Team not found for update with ID: " + team.getId());
            }
            return team;
        } catch (SQLException e) {
            throw handleSqlException("Failed to update team ID: " + team.getId(), e);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            delete(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for delete team", e);
        }
    }

    @Override
    public void delete(Connection conn, Long id) throws DataAccessException {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Team not found for delete with ID: " + id);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to delete team ID: " + id, e);
        }
    }

    @Override
    public TeamMember addMember(TeamMember member) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return addMember(conn, member);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for addMember", e);
        }
    }

    @Override
    public TeamMember addMember(Connection conn, TeamMember member) throws DataAccessException {
        String sql = "INSERT INTO team_members (team_id, employee_id, role_in_team, joined_at) VALUES (?, ?, ?, ?) RETURNING joined_at";
        LocalDateTime now = member.getJoinedAt() != null ? member.getJoinedAt() : LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, member.getTeamId());
            ps.setLong(2, member.getEmployeeId());
            ps.setString(3, member.getRoleInTeam());
            ps.setTimestamp(4, Timestamp.valueOf(now));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    member.setJoinedAt(rs.getTimestamp("joined_at").toLocalDateTime());
                    return member;
                } else {
                    throw new DataAccessException("Failed to add team member, no joined_at returned.");
                }
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to add member to team ID: " + member.getTeamId(), e);
        }
    }

    @Override
    public void updateMemberRole(Long teamId, Long employeeId, String roleInTeam) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            updateMemberRole(conn, teamId, employeeId, roleInTeam);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for updateMemberRole", e);
        }
    }

    @Override
    public void updateMemberRole(Connection conn, Long teamId, Long employeeId, String roleInTeam) throws DataAccessException {
        String sql = "UPDATE team_members SET role_in_team = ? WHERE team_id = ? AND employee_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleInTeam);
            ps.setLong(2, teamId);
            ps.setLong(3, employeeId);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Team member association not found for team " + teamId + " and employee " + employeeId);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to update member role in team ID: " + teamId, e);
        }
    }

    @Override
    public void removeMember(Long teamId, Long employeeId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            removeMember(conn, teamId, employeeId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for removeMember", e);
        }
    }

    @Override
    public void removeMember(Connection conn, Long teamId, Long employeeId) throws DataAccessException {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND employee_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teamId);
            ps.setLong(2, employeeId);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Team member association not found for team " + teamId + " and employee " + employeeId);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to remove member from team ID: " + teamId, e);
        }
    }

    @Override
    public List<TeamMember> findMembers(Long teamId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findMembers(conn, teamId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findMembers", e);
        }
    }

    @Override
    public List<TeamMember> findMembers(Connection conn, Long teamId) throws DataAccessException {
        String sql = "SELECT team_id, employee_id, role_in_team, joined_at FROM team_members WHERE team_id = ? ORDER BY joined_at ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                List<TeamMember> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapMemberRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find members for team ID: " + teamId, e);
        }
    }

    @Override
    public Team createWithMembers(Team team, List<TeamMember> members) throws DataAccessException {
        TransactionManager tm = new TransactionManager(connectionFactory);
        return tm.executeInTransaction(conn -> createWithMembers(conn, team, members));
    }

    @Override
    public Team createWithMembers(Connection conn, Team team, List<TeamMember> members) throws DataAccessException {
        Team createdTeam = create(conn, team);
        List<TeamMember> savedMembers = new ArrayList<>();
        if (members != null) {
            for (TeamMember m : members) {
                m.setTeamId(createdTeam.getId());
                savedMembers.add(addMember(conn, m));
            }
        }
        createdTeam.setMembers(savedMembers);
        return createdTeam;
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        Long managerId = rs.getLong("manager_id");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        return new Team(id, name, description, managerId, createdAt);
    }

    private TeamMember mapMemberRow(ResultSet rs) throws SQLException {
        Long teamId = rs.getLong("team_id");
        Long employeeId = rs.getLong("employee_id");
        String roleInTeam = rs.getString("role_in_team");
        Timestamp ts = rs.getTimestamp("joined_at");
        LocalDateTime joinedAt = ts != null ? ts.toLocalDateTime() : null;

        return new TeamMember(teamId, employeeId, roleInTeam, joinedAt);
    }

    private DataAccessException handleSqlException(String message, SQLException e) {
        if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
            return new DuplicateEntityException(message + " (Duplicate entity / unique constraint violation)", e);
        }
        return new DataAccessException(message + ": " + e.getMessage(), e);
    }
}
