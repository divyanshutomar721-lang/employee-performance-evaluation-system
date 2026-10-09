package com.epes.model;

/**
 * Status lifecycle values for an employee performance goal.
 */
public enum GoalStatus {
    /** Goal created but work has not begun. */
    NOT_STARTED,

    /** Goal actively in progress. */
    IN_PROGRESS,

    /** Goal successfully achieved. */
    COMPLETED,

    /** Goal target date passed without completion. */
    MISSED
}
