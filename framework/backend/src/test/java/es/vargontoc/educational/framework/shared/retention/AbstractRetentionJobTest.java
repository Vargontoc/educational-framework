package es.vargontoc.educational.framework.shared.retention;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractRetentionJobTest {

    @Test
    void execute_callsAllPolicies() {
        var callCount = new AtomicInteger(0);
        var policy1 = new TestPolicy("policy-1", 30, 5, callCount);
        var policy2 = new TestPolicy("policy-2", 7, 3, callCount);
        var job = new TestJob(List.of(policy1, policy2));

        job.execute();

        assertEquals(2, callCount.get());
    }

    @Test
    void execute_computesCutoffBasedOnRetentionDays() {
        var capturedCutoffs = new ArrayList<LocalDateTime>();
        var policy = new RetentionPolicy() {
            @Override
            public String name() { return "test"; }

            @Override
            public int retentionDays() { return 10; }

            @Override
            public int deleteExpired(LocalDateTime cutoff) {
                capturedCutoffs.add(cutoff);
                return 0;
            }
        };
        var job = new TestJob(List.of(policy));

        var before = LocalDateTime.now().minusDays(10);
        job.execute();
        var after = LocalDateTime.now().minusDays(10);

        assertEquals(1, capturedCutoffs.size());
        assertTrue(!capturedCutoffs.get(0).isBefore(before));
        assertTrue(!capturedCutoffs.get(0).isAfter(after));
    }

    @Test
    void execute_returnsDeletedCountFromPolicies() {
        var policy = new TestPolicy("test", 30, 42, new AtomicInteger());
        var job = new TestJob(List.of(policy));

        job.execute();

        assertEquals(42, policy.lastDeletedCount);
    }

    @Test
    void execute_withNoPolicies_doesNotThrow() {
        var job = new TestJob(List.of());

        job.execute();
    }

    private static class TestJob extends AbstractRetentionJob {

        private final List<RetentionPolicy> policies;

        TestJob(List<RetentionPolicy> policies) {
            this.policies = policies;
        }

        @Override
        protected List<RetentionPolicy> policies() {
            return policies;
        }

        @Override
        protected String jobName() {
            return "test-job";
        }
    }

    private static class TestPolicy implements RetentionPolicy {

        private final String name;
        private final int retentionDays;
        private final int deletedCount;
        private final AtomicInteger callCount;
        int lastDeletedCount;

        TestPolicy(String name, int retentionDays, int deletedCount, AtomicInteger callCount) {
            this.name = name;
            this.retentionDays = retentionDays;
            this.deletedCount = deletedCount;
            this.callCount = callCount;
        }

        @Override
        public String name() { return name; }

        @Override
        public int retentionDays() { return retentionDays; }

        @Override
        public int deleteExpired(LocalDateTime cutoff) {
            callCount.incrementAndGet();
            lastDeletedCount = deletedCount;
            return deletedCount;
        }
    }
}
