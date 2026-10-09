package com.epes.dao;

import com.epes.exception.DataAccessException;
import com.epes.model.Criterion;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link Criterion} entities.
 */
public interface CriterionDao {

    /**
     * Persists a new evaluation criterion.
     *
     * @param criterion criterion entity to create
     * @return persisted criterion with generated ID
     * @throws DataAccessException if a database error occurs
     */
    Criterion create(Criterion criterion) throws DataAccessException;

    /**
     * Persists a new evaluation criterion using an existing connection.
     *
     * @param conn      database connection
     * @param criterion criterion entity to create
     * @return persisted criterion with generated ID
     * @throws DataAccessException if a database error occurs
     */
    Criterion create(Connection conn, Criterion criterion) throws DataAccessException;

    /**
     * Finds a criterion by ID.
     *
     * @param id criterion identifier
     * @return optional containing the criterion if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Criterion> findById(Long id) throws DataAccessException;

    /**
     * Finds a criterion by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   criterion identifier
     * @return optional containing the criterion if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Criterion> findById(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds criteria defined by a manager, with optional active-only filtering.
     *
     * @param managerId  manager identifier
     * @param activeOnly if true, returns only active criteria; if false, returns all
     * @return list of criteria
     * @throws DataAccessException if a database error occurs
     */
    List<Criterion> findByManager(Long managerId, boolean activeOnly) throws DataAccessException;

    /**
     * Finds criteria defined by a manager using an existing connection.
     *
     * @param conn       database connection
     * @param managerId  manager identifier
     * @param activeOnly if true, returns only active criteria; if false, returns all
     * @return list of criteria
     * @throws DataAccessException if a database error occurs
     */
    List<Criterion> findByManager(Connection conn, Long managerId, boolean activeOnly) throws DataAccessException;

    /**
     * Updates an existing criterion.
     *
     * @param criterion updated criterion
     * @return updated criterion
     * @throws DataAccessException if a database error occurs
     */
    Criterion update(Criterion criterion) throws DataAccessException;

    /**
     * Updates an existing criterion using an existing connection.
     *
     * @param conn      database connection
     * @param criterion updated criterion
     * @return updated criterion
     * @throws DataAccessException if a database error occurs
     */
    Criterion update(Connection conn, Criterion criterion) throws DataAccessException;

    /**
     * Deactivates a criterion (soft delete).
     *
     * @param id criterion identifier
     * @throws DataAccessException if a database error occurs
     */
    void deactivate(Long id) throws DataAccessException;

    /**
     * Deactivates a criterion using an existing connection.
     *
     * @param conn database connection
     * @param id   criterion identifier
     * @throws DataAccessException if a database error occurs
     */
    void deactivate(Connection conn, Long id) throws DataAccessException;

    /**
     * Hard-deletes a criterion by ID.
     *
     * @param id criterion identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Long id) throws DataAccessException;

    /**
     * Hard-deletes a criterion by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   criterion identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Connection conn, Long id) throws DataAccessException;
}
