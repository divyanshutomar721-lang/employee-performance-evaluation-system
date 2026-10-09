package com.epes.service;

import com.epes.exception.ValidationException;
import com.epes.model.EvaluationScore;
import com.epes.model.RocketStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ScoreCalculator}.
 */
class ScoreCalculatorTest {

    @Test
    @DisplayName("Worked Example 1: ratings 4(w50), 3(w30), 5(w20) gives 78.00 and stage CRUISE")
    void testWorkedExampleOne() {
        List<EvaluationScore> scores = List.of(
                new EvaluationScore(null, null, 1L, "Technical", 50, 4, null),
                new EvaluationScore(null, null, 2L, "Teamwork", 30, 3, null),
                new EvaluationScore(null, null, 3L, "Delivery", 20, 5, null)
        );

        BigDecimal overallScore = ScoreCalculator.computeOverallScore(scores);
        assertEquals(new BigDecimal("78.00"), overallScore);
        assertEquals(RocketStage.CRUISE, ScoreCalculator.stageFor(overallScore));
    }

    @Test
    @DisplayName("Worked Example 2: all ratings 1 gives 20.00 and stage LAUNCHPAD")
    void testAllOnes() {
        List<EvaluationScore> scores = List.of(
                new EvaluationScore(null, null, 1L, "Technical", 60, 1, null),
                new EvaluationScore(null, null, 2L, "Teamwork", 40, 1, null)
        );

        BigDecimal overallScore = ScoreCalculator.computeOverallScore(scores);
        assertEquals(new BigDecimal("20.00"), overallScore);
        assertEquals(RocketStage.LAUNCHPAD, ScoreCalculator.stageFor(overallScore));
    }

    @Test
    @DisplayName("Worked Example 3: all ratings 5 gives 100.00 and stage SUMMIT")
    void testAllFives() {
        List<EvaluationScore> scores = List.of(
                new EvaluationScore(null, null, 1L, "Technical", 100, 5, null)
        );

        BigDecimal overallScore = ScoreCalculator.computeOverallScore(scores);
        assertEquals(new BigDecimal("100.00"), overallScore);
        assertEquals(RocketStage.SUMMIT, ScoreCalculator.stageFor(overallScore));
    }

    @Test
    @DisplayName("Verify HALF_UP rounding behavior for fractional scores")
    void testRoundingHalfUp() {
        // rating 4(w33), 3(w33), 4(w34) -> (4*33 + 3*33 + 4*34) = (132 + 99 + 136) = 367 / (5*100) * 100 = 73.40
        List<EvaluationScore> scores1 = List.of(
                new EvaluationScore(null, null, 1L, "A", 33, 4, null),
                new EvaluationScore(null, null, 2L, "B", 33, 3, null),
                new EvaluationScore(null, null, 3L, "C", 34, 4, null)
        );
        assertEquals(new BigDecimal("73.40"), ScoreCalculator.computeOverallScore(scores1));

        // rating 4(w1), 3(w2) -> (4 + 6) / 15 * 100 = 1000/15 = 66.6666... -> 66.67
        List<EvaluationScore> scores2 = List.of(
                new EvaluationScore(null, null, 1L, "A", 1, 4, null),
                new EvaluationScore(null, null, 2L, "B", 2, 3, null)
        );
        assertEquals(new BigDecimal("66.67"), ScoreCalculator.computeOverallScore(scores2));
    }

    @Test
    @DisplayName("Empty or null list throws ValidationException")
    void testEmptyOrNullList() {
        assertThrows(ValidationException.class, () -> ScoreCalculator.computeOverallScore(null));
        assertThrows(ValidationException.class, () -> ScoreCalculator.computeOverallScore(Collections.emptyList()));
    }

    @Test
    @DisplayName("Rating outside 1..5 throws ValidationException")
    void testInvalidRating() {
        List<EvaluationScore> zeroRating = List.of(
                new EvaluationScore(null, null, 1L, "A", 50, 0, null)
        );
        assertThrows(ValidationException.class, () -> ScoreCalculator.computeOverallScore(zeroRating));

        List<EvaluationScore> sixRating = List.of(
                new EvaluationScore(null, null, 1L, "A", 50, 6, null)
        );
        assertThrows(ValidationException.class, () -> ScoreCalculator.computeOverallScore(sixRating));
    }

    @Test
    @DisplayName("Zero total weight throws ValidationException")
    void testZeroTotalWeight() {
        List<EvaluationScore> zeroWeights = List.of(
                new EvaluationScore(null, null, 1L, "A", 0, 4, null)
        );
        assertThrows(ValidationException.class, () -> ScoreCalculator.computeOverallScore(zeroWeights));
    }
}
