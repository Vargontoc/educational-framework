package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.CuriosityViewed;
import es.vargontoc.educational.framework.tracking.ports.out.CuriosityViewedRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuriosityViewedServiceTest {

    @Mock
    private CuriosityViewedRepository repository;

    @InjectMocks
    private CuriosityViewedService service;

    @Test
    void registerView_createsRecord() {
        when(repository.findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(1L, 2L, 3L, 0))
                .thenReturn(Optional.empty());
        when(repository.save(any(CuriosityViewed.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registerView(1L, 2L, 3L, 0);

        var captor = ArgumentCaptor.forClass(CuriosityViewed.class);
        verify(repository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(1L, saved.getChildProfileId());
        assertEquals(2L, saved.getTopicId());
        assertEquals(3L, saved.getCuriosityId());
        assertEquals(0, saved.getCycleNumber());
        assertNotNull(saved.getViewedAt());
    }

    @Test
    void registerView_duplicateInSameCycle_ignored() {
        var existing = new CuriosityViewed();
        existing.setChildProfileId(1L);
        existing.setTopicId(2L);
        existing.setCuriosityId(3L);
        existing.setCycleNumber(0);
        when(repository.findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(1L, 2L, 3L, 0))
                .thenReturn(Optional.of(existing));

        service.registerView(1L, 2L, 3L, 0);

        verify(repository, never()).save(any());
    }

    @Test
    void registerView_afterReset_usesNewCycle() {
        when(repository.findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(1L, 2L, 3L, 1))
                .thenReturn(Optional.empty());
        when(repository.save(any(CuriosityViewed.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registerView(1L, 2L, 3L, 1);

        var captor = ArgumentCaptor.forClass(CuriosityViewed.class);
        verify(repository).save(captor.capture());
        assertEquals(1, captor.getValue().getCycleNumber());
    }

    @Test
    void getViewedCuriosities_filteredByChildAndTopic() {
        var viewed = new CuriosityViewed();
        viewed.setChildProfileId(1L);
        viewed.setTopicId(2L);
        viewed.setCuriosityId(3L);
        viewed.setCycleNumber(0);
        viewed.setViewedAt(LocalDateTime.now());

        when(repository.findByChildProfileIdAndTopicId(1L, 2L))
                .thenReturn(List.of(viewed));

        List<CuriosityViewed> result = service.getViewedCuriosities(1L, 2L);

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getCuriosityId());
    }

    @Test
    void resetCycle_returnsNextNumber() {
        when(repository.findMaxCycleNumber(1L, 2L)).thenReturn(Optional.of(0));

        int nextCycle = service.resetCycle(1L, 2L);

        assertEquals(1, nextCycle);
    }

    @Test
    void resetCycle_empty_returnsZero() {
        when(repository.findMaxCycleNumber(1L, 2L)).thenReturn(Optional.empty());

        int nextCycle = service.resetCycle(1L, 2L);

        assertEquals(0, nextCycle);
    }

    @Test
    void viewedAt_preservedIndependently() {
        var fixedTime = LocalDateTime.of(2026, 6, 17, 10, 30, 0);
        when(repository.findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(1L, 2L, 3L, 0))
                .thenReturn(Optional.empty());
        when(repository.save(any(CuriosityViewed.class))).thenAnswer(inv -> {
            var cv = inv.getArgument(0, CuriosityViewed.class);
            cv.setId(1L);
            cv.setCreatedAt(fixedTime);
            cv.setUpdatedAt(fixedTime);
            return cv;
        });

        service.registerView(1L, 2L, 3L, 0);

        var captor = ArgumentCaptor.forClass(CuriosityViewed.class);
        verify(repository).save(captor.capture());
        var saved = captor.getValue();
        assertNotNull(saved.getViewedAt());
        assertTrue(saved.getViewedAt().getYear() >= 2026);
    }

    @Test
    void getViewedCuriosities_withCycleNumber() {
        var viewed = new CuriosityViewed();
        viewed.setChildProfileId(1L);
        viewed.setTopicId(2L);
        viewed.setCuriosityId(3L);
        viewed.setCycleNumber(1);

        when(repository.findByChildProfileIdAndTopicIdAndCycleNumber(1L, 2L, 1))
                .thenReturn(List.of(viewed));

        List<CuriosityViewed> result = service.getViewedCuriosities(1L, 2L, 1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCycleNumber());
    }
}
