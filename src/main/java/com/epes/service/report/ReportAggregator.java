package com.epes.service.report;

import com.epes.model.Evaluation;
import com.epes.model.Feedback;
import com.epes.model.Goal;
import com.epes.model.GoalStatus;
import com.epes.model.RocketStage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Pure calculation and aggregation helper methods for reporting and data analytics.
 * Independent of database state and fully unit-testable.
 */
public final class ReportAggregator {

    private ReportAggregator() {
        // Pure utility class
    }

    /**
     * Calculates the arithmetic mean overall score across a list of evaluations.
     *
     * @param evaluations evaluation list
     * @return average score rounded to 2 decimal places, or 0.00 if empty
     */
    public static double calculateAverageScore(List<Evaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Evaluation eval : evaluations) {
            if (eval != null && eval.getOverallScore() != null) {
                sum += eval.getOverallScore().doubleValue();
            }
        }
        double avg = sum / evaluations.size();
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculates the performance trajectory trend by comparing the most recent evaluation
     * with the preceding one in a chronological (oldest-to-newest) list.
     *
     * @param chronologicalEvaluations list of evaluations ordered oldest first
     * @return {@link Trend} direction
     */
    public static Trend calculateTrend(List<Evaluation> chronologicalEvaluations) {
        if (chronologicalEvaluations == null || chronologicalEvaluations.size() < 2) {
            return Trend.NOT_ENOUGH_DATA;
        }

        int n = chronologicalEvaluations.size();
        Evaluation latest = chronologicalEvaluations.get(n - 1);
        Evaluation previous = chronologicalEvaluations.get(n - 2);

        if (latest.getOverallScore() == null || previous.getOverallScore() == null) {
            return Trend.NOT_ENOUGH_DATA;
        }

        double latestScore = latest.getOverallScore().doubleValue();
        double prevScore = previous.getOverallScore().doubleValue();
        double diff = latestScore - prevScore;

        if (diff > 2.0) {
            return Trend.IMPROVING;
        } else if (diff < -2.0) {
            return Trend.DECLINING;
        } else {
            return Trend.STABLE;
        }
    }

    /**
     * Builds a distribution count map across all {@link RocketStage} values.
     * Guaranteed to contain an entry for every stage initialized to 0.
     *
     * @param stages list of stages to aggregate
     * @return populated EnumMap containing all 5 stages
     */
    public static Map<RocketStage, Long> buildStageDistribution(List<RocketStage> stages) {
        Map<RocketStage, Long> map = new EnumMap<>(RocketStage.class);
        for (RocketStage stage : RocketStage.values()) {
            map.put(stage, 0L);
        }
        if (stages != null) {
            for (RocketStage stage : stages) {
                if (stage != null) {
                    map.put(stage, map.get(stage) + 1L);
                }
            }
        }
        return map;
    }

    /**
     * Aggregates goal counts by {@link GoalStatus}.
     * Guaranteed to contain an entry for every GoalStatus initialized to 0.
     *
     * @param goals goal list
     * @return populated EnumMap containing all 4 status counts
     */
    public static Map<GoalStatus, Long> calculateGoalCounts(List<Goal> goals) {
        Map<GoalStatus, Long> map = new EnumMap<>(GoalStatus.class);
        for (GoalStatus s : GoalStatus.values()) {
            map.put(s, 0L);
        }
        if (goals != null) {
            for (Goal g : goals) {
                if (g != null && g.getStatus() != null) {
                    map.put(g.getStatus(), map.get(g.getStatus()) + 1L);
                }
            }
        }
        return map;
    }

    /**
     * Computes the percentage of completed goals (0 to 100).
     *
     * @param goals goal list
     * @return percentage integer (0-100)
     */
    public static int calculateGoalCompletionPercent(List<Goal> goals) {
        if (goals == null || goals.isEmpty()) {
            return 0;
        }
        long completed = goals.stream()
                .filter(g -> g != null && g.getStatus() == GoalStatus.COMPLETED)
                .count();
        return (int) Math.round(((double) completed / goals.size()) * 100.0);
    }

    /**
     * Computes the average feedback rating across a list of feedback records.
     *
     * @param feedbackList list of feedback entries
     * @return average rating rounded to 2 decimal places, or 0.00 if empty
     */
    public static double calculateAverageFeedbackRating(List<Feedback> feedbackList) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Feedback fb : feedbackList) {
            if (fb != null) {
                sum += fb.getRating();
            }
        }
        double avg = sum / feedbackList.size();
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
