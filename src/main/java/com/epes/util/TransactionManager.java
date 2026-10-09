package com.epes.util;

import com.epes.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages explicit JDBC transactions, providing consistent commit, rollback,
 * and connection cleanup semantics.
 */
public class TransactionManager {

    private static final Logger LOG = Logger.getLogger(TransactionManager.class.getName());

    private final ConnectionFactory connectionFactory;

    /**
     * Constructs a TransactionManager using the default {@link ConnectionFactory}.
     *
     * @throws DataAccessException if the connection factory fails to initialize
     */
    public TransactionManager() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Constructs a TransactionManager with an explicit {@link ConnectionFactory}.
     *
     * @param connectionFactory connection factory to supply connections
     */
    public TransactionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Executes the given work within a database transaction boundary.
     * Commits if the work completes successfully, or rolls back on any exception.
     *
     * @param <T>  the return type
     * @param work the transactional block to execute
     * @return result from the work block
     * @throws DataAccessException if any database or transactional error occurs
     */
    public <T> T executeInTransaction(TransactionalWork<T> work) throws DataAccessException {
        Connection conn = connectionFactory.getConnection();
        boolean initialAutoCommit = true;
        try {
            initialAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T result = work.execute(conn);

            conn.commit();
            LOG.fine("Transaction committed successfully.");
            return result;
        } catch (Exception e) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                    LOG.log(Level.WARNING, "Transaction rolled back due to exception: " + e.getMessage(), e);
                }
            } catch (SQLException rollbackEx) {
                LOG.log(Level.SEVERE, "Failed to rollback transaction", rollbackEx);
            }

            if (e instanceof DataAccessException dae) {
                throw dae;
            } else if (e instanceof SQLException sqle) {
                throw new DataAccessException("Transaction failed with SQL error: " + sqle.getMessage(), sqle);
            } else {
                throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
            }
        } finally {
            try {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(initialAutoCommit);
                    conn.close();
                }
            } catch (SQLException closeEx) {
                LOG.log(Level.WARNING, "Failed to reset autoCommit or close connection", closeEx);
            }
        }
    }
}
