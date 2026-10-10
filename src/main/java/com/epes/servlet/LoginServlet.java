package com.epes.servlet;

import com.epes.config.AppConstants;
import com.epes.exception.AuthenticationException;
import com.epes.exception.DataAccessException;
import com.epes.filter.AccessPolicy;
import com.epes.model.User;
import com.epes.util.CsrfSupport;
import com.epes.util.CsrfTokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet handling user login via email authentication, session creation,
 * and session fixation defense.
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        if (user != null) {
            redirect(req, resp, "/home");
            return;
        }

        // Create a session to hold the CSRF token for the login form
        HttpSession session = req.getSession(true);
        String token = CsrfSupport.tokenFor(session);
        req.setAttribute(AppConstants.REQ_CSRF_TOKEN, token);
        req.setAttribute("tempLoginActive", appContext().getAuthService().isTemporaryLoginActive());

        forward(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        if (email != null) {
            email = email.trim();
        }

        try {
            User user = appContext().getAuthService().authenticate(email);

            // Session fixation defense: invalidate old session and create a fresh one
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = req.getSession(true);
            newSession.setAttribute(AppConstants.SESSION_USER, user);
            newSession.setAttribute(AppConstants.SESSION_CSRF, CsrfTokens.newToken());

            LOGGER.log(Level.INFO, "login ok user_id={0}", user.getId());

            redirect(req, resp, AccessPolicy.dashboardPathFor(user.getRole()));
        } catch (AuthenticationException ex) {
            String safeEmail = (email != null) ? email.replace('\r', '_').replace('\n', '_') : "";
            LOGGER.log(Level.WARNING, "Login failed for email: {0}", safeEmail);

            req.setAttribute("loginError", "Invalid email or account is inactive. Please try again.");
            req.setAttribute("email", email);
            req.setAttribute("tempLoginActive", appContext().getAuthService().isTemporaryLoginActive());

            // Ensure CSRF token is available on re-show
            HttpSession session = req.getSession(true);
            req.setAttribute(AppConstants.REQ_CSRF_TOKEN, CsrfSupport.tokenFor(session));

            forward(req, resp, "login");
        } catch (DataAccessException ex) {
            LOGGER.log(Level.SEVERE, "Database error during authentication", ex);

            req.setAttribute("loginError", "System is temporarily unavailable. Please try again shortly.");
            req.setAttribute("email", email);
            req.setAttribute("tempLoginActive", appContext().getAuthService().isTemporaryLoginActive());

            HttpSession session = req.getSession(true);
            req.setAttribute(AppConstants.REQ_CSRF_TOKEN, CsrfSupport.tokenFor(session));

            forward(req, resp, "login");
        }
    }
}
