package com.epes.concurrent;

import com.epes.service.GoalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GoalDeadlineScheduler}.
 */
class GoalDeadlineSchedulerTest {

    private GoalDeadlineScheduler scheduler;

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.stop();
        }
    }

    @Test
    @DisplayName("Scheduler runs periodic task and stop terminates cleanly")
    void testSchedulerLifecycle() throws Exception {
        AtomicInteger invocationCount = new AtomicInteger(0);

        // Fake GoalService implementation for unit testing without database
        GoalService fakeGoalService = new GoalService() {
            @Override
            public int markOverdueGoals(LocalDate today) {
                invocationCount.incrementAndGet();
                return 2;
            }

            @Override public com.epes.model.Goal assignGoal(com.epes.model.User a, Long e, String d, LocalDate t) { return null; }
            @Override public com.epes.model.Goal createPersonalGoal(com.epes.model.User a, String d, LocalDate t) { return null; }
            @Override public com.epes.model.Goal updateProgress(com.epes.model.User a, Long g, int p) { return null; }
            @Override public com.epes.model.Goal editGoal(com.epes.model.User a, Long g, String d, LocalDate t) { return null; }
            @Override public void deleteGoal(com.epes.model.User a, Long g) {}
            @Override public java.util.List<com.epes.model.Goal> listForEmployee(com.epes.model.User a, Long e) { return null; }
            @Override public java.util.List<com.epes.model.Goal> listAssignedByMe(com.epes.model.User a) { return null; }
        };

        // Create scheduler with 0s initial delay and 1m interval (we can trigger start & check immediate execution or stop)
        scheduler = new GoalDeadlineScheduler(fakeGoalService, 0, 1, TimeUnit.MINUTES);
        scheduler.start();

        // Wait up to 1 second for first execution to trigger
        long start = System.currentTimeMillis();
        while (invocationCount.get() == 0 && System.currentTimeMillis() - start < 1000) {
            Thread.sleep(50);
        }

        assertTrue(invocationCount.get() >= 1, "Scheduler task should have run at least once");
        assertTrue(scheduler.getTotalMarkedMissed() >= 2);
        assertNotNull(scheduler.getLastRun());

        // Test stopping the scheduler cleanly
        assertDoesNotThrow(() -> scheduler.stop());
    }
}
