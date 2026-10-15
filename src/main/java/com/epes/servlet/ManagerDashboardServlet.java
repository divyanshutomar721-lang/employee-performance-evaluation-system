package com.epes.servlet;

import com.epes.model.Evaluation;
import com.epes.model.Feedback;
import com.epes.model.Goal;
import com.epes.model.User;
import com.epes.service.UserSummary;
import com.epes.service.report.ManagerOverview;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet rendering the Manager dashboard with live counts and recent items
 * for Performance Evaluations, Goal Setting, and Feedback Provision.
 */
@WebServlet(name = "ManagerDashboardServlet", urlPatterns = "/manager/dashboard")
public class ManagerDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            ManagerOverview overview = appContext().getReportService().buildManagerOverview(user);
            int criteriaCount = appContext().getCriterionService().listMine(user, false).size();
            int teamCount = overview.getTeams().size();
            int employeeCount = overview.getTotalEmployees();

            req.setAttribute("teamCount", teamCount);
            req.setAttribute("criteriaCount", criteriaCount);
            req.setAttribute("employeeCount", employeeCount);

            // Compute aggregated stage distribution across manager's teams
            java.util.Map<com.epes.model.RocketStage, Long> totalStageDist = new java.util.EnumMap<>(com.epes.model.RocketStage.class);
            for (com.epes.model.RocketStage s : com.epes.model.RocketStage.values()) {
                totalStageDist.put(s, 0L);
            }
            long evaluatedCount = 0;
            for (com.epes.service.report.TeamReport tr : overview.getTeams()) {
                if (tr.getStageDistribution() != null) {
                    for (java.util.Map.Entry<com.epes.model.RocketStage, Long> entry : tr.getStageDistribution().entrySet()) {
                        totalStageDist.put(entry.getKey(), totalStageDist.get(entry.getKey()) + entry.getValue());
                        evaluatedCount += entry.getValue();
                    }
                }
            }

            if (evaluatedCount > 0) {
                List<String> labels = List.of("Launchpad", "Liftoff", "Orbit", "Cruise", "Summit");
                List<Long> counts = List.of(
                        totalStageDist.get(com.epes.model.RocketStage.LAUNCHPAD),
                        totalStageDist.get(com.epes.model.RocketStage.LIFTOFF),
                        totalStageDist.get(com.epes.model.RocketStage.ORBIT),
                        totalStageDist.get(com.epes.model.RocketStage.CRUISE),
                        totalStageDist.get(com.epes.model.RocketStage.SUMMIT)
                );
                long maxCount = counts.stream().max(Long::compare).orElse(5L);
                String stageChartJson = com.epes.util.ChartJson.barChart(
                        labels,
                        counts,
                        "Evaluated Employees",
                        0,
                        Math.max(5.0, (double) maxCount),
                        com.epes.util.ChartJson.STAGE_COLORS
                );
                req.setAttribute("stageChartJson", stageChartJson);
                req.setAttribute("needsCharts", true);
            }


            // Fetch recent items (limit 5)
            List<Evaluation> allEvals = appContext().getEvaluationService().listForManager(user);
            List<Evaluation> recentEvals = allEvals.stream().limit(5).toList();

            List<Goal> allGoals = appContext().getGoalService().listAssignedByMe(user);
            List<Goal> recentGoals = allGoals.stream().limit(5).toList();

            List<Feedback> allFeedback = appContext().getFeedbackService().listGivenByMe(user);
            List<Feedback> recentFeedback = allFeedback.stream().limit(5).toList();

            Set<Long> userIds = new HashSet<>();
            for (Evaluation e : recentEvals) {
                if (e.getEmployeeId() != null) userIds.add(e.getEmployeeId());
            }
            for (Goal g : recentGoals) {
                if (g.getEmployeeId() != null) userIds.add(g.getEmployeeId());
            }
            for (Feedback f : recentFeedback) {
                if (f.getEmployeeId() != null) userIds.add(f.getEmployeeId());
            }

            Map<Long, UserSummary> people = appContext().getUserLookupService().summaries(user, userIds);

            req.setAttribute("recentEvaluations", recentEvals);
            req.setAttribute("recentGoals", recentGoals);
            req.setAttribute("recentFeedback", recentFeedback);
            req.setAttribute("people", people);
            req.setAttribute("today", LocalDate.now());
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "manager");
    }
}
