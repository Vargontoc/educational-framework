package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;
import es.vargontoc.educational.framework.tracking.model.TopicSummary;
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
class TopicSummaryPersistenceAdapterTest {

    @Mock
    private TopicSummaryJpaRepository jpaRepository;

    private TopicSummaryPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TopicSummaryPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_convertsDomainToJpaEntity() {
        var summary = buildSummary();
        var captor = ArgumentCaptor.forClass(TopicSummaryJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, TopicSummaryJpaEntity.class);
            entity.setId(1L);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        adapter.save(summary);

        verify(jpaRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(40L, saved.getTopicId());
        assertEquals(5, saved.getTotalAttempts());
        assertEquals(3, saved.getTotalCorrect());
        assertEquals(2, saved.getTotalIncorrect());
        assertEquals("40.00", saved.getFailureRatePercent().toString());
        assertEquals("MEDIUM", saved.getPerformanceBand());
    }

    @Test
    void findByChildProfileIdAndTopicId_returnsDomain() {
        when(jpaRepository.findByChildProfileIdAndTopicId(10L, 40L))
            .thenReturn(Optional.of(buildJpaEntity()));

        var result = adapter.findByChildProfileIdAndTopicId(10L, 40L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getChildProfileId());
        assertEquals(40L, result.get().getTopicId());
        assertEquals(TopicPerformanceBand.MEDIUM, result.get().getPerformanceBand());
    }

    private TopicSummary buildSummary() {
        var summary = new TopicSummary();
        summary.setChildProfileId(10L);
        summary.setTopicId(40L);
        summary.setTotalAttempts(5);
        summary.setTotalCorrect(3);
        summary.setTotalIncorrect(2);
        summary.setTotalTimeouts(0);
        summary.setSuccessRatePercent(new BigDecimal("60.00"));
        summary.setFailureRatePercent(new BigDecimal("40.00"));
        summary.setAverageResponseTimeMs(5000);
        summary.setPerformanceBand(TopicPerformanceBand.MEDIUM);
        return summary;
    }

    private TopicSummaryJpaEntity buildJpaEntity() {
        var entity = new TopicSummaryJpaEntity();
        entity.setId(1L);
        entity.setChildProfileId(10L);
        entity.setTopicId(40L);
        entity.setTotalAttempts(5);
        entity.setTotalCorrect(3);
        entity.setTotalIncorrect(2);
        entity.setTotalTimeouts(0);
        entity.setSuccessRatePercent(new BigDecimal("60.00"));
        entity.setFailureRatePercent(new BigDecimal("40.00"));
        entity.setAverageResponseTimeMs(5000);
        entity.setPerformanceBand("MEDIUM");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
