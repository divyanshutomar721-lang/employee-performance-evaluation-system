package com.epes.service.report;

import com.epes.model.Evaluation;
import com.epes.model.GoalStatus;
import com.epes.model.RocketStage;
import com.epes.model.User;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Detailed performance and progression report for a single employee.
 */
public class EmployeeReport {

    private final User employee;
    private final List<Evaluation> evaluations;
    private final List<String> chartLabels;
    private final List<Double> chartScores;
    private final double averageScore;
    private final double latestScore;
    private final RocketStage latestStage;
    private final Trend trend;
    private final Map<GoalStatus, Long> goalCounts;
    private final double averageFeedbackRating;
    private final int feedbackCount;

    /**
     * Constructs an EmployeeReport.
     */
    public EmployeeReport(User employee, List<Evaluation> evaluations, List<String> chartLabels,
                          List<Double> chartScores, double averageScore, double latestScore,
                          RocketStage latestStage, Trend trend, Map<GoalStatus, Long> goalCounts,
                          double averageFeedbackRating, int feedbackCount) {
        this.employee = employee;
        this.evaluations = evaluations != null ? Collections.unmodifiableList(evaluations) : Collections.emptyList();
        this.chartLabels = chartLabels != null ? Collections.unmodifiableList(chartLabels) : Collections.emptyList();
        this.chartScores = chartScores != null ? Collections.unmodifiableList(chartScores) : Collections.emptyList();
        this.averageScore = averageScore;
        this.latestScore = latestScore;
        this.latestStage = latestStage;
        this.trend = trend;
        this.goalCounts = goalCounts != null ? Collections.unmodifiableMap(new EnumMap<>(goalCounts)) : Collections.emptyMap();
        this.averageFeedbackRating = averageFeedbackRating;
        this.feedbackCount = feedbackCount;
    }

    public User getEmployee() {
        return employee;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public List<String> getChartLabels() {
        return chartLabels;
    }

    public List<Double> getChartScores() {
        return chartScores;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public double getLatestScore() {
        return latestScore;
    }

    public RocketStage getLatestStage() {
        return latestStage;
    }

    public Trend getTrend() {
        return trend;
    }

    public Map<GoalStatus, Long> getGoalCounts() {
        return goalCounts;
    }

    public double getAverageFeedbackRating() {
        return averageFeedbackRating;
    }

    public int getFeedbackCount() {
        return feedbackCount;
    }
}
