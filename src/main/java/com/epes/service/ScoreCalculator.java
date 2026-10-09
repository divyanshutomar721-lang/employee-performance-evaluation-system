package com.epes.service;

import com.epes.exception.ValidationException;
import com.epes.model.EvaluationScore;
import com.epes.model.RocketStage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pure calculation utility for computing weighted performance evaluation scores
 * and determining performance tier stages.
 *
 * <p><b>Calculation Formula:</b>
 * <pre>
 * overall_score = ( sum(rating_i * weight_i) / (5.0 * sum(weight_i)) ) * 100.0
 * </pre>
 * The resulting score is rounded to 2 decimal places using {@link RoundingMode#HALF_UP}.
 *
 * <p><b>Worked Examples:</b>
 * <ul>
 *   <li>Ratings: 4 (weight 50), 3 (weight 30), 5 (weight 20)<br>
 *       {@code ((4*50 + 3*30 + 5*20) / (5 * (50+30+20))) * 100 = (390 / 500) * 100 = 78.00} &rarr; {@link RocketStage#CRUISE}</li>
 *   <li>All ratings 1: {@code ((1*100) / (5*100)) * 100 = 20.00} &rarr; {@link RocketStage#LAUNCHPAD}</li>
 *   <li>All ratings 5: {@code ((5*100) / (5*100)) * 100 = 100.00} &rarr; {@link RocketStage#SUMMIT}</li>
 * </ul>
 */
public final class ScoreCalculator {

    private ScoreCalculator() {
        // Pure utility class
    }

    /**
     * Computes the weighted overall evaluation score across all criterion ratings.
     *
     * @param scores list of individual evaluation scores
     * @return overall score between 0.00 and 100.00, rounded to 2 decimal places (HALF_UP)
     * @throws ValidationException if scores list is empty/null, any rating is outside 1..5,
     *                             or the total weight sum is zero
     */
    public static BigDecimal computeOverallScore(List<EvaluationScore> scores) throws ValidationException {
        if (scores == null || scores.isEmpty()) {
            throw new ValidationException("Evaluation must contain at least one criterion score");
        }

        long weightedSum = 0L;
        long totalWeight = 0L;

        for (int i = 0; i < scores.size(); i++) {
            EvaluationScore s = scores.get(i);
            if (s == null) {
                throw new ValidationException("Score at index " + i + " cannot be null");
            }
            if (s.getRating() < 1 || s.getRating() > 5) {
                throw new ValidationException("Rating for criterion '" + s.getCriterionName() +
                        "' must be between 1 and 5 (got: " + s.getRating() + ")");
            }
            if (s.getWeight() < 0) {
                throw new ValidationException("Weight for criterion '" + s.getCriterionName() +
                        "' cannot be negative (got: " + s.getWeight() + ")");
            }

            weightedSum += (long) s.getRating() * s.getWeight();
            totalWeight += s.getWeight();
        }

        if (totalWeight <= 0) {
            throw new ValidationException("Total evaluation criterion weight must be greater than zero");
        }

        // overall_score = (weightedSum / (5 * totalWeight)) * 100
        BigDecimal numerator = BigDecimal.valueOf(weightedSum)
                .multiply(BigDecimal.valueOf(100));
        BigDecimal denominator = BigDecimal.valueOf(5L * totalWeight);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /**
     * Determines the {@link RocketStage} for a computed overall score.
     *
     * @param score numeric score
     * @return corresponding performance stage tier
     */
    public static RocketStage stageFor(double score) {
        return RocketStage.fromScore(score);
    }

    /**
     * Determines the {@link RocketStage} for a computed {@link BigDecimal} overall score.
     *
     * @param score numeric score
     * @return corresponding performance stage tier
     */
    public static RocketStage stageFor(BigDecimal score) {
        if (score == null) {
            return RocketStage.LAUNCHPAD;
        }
        return RocketStage.fromScore(score.doubleValue());
    }
}
