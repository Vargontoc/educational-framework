package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ActivitySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivitySummaryPersistenceAdapterTest {

    @Mock
    private ActivitySummaryJpaRepository jpaRepository;

    private ActivitySummaryPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ActivitySummaryPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_convertsDomainToJpaEntity() {
        var summary = buildSummary();
        var captor = ArgumentCaptor.forClass(ActivitySummaryJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, ActivitySummaryJpaEntity.class);
            entity.setId(1L);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        adapter.save(summary);

        verify(jpaRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(20L, saved.getActivityId());
        assertEquals(5, saved.getTotalAttempts());
        assertEquals(3, saved.getTotalCorrect());
        assertEquals("75.00", saved.getSuccessRatePercent().toString());
        assertEquals(5000, saved.getAverageResponseTimeMs());
    }

    @Test
    void findByChildProfileIdAndActivityId_returnsDomain() {
        when(jpaRepository.findByChildProfileIdAndActivityId(10L, 20L))
            .thenReturn(Optional.of(buildJpaEntity()));

        var result = adapter.findByChildProfileIdAndActivityId(10L, 20L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getChildProfileId());
        assertEquals(20L, result.get().getActivityId());
        assertEquals(5, result.get().getTotalAttempts());
    }

    private ActivitySummary buildSummary() {
        var summary = new ActivitySummary();
        summary.setChildProfileId(10L);
        summary.setActivityId(20L);
        summary.setTotalAttempts(5);
        summary.setTotalCorrect(3);
        summary.setTotalIncorrect(2);
        summary.setTotalTimeouts(0);
        summary.setSuccessRatePercent(new BigDecimal("75.00"));
        summary.setAverageResponseTimeMs(5000);
        summary.setCurrentDifficultyLevelId(50L);
        return summary;
    }

    private ActivitySummaryJpaEntity buildJpaEntity() {
        var entity = new ActivitySummaryJpaEntity();
        entity.setId(1L);
        entity.setChildProfileId(10L);
        entity.setActivityId(20L);
        entity.setTotalAttempts(5);
        entity.setTotalCorrect(3);
        entity.setTotalIncorrect(2);
        entity.setTotalTimeouts(0);
        entity.setSuccessRatePercent(new BigDecimal("75.00"));
        entity.setAverageResponseTimeMs(5000);
        entity.setCurrentDifficultyLevelId(50L);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
