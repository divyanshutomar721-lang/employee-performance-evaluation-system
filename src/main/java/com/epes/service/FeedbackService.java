package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Feedback;
import com.epes.model.User;

import java.util.List;

/**
 * Service interface for continuous 360 and manager feedback.
 */
public interface FeedbackService {

    /**
     * Submits continuous feedback for an employee. Restricted to a manager supervising the employee.
     */
    Feedback giveFeedback(User actor, Long employeeId, Long evaluationIdOrNull, String comments, int rating)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException;

    /**
     * Lists feedback received by an employee, ordered newest first.
     * Accessible by the employee, their supervising manager, or admin.
     */
    List<Feedback> listForEmployee(User actor, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Lists feedback submitted by the acting manager.
     */
    List<Feedback> listGivenByMe(User actor)
            throws AuthorizationException, DataAccessException;

    /**
     * Deletes a feedback record. Restricted to the authoring manager.
     */
    void deleteFeedback(User actor, Long feedbackId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;
}
