package com.epes.servlet;

import com.epes.model.Evaluation;
import com.epes.model.EvaluationScore;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.service.UserSummary;
import com.epes.service.report.EmployeeReport;
import com.epes.util.ChartJson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet handling Employee Performance Tracking view with chronological line chart,
 * latest evaluation criterion rating bar chart, and evaluation review history.
 */
@WebServlet(name = "EmployeePerformanceServlet", urlPatterns = "/employee/performance")
public class EmployeePerformanceServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            EmployeeReport report = appContext().getReportService().buildEmployeeReport(user, user.getId());
            List<Evaluation> evals = appContext().getEvaluationService().listForEmployee(user, user.getId());

            req.setAttribute("evaluations", evals);
            boolean hasEvaluations = evals != null && !evals.isEmpty();
            req.setAttribute("hasEvaluations", hasEvaluations);

            if (hasEvaluations) {
                // Line chart: overall score by period (oldest first)
                String lineChartJson = ChartJson.lineChart(
                        report.getChartLabels(),
                        report.getChartScores(),
                        "Overall Score",
                        0,
                        100
                );
                req.setAttribute("lineChartJson", lineChartJson);

                // Bar chart of latest evaluation's ratings per criterion (y: 0 to 5)
                Evaluation latest = evals.get(0);
                Evaluation latestWithScores = appContext().getEvaluationService().getEvaluation(user, latest.getId());
                req.setAttribute("latestEvaluation", latestWithScores);

                List<String> criterionNames = new ArrayList<>();
                List<Integer> criterionRatings = new ArrayList<>();
                if (latestWithScores.getScores() != null) {
                    for (EvaluationScore s : latestWithScores.getScores()) {
                        criterionNames.add(s.getCriterionName());
                        criterionRatings.add(s.getRating());
                    }
                }
                String barChartJson = ChartJson.barChart(criterionNames, criterionRatings, "Rating", 0, 5);
                req.setAttribute("barChartJson", barChartJson);

                // Manager summaries and team names
                Set<Long> managerIds = new HashSet<>();
                Map<Long, String> teamNames = new HashMap<>();
                for (Evaluation e : evals) {
                    if (e.getManagerId() != null) {
                        managerIds.add(e.getManagerId());
                    }
                    if (e.getTeamId() != null && !teamNames.containsKey(e.getTeamId())) {
                        Team t = appContext().getTeamDao().findById(e.getTeamId()).orElse(null);
                        teamNames.put(e.getTeamId(), t != null ? t.getName() : "Team #" + e.getTeamId());
                    }
                }
                Map<Long, UserSummary> managers = appContext().getUserLookupService().summaries(user, managerIds);
                req.setAttribute("managers", managers);
                req.setAttribute("teamNames", teamNames);
                req.setAttribute("needsCharts", true);
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "employee/performance");
    }
}
