package com.epes.dao;

import com.epes.exception.DataAccessException;
import com.epes.model.Role;
import com.epes.model.User;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access interface for {@link User} entities.
 */
public interface UserDao {

    /**
     * Persists a new user record.
     *
     * @param user user entity to create
     * @return created user with generated ID and timestamp
     * @throws DataAccessException if a database error occurs
     */
    User create(User user) throws DataAccessException;

    /**
     * Persists a new user record using an existing connection.
     *
     * @param conn database connection
     * @param user user entity to create
     * @return created user with generated ID and timestamp
     * @throws DataAccessException if a database error occurs
     */
    User create(Connection conn, User user) throws DataAccessException;

    /**
     * Finds a user by ID.
     *
     * @param id user identifier
     * @return optional containing the user if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<User> findById(Long id) throws DataAccessException;

    /**
     * Finds a user by ID using an existing connection.
     *
     * @param conn database connection
     * @param id   user identifier
     * @return optional containing the user if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<User> findById(Connection conn, Long id) throws DataAccessException;

    /**
     * Finds a user by email (case-insensitive).
     *
     * @param email email address to search
     * @return optional containing the user if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<User> findByEmail(String email) throws DataAccessException;

    /**
     * Finds a user by email using an existing connection.
     *
     * @param conn  database connection
     * @param email email address to search
     * @return optional containing the user if found
     * @throws DataAccessException if a database error occurs
     */
    Optional<User> findByEmail(Connection conn, String email) throws DataAccessException;

    /**
     * Retrieves all users.
     *
     * @return list of all users
     * @throws DataAccessException if a database error occurs
     */
    List<User> findAll() throws DataAccessException;

    /**
     * Retrieves all users using an existing connection.
     *
     * @param conn database connection
     * @return list of all users
     * @throws DataAccessException if a database error occurs
     */
    List<User> findAll(Connection conn) throws DataAccessException;

    /**
     * Retrieves all users with a specific role.
     *
     * @param role role filter
     * @return list of matching users
     * @throws DataAccessException if a database error occurs
     */
    List<User> findByRole(Role role) throws DataAccessException;

    /**
     * Retrieves all users with a specific role using an existing connection.
     *
     * @param conn database connection
     * @param role role filter
     * @return list of matching users
     * @throws DataAccessException if a database error occurs
     */
    List<User> findByRole(Connection conn, Role role) throws DataAccessException;

    /**
     * Updates an existing user's details.
     *
     * @param user user containing updated values
     * @return updated user
     * @throws DataAccessException if a database error occurs
     */
    User update(User user) throws DataAccessException;

    /**
     * Updates an existing user's details using an existing connection.
     *
     * @param conn database connection
     * @param user user containing updated values
     * @return updated user
     * @throws DataAccessException if a database error occurs
     */
    User update(Connection conn, User user) throws DataAccessException;

    /**
     * Soft-deactivates a user by setting active = false.
     *
     * @param id user identifier
     * @throws DataAccessException if a database error occurs
     */
    void deactivate(Long id) throws DataAccessException;

    /**
     * Soft-deactivates a user using an existing connection.
     *
     * @param conn database connection
     * @param id   user identifier
     * @throws DataAccessException if a database error occurs
     */
    void deactivate(Connection conn, Long id) throws DataAccessException;

    /**
     * Counts active and inactive users for a given role.
     *
     * @param role role to count
     * @return count of users with the given role
     * @throws DataAccessException if a database error occurs
     */
    long countByRole(Role role) throws DataAccessException;

    /**
     * Counts active and inactive users for a given role using an existing connection.
     *
     * @param conn database connection
     * @param role role to count
     * @return count of users with the given role
     * @throws DataAccessException if a database error occurs
     */
    long countByRole(Connection conn, Role role) throws DataAccessException;
}
