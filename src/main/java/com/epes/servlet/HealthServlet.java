package com.epes.servlet;

import com.epes.exception.DataAccessException;
import com.epes.util.ConnectionFactory;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lightweight health-check endpoint.
 *
 * <p>Maps to {@code /health} and attempts a simple {@code SELECT 1}
 * against the configured database. Returns plain-text status.</p>
 */
@WebServlet(name = "HealthServlet", urlPatterns = "/health")
public class HealthServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HealthServlet.class.getName());

    /**
     * Handles GET requests — performs a database connectivity check.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        try {
            ConnectionFactory cf = new ConnectionFactory();
            try (Connection conn = cf.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs   = stmt.executeQuery("SELECT 1")) {

                if (rs.next()) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    PrintWriter out = resp.getWriter();
                    out.print("EPES OK - database reachable");
                    out.flush();
                    LOG.info("Health check passed.");
                }
            }
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "Health check failed.", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            out.print("EPES ERROR - database unreachable. Check server logs for details.");
            out.flush();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected error during health check.", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = resp.getWriter();
            out.print("EPES ERROR - unexpected failure. Check server logs for details.");
            out.flush();
        }
    }
}
