package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.TracingPattern;
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
class TracingPatternPersistenceAdapterTest {

    @Mock
    private TracingPatternJpaRepository jpaRepository;

    private TracingPatternPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TracingPatternPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_serializesPoints_asSemicolonSeparated() {
        var pattern = buildDomain(List.of(List.of(0.0, 0.0), List.of(0.5, 0.5), List.of(1.0, 1.0)));
        var captor = ArgumentCaptor.forClass(TracingPatternJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(pattern);

        verify(jpaRepository).save(captor.capture());
        assertEquals("0.0,0.0;0.5,0.5;1.0,1.0", captor.getValue().getPoints());
    }

    @Test
    void save_emptyPoints_storesNull() {
        var pattern = buildDomain(List.of());
        var captor = ArgumentCaptor.forClass(TracingPatternJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(pattern);

        verify(jpaRepository).save(captor.capture());
        assertNull(captor.getValue().getPoints());
    }

    @Test
    void findById_parsesSemicolonSeparatedPoints_toListOfLists() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("0.0,0.0;0.5,0.5;1.0,1.0")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().getPoints().size());
        assertEquals(List.of(0.0, 0.0), result.get().getPoints().get(0));
        assertEquals(List.of(0.5, 0.5), result.get().getPoints().get(1));
        assertEquals(List.of(1.0, 1.0), result.get().getPoints().get(2));
    }

    @Test
    void findById_nullPoints_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity(null)));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getPoints());
    }

    @Test
    void findById_blankPoints_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("   ")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getPoints());
    }

    @Test
    void parsePoints_withPaddingSpaces_trimsEntries() {
        var result = TracingPatternPersistenceAdapter.parsePoints(" 0.0 , 0.0 ; 1.0 , 1.0 ");

        assertEquals(2, result.size());
        assertEquals(List.of(0.0, 0.0), result.get(0));
        assertEquals(List.of(1.0, 1.0), result.get(1));
    }

    private TracingPattern buildDomain(List<List<Double>> points) {
        var pattern = new TracingPattern();
        pattern.setTopicId(1L);
        pattern.setName("Square Pattern");
        pattern.setDescription("A square tracing pattern");
        pattern.setPoints(points);
        pattern.setStatus(ContentStatus.ACTIVE);
        pattern.setCreatedAt(LocalDateTime.now());
        return pattern;
    }

    private TracingPatternJpaEntity buildJpaEntity(String points) {
        var entity = new TracingPatternJpaEntity();
        entity.setId(1L);
        entity.setTopicId(1L);
        entity.setName("Square Pattern");
        entity.setDescription("A square tracing pattern");
        entity.setPoints(points);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
