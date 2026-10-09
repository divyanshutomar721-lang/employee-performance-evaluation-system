package com.epes.service.report;

/**
 * Trend direction comparing the most recent performance score with the preceding one.
 */
public enum Trend {
    /** Latest score is more than 2.00 points higher than the previous evaluation. */
    IMPROVING,

    /** Score difference is between -2.00 and +2.00 points inclusive. */
    STABLE,

    /** Latest score is more than 2.00 points lower than the previous evaluation. */
    DECLINING,

    /** Fewer than 2 evaluations available to establish a trend. */
    NOT_ENOUGH_DATA
}
