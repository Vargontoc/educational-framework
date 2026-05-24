package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ActivityResource;
import es.vargontoc.educational.framework.content.model.ResourceType;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.ActivityResourceRepository;
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
class ActivityResourceServiceTest {

    @Mock
    private ActivityResourceRepository activityResourceRepository;

    @Mock
    private ActivityRepository activityRepository;

    private ActivityResourceService activityResourceService;

    @BeforeEach
    void setUp() {
        activityResourceService = new ActivityResourceService(activityResourceRepository, activityRepository);
    }

    @Test
    void createResource_happyPath() {
        var activity = new Activity();
        activity.setId(1L);

        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityResourceRepository.save(any(ActivityResource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = activityResourceService.createResource(1L, null, ResourceType.IMAGE, "/images/game1.png", "{\"width\":100}");

        assertEquals(1L, result.getActivityId());
        assertEquals(ResourceType.IMAGE, result.getResourceType());
        assertEquals("/images/game1.png", result.getPath());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createResource_activityNotFound_throwsResourceNotFound() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            activityResourceService.createResource(99L, null, ResourceType.IMAGE, "/images/game1.png", null));
    }

    @Test
    void listByActivity_returnsFiltered() {
        when(activityResourceRepository.findByActivityId(1L)).thenReturn(List.of(new ActivityResource()));

        var result = activityResourceService.listByActivity(1L);

        assertEquals(1, result.size());
    }

    @Test
    void updateResource_happyPath() {
        var existing = new ActivityResource();
        existing.setId(1L);
        existing.setActivityId(1L);
        existing.setResourceType(ResourceType.IMAGE);
        existing.setPath("/old/path.png");

        when(activityResourceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(activityResourceRepository.save(any(ActivityResource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = activityResourceService.updateResource(1L, null, ResourceType.VIDEO, "/new/video.mp4", null);

        assertEquals(ResourceType.VIDEO, result.getResourceType());
        assertEquals("/new/video.mp4", result.getPath());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateResource_notFound_throwsResourceNotFound() {
        when(activityResourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            activityResourceService.updateResource(99L, null, ResourceType.IMAGE, "/path", null));
    }
}
