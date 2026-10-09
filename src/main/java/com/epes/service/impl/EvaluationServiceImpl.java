package com.epes.service.impl;

import com.epes.dao.CriterionDao;
import com.epes.dao.EvaluationDao;
import com.epes.dao.TeamDao;
import com.epes.dao.UserDao;
import com.epes.dao.impl.CriterionDaoImpl;
import com.epes.dao.impl.EvaluationDaoImpl;
import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Criterion;
import com.epes.model.Evaluation;
import com.epes.model.EvaluationScore;
import com.epes.model.RocketStage;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.service.EvaluationService;
import com.epes.service.ScoreCalculator;
import com.epes.service.ScoreInput;
import com.epes.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link EvaluationService} enforcing managerial supervision rules,
 * duplicate review prevention, criterion snapshotting, and weighted scoring.
 */
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationDao evaluationDao;
    private final UserDao userDao;
    private final TeamDao teamDao;
    private final CriterionDao criterionDao;

    /** Default constructor. */
    public EvaluationServiceImpl() throws DataAccessException {
        this(new EvaluationDaoImpl(), new UserDaoImpl(), new TeamDaoImpl(), new CriterionDaoImpl());
    }

    /** Parameterized constructor. */
    public EvaluationServiceImpl(EvaluationDao evaluationDao, UserDao userDao, TeamDao teamDao, CriterionDao criterionDao) {
        this.evaluationDao = evaluationDao;
        this.userDao = userDao;
        this.teamDao = teamDao;
        this.criterionDao = criterionDao;
    }

    @Override
    public Evaluation createEvaluation(User actor, Long employeeId, Long teamIdOrNull,
                                       String periodLabel, String summaryComment,
                                       Map<Long, ScoreInput> inputsByCriterionId)
            throws AuthorizationException, ValidationException, BusinessRuleException, EntityNotFoundException, DataAccessException {
        requireManager(actor);

        var collector = Validator.newCollector();
        collector.checkNotNull(employeeId, "Employee ID")
                .checkNotBlank(periodLabel, "Period label")
                .checkMaxLength(periodLabel, 100, "Period label");
        if (inputsByCriterionId == null || inputsByCriterionId.isEmpty()) {
            collector.addError("At least one evaluation criterion score is required");
        }
        collector.throwIfAny();

        // 1. Verify employee exists, is active, and has role EMPLOYEE
        User employee = userDao.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
        if (!employee.isActive() || employee.getRole() != Role.EMPLOYEE) {
            throw new BusinessRuleException("Evaluations can only be conducted for active employees");
        }

        // 2. Verify employee is in at least one team managed by actor (and in teamIdOrNull if given)
        List<Team> managedTeams = teamDao.findByManager(actor.getId());
        List<Team> employeeTeams = teamDao.findByEmployee(employeeId);

        boolean inAnyManagedTeam = employeeTeams.stream()
                .anyMatch(t -> t.getManagerId().equals(actor.getId()));
        if (!inAnyManagedTeam) {
            throw new AuthorizationException("You can only evaluate employees in teams that you manage");
        }

        if (teamIdOrNull != null) {
            boolean inSpecifiedTeam = employeeTeams.stream()
                    .anyMatch(t -> t.getId().equals(teamIdOrNull) && t.getManagerId().equals(actor.getId()));
            if (!inSpecifiedTeam) {
                throw new BusinessRuleException("Employee is not a member of the specified team (ID: " + teamIdOrNull + ")");
            }
        }

        // 3. Duplicate evaluation check (same employee + same manager + same periodLabel)
        String cleanPeriod = periodLabel.trim();
        List<Evaluation> existingEvals = evaluationDao.findByEmployee(employeeId);
        boolean duplicate = existingEvals.stream().anyMatch(e ->
                e.getManagerId().equals(actor.getId()) && e.getPeriodLabel().equalsIgnoreCase(cleanPeriod));
        if (duplicate) {
            throw new BusinessRuleException("An evaluation for employee " + employee.getFullName() +
                    " and period '" + cleanPeriod + "' already exists");
        }

        // 4. Validate criteria ownership, active status, and build snapshots
        List<EvaluationScore> scores = new ArrayList<>();
        for (Map.Entry<Long, ScoreInput> entry : inputsByCriterionId.entrySet()) {
            Long criterionId = entry.getKey();
            ScoreInput input = entry.getValue();

            if (criterionId == null || input == null) {
                throw new ValidationException("Invalid criterion score input");
            }
            if (input.getRating() < 1 || input.getRating() > 5) {
                throw new ValidationException("Criterion rating must be between 1 and 5 (got: " + input.getRating() + ")");
            }

            Criterion criterion = criterionDao.findById(criterionId)
                    .orElseThrow(() -> new EntityNotFoundException("Criterion not found with ID: " + criterionId));

            if (!criterion.getManagerId().equals(actor.getId())) {
                throw new AuthorizationException("Criterion '" + criterion.getName() + "' (ID: " + criterionId + ") belongs to another manager");
            }
            if (!criterion.isActive()) {
                throw new BusinessRuleException("Criterion '" + criterion.getName() + "' is inactive and cannot be used in new evaluations");
            }

            // Snapshot criterion name and weight
            EvaluationScore score = new EvaluationScore(
                    null,
                    null,
                    criterion.getId(),
                    criterion.getName(),
                    criterion.getWeight(),
                    input.getRating(),
                    input.getComment()
            );
            scores.add(score);
        }

        // 5. Calculate overall weighted score and RocketStage
        BigDecimal overallScore = ScoreCalculator.computeOverallScore(scores);
        RocketStage overallStage = ScoreCalculator.stageFor(overallScore);

        Evaluation eval = new Evaluation(
                null,
                employeeId,
                actor.getId(),
                teamIdOrNull,
                cleanPeriod,
                overallScore,
                overallStage,
                summaryComment,
                LocalDateTime.now()
        );

        return evaluationDao.saveWithScores(eval, scores);
    }

    @Override
    public Evaluation getEvaluation(User actor, Long evaluationId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (evaluationId == null) {
            throw new ValidationException("Evaluation ID cannot be null");
        }

        Evaluation eval = evaluationDao.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found with ID: " + evaluationId));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isCreator = eval.getManagerId().equals(actor.getId());
        boolean isSubject = eval.getEmployeeId().equals(actor.getId());

        if (!isAdmin && !isCreator && !isSubject) {
            throw new AuthorizationException("You do not have permission to view this evaluation");
        }
        return eval;
    }

    @Override
    public List<Evaluation> listForEmployee(User actor, Long employeeId)
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
            throw new AuthorizationException("You do not have permission to view evaluations for this employee");
        }

        return evaluationDao.findByEmployee(employeeId);
    }

    @Override
    public List<Evaluation> listForManager(User actor) throws AuthorizationException, DataAccessException {
        requireManager(actor);
        return evaluationDao.findByManager(actor.getId());
    }

    @Override
    public void deleteEvaluation(User actor, Long evaluationId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        requireManager(actor);
        if (evaluationId == null) {
            throw new ValidationException("Evaluation ID cannot be null");
        }

        Evaluation eval = evaluationDao.findById(evaluationId)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found with ID: " + evaluationId));

        if (!eval.getManagerId().equals(actor.getId())) {
            throw new AuthorizationException("Only the manager who created this evaluation can delete it");
        }

        evaluationDao.delete(evaluationId);
    }

    private void requireManager(User actor) {
        if (actor == null || actor.getRole() != Role.MANAGER) {
            throw new AuthorizationException("Only managers can perform evaluation actions");
        }
    }
}
