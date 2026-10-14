package com.epes.servlet;

import com.epes.model.Role;
import com.epes.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet rendering the Admin dashboard with live system overview counts.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = "/admin/dashboard")
public class AdminDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            List<User> users = appContext().getUserService().listUsers(user);
            long totalUsers = users.size();
            long adminCount = users.stream().filter(u -> u.getRole() == Role.ADMIN).count();
            long managerCount = users.stream().filter(u -> u.getRole() == Role.MANAGER).count();
            long employeeCount = users.stream().filter(u -> u.getRole() == Role.EMPLOYEE).count();
            long activeCount = users.stream().filter(User::isActive).count();
            long inactiveCount = users.stream().filter(u -> !u.isActive()).count();

            req.setAttribute("totalUsers", totalUsers);
            req.setAttribute("adminCount", adminCount);
            req.setAttribute("managerCount", managerCount);
            req.setAttribute("employeeCount", employeeCount);
            req.setAttribute("activeCount", activeCount);
            req.setAttribute("inactiveCount", inactiveCount);
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "admin");
    }
}
