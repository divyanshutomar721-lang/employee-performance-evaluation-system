package com.epes.concurrent;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing asynchronous background report generation jobs.
 *
 * <p><b>Thread Safety Architectural Decisions:</b>
 * <ul>
 *   <li>{@link ConcurrentHashMap}: Provides non-blocking, lock-striped concurrent read and write
 *       operations for tracking job status and results across multiple worker and client threads.</li>
 *   <li>{@link ExecutorService} with daemon threads: Fixed pool of 2 worker threads ({@code "epes-report-N"})
 *       prevents runaway thread allocation during heavy report generation and automatically allows clean JVM teardown.</li>
 *   <li>LRU eviction cap: Prevents memory leaks by maintaining a maximum of 100 job metadata entries.</li>
 * </ul>
 */
public class ReportJobService {

    private static final Logger LOG = Logger.getLogger(ReportJobService.class.getName());
    private static final int MAX_HISTORY = 100;

    private final ExecutorService executor;
    private final Map<String, JobHolder> jobs = new ConcurrentHashMap<>();

    /**
     * Default constructor creating a fixed thread pool of 2 daemon threads.
     */
    public ReportJobService() {
        AtomicInteger threadCounter = new AtomicInteger(1);
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "epes-report-" + threadCounter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
        this.executor = Executors.newFixedThreadPool(2, factory);
    }

    /**
     * Submits a callable task for asynchronous execution.
     *
     * @param task task producing a result
     * @return generated unique job ID
     */
    public String submit(Callable<?> task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        pruneOldJobsIfFull();

        String jobId = UUID.randomUUID().toString();
        JobHolder holder = new JobHolder(jobId);
        jobs.put(jobId, holder);

        executor.submit(() -> {
            holder.status = JobStatus.RUNNING;
            try {
                holder.result = task.call();
                holder.status = JobStatus.DONE;
            } catch (Throwable t) {
                holder.status = JobStatus.FAILED;
                holder.errorMessage = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
                LOG.log(Level.WARNING, "Report job " + jobId + " failed: " + holder.errorMessage, t);
            } finally {
                holder.completedAt = Instant.now();
            }
        });

        return jobId;
    }

    /**
     * Returns the current lifecycle status of a job.
     *
     * @param jobId job identifier
     * @return current {@link JobStatus}, or {@code null} if job ID not found
     */
    public JobStatus getStatus(String jobId) {
        JobHolder h = jobs.get(jobId);
        return h != null ? h.status : null;
    }

    /**
     * Returns the result of a completed job, or {@code null} if not yet complete or failed.
     *
     * @param jobId job identifier
     * @return result object or null
     */
    public Object getResult(String jobId) {
        JobHolder h = jobs.get(jobId);
        return h != null ? h.result : null;
    }

    /**
     * Returns the error message of a failed job, or {@code null} if not failed.
     *
     * @param jobId job identifier
     * @return error description or null
     */
    public String getError(String jobId) {
        JobHolder h = jobs.get(jobId);
        return h != null ? h.errorMessage : null;
    }

    /**
     * Shuts down the executor pool and awaits termination up to 5 seconds.
     */
    public synchronized void shutdown() {
        LOG.info("Shutting down ReportJobService thread pool...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOG.info("ReportJobService shut down cleanly.");
    }

    private void pruneOldJobsIfFull() {
        if (jobs.size() >= MAX_HISTORY) {
            jobs.values().stream()
                    .filter(j -> j.status == JobStatus.DONE || j.status == JobStatus.FAILED)
                    .min(Comparator.comparing(j -> j.createdAt))
                    .ifPresent(oldest -> jobs.remove(oldest.jobId));
        }
    }

    private static class JobHolder {
        private final String jobId;
        private final Instant createdAt = Instant.now();
        private volatile Instant completedAt;
        private volatile JobStatus status = JobStatus.PENDING;
        private volatile Object result;
        private volatile String errorMessage;

        JobHolder(String jobId) {
            this.jobId = jobId;
        }
    }
}
