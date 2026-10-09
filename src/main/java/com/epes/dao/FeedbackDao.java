package com.epes.dao;

import com.epes.exception.DataAccessException;
import com.epes.model.Feedback;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link Feedback} entities.
 */
public interface FeedbackDao {

    /**
     * Persists a new feedback record.
     *
     * @param feedback feedback to create
     * @return persisted feedback with generated ID and timestamp
     * @throws DataAccessException if a database error occurs
     */
    Feedback create(Feedback feedback) throws DataAccessException;

    /**
     * Persists a new feedback record using an existing connection.
     *
     * @param conn     database connection
     * @param feedback feedback to create
     * @return persisted feedback with generated ID and timestamp
     * @throws DataAccessException if a database error occurs
     */
    Feedback create(Connection conn, Feedback feedback) throws DataAccessException;

    /**
     * Finds a feedback record by ID.
     *
     * @param id feedback identifier
     * @return optional containing the feedback if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Feedback> findById(Long id) throws DataAccessException;

    /**
     * Finds a feedback record by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   feedback identifier
     * @return optional containing the feedback if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Feedback> findById(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds all feedback records for an employee, ordered newest first.
     *
     * @param employeeId employee user ID
     * @return list of feedback records
     * @throws DataAccessException if a database error occurs
     */
    List<Feedback> findByEmployee(Long employeeId) throws DataAccessException;

    /**
     * Finds all feedback records for an employee, ordered newest first, using an existing connection.
     *
     * @param conn       database connection
     * @param employeeId employee user ID
     * @return list of feedback records
     * @throws DataAccessException if a database error occurs
     */
    List<Feedback> findByEmployee(Connection conn, Long employeeId) throws DataAccessException;

    /**
     * Finds all feedback records authored by a manager.
     *
     * @param managerId manager user ID
     * @return list of feedback records
     * @throws DataAccessException if a database error occurs
     */
    List<Feedback> findByManager(Long managerId) throws DataAccessException;

    /**
     * Finds all feedback records authored by a manager using an existing connection.
     *
     * @param conn      database connection
     * @param managerId manager user ID
     * @return list of feedback records
     * @throws DataAccessException if a database error occurs
     */
    List<Feedback> findByManager(Connection conn, Long managerId) throws DataAccessException;

    /**
     * Deletes a feedback record by ID.
     *
     * @param id feedback identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Long id) throws DataAccessException;

    /**
     * Deletes a feedback record by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   feedback identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Connection conn, Long id) throws DataAccessException;
}
