package com.epes.servlet;

import com.epes.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet rendering the Employee dashboard shell.
 */
@WebServlet(name = "EmployeeDashboardServlet", urlPatterns = "/employee/dashboard")
public class EmployeeDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);
        forward(req, resp, "employee");
    }
}
