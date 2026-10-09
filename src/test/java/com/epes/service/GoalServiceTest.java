package com.epes.service;

import com.epes.exception.ValidationException;
import com.epes.model.Goal;
import com.epes.model.GoalStatus;
import com.epes.model.GoalType;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GoalService}.
 */
class GoalServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Manager assigns goal, employee updates progress through lifecycle to COMPLETED")
    void testGoalLifecycle() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Goal Mgr", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Goal Emp", Role.EMPLOYEE, true, null));

        appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Dev Team",
                List.of(employee.getId()), Map.of());

        // Assign goal
        Goal g = appContext.getGoalService().assignGoal(
                manager, employee.getId(), "Deliver authentication module", LocalDate.now().plusDays(10));
        assertNotNull(g.getId());
        assertEquals(GoalType.MANAGER_ASSIGNED, g.getGoalType());
        assertEquals(GoalStatus.NOT_STARTED, g.getStatus());

        // Update progress to 50% -> IN_PROGRESS
        Goal inProg = appContext.getGoalService().updateProgress(employee, g.getId(), 50);
        assertEquals(50, inProg.getProgressPercent());
        assertEquals(GoalStatus.IN_PROGRESS, inProg.getStatus());

        // Update progress to 100% -> COMPLETED
        Goal completed = appContext.getGoalService().updateProgress(employee, g.getId(), 100);
        assertEquals(100, completed.getProgressPercent());
        assertEquals(GoalStatus.COMPLETED, completed.getStatus());

        // Reopen goal by lowering progress to 80% -> IN_PROGRESS
        Goal reopened = appContext.getGoalService().updateProgress(employee, g.getId(), 80);
        assertEquals(80, reopened.getProgressPercent());
        assertEquals(GoalStatus.IN_PROGRESS, reopened.getStatus());
    }

    @Test
    @DisplayName("Employee creates personal goal and edits it")
    void testPersonalGoal() throws Exception {
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Self Emp", Role.EMPLOYEE, true, null));

        Goal personal = appContext.getGoalService().createPersonalGoal(
                employee, "Complete Kubernetes certification", LocalDate.now().plusMonths(1));
        assertNotNull(personal.getId());
        assertEquals(GoalType.PERSONAL, personal.getGoalType());

        Goal edited = appContext.getGoalService().editGoal(
                employee, personal.getId(), "Complete CKA Certification", LocalDate.now().plusMonths(2));
        assertEquals("Complete CKA Certification", edited.getDescription());
    }

    @Test
    @DisplayName("Creating goal with past target date throws ValidationException")
    void testPastDateRejected() throws Exception {
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Date Emp", Role.EMPLOYEE, true, null));

        assertThrows(ValidationException.class, () ->
                appContext.getGoalService().createPersonalGoal(
                        employee, "Read book", LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("markOverdueGoals marks overdue non-completed goals as MISSED")
    void testMarkOverdueGoals() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Overdue Mgr", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Overdue Emp", Role.EMPLOYEE, true, null));

        appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Overdue Team",
                List.of(employee.getId()), Map.of());

        // Directly insert an overdue goal (targetDate yesterday) via DAO
        Goal overdueGoal = new Goal(
                null,
                employee.getId(),
                manager.getId(),
                GoalType.MANAGER_ASSIGNED,
                "Past deadline task",
                LocalDate.now().minusDays(2),
                GoalStatus.IN_PROGRESS,
                30,
                null,
                null
        );
        appContext.getGoalDao().create(overdueGoal);

        // Run markOverdueGoals for today
        int markedCount = appContext.getGoalService().markOverdueGoals(LocalDate.now());
        assertTrue(markedCount >= 1);

        Goal updated = appContext.getGoalDao().findById(overdueGoal.getId()).orElseThrow();
        assertEquals(GoalStatus.MISSED, updated.getStatus());
    }
}
