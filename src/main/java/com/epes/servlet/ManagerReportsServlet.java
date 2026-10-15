package com.epes.servlet;

import com.epes.exception.ValidationException;
import com.epes.model.GoalStatus;
import com.epes.model.RocketStage;
import com.epes.model.User;
import com.epes.service.report.EmployeeReport;
import com.epes.service.report.ManagerOverview;
import com.epes.service.report.TeamReport;
import com.epes.util.ChartJson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling managerial performance reporting: departmental overview,
 * team stage distribution and ranked member rosters, and individual employee drilldowns.
 */
@WebServlet(name = "ManagerReportsServlet", urlPatterns = {
        "/manager/reports",
        "/manager/reports/team",
        "/manager/reports/employee"
})
public class ManagerReportsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        String path = req.getServletPath();

        try {
            switch (path) {
                case "/manager/reports" -> showOverview(req, resp, user);
                case "/manager/reports/team" -> showTeamReport(req, resp, user);
                case "/manager/reports/employee" -> showEmployeeReport(req, resp, user);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void showOverview(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {
        ManagerOverview overview = appContext().getReportService().buildManagerOverview(user);
        req.setAttribute("overview", overview);
        forward(req, resp, "manager/reports");
    }

    private void showTeamReport(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            throw new ValidationException("Team ID parameter is required");
        }
        Long teamId = Long.valueOf(idParam.trim());

        TeamReport teamReport = appContext().getReportService().buildTeamReport(user, teamId);
        req.setAttribute("teamReport", teamReport);

        // Stage distribution bar chart (all 5 stages, colored by stage)
        Map<RocketStage, Long> dist = teamReport.getStageDistribution();
        List<String> labels = List.of("Launchpad", "Liftoff", "Orbit", "Cruise", "Summit");
        List<Long> counts = List.of(
                dist.getOrDefault(RocketStage.LAUNCHPAD, 0L),
                dist.getOrDefault(RocketStage.LIFTOFF, 0L),
                dist.getOrDefault(RocketStage.ORBIT, 0L),
                dist.getOrDefault(RocketStage.CRUISE, 0L),
                dist.getOrDefault(RocketStage.SUMMIT, 0L)
        );
        long maxCount = counts.stream().max(Long::compare).orElse(5L);
        double yMax = Math.max(5.0, (double) maxCount);

        String stageChartJson = ChartJson.barChart(
                labels,
                counts,
                "Employees in Stage",
                0,
                yMax,
                ChartJson.STAGE_COLORS
        );
        req.setAttribute("stageChartJson", stageChartJson);
        req.setAttribute("needsCharts", true);

        forward(req, resp, "manager/team-report");
    }

    private void showEmployeeReport(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            throw new ValidationException("Employee ID parameter is required");
        }
        Long employeeId = Long.valueOf(idParam.trim());

        EmployeeReport report = appContext().getReportService().buildEmployeeReport(user, employeeId);
        req.setAttribute("report", report);
        req.setAttribute("hasEvaluations", !report.getEvaluations().isEmpty());

        // 1. Line chart
        String lineChartJson = ChartJson.lineChart(
                report.getChartLabels(),
                report.getChartScores(),
                "Overall Score",
                0,
                100
        );
        req.setAttribute("lineChartJson", lineChartJson);

        // 2. Goal Doughnut chart
        Map<GoalStatus, Long> gc = report.getGoalCounts();
        List<String> goalLabels = List.of("Not Started", "In Progress", "Completed", "Missed");
        List<Long> goalValues = List.of(
                gc.getOrDefault(GoalStatus.NOT_STARTED, 0L),
                gc.getOrDefault(GoalStatus.IN_PROGRESS, 0L),
                gc.getOrDefault(GoalStatus.COMPLETED, 0L),
                gc.getOrDefault(GoalStatus.MISSED, 0L)
        );
        String goalDoughnutJson = ChartJson.doughnutChart(goalLabels, goalValues, "Goal Distribution");
        req.setAttribute("goalDoughnutJson", goalDoughnutJson);

        // 3. Criterion Bar chart
        Map<String, Double> critAvgs = report.getAverageRatingByCriterion();
        List<String> critLabels = new ArrayList<>(critAvgs.keySet());
        List<Double> critValues = new ArrayList<>(critAvgs.values());
        String critBarJson = ChartJson.barChart(critLabels, critValues, "Criterion Average", 0, 5);
        req.setAttribute("critBarJson", critBarJson);

        String teamIdParam = req.getParameter("teamId");
        if (teamIdParam != null && !teamIdParam.trim().isEmpty()) {
            req.setAttribute("backUrl", req.getContextPath() + "/manager/reports/team?id=" + teamIdParam.trim());
            req.setAttribute("backLabel", "Team Report");
        } else {
            req.setAttribute("backUrl", req.getContextPath() + "/manager/reports");
            req.setAttribute("backLabel", "Manager Reports");
        }
        req.setAttribute("needsCharts", true);

        forward(req, resp, "employee/reports");
    }
}
