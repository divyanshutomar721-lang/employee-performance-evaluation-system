package com.epes.util;

import com.epes.exception.DataAccessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility responsible for executing database schema DDL scripts from classpath.
 */
public class SchemaInitializer {

    private static final Logger LOG = Logger.getLogger(SchemaInitializer.class.getName());
    private static final String SCHEMA_FILE = "/db/schema.sql";

    private final ConnectionFactory connectionFactory;

    /**
     * Constructs a SchemaInitializer using the provided ConnectionFactory.
     *
     * @param connectionFactory connection factory to obtain connections
     */
    public SchemaInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Default constructor using a standard ConnectionFactory.
     *
     * @throws DataAccessException if connection factory fails initialization
     */
    public SchemaInitializer() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Reads and executes the schema.sql script inside a single transaction.
     *
     * @throws DataAccessException if reading or executing the SQL script fails
     */
    public void initialize() throws DataAccessException {
        String sql = loadSchemaSql();
        try (Connection conn = connectionFactory.getConnection()) {
            boolean initialAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                conn.commit();
                LOG.info("Database schema initialized successfully from " + SCHEMA_FILE);
            } catch (SQLException e) {
                conn.rollback();
                LOG.log(Level.SEVERE, "Failed executing schema script, rolled back.", e);
                throw new DataAccessException("Failed executing schema initialization script: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(initialAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection error during schema initialization: " + e.getMessage(), e);
        }
    }

    /**
     * Loads the SQL script content from the classpath.
     */
    private String loadSchemaSql() throws DataAccessException {
        InputStream is = getClass().getResourceAsStream(SCHEMA_FILE);
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db/schema.sql");
        }
        if (is == null) {
            throw new DataAccessException("Schema script file not found on classpath: " + SCHEMA_FILE);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new DataAccessException("Error reading schema script from classpath: " + SCHEMA_FILE, e);
        }
    }
}
