package com.epes.servlet;

import com.epes.model.GoalStatus;
import com.epes.model.User;
import com.epes.service.report.EmployeeReport;
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
 * Servlet generating self-assessment performance report with KPI analytics cards,
 * score history line charts, goal status doughnut charts, and criterion average bar charts.
 */
@WebServlet(name = "EmployeeReportsServlet", urlPatterns = "/employee/reports")
public class EmployeeReportsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            EmployeeReport report = appContext().getReportService().buildEmployeeReport(user, user.getId());
            req.setAttribute("report", report);
            req.setAttribute("hasEvaluations", !report.getEvaluations().isEmpty());

            // 1. Line chart: overall scores across periods
            String lineChartJson = ChartJson.lineChart(
                    report.getChartLabels(),
                    report.getChartScores(),
                    "Overall Score",
                    0,
                    100
            );
            req.setAttribute("lineChartJson", lineChartJson);

            // 2. Doughnut chart: goal status distribution
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

            // 3. Bar chart: criterion average ratings across all evaluations (y: 0 to 5)
            Map<String, Double> critAvgs = report.getAverageRatingByCriterion();
            List<String> critLabels = new ArrayList<>(critAvgs.keySet());
            List<Double> critValues = new ArrayList<>(critAvgs.values());
            String critBarJson = ChartJson.barChart(critLabels, critValues, "Criterion Average", 0, 5);
            req.setAttribute("critBarJson", critBarJson);

            req.setAttribute("needsCharts", true);

        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "employee/reports");
    }
}
