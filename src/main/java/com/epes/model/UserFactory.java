package com.epes.model;

import java.time.LocalDateTime;

/**
 * Factory class for instantiating polymorphic {@link User} instances
 * based on role.
 */
public final class UserFactory {

    private UserFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a new User instance corresponding to the given Role.
     *
     * @param role role of the user
     * @return concrete User subclass (Admin, Manager, or Employee)
     */
    public static User create(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return switch (role) {
            case ADMIN -> new Admin();
            case MANAGER -> new Manager();
            case EMPLOYEE -> new Employee();
        };
    }

    /**
     * Creates and populates a new User instance.
     *
     * @param id        user identifier
     * @param email     user email
     * @param fullName  user display name
     * @param role      user role
     * @param active    account active status
     * @param createdAt registration timestamp
     * @return concrete User subclass instance
     */
    public static User create(Long id, String email, String fullName, Role role, boolean active, LocalDateTime createdAt) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return switch (role) {
            case ADMIN -> new Admin(id, email, fullName, active, createdAt);
            case MANAGER -> new Manager(id, email, fullName, active, createdAt);
            case EMPLOYEE -> new Employee(id, email, fullName, active, createdAt);
        };
    }
}
