package com.epes.concurrent;

/**
 * Execution lifecycle states for an asynchronous background job.
 */
public enum JobStatus {
    /** Job is queued in thread pool awaiting execution. */
    PENDING,

    /** Job is currently executing on a worker thread. */
    RUNNING,

    /** Job has completed successfully. */
    DONE,

    /** Job threw an exception and failed. */
    FAILED
}
