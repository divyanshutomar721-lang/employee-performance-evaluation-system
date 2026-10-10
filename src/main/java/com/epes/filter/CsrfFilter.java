package com.epes.filter;

import com.epes.config.AppConstants;
import com.epes.util.CsrfSupport;
import com.epes.util.CsrfTokens;
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
 * Filter that validates CSRF tokens on all POST requests and exposes
 * the active session CSRF token to request attributes for view rendering.
 */
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse resp) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                String token = CsrfSupport.tokenFor(session);
                req.setAttribute(AppConstants.REQ_CSRF_TOKEN, token);
            }

            if ("POST".equalsIgnoreCase(req.getMethod())) {
                String sessionToken = (session != null)
                        ? (String) session.getAttribute(AppConstants.SESSION_CSRF)
                        : null;
                String requestToken = req.getParameter("_csrf");

                if (sessionToken == null || !CsrfTokens.matches(sessionToken, requestToken)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    req.setAttribute(AppConstants.REQ_ERROR_STATUS, HttpServletResponse.SC_FORBIDDEN);
                    req.setAttribute(AppConstants.REQ_ERROR_MESSAGE, "Your form expired. Please go back and try again.");
                    req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
