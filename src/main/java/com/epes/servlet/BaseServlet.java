package com.epes.servlet;

import com.epes.config.AppConstants;
import com.epes.config.AppContext;
import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ServiceException;
import com.epes.exception.ValidationException;
import com.epes.model.User;
import com.epes.util.Flash;
import com.epes.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Abstract base class for all EPES servlets.
 *
 * <p>Provides helper methods for dependency retrieval, session user access,
 * view forwarding, redirecting, flash messaging, and exception mapping.</p>
 */
public abstract class BaseServlet extends HttpServlet {

    /**
     * Retrieves the central {@link AppContext} from the servlet context.
     *
     * @return the application context
     * @throws IllegalStateException if the context is not initialized (503)
     */
    protected AppContext appContext() {
        AppContext ctx = (AppContext) getServletContext().getAttribute(AppConstants.APP_CONTEXT);
        if (ctx == null) {
            throw new IllegalStateException("The system is starting or the database is not available. Please try again shortly.");
        }
        return ctx;
    }

    /**
     * Retrieves the authenticated {@link User} from the current session, or null if none.
     *
     * @param req the HTTP request
     * @return current user or null
     */
    protected User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(AppConstants.SESSION_USER);
    }

    /**
     * Forwards the request to a JSP view under {@code /WEB-INF/views/<viewName>.jsp}.
     * Also consumes any flash messages into request attributes for rendering.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @param viewName the name of the view without extension (e.g. "login" or "admin")
     * @throws ServletException on forwarding error
     * @throws IOException on IO error
     */
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String viewName)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            req.setAttribute(AppConstants.REQ_FLASH_MESSAGES, Flash.consume(session));
        }
        req.getRequestDispatcher("/WEB-INF/views/" + viewName + ".jsp").forward(req, resp);
    }

    /**
     * Redirects to a path within the application context.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @param pathWithinApp the destination path relative to context root
     * @throws IOException on redirect error
     */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String pathWithinApp)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + pathWithinApp);
    }

    /**
     * Adds a flash message to the current user's session.
     *
     * @param req the HTTP request
     * @param type message visual type
     * @param text message text
     */
    protected void flash(HttpServletRequest req, FlashMessage.Type type, String text) {
        Flash.add(req.getSession(true), type, text);
    }

    /**
     * Maps known service exceptions to view attributes, flash messages, or error codes.
     *
     * @param req the HTTP request
     * @param resp the HTTP response
     * @param ex the exception to handle
     * @throws ServletException if unexpected error or forwarding failure
     * @throws IOException on IO error
     */
    protected void handleServiceException(HttpServletRequest req, HttpServletResponse resp, Exception ex)
            throws ServletException, IOException {
        if (ex instanceof ValidationException valEx) {
            req.setAttribute(AppConstants.REQ_VALIDATION_ERRORS, valEx.getErrors());
        } else if (ex instanceof AuthorizationException) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute(AppConstants.REQ_ERROR_STATUS, HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute(AppConstants.REQ_ERROR_MESSAGE, ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        } else if (ex instanceof BusinessRuleException || ex instanceof EntityNotFoundException) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        } else {
            throw new ServletException(ex);
        }
    }
}
