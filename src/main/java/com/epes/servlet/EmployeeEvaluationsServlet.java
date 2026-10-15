package com.epes.servlet;

import com.epes.exception.ValidationException;
import com.epes.model.Evaluation;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.service.UserSummary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Servlet handling read-only evaluation detail inspection for employees.
 * Access is verified by the EvaluationService ensuring an employee may only view their own evaluations.
 */
@WebServlet(name = "EmployeeEvaluationsServlet", urlPatterns = "/employee/evaluations/view")
public class EmployeeEvaluationsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            String idParam = req.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                throw new ValidationException("Evaluation ID parameter is required");
            }
            Long id = Long.valueOf(idParam.trim());

            Evaluation evaluation = appContext().getEvaluationService().getEvaluation(user, id);
            req.setAttribute("evaluation", evaluation);

            Set<Long> userIds = new HashSet<>();
            if (evaluation.getEmployeeId() != null) userIds.add(evaluation.getEmployeeId());
            if (evaluation.getManagerId() != null) userIds.add(evaluation.getManagerId());

            Map<Long, UserSummary> people = appContext().getUserLookupService().summaries(user, userIds);
            req.setAttribute("people", people);

            if (evaluation.getTeamId() != null) {
                Team team = appContext().getTeamDao().findById(evaluation.getTeamId()).orElse(null);
                req.setAttribute("teamName", team != null ? team.getName() : "Team #" + evaluation.getTeamId());
            } else {
                req.setAttribute("teamName", "Individual Review");
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "employee/evaluation-detail");
    }
}
