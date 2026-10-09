package com.epes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link RocketStage} evaluation mapping and band boundary checks.
 */
class RocketStageTest {

    @ParameterizedTest(name = "Score {0} maps to {1}")
    @CsvSource({
            "0.0, LAUNCHPAD",
            "15.5, LAUNCHPAD",
            "39.99, LAUNCHPAD",
            "40.0, LIFTOFF",
            "45.0, LIFTOFF",
            "59.99, LIFTOFF",
            "60.0, ORBIT",
            "68.5, ORBIT",
            "74.99, ORBIT",
            "75.0, CRUISE",
            "82.3, CRUISE",
            "89.99, CRUISE",
            "90.0, SUMMIT",
            "95.5, SUMMIT",
            "100.0, SUMMIT"
    })
    @DisplayName("Verify exact band boundaries for RocketStage.fromScore")
    void testFromScoreBands(double score, RocketStage expectedStage) {
        assertEquals(expectedStage, RocketStage.fromScore(score));
    }

    @Test
    @DisplayName("Verify numeric levels and labels")
    void testLevelsAndLabels() {
        assertEquals(1, RocketStage.LAUNCHPAD.getLevel());
        assertEquals("Launchpad", RocketStage.LAUNCHPAD.getLabel());

        assertEquals(2, RocketStage.LIFTOFF.getLevel());
        assertEquals("Liftoff", RocketStage.LIFTOFF.getLabel());

        assertEquals(3, RocketStage.ORBIT.getLevel());
        assertEquals("Orbit", RocketStage.ORBIT.getLabel());

        assertEquals(4, RocketStage.CRUISE.getLevel());
        assertEquals("Cruise", RocketStage.CRUISE.getLabel());

        assertEquals(5, RocketStage.SUMMIT.getLevel());
        assertEquals("Summit", RocketStage.SUMMIT.getLabel());
    }
}
