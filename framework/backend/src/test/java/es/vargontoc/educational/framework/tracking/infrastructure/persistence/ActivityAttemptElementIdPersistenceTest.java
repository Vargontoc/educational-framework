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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityAttemptElementIdPersistenceTest {

    @Mock
    private ActivityAttemptJpaRepository jpaRepository;

    private ActivityAttemptPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ActivityAttemptPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_persistsElementId_whenProvided() {
        var attempt = buildAttempt(99L);
        var captor = ArgumentCaptor.forClass(ActivityAttemptJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, ActivityAttemptJpaEntity.class);
            entity.setId(1L);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        adapter.save(attempt);

        verify(jpaRepository).save(captor.capture());
        assertEquals(99L, captor.getValue().getElementId());
    }

    @Test
    void save_persistsNullElementId_whenNotProvided() {
        var attempt = buildAttempt(null);
        var captor = ArgumentCaptor.forClass(ActivityAttemptJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, ActivityAttemptJpaEntity.class);
            entity.setId(1L);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        adapter.save(attempt);

        verify(jpaRepository).save(captor.capture());
        assertNull(captor.getValue().getElementId());
    }

    @Test
    void save_returnsDomainWithElementId() {
        var attempt = buildAttempt(99L);
        when(jpaRepository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, ActivityAttemptJpaEntity.class);
            entity.setId(1L);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setElementId(99L);
            return entity;
        });

        var result = adapter.save(attempt);
        assertEquals(99L, result.getElementId());
    }

    private ActivityAttempt buildAttempt(Long elementId) {
        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(10L);
        attempt.setActivityId(20L);
        attempt.setChildSessionId(30L);
        attempt.setTopicId(40L);
        attempt.setElementId(elementId);
        attempt.setDifficultyLevelId(50L);
        attempt.setResult(AttemptResult.CORRECT);
        attempt.setResponseTimeMs(5000);
        return attempt;
    }
}
