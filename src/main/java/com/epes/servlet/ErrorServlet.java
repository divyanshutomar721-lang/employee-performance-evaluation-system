package com.epes.servlet;

import com.epes.config.AppConstants;
import com.epes.exception.ServiceException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central error-handling servlet referenced by {@code web.xml} error-page mappings.
 *
 * <p>Logs technical exception details while presenting clean, user-friendly
 * error descriptions without leaking stack traces or sensitive internals.</p>
 */
@WebServlet(name = "ErrorServlet", urlPatterns = "/error")
public class ErrorServlet extends BaseServlet {

    private static final Logger LOGGER = Logger.getLogger(ErrorServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processError(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processError(req, resp);
    }

    private void processError(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer statusCode = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Throwable throwable = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String customMessage = (String) req.getAttribute(AppConstants.REQ_ERROR_MESSAGE);
        Integer customStatus = (Integer) req.getAttribute(AppConstants.REQ_ERROR_STATUS);

        int status = (customStatus != null) ? customStatus
                : ((statusCode != null) ? statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        if (throwable != null) {
            LOGGER.log(Level.SEVERE, "Handled error with status " + status, throwable);
        } else if (status >= 400) {
            LOGGER.log(Level.WARNING, "Handled error response with status {0}", status);
        }

        String userMessage;
        if (customMessage != null && !customMessage.isBlank()) {
            userMessage = customMessage;
        } else if (throwable instanceof ServiceException || (throwable != null && throwable.getCause() instanceof ServiceException)) {
            Throwable cause = (throwable instanceof ServiceException) ? throwable : throwable.getCause();
            userMessage = cause.getMessage();
        } else {
            userMessage = switch (status) {
                case HttpServletResponse.SC_FORBIDDEN -> "You do not have permission to open this page.";
                case HttpServletResponse.SC_NOT_FOUND -> "The requested page was not found.";
                default -> "An unexpected system error occurred. Please try again later.";
            };
        }

        resp.setStatus(status);
        req.setAttribute(AppConstants.REQ_ERROR_STATUS, status);
        req.setAttribute(AppConstants.REQ_ERROR_MESSAGE, userMessage);

        forward(req, resp, "error");
    }
}
