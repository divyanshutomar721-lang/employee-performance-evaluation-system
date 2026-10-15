package com.epes.servlet;

import com.epes.model.Evaluation;
import com.epes.model.Feedback;
import com.epes.model.User;
import com.epes.service.UserSummary;
import com.epes.service.report.ReportAggregator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet handling Employee Feedback History view, displaying qualitative and quantitative
 * feedback submitted by supervisors, along with linked evaluation periods and average ratings.
 */
@WebServlet(name = "EmployeeFeedbackServlet", urlPatterns = "/employee/feedback")
public class EmployeeFeedbackServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            List<Feedback> feedbackList = appContext().getFeedbackService().listForEmployee(user, user.getId());
            req.setAttribute("feedbackList", feedbackList);

            double averageRating = ReportAggregator.calculateAverageFeedbackRating(feedbackList);
            req.setAttribute("averageRating", averageRating);

            Set<Long> managerIds = new HashSet<>();
            Map<Long, String> evalPeriods = new HashMap<>();

            for (Feedback fb : feedbackList) {
                if (fb.getManagerId() != null) {
                    managerIds.add(fb.getManagerId());
                }
                if (fb.getEvaluationId() != null && !evalPeriods.containsKey(fb.getEvaluationId())) {
                    Evaluation eval = appContext().getEvaluationDao().findById(fb.getEvaluationId()).orElse(null);
                    evalPeriods.put(fb.getEvaluationId(), eval != null ? eval.getPeriodLabel() : "-");
                }
            }

            Map<Long, UserSummary> managers = appContext().getUserLookupService().summaries(user, managerIds);
            req.setAttribute("managers", managers);
            req.setAttribute("evalPeriods", evalPeriods);

        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "employee/feedback");
    }
}
