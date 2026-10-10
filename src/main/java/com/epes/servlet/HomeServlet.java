package com.epes.servlet;

import com.epes.filter.AccessPolicy;
import com.epes.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet mapped to {@code /home} that routes authenticated users
 * to their respective role dashboards, and unauthenticated users to {@code /login}.
 */
@WebServlet(name = "HomeServlet", urlPatterns = "/home")
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user != null) {
            redirect(req, resp, AccessPolicy.dashboardPathFor(user.getRole()));
        } else {
            redirect(req, resp, "/login");
        }
    }
}
