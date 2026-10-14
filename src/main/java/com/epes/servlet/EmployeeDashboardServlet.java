package com.epes.servlet;

import com.epes.model.Evaluation;
import com.epes.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet rendering the Employee dashboard with performance stage indication.
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
                List<Evaluation> evals = appContext().getEvaluationService().listForEmployee(user, user.getId());
                if (evals != null && !evals.isEmpty()) {
                    Evaluation latest = evals.get(0);
                    req.setAttribute("rocketStage", latest.getOverallStage() != null ? latest.getOverallStage().getLabel() : null);
                }
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "employee");
    }
}
