package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityAttemptPersistenceAdapterTest {

    @Mock
    private ActivityAttemptJpaRepository jpaRepository;

    private ActivityAttemptPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ActivityAttemptPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_convertsDomainToJpaEntity() {
        var attempt = buildAttempt();
        var captor = ArgumentCaptor.forClass(ActivityAttemptJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, ActivityAttemptJpaEntity.class);
            entity.setId(1L);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        adapter.save(attempt);

        verify(jpaRepository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(10L, saved.getChildProfileId());
        assertEquals(20L, saved.getActivityId());
        assertEquals(30L, saved.getChildSessionId());
        assertEquals(40L, saved.getTopicId());
        assertEquals(50L, saved.getDifficultyLevelId());
        assertEquals("CORRECT", saved.getResult());
        assertEquals(5000, saved.getResponseTimeMs());
        assertEquals("{\"engine\":\"test\"}", saved.getAttemptContext());
    }

    @Test
    void save_returnsDomainWithCorrectValues() {
        var attempt = buildAttempt();
        when(jpaRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, ActivityAttemptJpaEntity.class);
            entity.setId(1L);
            entity.setCreatedAt(LocalDateTime.of(2026, 6, 16, 10, 0));
            entity.setUpdatedAt(LocalDateTime.of(2026, 6, 16, 10, 0));
            return entity;
        });

        var result = adapter.save(attempt);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10L, result.getChildProfileId());
        assertEquals(AttemptResult.CORRECT, result.getResult());
        assertEquals(5000, result.getResponseTimeMs());
        assertEquals("{\"engine\":\"test\"}", result.getAttemptContext());
    }

    private ActivityAttempt buildAttempt() {
        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(10L);
        attempt.setActivityId(20L);
        attempt.setChildSessionId(30L);
        attempt.setTopicId(40L);
        attempt.setDifficultyLevelId(50L);
        attempt.setResult(AttemptResult.CORRECT);
        attempt.setResponseTimeMs(5000);
        attempt.setAttemptContext("{\"engine\":\"test\"}");
        return attempt;
    }
}
