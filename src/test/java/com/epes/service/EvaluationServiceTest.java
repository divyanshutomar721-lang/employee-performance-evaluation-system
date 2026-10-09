package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.model.Criterion;
import com.epes.model.Evaluation;
import com.epes.model.EvaluationScore;
import com.epes.model.RocketStage;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link EvaluationService}.
 */
class EvaluationServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Create evaluation with calculated score and snapshots, and verify authorization")
    void testCreateEvaluationHappyPath() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Eval Manager", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Eval Employee", Role.EMPLOYEE, true, null));

        // Put employee in manager's team
        Team team = appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Dev Team",
                List.of(employee.getId()), Map.of(employee.getId(), "Engineer"));

        // Create criteria
        Criterion c1 = appContext.getCriterionService().create(manager, "Execution", "Delivery speed", 50);
        Criterion c2 = appContext.getCriterionService().create(manager, "Quality", "Bug free code", 50);

        String period = randomPeriod();
        Evaluation eval = appContext.getEvaluationService().createEvaluation(
                manager,
                employee.getId(),
                team.getId(),
                period,
                "Strong performance throughout the cycle",
                Map.of(
                        c1.getId(), new ScoreInput(4, "Great delivery"),
                        c2.getId(), new ScoreInput(5, "Zero regressions")
                )
        );

        assertNotNull(eval.getId());
        // (4*50 + 5*50) / (5*100) * 100 = 450/500 * 100 = 90.00 -> SUMMIT
        assertEquals(new BigDecimal("90.00"), eval.getOverallScore());
        assertEquals(RocketStage.SUMMIT, eval.getOverallStage());
        assertEquals(2, eval.getScores().size());

        // Verify snapshots are preserved
        EvaluationScore score1 = eval.getScores().stream()
                .filter(s -> s.getCriterionId().equals(c1.getId()))
                .findFirst().orElseThrow();
        assertEquals("Execution", score1.getCriterionName());
        assertEquals(50, score1.getWeight());
        assertEquals(4, score1.getRating());

        // Both employee and manager can retrieve it
        Evaluation byMgr = appContext.getEvaluationService().getEvaluation(manager, eval.getId());
        assertEquals(eval.getId(), byMgr.getId());

        Evaluation byEmp = appContext.getEvaluationService().getEvaluation(employee, eval.getId());
        assertEquals(eval.getId(), byEmp.getId());
    }

    @Test
    @DisplayName("Evaluating an employee outside managed teams throws AuthorizationException")
    void testEvaluatingUnsupervisedEmployeeFails() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Manager X", Role.MANAGER, true, null));
        User otherManager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Manager Y", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Emp Y", Role.EMPLOYEE, true, null));

        // Employee belongs to otherManager
        appContext.getTeamService().createTeam(
                otherManager, randomTeamName(), "Team Y",
                List.of(employee.getId()), Map.of());

        Criterion c = appContext.getCriterionService().create(manager, "Standard", "Desc", 100);

        assertThrows(AuthorizationException.class, () ->
                appContext.getEvaluationService().createEvaluation(
                        manager,
                        employee.getId(),
                        null,
                        randomPeriod(),
                        "Remarks",
                        Map.of(c.getId(), new ScoreInput(4, "Good"))
                ));
    }

    @Test
    @DisplayName("Duplicate evaluation for same employee, manager, and period throws BusinessRuleException")
    void testDuplicatePeriodRejected() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Manager Dup", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Emp Dup", Role.EMPLOYEE, true, null));

        appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Team Dup",
                List.of(employee.getId()), Map.of());

        Criterion c = appContext.getCriterionService().create(manager, "Focus", "Desc", 100);
        String period = randomPeriod();

        appContext.getEvaluationService().createEvaluation(
                manager, employee.getId(), null, period, "First eval",
                Map.of(c.getId(), new ScoreInput(4, "Comment")));

        assertThrows(BusinessRuleException.class, () ->
                appContext.getEvaluationService().createEvaluation(
                        manager, employee.getId(), null, period, "Second eval duplicate",
                        Map.of(c.getId(), new ScoreInput(5, "Comment"))));
    }

    @Test
    @DisplayName("Using an inactive criterion throws BusinessRuleException")
    void testInactiveCriterionRejected() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Manager Inact", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Emp Inact", Role.EMPLOYEE, true, null));

        appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Team Inact",
                List.of(employee.getId()), Map.of());

        Criterion c = appContext.getCriterionService().create(manager, "Old Standard", "Desc", 100);
        appContext.getCriterionService().deactivate(manager, c.getId());

        assertThrows(BusinessRuleException.class, () ->
                appContext.getEvaluationService().createEvaluation(
                        manager, employee.getId(), null, randomPeriod(), "Comment",
                        Map.of(c.getId(), new ScoreInput(4, "Good"))));
    }
}
