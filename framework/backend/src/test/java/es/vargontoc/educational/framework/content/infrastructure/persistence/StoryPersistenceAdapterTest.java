package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;
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
class StoryPersistenceAdapterTest {

    @Mock
    private StoryJpaRepository jpaRepository;

    private StoryPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StoryPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_joinsTopicIds_asCommaSeparated() {
        var story = buildDomain(List.of(1L, 2L, 3L));
        var captor = ArgumentCaptor.forClass(StoryJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(story);

        verify(jpaRepository).save(captor.capture());
        assertEquals("1,2,3", captor.getValue().getTopicIds());
    }

    @Test
    void save_emptyTopicIds_storesNull() {
        var story = buildDomain(List.of());
        var captor = ArgumentCaptor.forClass(StoryJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(story);

        verify(jpaRepository).save(captor.capture());
        assertNull(captor.getValue().getTopicIds());
    }

    @Test
    void findById_parsesCommaSeparatedTopicIds_toList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("1,2,3")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(1L, 2L, 3L), result.get().getTopicIds());
    }

    @Test
    void findById_nullTopicIds_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity(null)));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getTopicIds());
    }

    @Test
    void findById_blankTopicIds_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("   ")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getTopicIds());
    }

    @Test
    void findById_topicIdsWithPaddingSpaces_trimsEntries() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity(" 1 , 2 , 3 ")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(1L, 2L, 3L), result.get().getTopicIds());
    }

    private Story buildDomain(List<Long> topicIds) {
        var story = new Story();
        story.setTitle("The Little Star");
        story.setDescription("A story about a star");
        story.setMinAge(3);
        story.setMaxAge(6);
        story.setEstimatedDurationMinutes(5);
        story.setTopicIds(topicIds);
        story.setStatus(ContentStatus.ACTIVE);
        story.setCreatedAt(LocalDateTime.now());
        return story;
    }

    private StoryJpaEntity buildJpaEntity(String topicIds) {
        var entity = new StoryJpaEntity();
        entity.setId(1L);
        entity.setTitle("The Little Star");
        entity.setDescription("A story about a star");
        entity.setMinAge(3);
        entity.setMaxAge(6);
        entity.setEstimatedDurationMinutes(5);
        entity.setTopicIds(topicIds);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
