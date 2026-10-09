package com.epes.dao;

import com.epes.exception.DataAccessException;
import com.epes.model.Team;
import com.epes.model.TeamMember;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link Team} and {@link TeamMember} entities.
 */
public interface TeamDao {

    /**
     * Persists a new team.
     *
     * @param team team entity
     * @return persisted team with generated ID and timestamp
     * @throws DataAccessException if a database error occurs
     */
    Team create(Team team) throws DataAccessException;

    /**
     * Persists a new team using an existing connection.
     *
     * @param conn database connection
     * @param team team entity
     * @return persisted team with generated ID and timestamp
     * @throws DataAccessException if a database error occurs
     */
    Team create(Connection conn, Team team) throws DataAccessException;

    /**
     * Finds a team by ID with its members loaded.
     *
     * @param id team identifier
     * @return optional containing the team if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Team> findById(Long id) throws DataAccessException;

    /**
     * Finds a team by ID with its members loaded using an existing connection.
     *
     * @param conn database connection
     * @param id   team identifier
     * @return optional containing the team if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Team> findById(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds all teams managed by a specific manager.
     *
     * @param managerId manager user ID
     * @return list of teams
     * @throws DataAccessException if a database error occurs
     */
    List<Team> findByManager(Long managerId) throws DataAccessException;

    /**
     * Finds all teams managed by a specific manager using an existing connection.
     *
     * @param conn      database connection
     * @param managerId manager user ID
     * @return list of teams
     * @throws DataAccessException if a database error occurs
     */
    List<Team> findByManager(Connection conn, Long managerId) throws DataAccessException;

    /**
     * Finds all teams where an employee is a member.
     *
     * @param employeeId employee user ID
     * @return list of teams
     * @throws DataAccessException if a database error occurs
     */
    List<Team> findByEmployee(Long employeeId) throws DataAccessException;

    /**
     * Finds all teams where an employee is a member using an existing connection.
     *
     * @param conn       database connection
     * @param employeeId employee user ID
     * @return list of teams
     * @throws DataAccessException if a database error occurs
     */
    List<Team> findByEmployee(Connection conn, Long employeeId) throws DataAccessException;

    /**
     * Updates an existing team's name and description.
     *
     * @param team updated team
     * @return updated team
     * @throws DataAccessException if a database error occurs
     */
    Team update(Team team) throws DataAccessException;

    /**
     * Updates an existing team's name and description using an existing connection.
     *
     * @param conn database connection
     * @param team updated team
     * @return updated team
     * @throws DataAccessException if a database error occurs
     */
    Team update(Connection conn, Team team) throws DataAccessException;

    /**
     * Deletes a team by ID (cascades members).
     *
     * @param id team identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Long id) throws DataAccessException;

    /**
     * Deletes a team by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   team identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Connection conn, Long id) throws DataAccessException;

    /**
     * Adds an employee to a team.
     *
     * @param member team member entity
     * @return persisted team member
     * @throws DataAccessException if a database error occurs
     */
    TeamMember addMember(TeamMember member) throws DataAccessException;

    /**
     * Adds an employee to a team using an existing connection.
     *
     * @param conn   database connection
     * @param member team member entity
     * @return persisted team member
     * @throws DataAccessException if a database error occurs
     */
    TeamMember addMember(Connection conn, TeamMember member) throws DataAccessException;

    /**
     * Updates the role title of an employee in a team.
     *
     * @param teamId      team identifier
     * @param employeeId  employee identifier
     * @param roleInTeam  new role title
     * @throws DataAccessException if a database error occurs
     */
    void updateMemberRole(Long teamId, Long employeeId, String roleInTeam) throws DataAccessException;

    /**
     * Updates the role title of an employee in a team using an existing connection.
     *
     * @param conn        database connection
     * @param teamId      team identifier
     * @param employeeId  employee identifier
     * @param roleInTeam  new role title
     * @throws DataAccessException if a database error occurs
     */
    void updateMemberRole(Connection conn, Long teamId, Long employeeId, String roleInTeam) throws DataAccessException;

    /**
     * Removes an employee from a team.
     *
     * @param teamId     team identifier
     * @param employeeId employee identifier
     * @throws DataAccessException if a database error occurs
     */
    void removeMember(Long teamId, Long employeeId) throws DataAccessException;

    /**
     * Removes an employee from a team using an existing connection.
     *
     * @param conn       database connection
     * @param teamId     team identifier
     * @param employeeId employee identifier
     * @throws DataAccessException if a database error occurs
     */
    void removeMember(Connection conn, Long teamId, Long employeeId) throws DataAccessException;

    /**
     * Finds all members of a team.
     *
     * @param teamId team identifier
     * @return list of team members
     * @throws DataAccessException if a database error occurs
     */
    List<TeamMember> findMembers(Long teamId) throws DataAccessException;

    /**
     * Finds all members of a team using an existing connection.
     *
     * @param conn   database connection
     * @param teamId team identifier
     * @return list of team members
     * @throws DataAccessException if a database error occurs
     */
    List<TeamMember> findMembers(Connection conn, Long teamId) throws DataAccessException;

    /**
     * Creates a team and all its members in a single atomic transaction.
     *
     * @param team    team to create
     * @param members members to attach
     * @return created team with loaded members
     * @throws DataAccessException if a database error occurs
     */
    Team createWithMembers(Team team, List<TeamMember> members) throws DataAccessException;

    /**
     * Creates a team and all its members using an existing connection.
     *
     * @param conn    database connection
     * @param team    team to create
     * @param members members to attach
     * @return created team with loaded members
     * @throws DataAccessException if a database error occurs
     */
    Team createWithMembers(Connection conn, Team team, List<TeamMember> members) throws DataAccessException;
}
