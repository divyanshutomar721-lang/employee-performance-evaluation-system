package com.epes.servlet;

import com.epes.model.User;
import com.epes.service.report.ManagerOverview;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet rendering the Manager dashboard with live team and criteria counts.
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
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "manager");
    }
}
