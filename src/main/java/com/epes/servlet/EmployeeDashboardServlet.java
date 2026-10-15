package com.epes.servlet;

import com.epes.model.Feedback;
import com.epes.model.GoalStatus;
import com.epes.model.User;
import com.epes.service.UserSummary;
import com.epes.service.report.EmployeeReport;
import com.epes.util.ChartJson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet rendering the comprehensive Employee dashboard with current rocket stage,
 * trajectory trend, progress score line chart, goal status counts, and latest feedback.
 */
@WebServlet(name = "EmployeeDashboardServlet", urlPatterns = "/employee/dashboard")
public class EmployeeDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            if (user != null && user.getId() != null) {
                EmployeeReport report = appContext().getReportService().buildEmployeeReport(user, user.getId());
                req.setAttribute("report", report);

                boolean hasEvaluations = !report.getEvaluations().isEmpty();
                req.setAttribute("hasEvaluations", hasEvaluations);

                if (hasEvaluations) {
                    req.setAttribute("latestScore", report.getLatestScore());
                    req.setAttribute("latestStage", report.getLatestStage());
                    req.setAttribute("trend", report.getTrend());

                    // Progress line chart (oldest first)
                    String lineChartJson = ChartJson.lineChart(
                            report.getChartLabels(),
                            report.getChartScores(),
                            "Overall Score",
                            0,
                            100
                    );
                    req.setAttribute("lineChartJson", lineChartJson);
                    req.setAttribute("needsCharts", true);
                }

                // Goal counts
                Map<GoalStatus, Long> goalCounts = report.getGoalCounts();
                req.setAttribute("goalCounts", goalCounts);

                // Latest 3 feedback items with manager names
                List<Feedback> allFeedback = appContext().getFeedbackService().listForEmployee(user, user.getId());
                List<Feedback> latestFeedback = allFeedback.stream().limit(3).toList();
                req.setAttribute("latestFeedback", latestFeedback);

                Set<Long> managerIds = new HashSet<>();
                for (Feedback fb : latestFeedback) {
                    if (fb.getManagerId() != null) {
                        managerIds.add(fb.getManagerId());
                    }
                }
                Map<Long, UserSummary> managers = appContext().getUserLookupService().summaries(user, managerIds);
                req.setAttribute("managers", managers);
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "employee");
    }
}
