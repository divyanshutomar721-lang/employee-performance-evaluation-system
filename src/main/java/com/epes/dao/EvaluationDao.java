package com.epes.dao;

import com.epes.exception.DataAccessException;
import com.epes.model.Evaluation;
import com.epes.model.EvaluationScore;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link Evaluation} and {@link EvaluationScore} entities.
 */
public interface EvaluationDao {

    /**
     * Persists an evaluation record and all associated score snapshots in an atomic transaction.
     *
     * @param evaluation parent evaluation record
     * @param scores     criterion score snapshots
     * @return persisted evaluation with loaded scores
     * @throws DataAccessException if a database error occurs
     */
    Evaluation saveWithScores(Evaluation evaluation, List<EvaluationScore> scores) throws DataAccessException;

    /**
     * Persists an evaluation record and all associated score snapshots using an existing connection.
     *
     * @param conn       database connection
     * @param evaluation parent evaluation record
     * @param scores     criterion score snapshots
     * @return persisted evaluation with loaded scores
     * @throws DataAccessException if a database error occurs
     */
    Evaluation saveWithScores(Connection conn, Evaluation evaluation, List<EvaluationScore> scores) throws DataAccessException;

    /**
     * Finds an evaluation by ID with its scores loaded.
     *
     * @param id evaluation identifier
     * @return optional containing the evaluation if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Evaluation> findById(Long id) throws DataAccessException;

    /**
     * Finds an evaluation by ID with its scores loaded using an existing connection.
     *
     * @param conn database connection
     * @param id   evaluation identifier
     * @return optional containing the evaluation if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<Evaluation> findById(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds all evaluations for an employee, ordered newest first.
     *
     * @param employeeId employee user ID
     * @return list of evaluations
     * @throws DataAccessException if a database error occurs
     */
    List<Evaluation> findByEmployee(Long employeeId) throws DataAccessException;

    /**
     * Finds all evaluations for an employee, ordered newest first, using an existing connection.
     *
     * @param conn       database connection
     * @param employeeId employee user ID
     * @return list of evaluations
     * @throws DataAccessException if a database error occurs
     */
    List<Evaluation> findByEmployee(Connection conn, Long employeeId) throws DataAccessException;

    /**
     * Finds all evaluations conducted by a manager.
     *
     * @param managerId manager user ID
     * @return list of evaluations
     * @throws DataAccessException if a database error occurs
     */
    List<Evaluation> findByManager(Long managerId) throws DataAccessException;

    /**
     * Finds all evaluations conducted by a manager using an existing connection.
     *
     * @param conn      database connection
     * @param managerId manager user ID
     * @return list of evaluations
     * @throws DataAccessException if a database error occurs
     */
    List<Evaluation> findByManager(Connection conn, Long managerId) throws DataAccessException;

    /**
     * Finds all evaluations associated with a specific team.
     *
     * @param teamId team identifier
     * @return list of evaluations
     * @throws DataAccessException if a database error occurs
     */
    List<Evaluation> findByTeam(Long teamId) throws DataAccessException;

    /**
     * Finds all evaluations associated with a specific team using an existing connection.
     *
     * @param conn   database connection
     * @param teamId team identifier
     * @return list of evaluations
     * @throws DataAccessException if a database error occurs
     */
    List<Evaluation> findByTeam(Connection conn, Long teamId) throws DataAccessException;

    /**
     * Deletes an evaluation by ID (cascades scores).
     *
     * @param id evaluation identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Long id) throws DataAccessException;

    /**
     * Deletes an evaluation by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   evaluation identifier
     * @throws DataAccessException if a database error occurs
     */
    void delete(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds all score snapshots for an evaluation.
     *
     * @param evaluationId evaluation identifier
     * @return list of evaluation scores
     * @throws DataAccessException if a database error occurs
     */
    List<EvaluationScore> findScoresByEvaluationId(Long evaluationId) throws DataAccessException;

    /**
     * Finds all score snapshots for an evaluation using an existing connection.
     *
     * @param conn         database connection
     * @param evaluationId evaluation identifier
     * @return list of evaluation scores
     * @throws DataAccessException if a database error occurs
     */
    List<EvaluationScore> findScoresByEvaluationId(Connection conn, Long evaluationId) throws DataAccessException;
}
