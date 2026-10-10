package com.epes.filter;

import com.epes.config.AppConstants;
import com.epes.model.Role;
import com.epes.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filter that enforces role-based access control based on URL paths.
 */
public class RoleFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(RoleFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse resp) {
            String contextPath = req.getContextPath();
            String uri = req.getRequestURI();
            String pathWithinApp = uri.substring(contextPath.length());

            Role requiredRole = AccessPolicy.requiredRoleFor(pathWithinApp);
            if (requiredRole != null) {
                HttpSession session = req.getSession(false);
                User user = (session != null) ? (User) session.getAttribute(AppConstants.SESSION_USER) : null;

                if (user == null || user.getRole() != requiredRole) {
                    String email = (user != null) ? user.getEmail() : "anonymous";
                    String safeEmail = email.replace('\r', '_').replace('\n', '_');
                    String safePath = pathWithinApp.replace('\r', '_').replace('\n', '_');
                    LOGGER.log(Level.WARNING, "Forbidden access attempt by {0} to {1}", new Object[]{safeEmail, safePath});

                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    req.setAttribute(AppConstants.REQ_ERROR_STATUS, HttpServletResponse.SC_FORBIDDEN);
                    req.setAttribute(AppConstants.REQ_ERROR_MESSAGE, "You do not have permission to open this page");
                    req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
