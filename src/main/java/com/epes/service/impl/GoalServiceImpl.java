package com.epes.service.impl;

import com.epes.dao.GoalDao;
import com.epes.dao.TeamDao;
import com.epes.dao.UserDao;
import com.epes.dao.impl.GoalDaoImpl;
import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Goal;
import com.epes.model.GoalStatus;
import com.epes.model.GoalType;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.service.GoalProgressRule;
import com.epes.service.GoalService;
import com.epes.util.ConnectionFactory;
import com.epes.util.TransactionManager;
import com.epes.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of {@link GoalService} enforcing progress lifecycle rules,
 * ownership permissions, and automated deadline monitoring.
 */
public class GoalServiceImpl implements GoalService {

    private final GoalDao goalDao;
    private final UserDao userDao;
    private final TeamDao teamDao;
    private final TransactionManager transactionManager;

    /** Default constructor. */
    public GoalServiceImpl() throws DataAccessException {
        this(new GoalDaoImpl(), new UserDaoImpl(), new TeamDaoImpl(), new TransactionManager());
    }

    /** Parameterized constructor. */
    public GoalServiceImpl(GoalDao goalDao, UserDao userDao, TeamDao teamDao, TransactionManager transactionManager) {
        this.goalDao = goalDao;
        this.userDao = userDao;
        this.teamDao = teamDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public Goal assignGoal(User actor, Long employeeId, String description, LocalDate targetDate)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException {
        if (actor == null || actor.getRole() != Role.MANAGER) {
            throw new AuthorizationException("Only managers can assign goals to employees");
        }

        var collector = Validator.newCollector();
        collector.checkNotNull(employeeId, "Employee ID")
                .checkNotBlank(description, "Goal description")
                .checkMaxLength(description, 500, "Goal description")
                .checkFutureOrToday(targetDate, "Target date");
        collector.throwIfAny();

        User employee = userDao.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
        if (!employee.isActive() || employee.getRole() != Role.EMPLOYEE) {
            throw new BusinessRuleException("Goals can only be assigned to active employees");
        }

        List<Team> employeeTeams = teamDao.findByEmployee(employeeId);
        boolean isSupervised = employeeTeams.stream().anyMatch(t -> t.getManagerId().equals(actor.getId()));
        if (!isSupervised) {
            throw new AuthorizationException("You can only assign goals to employees in teams you manage");
        }

        Goal goal = new Goal(
                null,
                employeeId,
                actor.getId(),
                GoalType.MANAGER_ASSIGNED,
                description.trim(),
                targetDate,
                GoalStatus.NOT_STARTED,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        return goalDao.create(goal);
    }

    @Override
    public Goal createPersonalGoal(User actor, String description, LocalDate targetDate)
            throws AuthorizationException, ValidationException, DataAccessException {
        if (actor == null || actor.getRole() != Role.EMPLOYEE) {
            throw new AuthorizationException("Only employees can create personal goals");
        }

        var collector = Validator.newCollector();
        collector.checkNotBlank(description, "Goal description")
                .checkMaxLength(description, 500, "Goal description")
                .checkFutureOrToday(targetDate, "Target date");
        collector.throwIfAny();

        Goal goal = new Goal(
                null,
                actor.getId(),
                actor.getId(),
                GoalType.PERSONAL,
                description.trim(),
                targetDate,
                GoalStatus.NOT_STARTED,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        return goalDao.create(goal);
    }

    @Override
    public Goal updateProgress(User actor, Long goalId, int progressPercent)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (goalId == null) {
            throw new ValidationException("Goal ID cannot be null");
        }

        var collector = Validator.newCollector();
        collector.checkRange(progressPercent, 0, 100, "Progress percent");
        collector.throwIfAny();

        Goal goal = goalDao.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found with ID: " + goalId));

        boolean isAssignedEmployee = goal.getEmployeeId().equals(actor.getId());
        boolean isSettingManager = goal.getSetBy().equals(actor.getId());

        if (!isAssignedEmployee && !isSettingManager) {
            throw new AuthorizationException("You do not have permission to update progress on this goal");
        }

        GoalStatus newStatus = GoalProgressRule.determineStatus(progressPercent);
        goal.setProgressPercent(progressPercent);
        goal.setStatus(newStatus);

        return goalDao.update(goal);
    }

    @Override
    public Goal editGoal(User actor, Long goalId, String description, LocalDate targetDate)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (goalId == null) {
            throw new ValidationException("Goal ID cannot be null");
        }

        var collector = Validator.newCollector();
        collector.checkNotBlank(description, "Goal description")
                .checkMaxLength(description, 500, "Goal description")
                .checkNotNull(targetDate, "Target date");
        collector.throwIfAny();

        Goal goal = goalDao.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found with ID: " + goalId));

        if (goal.getGoalType() == GoalType.PERSONAL) {
            if (!goal.getEmployeeId().equals(actor.getId())) {
                throw new AuthorizationException("Personal goals can only be edited by the employee");
            }
        } else if (goal.getGoalType() == GoalType.MANAGER_ASSIGNED) {
            if (!goal.getSetBy().equals(actor.getId())) {
                throw new AuthorizationException("Manager-assigned goals can only be edited by the setting manager");
            }
        }

        goal.setDescription(description.trim());
        goal.setTargetDate(targetDate);

        return goalDao.update(goal);
    }

    @Override
    public void deleteGoal(User actor, Long goalId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (goalId == null) {
            throw new ValidationException("Goal ID cannot be null");
        }

        Goal goal = goalDao.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found with ID: " + goalId));

        if (goal.getGoalType() == GoalType.PERSONAL) {
            if (!goal.getEmployeeId().equals(actor.getId())) {
                throw new AuthorizationException("Personal goals can only be deleted by the employee");
            }
        } else if (goal.getGoalType() == GoalType.MANAGER_ASSIGNED) {
            if (!goal.getSetBy().equals(actor.getId())) {
                throw new AuthorizationException("Manager-assigned goals can only be deleted by the setting manager");
            }
        }

        goalDao.delete(goalId);
    }

    @Override
    public List<Goal> listForEmployee(User actor, Long employeeId)
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
            throw new AuthorizationException("You do not have permission to view goals for this employee");
        }

        return goalDao.findByEmployee(employeeId);
    }

    @Override
    public List<Goal> listAssignedByMe(User actor) throws AuthorizationException, DataAccessException {
        if (actor == null || actor.getRole() != Role.MANAGER) {
            throw new AuthorizationException("Only managers can list assigned goals");
        }
        return goalDao.findBySetBy(actor.getId());
    }

    @Override
    public int markOverdueGoals(LocalDate today) throws DataAccessException {
        if (today == null) {
            today = LocalDate.now();
        }
        final LocalDate cutoff = today.minusDays(1);

        return transactionManager.executeInTransaction(conn -> {
            List<Goal> overdue = goalDao.findDueBefore(conn, cutoff);
            int count = 0;
            for (Goal g : overdue) {
                if (g.getStatus() != GoalStatus.COMPLETED && g.getStatus() != GoalStatus.MISSED) {
                    g.setStatus(GoalStatus.MISSED);
                    goalDao.update(conn, g);
                    count++;
                }
            }
            return count;
        });
    }
}
