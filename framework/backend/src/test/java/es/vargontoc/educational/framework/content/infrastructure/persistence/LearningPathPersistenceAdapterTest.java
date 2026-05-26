package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.LearningPath;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningPathPersistenceAdapterTest {

    @Mock
    private LearningPathJpaRepository jpaRepository;

    private LearningPathPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LearningPathPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_mapsDomainToJpa() {
        var learningPath = buildDomain();
        var captor = ArgumentCaptor.forClass(LearningPathJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(learningPath);

        verify(jpaRepository).save(captor.capture());
        assertEquals("Math Basics", captor.getValue().getName());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals("es-ES", captor.getValue().getLocale());
    }

    @Test
    void findById_mapsJpaToDomain() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity()));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Math Basics", result.get().getName());
        assertEquals(ContentStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    void findAll_mapsList() {
        when(jpaRepository.findAll()).thenReturn(List.of(buildJpaEntity()));

        var result = adapter.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findByStatus_filtersCorrectly() {
        when(jpaRepository.findByStatus("ACTIVE")).thenReturn(List.of(buildJpaEntity()));

        var result = adapter.findByStatus(ContentStatus.ACTIVE);

        assertEquals(1, result.size());
    }

    private LearningPath buildDomain() {
        var path = new LearningPath();
        path.setName("Math Basics");
        path.setDescription("Learn basic math");
        path.setMinAge(3);
        path.setMaxAge(6);
        path.setLocale("es-ES");
        path.setStatus(ContentStatus.ACTIVE);
        path.setCreatedAt(LocalDateTime.now());
        return path;
    }

    private LearningPathJpaEntity buildJpaEntity() {
        var entity = new LearningPathJpaEntity();
        entity.setId(1L);
        entity.setName("Math Basics");
        entity.setDescription("Learn basic math");
        entity.setMinAge(3);
        entity.setMaxAge(6);
        entity.setLocale("es-ES");
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
