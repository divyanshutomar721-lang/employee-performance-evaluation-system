package com.epes.model;

/**
 * Performance tier representation ("Rocket Stage") mapped from evaluation overall scores.
 */
public enum RocketStage {
    /** Stage 1: Foundation/orientation phase (score < 40). */
    LAUNCHPAD(1, "Launchpad"),

    /** Stage 2: Early growth and ascent (score 40 to < 60). */
    LIFTOFF(2, "Liftoff"),

    /** Stage 3: Steady core performer (score 60 to < 75). */
    ORBIT(3, "Orbit"),

    /** Stage 4: High velocity mastery (score 75 to < 90). */
    CRUISE(4, "Cruise"),

    /** Stage 5: Peak exemplary execution (score >= 90). */
    SUMMIT(5, "Summit");

    private final int level;
    private final String label;

    RocketStage(int level, String label) {
        this.level = level;
        this.label = label;
    }

    /**
     * Returns the numeric progression level (1 to 5).
     *
     * @return numeric level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the user-friendly display label.
     *
     * @return display label string
     */
    public String getLabel() {
        return label;
    }

    /**
     * Maps an overall score (0.0 to 100.0) to its corresponding RocketStage.
     *
     * @param score the evaluation score
     * @return matching RocketStage tier
     */
    public static RocketStage fromScore(double score) {
        if (score < 40.0) {
            return LAUNCHPAD;
        } else if (score < 60.0) {
            return LIFTOFF;
        } else if (score < 75.0) {
            return ORBIT;
        } else if (score < 90.0) {
            return CRUISE;
        } else {
            return SUMMIT;
        }
    }
}
