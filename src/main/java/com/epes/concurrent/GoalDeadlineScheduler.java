package com.epes.concurrent;

import com.epes.service.GoalService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background scheduler daemon for evaluating and updating overdue employee goals.
 *
 * <p><b>Thread Safety Architectural Decisions:</b>
 * <ul>
 *   <li>{@link AtomicLong}: Provides lock-free, atomic increments for {@code totalMarkedMissed}
 *       across scheduler executions without blocking callers reading the statistic.</li>
 *   <li>{@code volatile Instant lastRun}: Ensures memory visibility across CPU caches so that
 *       threads invoking {@link #getLastRun()} immediately observe the latest timestamp without
 *       requiring explicit synchronized blocks.</li>
 *   <li>{@link ScheduledExecutorService}: Employs a dedicated daemon thread named
 *       {@code "epes-goal-deadline"} to ensure application shutdown is never blocked by scheduler threads.</li>
 * </ul>
 */
public class GoalDeadlineScheduler {

    private static final Logger LOG = Logger.getLogger(GoalDeadlineScheduler.class.getName());

    private final GoalService goalService;
    private final ScheduledExecutorService scheduler;
    private final long initialDelaySeconds;
    private final long intervalMinutes;

    private final AtomicLong totalMarkedMissed = new AtomicLong(0);
    private volatile Instant lastRun;

    /**
     * Constructs the scheduler reading {@code GOAL_CHECK_INTERVAL_MINUTES} from environment (default: 60).
     *
     * @param goalService goal service to perform overdue checks
     */
    public GoalDeadlineScheduler(GoalService goalService) {
        this(goalService, 10, parseIntervalMinutes(60), TimeUnit.MINUTES);
    }

    /**
     * Testable constructor allowing configurable delay, interval, and time unit.
     *
     * @param goalService         goal service
     * @param initialDelaySeconds initial startup delay in seconds
     * @param interval            execution interval
     * @param unit                time unit for interval
     */
    public GoalDeadlineScheduler(GoalService goalService, long initialDelaySeconds, long interval, TimeUnit unit) {
        this.goalService = goalService;
        this.initialDelaySeconds = initialDelaySeconds;
        this.intervalMinutes = unit.toMinutes(interval);

        ThreadFactory daemonFactory = r -> {
            Thread t = new Thread(r, "epes-goal-deadline");
            t.setDaemon(true);
            return t;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(daemonFactory);
    }

    /**
     * Starts the periodic overdue goal scanning schedule.
     */
    public synchronized void start() {
        LOG.info("Starting GoalDeadlineScheduler (initial delay: " + initialDelaySeconds
                + "s, interval: " + intervalMinutes + "m)...");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                lastRun = Instant.now();
                int marked = goalService.markOverdueGoals(LocalDate.now());
                if (marked > 0) {
                    long total = totalMarkedMissed.addAndGet(marked);
                    LOG.info("GoalDeadlineScheduler: Marked " + marked + " overdue goals as MISSED. (Total: " + total + ")");
                } else {
                    LOG.fine("GoalDeadlineScheduler: No overdue goals detected.");
                }
            } catch (Throwable t) {
                // Catch Throwable so runtime exceptions or errors never suppress future scheduled executions
                LOG.log(Level.SEVERE, "Unexpected error in GoalDeadlineScheduler execution: " + t.getMessage(), t);
            }
        }, initialDelaySeconds, Math.max(1, intervalMinutes), TimeUnit.MINUTES);
    }

    /**
     * Stops the scheduler and waits for any active task to finish.
     */
    public synchronized void stop() {
        LOG.info("Stopping GoalDeadlineScheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOG.info("GoalDeadlineScheduler stopped cleanly.");
    }

    public long getTotalMarkedMissed() {
        return totalMarkedMissed.get();
    }

    public Instant getLastRun() {
        return lastRun;
    }

    private static long parseIntervalMinutes(long defaultVal) {
        String env = System.getenv("GOAL_CHECK_INTERVAL_MINUTES");
        if (env == null || env.isBlank()) {
            return defaultVal;
        }
        try {
            long parsed = Long.parseLong(env.trim());
            return Math.max(1, parsed);
        } catch (NumberFormatException e) {
            LOG.warning("Invalid GOAL_CHECK_INTERVAL_MINUTES value '" + env + "', using default: " + defaultVal);
            return defaultVal;
        }
    }
}
