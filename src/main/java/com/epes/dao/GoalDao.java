package com.epes.dao;

import com.epes.exception.DataAccessException;
import com.epes.model.Goal;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link Goal} entities.
 */
public interface GoalDao {

    /**
     * Persists a new goal.
     *
     * @param goal goal to create
     * @return persisted goal with generated ID and timestamps
     * @throws DataAccessException if a database error occurs
     */
    Goal create(Goal goal) throws DataAccessException;

    /**
     * Persists a new goal using an existing connection.
     *
     * @param conn database connection
     * @param goal goal to create
     * @return persisted goal with generated ID and timestamps
     * @throws DataAccessException if a database error occurs
     */
    Goal create(Connection conn, Goal goal) throws DataAccessException;

    /**
     * Finds a goal by ID.
     *
     * @param id goal identifier
     * @return optional containing the goal if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Goal> findById(Long id) throws DataAccessException;

    /**
     * Finds a goal by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   goal identifier
     * @return optional containing the goal if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Goal> findById(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds all goals assigned to an employee.
     *
     * @param employeeId employee user ID
     * @return list of goals
     * @throws DataAccessException if a database error occurs
     */
    List<Goal> findByEmployee(Long employeeId) throws DataAccessException;

    /**
     * Finds all goals assigned to an employee using an existing connection.
     *
     * @param conn       database connection
     * @param employeeId employee user ID
     * @return list of goals
     * @throws DataAccessException if a database error occurs
     */
    List<Goal> findByEmployee(Connection conn, Long employeeId) throws DataAccessException;

    /**
     * Finds all goals authored/set by a specific user.
     *
     * @param setById author user ID
     * @return list of goals
     * @throws DataAccessException if a database error occurs
     */
    List<Goal> findBySetBy(Long setById) throws DataAccessException;

    /**
     * Finds all goals authored/set by a specific user using an existing connection.
     *
     * @param conn    database connection
     * @param setById author user ID
     * @return list of goals
     * @throws DataAccessException if a database error occurs
     */
    List<Goal> findBySetBy(Connection conn, Long setById) throws DataAccessException;

    /**
     * Updates an existing goal (status, progress, description, target date; updates updated_at).
     *
     * @param goal updated goal
     * @return updated goal
     * @throws DataAccessException if a database error occurs
     */
    Goal update(Goal goal) throws DataAccessException;

    /**
     * Updates an existing goal using an existing connection.
     *
     * @param conn database connection
     * @param goal updated goal
     * @return updated goal
     * @throws DataAccessException if a database error occurs
     */
    Goal update(Connection conn, Goal goal) throws DataAccessException;

    /**
     * Deletes a goal by ID.
     *
     * @param id goal identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Long id) throws DataAccessException;

    /**
     * Deletes a goal by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   goal identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds non-completed goals with target date on or before the given date.
     *
     * @param date target cutoff date
     * @return list of overdue/due goals
     * @throws DataAccessException if a database error occurs
     */
    List<Goal> findDueBefore(LocalDate date) throws DataAccessException;

    /**
     * Finds non-completed goals with target date on or before the given date using an existing connection.
     *
     * @param conn database connection
     * @param date target cutoff date
     * @return list of overdue/due goals
     * @throws DataAccessException if a database error occurs
     */
    List<Goal> findDueBefore(Connection conn, LocalDate date) throws DataAccessException;
}
