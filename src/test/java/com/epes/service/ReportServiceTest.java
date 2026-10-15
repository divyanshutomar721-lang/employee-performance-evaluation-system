package com.epes.service;

import com.epes.model.Criterion;
import com.epes.model.RocketStage;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.model.UserFactory;
import com.epes.service.report.EmployeeReport;
import com.epes.service.report.ManagerOverview;
import com.epes.service.report.TeamReport;
import com.epes.service.report.Trend;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link ReportService}.
 */
class ReportServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Build employee report from two evaluations and verify trend, charts, and averages")
    void testEmployeeReport() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Report Mgr", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Report Emp", Role.EMPLOYEE, true, null));

        Team team = appContext.getTeamService().createTeam(
                manager, randomTeamName(), "Dev Team",
                List.of(employee.getId()), Map.of());

        Criterion c1 = appContext.getCriterionService().create(manager, "Skill", "Competence", 100);

        // Evaluation 1 (Score 60.00 -> ORBIT)
        appContext.getEvaluationService().createEvaluation(
                manager, employee.getId(), team.getId(), "Q1 2026", "Good start",
                Map.of(c1.getId(), new ScoreInput(3, "Average"))
        );

        // Evaluation 2 (Score 80.00 -> CRUISE, improvement +20 > 2.0 -> IMPROVING)
        appContext.getEvaluationService().createEvaluation(
                manager, employee.getId(), team.getId(), "Q2 2026", "Significant growth",
                Map.of(c1.getId(), new ScoreInput(4, "Great"))
        );

        // Create a personal goal
        appContext.getGoalService().createPersonalGoal(employee, "Learn Java Concurrency", LocalDate.now().plusDays(30));

        // Submit feedback
        appContext.getFeedbackService().giveFeedback(manager, employee.getId(), null, "Strong progress this quarter", 4);

        EmployeeReport report = appContext.getReportService().buildEmployeeReport(employee, employee.getId());
        assertNotNull(report);
        assertEquals(employee.getId(), report.getEmployee().getId());
        assertEquals(2, report.getEvaluations().size());
        assertEquals(List.of("Q1 2026", "Q2 2026"), report.getChartLabels());
        assertEquals(List.of(60.0, 80.0), report.getChartScores());
        assertEquals(70.00, report.getAverageScore());
        assertEquals(80.00, report.getLatestScore());
        assertEquals(RocketStage.CRUISE, report.getLatestStage());
        assertEquals(Trend.IMPROVING, report.getTrend());
        assertEquals(1, report.getFeedbackCount());
        assertEquals(4.00, report.getAverageFeedbackRating());
        assertNotNull(report.getAverageRatingByCriterion());
        assertEquals(1, report.getAverageRatingByCriterion().size());
        assertEquals(3.50, report.getAverageRatingByCriterion().get("Skill"));
    }


    @Test
    @DisplayName("Build team report with sorted employee summaries and stage distribution")
    void testTeamReportAndManagerOverview() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Team Report Mgr", Role.MANAGER, true, null));
        User emp1 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Emp Alpha", Role.EMPLOYEE, true, null));
        User emp2 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Emp Beta", Role.EMPLOYEE, true, null));

        Team team = appContext.getTeamService().createTeam(
                manager, randomTeamName(), "High Performers",
                List.of(emp1.getId(), emp2.getId()), Map.of());

        Criterion c = appContext.getCriterionService().create(manager, "Execution", "Speed", 100);

        // Emp1 gets 100.00 (SUMMIT)
        appContext.getEvaluationService().createEvaluation(
                manager, emp1.getId(), team.getId(), "Q1 2026", "Perfect",
                Map.of(c.getId(), new ScoreInput(5, "Top"))
        );

        // Emp2 gets 80.00 (CRUISE)
        appContext.getEvaluationService().createEvaluation(
                manager, emp2.getId(), team.getId(), "Q1 2026", "Great",
                Map.of(c.getId(), new ScoreInput(4, "Great"))
        );

        TeamReport teamReport = appContext.getReportService().buildTeamReport(manager, team.getId());
        assertNotNull(teamReport);
        assertEquals(2, teamReport.getRows().size());

        // Verify sorted descending by average score (Emp1 first, Emp2 second)
        assertEquals(emp1.getId(), teamReport.getRows().get(0).getEmployeeId());
        assertEquals(100.00, teamReport.getRows().get(0).getAverageScore());
        assertEquals(emp2.getId(), teamReport.getRows().get(1).getEmployeeId());
        assertEquals(80.00, teamReport.getRows().get(1).getAverageScore());

        // Verify stage distribution has all 5 stages
        Map<RocketStage, Long> dist = teamReport.getStageDistribution();
        assertEquals(5, dist.size());
        assertEquals(1L, dist.get(RocketStage.SUMMIT));
        assertEquals(1L, dist.get(RocketStage.CRUISE));
        assertEquals(0L, dist.get(RocketStage.LAUNCHPAD));

        // Test Manager Overview
        ManagerOverview overview = appContext.getReportService().buildManagerOverview(manager);
        assertEquals(1, overview.getTeams().size());
        assertEquals(2, overview.getTotalEmployees());
        assertEquals(90.00, overview.getOverallAverage());
    }
}
