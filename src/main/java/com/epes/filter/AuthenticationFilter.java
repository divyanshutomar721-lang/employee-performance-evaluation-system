package com.epes.filter;

import com.epes.config.AppConstants;
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

/**
 * Filter that enforces authentication on all non-public URLs.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse resp) {
            String contextPath = req.getContextPath();
            String uri = req.getRequestURI();
            String pathWithinApp = uri.substring(contextPath.length());

            if (AccessPolicy.isPublicPath(pathWithinApp)) {
                chain.doFilter(request, response);
                return;
            }

            HttpSession session = req.getSession(false);
            User user = (session != null) ? (User) session.getAttribute(AppConstants.SESSION_USER) : null;

            if (user == null) {
                resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                resp.sendRedirect(contextPath + "/login");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
