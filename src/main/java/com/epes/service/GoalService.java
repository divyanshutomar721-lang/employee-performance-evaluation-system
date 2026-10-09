package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Goal;
import com.epes.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for goal management, progress tracking, and deadline enforcement.
 */
public interface GoalService {

    /**
     * Assigns a goal to an employee. Restricted to a manager supervising the employee.
     */
    Goal assignGoal(User actor, Long employeeId, String description, LocalDate targetDate)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException;

    /**
     * Creates a self-assigned personal goal. Restricted to employees.
     */
    Goal createPersonalGoal(User actor, String description, LocalDate targetDate)
            throws AuthorizationException, ValidationException, DataAccessException;

    /**
     * Updates goal progress percentage (0-100) and automatically adjusts lifecycle status.
     * Accessible by the assigned employee or the setting manager.
     */
    Goal updateProgress(User actor, Long goalId, int progressPercent)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException;

    /**
     * Modifies goal description and target date.
     * Personal goals editable only by the employee; manager-assigned goals editable only by the setting manager.
     */
    Goal editGoal(User actor, Long goalId, String description, LocalDate targetDate)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException;

    /**
     * Deletes a goal.
     * Personal goals deletable only by the employee; manager-assigned goals deletable only by the setting manager.
     */
    void deleteGoal(User actor, Long goalId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Lists goals for an employee. Accessible by the employee, their manager, or admin.
     */
    List<Goal> listForEmployee(User actor, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Lists goals assigned by the acting manager.
     */
    List<Goal> listAssignedByMe(User actor)
            throws AuthorizationException, DataAccessException;

    /**
     * System method (no actor) invoked by background schedulers to mark overdue goals as MISSED.
     *
     * @param today reference date for deadline evaluation
     * @return count of goals transitioned to MISSED status
     * @throws DataAccessException if a database error occurs
     */
    int markOverdueGoals(LocalDate today) throws DataAccessException;
}
