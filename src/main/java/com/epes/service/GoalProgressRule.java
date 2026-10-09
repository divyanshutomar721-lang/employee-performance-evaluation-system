package com.epes.service;

import com.epes.model.GoalStatus;

/**
 * Pure rule engine mapping numeric progress percentage (0-100) to lifecycle {@link GoalStatus}.
 */
public final class GoalProgressRule {

    private GoalProgressRule() {
        // Prevent instantiation
    }

    /**
     * Determines the appropriate {@link GoalStatus} for a given progress percentage.
     *
     * <ul>
     *   <li>{@code 0} &rarr; {@link GoalStatus#NOT_STARTED}</li>
     *   <li>{@code 1 .. 99} &rarr; {@link GoalStatus#IN_PROGRESS}</li>
     *   <li>{@code 100} &rarr; {@link GoalStatus#COMPLETED}</li>
     * </ul>
     *
     * @param progressPercent progress percentage (0-100)
     * @return updated goal lifecycle status
     */
    public static GoalStatus determineStatus(int progressPercent) {
        if (progressPercent <= 0) {
            return GoalStatus.NOT_STARTED;
        } else if (progressPercent >= 100) {
            return GoalStatus.COMPLETED;
        } else {
            return GoalStatus.IN_PROGRESS;
        }
    }
}
