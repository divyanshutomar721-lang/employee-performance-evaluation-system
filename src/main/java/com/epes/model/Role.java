package com.epes.model;

/**
 * System roles determining user permissions and available workflows.
 */
public enum Role {
    /** Administrator with full system management permissions. */
    ADMIN,

    /** Manager responsible for evaluating direct reports, managing teams, and setting criteria. */
    MANAGER,

    /** Employee who tracks personal goals, views performance reviews, and submits feedback. */
    EMPLOYEE
}
