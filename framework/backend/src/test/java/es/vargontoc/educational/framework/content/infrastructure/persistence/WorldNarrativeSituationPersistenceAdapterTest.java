package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.SituationType;
import es.vargontoc.educational.framework.content.model.Tone;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldNarrativeSituationPersistenceAdapterTest {

    @Mock
    private WorldNarrativeSituationJpaRepository jpaRepository;

    private WorldNarrativeSituationPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorldNarrativeSituationPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_persistsWorldNarrativeSituation() {
        var situation = buildDomain();
        var captor = ArgumentCaptor.forClass(WorldNarrativeSituationJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(situation);

        verify(jpaRepository).save(captor.capture());
        assertEquals("HOST_FOUND_SOMETHING", captor.getValue().getCode());
        assertEquals("FOUND_OBJECT", captor.getValue().getSituationType());
        assertEquals("JOYFUL", captor.getValue().getTone());
        assertEquals("ACTIVE", captor.getValue().getStatus());
    }

    @Test
    void findByCode_existingCode_returnsWorldNarrativeSituation() {
        when(jpaRepository.findByCode("HOST_FOUND_SOMETHING")).thenReturn(Optional.of(buildJpaEntity()));

        var result = adapter.findByCode("HOST_FOUND_SOMETHING");

        assertTrue(result.isPresent());
        assertEquals("HOST_FOUND_SOMETHING", result.get().getCode());
        assertEquals(SituationType.FOUND_OBJECT, result.get().getSituationType());
        assertEquals(Tone.JOYFUL, result.get().getTone());
        assertNotNull(result.get().getCreatedAt());
    }

    @Test
    void findByCode_nonExistingCode_returnsEmpty() {
        when(jpaRepository.findByCode("NON_EXISTING")).thenReturn(Optional.empty());

        var result = adapter.findByCode("NON_EXISTING");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByCode_existingCode_returnsTrue() {
        when(jpaRepository.existsByCode("HOST_FOUND_SOMETHING")).thenReturn(true);

        var result = adapter.existsByCode("HOST_FOUND_SOMETHING");

        assertTrue(result);
    }

    private WorldNarrativeSituation buildDomain() {
        var situation = new WorldNarrativeSituation();
        situation.setCode("HOST_FOUND_SOMETHING");
        situation.setDisplayText("The dog found something interesting!");
        situation.setSituationType(SituationType.FOUND_OBJECT);
        situation.setTone(Tone.JOYFUL);
        situation.setMinAge(3);
        situation.setMaxAge(4);
        situation.setStatus(ContentStatus.ACTIVE);
        situation.setSortOrder(1);
        situation.setCreatedAt(LocalDateTime.now());
        return situation;
    }

    private WorldNarrativeSituationJpaEntity buildJpaEntity() {
        var entity = new WorldNarrativeSituationJpaEntity();
        entity.setId(1L);
        entity.setCode("HOST_FOUND_SOMETHING");
        entity.setDisplayText("The dog found something interesting!");
        entity.setSituationType("FOUND_OBJECT");
        entity.setTone("JOYFUL");
        entity.setMinAge(3);
        entity.setMaxAge(4);
        entity.setStatus("ACTIVE");
        entity.setSortOrder(1);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
