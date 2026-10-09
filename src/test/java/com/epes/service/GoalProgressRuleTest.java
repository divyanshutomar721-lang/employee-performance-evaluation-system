package com.epes.service;

import com.epes.model.GoalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link GoalProgressRule}.
 */
class GoalProgressRuleTest {

    @ParameterizedTest(name = "Progress {0}% maps to {1}")
    @CsvSource({
            "0, NOT_STARTED",
            "-5, NOT_STARTED",
            "1, IN_PROGRESS",
            "50, IN_PROGRESS",
            "99, IN_PROGRESS",
            "100, COMPLETED",
            "105, COMPLETED"
    })
    @DisplayName("Verify progress percentage mapping to GoalStatus")
    void testDetermineStatus(int progress, GoalStatus expectedStatus) {
        assertEquals(expectedStatus, GoalProgressRule.determineStatus(progress));
    }
}
