package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Curiosity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuriosityPersistenceAdapterTest {

    @Mock
    private CuriosityJpaRepository jpaRepository;

    private CuriosityPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CuriosityPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_joinsTags_asCommaSeparated() {
        var curiosity = buildCuriosity(List.of("animales", "insectos"));
        var captor = ArgumentCaptor.forClass(CuriosityJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(curiosity);

        verify(jpaRepository).save(captor.capture());
        assertEquals("animales,insectos", captor.getValue().getTags());
    }

    @Test
    void save_emptyTags_storesNull() {
        var curiosity = buildCuriosity(List.of());
        var captor = ArgumentCaptor.forClass(CuriosityJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(curiosity);

        verify(jpaRepository).save(captor.capture());
        assertNull(captor.getValue().getTags());
    }

    @Test
    void findById_parsesCommaSeparatedTags_toList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("animales,insectos")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of("animales", "insectos"), result.get().getTags());
    }

    @Test
    void findById_nullTags_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity(null)));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getTags());
    }

    @Test
    void findById_blankTags_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("   ")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getTags());
    }

    @Test
    void findById_tagsWithPaddingSpaces_trimsEntries() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity(" animales , insectos ")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of("animales", "insectos"), result.get().getTags());
    }

    private Curiosity buildCuriosity(List<String> tags) {
        var curiosity = new Curiosity();
        curiosity.setText("Las mariposas prueban con sus patas");
        curiosity.setLocale("es-ES");
        curiosity.setStatus(ContentStatus.ACTIVE);
        curiosity.setTags(tags);
        curiosity.setCreatedAt(LocalDateTime.now());
        return curiosity;
    }

    private CuriosityJpaEntity buildJpaEntity(String tags) {
        var entity = new CuriosityJpaEntity();
        entity.setId(1L);
        entity.setText("Las mariposas prueban con sus patas");
        entity.setLocale("es-ES");
        entity.setStatus("ACTIVE");
        entity.setTags(tags);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
