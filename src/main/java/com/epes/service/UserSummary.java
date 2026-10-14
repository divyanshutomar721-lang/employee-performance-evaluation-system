package com.epes.service;

import com.epes.model.Role;
import com.epes.model.User;

import java.util.Objects;

/**
 * Lightweight, record-free summary projection of a {@link User}.
 *
 * <p>Contains only display and identity fields required for presentation
 * (e.g., rendering user names and roles in views) without exposing internal state.</p>
 */
public class UserSummary {

    private final Long id;
    private final String fullName;
    private final String email;
    private final Role role;
    private final boolean active;

    /**
     * Constructs a new UserSummary.
     *
     * @param id       user identifier
     * @param fullName full display name
     * @param email    email address
     * @param role     assigned role
     * @param active   account active flag
     */
    public UserSummary(Long id, String fullName, String email, Role role, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    /**
     * Factory method creating a summary from a domain {@link User}.
     *
     * @param user source domain user
     * @return summary projection or null if user is null
     */
    public static UserSummary from(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.isActive());
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSummary that = (UserSummary) o;
        return active == that.active &&
                Objects.equals(id, that.id) &&
                Objects.equals(fullName, that.fullName) &&
                Objects.equals(email, that.email) &&
                role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, email, role, active);
    }

    @Override
    public String toString() {
        return "UserSummary{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}
