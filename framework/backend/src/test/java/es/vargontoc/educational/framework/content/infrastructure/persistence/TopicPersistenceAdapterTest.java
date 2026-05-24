package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Topic;
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
class TopicPersistenceAdapterTest {

    @Mock
    private TopicJpaRepository jpaRepository;

    private TopicPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TopicPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_joinsCompatibleVariants_asCommaSeparated() {
        var topic = buildTopic(List.of("RECOGNITION", "MEMORY", "SEQUENCE"));
        var captor = ArgumentCaptor.forClass(TopicJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(topic);

        verify(jpaRepository).save(captor.capture());
        assertEquals("RECOGNITION,MEMORY,SEQUENCE", captor.getValue().getCompatibleVariants());
    }

    @Test
    void save_emptyVariants_storesNull() {
        var topic = buildTopic(List.of());
        var captor = ArgumentCaptor.forClass(TopicJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(topic);

        verify(jpaRepository).save(captor.capture());
        assertNull(captor.getValue().getCompatibleVariants());
    }

    @Test
    void findById_parsesCommaSeparatedVariants_toList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("RECOGNITION,MEMORY")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of("RECOGNITION", "MEMORY"), result.get().getCompatibleVariants());
    }

    @Test
    void findById_nullVariants_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity(null)));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getCompatibleVariants());
    }

    @Test
    void findById_blankVariants_returnsEmptyList() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity("   ")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of(), result.get().getCompatibleVariants());
    }

    @Test
    void findById_variantsWithPaddingSpaces_trimsEntries() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildJpaEntity(" RECOGNITION , MEMORY ")));

        var result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(List.of("RECOGNITION", "MEMORY"), result.get().getCompatibleVariants());
    }

    private Topic buildTopic(List<String> variants) {
        var topic = new Topic();
        topic.setName("Dog");
        topic.setCategoryId(1L);
        topic.setStatus(ContentStatus.ACTIVE);
        topic.setCompatibleVariants(variants);
        topic.setCreatedAt(LocalDateTime.now());
        return topic;
    }

    private TopicJpaEntity buildJpaEntity(String compatibleVariants) {
        var entity = new TopicJpaEntity();
        entity.setId(1L);
        entity.setName("Dog");
        entity.setCategoryId(1L);
        entity.setStatus("ACTIVE");
        entity.setCompatibleVariants(compatibleVariants);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
