package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
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
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private TopicRepository topicRepository;

    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityService(activityRepository, topicRepository);
    }

    @Test
    void createActivity_happyPath() {
        var topic = new Topic();
        topic.setId(1L);

        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = activityService.createActivity("Counting Game", "Learn to count", null, ContentStatus.ACTIVE, 5, 10, List.of(1L));

        assertEquals("Counting Game", result.getName());
        assertEquals(1, result.getTopicIds().size());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createActivity_topicNotFound_throwsResourceNotFound() {
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            activityService.createActivity("Counting Game", "Learn to count", null, ContentStatus.ACTIVE, 5, 10, List.of(99L)));
    }

    @Test
    void createActivity_nullTopicIds_passes() {
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = activityService.createActivity("Counting Game", "Learn to count", null, ContentStatus.ACTIVE, 5, 10, null);

        assertNotNull(result.getTopicIds());
        assertEquals(0, result.getTopicIds().size());
    }

    @Test
    void listActivitiesByTopic_returnsFiltered() {
        when(activityRepository.findByTopicId(1L)).thenReturn(List.of(new Activity()));

        var result = activityService.listActivitiesByTopic(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getActivity_notFound_throwsResourceNotFound() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> activityService.getActivity(99L));
    }
}
