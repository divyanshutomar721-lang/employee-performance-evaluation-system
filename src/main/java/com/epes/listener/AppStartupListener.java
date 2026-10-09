package com.epes.listener;

import com.epes.config.AppConstants;
import com.epes.config.AppContext;
import com.epes.dao.UserDao;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.ConfigurationException;
import com.epes.exception.DataAccessException;
import com.epes.model.Admin;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.util.ConnectionFactory;
import com.epes.util.SchemaInitializer;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application startup and shutdown listener.
 *
 * <p>Initializes the database schema, seeds the optional administrator, constructs the central
 * {@link AppContext}, starts background threads, and guarantees graceful cleanup on shutdown.</p>
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("Initializing Employee Performance Evaluation System (EPES)...");

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();

            // 1. Initialize DB schema
            SchemaInitializer schemaInitializer = new SchemaInitializer(connectionFactory);
            schemaInitializer.initialize();
            LOG.info("EPES database schema verification completed.");

            // 2. Bootstrap admin user if ADMIN_EMAIL is provided
            bootstrapAdminUser(connectionFactory);

            // 3. Instantiate AppContext, start background daemons, and store in ServletContext
            AppContext appContext = new AppContext(connectionFactory);
            appContext.start();
            sce.getServletContext().setAttribute(AppConstants.APP_CONTEXT, appContext);

            LOG.info("EPES Application initialized successfully.");
        } catch (ConfigurationException ce) {
            LOG.log(Level.WARNING, "Database configuration missing during startup: " + ce.getMessage()
                    + " — Application will still start; check /health endpoint.", ce);
        } catch (DataAccessException dae) {
            LOG.log(Level.WARNING, "Database error during startup schema initialization: " + dae.getMessage()
                    + " — Application will still start; check /health endpoint.", dae);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Unexpected error during application initialization: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down Employee Performance Evaluation System (EPES)...");
        Object ctx = sce.getServletContext().getAttribute(AppConstants.APP_CONTEXT);
        if (ctx instanceof AppContext appContext) {
            appContext.shutdown();
        }
        LOG.info("EPES shutdown completed.");
    }

    /**
     * Inspects the ADMIN_EMAIL environment variable and seeds an initial admin if not present.
     */
    private void bootstrapAdminUser(ConnectionFactory connectionFactory) {
        String adminEmail = System.getenv("ADMIN_EMAIL");
        if (adminEmail == null || adminEmail.isBlank()) {
            LOG.warning("Environment variable ADMIN_EMAIL is not set. No initial admin user created.");
            return;
        }

        adminEmail = adminEmail.trim().toLowerCase();
        try {
            UserDao userDao = new UserDaoImpl(connectionFactory);
            Optional<User> existing = userDao.findByEmail(adminEmail);
            if (existing.isEmpty()) {
                Admin admin = new Admin();
                admin.setEmail(adminEmail);
                admin.setFullName("System Admin");
                admin.setRole(Role.ADMIN);
                admin.setActive(true);
                admin.setCreatedAt(LocalDateTime.now());

                userDao.create(admin);
                LOG.info("Bootstrap admin user created successfully with email: " + adminEmail);
            } else {
                LOG.info("Admin user already exists for email: " + adminEmail);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not bootstrap admin user: " + e.getMessage(), e);
        }
    }
}
