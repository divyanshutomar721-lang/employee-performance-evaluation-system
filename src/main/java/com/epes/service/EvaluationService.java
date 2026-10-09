package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Evaluation;
import com.epes.model.User;

import java.util.List;
import java.util.Map;

/**
 * Service interface for conducting and reviewing employee performance evaluations.
 */
public interface EvaluationService {

    /**
     * Creates and calculates a new performance evaluation with criterion snapshots.
     * Restricted to managers supervising the evaluated employee.
     *
     * @param actor                authenticated manager conducting the evaluation
     * @param employeeId           evaluated employee ID
     * @param teamIdOrNull         optional associated team ID
     * @param periodLabel          evaluation period (e.g. "Q3 2026")
     * @param summaryComment       overall summary remarks
     * @param inputsByCriterionId  map of criterion ID to rating (1-5) and specific comment
     * @return persisted evaluation with calculated scores and RocketStage tier
     * @throws AuthorizationException if actor is not a manager or does not supervise the employee
     * @throws ValidationException    if inputs or ratings fail constraints
     * @throws BusinessRuleException  if a duplicate evaluation exists for this period, or criteria/employee are invalid
     * @throws EntityNotFoundException if referenced employee, team, or criterion does not exist
     * @throws DataAccessException   if a database error occurs
     */
    Evaluation createEvaluation(User actor, Long employeeId, Long teamIdOrNull,
                               String periodLabel, String summaryComment,
                               Map<Long, ScoreInput> inputsByCriterionId)
            throws AuthorizationException, ValidationException, BusinessRuleException, EntityNotFoundException, DataAccessException;

    /**
     * Retrieves an evaluation by ID with its scores.
     * Accessible by the creating manager, the evaluated employee, or administrators.
     */
    Evaluation getEvaluation(User actor, Long evaluationId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Lists all evaluations for an employee (newest first).
     * Accessible by the employee themself, a supervising manager, or administrators.
     */
    List<Evaluation> listForEmployee(User actor, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Lists all evaluations conducted by the acting manager.
     */
    List<Evaluation> listForManager(User actor)
            throws AuthorizationException, DataAccessException;

    /**
     * Deletes an evaluation. Restricted to the manager who created it.
     */
    void deleteEvaluation(User actor, Long evaluationId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;
}
