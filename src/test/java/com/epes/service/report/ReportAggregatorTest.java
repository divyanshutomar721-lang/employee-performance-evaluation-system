package com.epes.service.report;

import com.epes.model.Evaluation;
import com.epes.model.Feedback;
import com.epes.model.Goal;
import com.epes.model.GoalStatus;
import com.epes.model.RocketStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for statistical methods in {@link ReportAggregator}.
 */
class ReportAggregatorTest {

    @Test
    @DisplayName("Calculate average score with rounding")
    void testCalculateAverageScore() {
        assertEquals(0.0, ReportAggregator.calculateAverageScore(List.of()));

        Evaluation e1 = new Evaluation();
        e1.setOverallScore(new BigDecimal("80.00"));
        Evaluation e2 = new Evaluation();
        e2.setOverallScore(new BigDecimal("90.00"));

        assertEquals(85.00, ReportAggregator.calculateAverageScore(List.of(e1, e2)));
    }

    @Test
    @DisplayName("Calculate trend covers all 4 trend outcomes")
    void testCalculateTrend() {
        // Less than 2 evaluations -> NOT_ENOUGH_DATA
        assertEquals(Trend.NOT_ENOUGH_DATA, ReportAggregator.calculateTrend(List.of()));
        Evaluation single = new Evaluation();
        single.setOverallScore(new BigDecimal("75.00"));
        assertEquals(Trend.NOT_ENOUGH_DATA, ReportAggregator.calculateTrend(List.of(single)));

        // IMPROVING (> 2.0 increase)
        Evaluation prev = new Evaluation();
        prev.setOverallScore(new BigDecimal("70.00"));
        Evaluation latestImp = new Evaluation();
        latestImp.setOverallScore(new BigDecimal("75.00"));
        assertEquals(Trend.IMPROVING, ReportAggregator.calculateTrend(List.of(prev, latestImp)));

        // DECLINING (> 2.0 decrease)
        Evaluation latestDec = new Evaluation();
        latestDec.setOverallScore(new BigDecimal("65.00"));
        assertEquals(Trend.DECLINING, ReportAggregator.calculateTrend(List.of(prev, latestDec)));

        // STABLE (between -2.0 and +2.0)
        Evaluation latestStable = new Evaluation();
        latestStable.setOverallScore(new BigDecimal("71.50"));
        assertEquals(Trend.STABLE, ReportAggregator.calculateTrend(List.of(prev, latestStable)));
    }

    @Test
    @DisplayName("Stage distribution contains all 5 stages with correct initial zeroes and counts")
    void testStageDistribution() {
        List<RocketStage> stages = List.of(RocketStage.CRUISE, RocketStage.SUMMIT, RocketStage.CRUISE);
        Map<RocketStage, Long> dist = ReportAggregator.buildStageDistribution(stages);

        assertEquals(5, dist.size());
        assertEquals(0L, dist.get(RocketStage.LAUNCHPAD));
        assertEquals(0L, dist.get(RocketStage.LIFTOFF));
        assertEquals(0L, dist.get(RocketStage.ORBIT));
        assertEquals(2L, dist.get(RocketStage.CRUISE));
        assertEquals(1L, dist.get(RocketStage.SUMMIT));
    }

    @Test
    @DisplayName("Goal counts contain all 4 statuses")
    void testGoalCounts() {
        Goal g1 = new Goal();
        g1.setStatus(GoalStatus.IN_PROGRESS);
        Goal g2 = new Goal();
        g2.setStatus(GoalStatus.COMPLETED);

        Map<GoalStatus, Long> counts = ReportAggregator.calculateGoalCounts(List.of(g1, g2));
        assertEquals(4, counts.size());
        assertEquals(0L, counts.get(GoalStatus.NOT_STARTED));
        assertEquals(1L, counts.get(GoalStatus.IN_PROGRESS));
        assertEquals(1L, counts.get(GoalStatus.COMPLETED));
        assertEquals(0L, counts.get(GoalStatus.MISSED));
    }

    @Test
    @DisplayName("Calculate goal completion percentage and feedback averages")
    void testPercentagesAndRatings() {
        Goal g1 = new Goal();
        g1.setStatus(GoalStatus.COMPLETED);
        Goal g2 = new Goal();
        g2.setStatus(GoalStatus.IN_PROGRESS);

        assertEquals(50, ReportAggregator.calculateGoalCompletionPercent(List.of(g1, g2)));

        Feedback fb1 = new Feedback();
        fb1.setRating(5);
        Feedback fb2 = new Feedback();
        fb2.setRating(4);
        assertEquals(4.50, ReportAggregator.calculateAverageFeedbackRating(List.of(fb1, fb2)));
    }
}
