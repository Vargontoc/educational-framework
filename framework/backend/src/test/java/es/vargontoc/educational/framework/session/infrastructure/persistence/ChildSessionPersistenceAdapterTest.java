package es.vargontoc.educational.framework.session.infrastructure.persistence;

import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildSessionPersistenceAdapterTest {

    @Mock
    private ChildSessionJpaRepository jpaRepository;

    private ChildSessionPersistenceAdapter adapter;

    @Test
    void findActiveByChildProfileId_noRows_returnsEmpty() {
        adapter = new ChildSessionPersistenceAdapter(jpaRepository);
        when(jpaRepository.findByChildProfileIdAndStatusOrderByStartedAtDesc(10L, ChildSessionStatus.ACTIVE.name()))
            .thenReturn(List.of());

        var result = adapter.findActiveByChildProfileId(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveByChildProfileId_oneRow_returnsIt() {
        adapter = new ChildSessionPersistenceAdapter(jpaRepository);
        var entity = entity(1L, LocalDateTime.now().minusMinutes(5));
        when(jpaRepository.findByChildProfileIdAndStatusOrderByStartedAtDesc(10L, ChildSessionStatus.ACTIVE.name()))
            .thenReturn(List.of(entity));

        var result = adapter.findActiveByChildProfileId(10L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findActiveByChildProfileId_multipleRows_returnsMostRecentlyStartedWithoutThrowing() {
        adapter = new ChildSessionPersistenceAdapter(jpaRepository);
        var older = entity(1L, LocalDateTime.now().minusMinutes(10));
        var newer = entity(2L, LocalDateTime.now().minusMinutes(1));
        // JPA method is already ordered desc by startedAt; the mock returns them in that order.
        when(jpaRepository.findByChildProfileIdAndStatusOrderByStartedAtDesc(10L, ChildSessionStatus.ACTIVE.name()))
            .thenReturn(List.of(newer, older));

        var result = adapter.findActiveByChildProfileId(10L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId());
    }

    private static ChildSessionJpaEntity entity(Long id, LocalDateTime startedAt) {
        var entity = new ChildSessionJpaEntity();
        entity.setId(id);
        entity.setChildProfileId(10L);
        entity.setFamilyId(1L);
        entity.setStartedAt(startedAt);
        entity.setLastActivityAt(startedAt);
        entity.setStatus(ChildSessionStatus.ACTIVE.name());
        entity.setHeartbeatIntervalSeconds(30);
        return entity;
    }
}
