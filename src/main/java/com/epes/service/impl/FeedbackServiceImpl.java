package com.epes.service.impl;

import com.epes.dao.EvaluationDao;
import com.epes.dao.FeedbackDao;
import com.epes.dao.TeamDao;
import com.epes.dao.UserDao;
import com.epes.dao.impl.EvaluationDaoImpl;
import com.epes.dao.impl.FeedbackDaoImpl;
import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Evaluation;
import com.epes.model.Feedback;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.service.FeedbackService;
import com.epes.util.Validator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of {@link FeedbackService} managing continuous employee ratings and comments.
 */
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackDao feedbackDao;
    private final UserDao userDao;
    private final TeamDao teamDao;
    private final EvaluationDao evaluationDao;

    /** Default constructor. */
    public FeedbackServiceImpl() throws DataAccessException {
        this(new FeedbackDaoImpl(), new UserDaoImpl(), new TeamDaoImpl(), new EvaluationDaoImpl());
    }

    /** Parameterized constructor. */
    public FeedbackServiceImpl(FeedbackDao feedbackDao, UserDao userDao, TeamDao teamDao, EvaluationDao evaluationDao) {
        this.feedbackDao = feedbackDao;
        this.userDao = userDao;
        this.teamDao = teamDao;
        this.evaluationDao = evaluationDao;
    }

    @Override
    public Feedback giveFeedback(User actor, Long employeeId, Long evaluationIdOrNull, String comments, int rating)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException {
        if (actor == null || actor.getRole() != Role.MANAGER) {
            throw new AuthorizationException("Only managers can submit employee feedback");
        }

        var collector = Validator.newCollector();
        collector.checkNotNull(employeeId, "Employee ID")
                .checkNotBlank(comments, "Comments")
                .checkMaxLength(comments, 1000, "Comments")
                .checkRange(rating, 1, 5, "Rating");
        collector.throwIfAny();

        User employee = userDao.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
        if (!employee.isActive() || employee.getRole() != Role.EMPLOYEE) {
            throw new BusinessRuleException("Feedback can only be submitted for active employees");
        }

        List<Team> employeeTeams = teamDao.findByEmployee(employeeId);
        boolean isSupervised = employeeTeams.stream().anyMatch(t -> t.getManagerId().equals(actor.getId()));
        if (!isSupervised) {
            throw new AuthorizationException("You can only give feedback to employees in teams you manage");
        }

        if (evaluationIdOrNull != null) {
            Evaluation eval = evaluationDao.findById(evaluationIdOrNull)
                    .orElseThrow(() -> new EntityNotFoundException("Evaluation not found with ID: " + evaluationIdOrNull));
            if (!eval.getEmployeeId().equals(employeeId)) {
                throw new BusinessRuleException("Evaluation ID " + evaluationIdOrNull + " does not belong to employee " + employeeId);
            }
            if (!eval.getManagerId().equals(actor.getId())) {
                throw new AuthorizationException("Evaluation ID " + evaluationIdOrNull + " was created by another manager");
            }
        }

        Feedback feedback = new Feedback(
                null,
                employeeId,
                actor.getId(),
                evaluationIdOrNull,
                comments.trim(),
                rating,
                LocalDateTime.now()
        );
        return feedbackDao.create(feedback);
    }

    @Override
    public List<Feedback> listForEmployee(User actor, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (employeeId == null) {
            throw new ValidationException("Employee ID cannot be null");
        }

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isSelf = actor.getId().equals(employeeId);
        boolean isManagerOfEmployee = false;

        if (actor.getRole() == Role.MANAGER) {
            List<Team> empTeams = teamDao.findByEmployee(employeeId);
            isManagerOfEmployee = empTeams.stream().anyMatch(t -> t.getManagerId().equals(actor.getId()));
        }

        if (!isAdmin && !isSelf && !isManagerOfEmployee) {
            throw new AuthorizationException("You do not have permission to view feedback for this employee");
        }

        return feedbackDao.findByEmployee(employeeId);
    }

    @Override
    public List<Feedback> listGivenByMe(User actor) throws AuthorizationException, DataAccessException {
        if (actor == null || actor.getRole() != Role.MANAGER) {
            throw new AuthorizationException("Only managers can list submitted feedback");
        }
        return feedbackDao.findByManager(actor.getId());
    }

    @Override
    public void deleteFeedback(User actor, Long feedbackId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (feedbackId == null) {
            throw new ValidationException("Feedback ID cannot be null");
        }

        Feedback fb = feedbackDao.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found with ID: " + feedbackId));

        if (!fb.getManagerId().equals(actor.getId())) {
            throw new AuthorizationException("Only the authoring manager can delete feedback");
        }

        feedbackDao.delete(feedbackId);
    }
}
