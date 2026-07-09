package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.Category;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.CategoryRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService(topicRepository, categoryRepository);
    }

    @Test
    void createTopic_happyPath() {
        var category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(topicRepository.existsByNameAndCategoryId("Addition", 1L)).thenReturn(false);
        when(topicRepository.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = topicService.createTopic("Addition", "Basic addition", 1L, ContentStatus.ACTIVE, 5, 10, List.of("RECOGNITION", "MEMORY"));

        assertEquals("Addition", result.getName());
        assertEquals(1L, result.getCategoryId());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createTopic_categoryNotFound_throwsResourceNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            topicService.createTopic("Addition", "Basic addition", 99L, ContentStatus.ACTIVE, 5, 10, null));
    }

    @Test
    void createTopic_duplicateName_throwsConflict() {
        var category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(topicRepository.existsByNameAndCategoryId("Addition", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
            topicService.createTopic("Addition", "Basic addition", 1L, ContentStatus.ACTIVE, 5, 10, null));
    }

    @Test
    void listTopicsByCategory_returnsFiltered() {
        when(topicRepository.findByCategoryId(1L)).thenReturn(List.of(new Topic(), new Topic()));

        var result = topicService.listTopicsByCategory(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getTopic_notFound_throwsResourceNotFound() {
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> topicService.getTopic(99L));
    }

    @Test
    void listTopicsByRecognitionType_returnsFiltered() {
        var topic = new Topic();
        topic.setRecognitionType(RecognitionType.ANIMAL);

        when(topicRepository.findByRecognitionType(RecognitionType.ANIMAL)).thenReturn(List.of(topic));

        var result = topicService.listTopicsByRecognitionType(RecognitionType.ANIMAL);

        assertEquals(1, result.size());
        assertEquals(RecognitionType.ANIMAL, result.get(0).getRecognitionType());
    }

    @Test
    void listTopicsByRecognitionTypeAndHabitat_returnsFiltered() {
        var topic = new Topic();
        topic.setRecognitionType(RecognitionType.ANIMAL);
        topic.setHabitatTag(Biome.FARM);

        when(topicRepository.findByRecognitionTypeAndHabitatTag(RecognitionType.ANIMAL, Biome.FARM))
            .thenReturn(List.of(topic));

        var result = topicService.listTopicsByRecognitionTypeAndHabitat(RecognitionType.ANIMAL, Biome.FARM);

        assertEquals(1, result.size());
        assertEquals(RecognitionType.ANIMAL, result.get(0).getRecognitionType());
        assertEquals(Biome.FARM, result.get(0).getHabitatTag());
    }

    @Test
    void listTopicsByRecognitionTypeAndHabitat_returnsEmptyWhenNoMatch() {
        when(topicRepository.findByRecognitionTypeAndHabitatTag(RecognitionType.ANIMAL, Biome.JUNGLE))
            .thenReturn(List.of());

        var result = topicService.listTopicsByRecognitionTypeAndHabitat(RecognitionType.ANIMAL, Biome.JUNGLE);

        assertEquals(0, result.size());
    }
}
