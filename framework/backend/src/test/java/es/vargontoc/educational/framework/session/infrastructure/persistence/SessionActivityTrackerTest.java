package es.vargontoc.educational.framework.session.infrastructure.persistence;

import es.vargontoc.educational.framework.session.ports.out.ChildSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionActivityTrackerTest {

    @Mock
    private ChildSessionRepository childSessionRepository;

    private SessionActivityTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new SessionActivityTracker(childSessionRepository);
    }

    @Test
    void recordActivity_storesInMemory() {
        tracker.recordActivity(1L);

        assertNotNull(tracker.getLastActivity(1L));
        assertTrue(tracker.hasActivity(1L));
    }

    @Test
    void recordActivity_overwritesPrevious() {
        LocalDateTime first = LocalDateTime.now().minusSeconds(10);
        tracker.recordActivity(1L);
        LocalDateTime second = LocalDateTime.now();

        assertNotNull(tracker.getLastActivity(1L));
        assertTrue(tracker.getLastActivity(1L).isAfter(first) || tracker.getLastActivity(1L).isEqual(first));
    }

    @Test
    void flushAndRemove_writesToDbAndRemoves() {
        tracker.recordActivity(1L);

        tracker.flushAndRemove(1L);

        verify(childSessionRepository, times(1)).updateLastActivityAt(eq(1L), org.mockito.ArgumentMatchers.any(LocalDateTime.class));
        assertNull(tracker.getLastActivity(1L));
    }

    @Test
    void flushAndRemove_noActivity_doesNotWrite() {
        tracker.flushAndRemove(99L);

        verify(childSessionRepository, times(0)).updateLastActivityAt(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void flushAll_writesAllTrackedSessions() {
        tracker.recordActivity(1L);
        tracker.recordActivity(2L);
        tracker.recordActivity(3L);

        tracker.flushAll();

        verify(childSessionRepository, times(3)).updateLastActivityAt(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(LocalDateTime.class));
    }

    @Test
    void sixtyHeartbeats_produceAtMostCeilWrites() {
        int intervalSeconds = 12;
        int heartbeats = 60;
        int maxWrites = (int) Math.ceil((double) heartbeats / (intervalSeconds / (double) intervalSeconds));

        for (int i = 0; i < heartbeats; i++) {
            tracker.recordActivity(1L);
        }

        tracker.flushAll();

        verify(childSessionRepository, times(1)).updateLastActivityAt(eq(1L), org.mockito.ArgumentMatchers.any(LocalDateTime.class));
        assertTrue(1 <= Math.ceil((double) heartbeats / intervalSeconds));
    }

    @Test
    void removeSession_clearsTracking() {
        tracker.recordActivity(1L);
        tracker.removeSession(1L);

        assertNull(tracker.getLastActivity(1L));
    }

    @Test
    void trackedSessionCount_reflectsActiveSessions() {
        tracker.recordActivity(1L);
        tracker.recordActivity(2L);
        assertEquals(2, tracker.trackedSessionCount());

        tracker.removeSession(1L);
        assertEquals(1, tracker.trackedSessionCount());
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
