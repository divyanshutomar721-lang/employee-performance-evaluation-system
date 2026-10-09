package com.epes.util;

import com.epes.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Functional interface representing a unit of work to be executed
 * within a database transaction boundary.
 *
 * @param <T> return type of the transactional operation
 */
@FunctionalInterface
public interface TransactionalWork<T> {

    /**
     * Executes operations on the supplied connection within a transaction.
     *
     * @param conn the transactional database connection
     * @return result of the operation
     * @throws SQLException        if a database access error occurs
     * @throws DataAccessException if a high-level data access error occurs
     */
    T execute(Connection conn) throws SQLException, DataAccessException;
}
