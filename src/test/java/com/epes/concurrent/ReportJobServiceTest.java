package com.epes.concurrent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReportJobService}.
 */
class ReportJobServiceTest {

    private ReportJobService reportJobService;

    @BeforeEach
    void setUp() {
        reportJobService = new ReportJobService();
    }

    @AfterEach
    void tearDown() {
        if (reportJobService != null) {
            reportJobService.shutdown();
        }
    }

    @Test
    @DisplayName("Submit successful job and poll until DONE")
    void testSuccessfulJobExecution() throws Exception {
        String jobId = reportJobService.submit(() -> "Sample Report Content");
        assertNotNull(jobId);

        // Poll until DONE or timeout (5 seconds)
        long start = System.currentTimeMillis();
        while (reportJobService.getStatus(jobId) != JobStatus.DONE && System.currentTimeMillis() - start < 5000) {
            Thread.sleep(50);
        }

        assertEquals(JobStatus.DONE, reportJobService.getStatus(jobId));
        assertEquals("Sample Report Content", reportJobService.getResult(jobId));
        assertNull(reportJobService.getError(jobId));
    }

    @Test
    @DisplayName("Submit failing job and verify FAILED status with error message")
    void testFailingJobExecution() throws Exception {
        String jobId = reportJobService.submit(() -> {
            throw new IllegalStateException("Simulated report failure");
        });
        assertNotNull(jobId);

        long start = System.currentTimeMillis();
        while (reportJobService.getStatus(jobId) != JobStatus.FAILED && System.currentTimeMillis() - start < 5000) {
            Thread.sleep(50);
        }

        assertEquals(JobStatus.FAILED, reportJobService.getStatus(jobId));
        assertNull(reportJobService.getResult(jobId));
        assertTrue(reportJobService.getError(jobId).contains("Simulated report failure"));
    }

    @Test
    @DisplayName("Shutdown completes cleanly")
    void testShutdown() {
        assertDoesNotThrow(() -> reportJobService.shutdown());
    }
}
