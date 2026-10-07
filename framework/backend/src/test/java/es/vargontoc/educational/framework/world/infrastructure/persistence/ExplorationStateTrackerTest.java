package es.vargontoc.educational.framework.world.infrastructure.persistence;

import es.vargontoc.educational.framework.world.ports.out.WorldExplorationStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExplorationStateTrackerTest {

    @Mock
    private WorldExplorationStateRepository worldExplorationStateRepository;

    private ExplorationStateTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new ExplorationStateTracker(worldExplorationStateRepository);
    }

    @Test
    void recordBiomeChange_tracksBiomeAsDirty() {
        tracker.recordBiomeChange(42L, "BEACH", 0.5, 0.5);

        assertEquals("BEACH", tracker.getLastPersistedBiome(42L));
    }

    @Test
    void recordPosition_tracksPositionOnly() {
        tracker.recordPosition(42L, 0.7, 0.3);

        assertNull(tracker.getLastPersistedBiome(42L));
    }

    @Test
    void flushAndRemove_biomeDirty_writesBiomeAndPosition() {
        tracker.recordBiomeChange(42L, "BEACH", 0.5, 0.5);

        tracker.flushAndRemove(42L);

        verify(worldExplorationStateRepository, times(1)).updateBiomeAndPosition(
            org.mockito.ArgumentMatchers.eq(42L),
            org.mockito.ArgumentMatchers.eq("BEACH"),
            org.mockito.ArgumentMatchers.eq(0.5),
            org.mockito.ArgumentMatchers.eq(0.5));
    }

    @Test
    void flushAndRemove_positionOnly_writesPosition() {
        tracker.recordPosition(42L, 0.7, 0.3);

        tracker.flushAndRemove(42L);

        verify(worldExplorationStateRepository, times(1)).updatePosition(
            org.mockito.ArgumentMatchers.eq(42L),
            org.mockito.ArgumentMatchers.eq(0.7),
            org.mockito.ArgumentMatchers.eq(0.3));
    }

    @Test
    void flushAndRemove_nothingTracked_noDbAccess() {
        tracker.flushAndRemove(99L);

        verify(worldExplorationStateRepository, times(0)).updatePosition(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(worldExplorationStateRepository, times(0)).updateBiomeAndPosition(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void flushAll_writesAllDirty() {
        tracker.recordBiomeChange(1L, "BEACH", 0.1, 0.2);
        tracker.recordPosition(2L, 0.3, 0.4);

        tracker.flushAll();

        verify(worldExplorationStateRepository, times(1)).updateBiomeAndPosition(
            org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("BEACH"),
            org.mockito.ArgumentMatchers.eq(0.1), org.mockito.ArgumentMatchers.eq(0.2));
        verify(worldExplorationStateRepository, times(1)).updatePosition(
            org.mockito.ArgumentMatchers.eq(2L),
            org.mockito.ArgumentMatchers.eq(0.3), org.mockito.ArgumentMatchers.eq(0.4));
    }

    @Test
    void biomeChange_persistsImmediatelyViaFlushAndRemove() {
        tracker.recordBiomeChange(42L, "WOODS", null, null);

        tracker.flushAndRemove(42L);

        verify(worldExplorationStateRepository, times(1)).updateBiomeAndPosition(
            org.mockito.ArgumentMatchers.eq(42L),
            org.mockito.ArgumentMatchers.eq("WOODS"),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    void removeProfile_clearsTracking() {
        tracker.recordBiomeChange(42L, "BEACH", 0.5, 0.5);
        tracker.removeProfile(42L);

        assertNull(tracker.getLastPersistedBiome(42L));
    }
}
