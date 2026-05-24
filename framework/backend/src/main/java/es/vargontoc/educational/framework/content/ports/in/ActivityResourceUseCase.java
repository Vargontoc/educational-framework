package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.ActivityResource;
import es.vargontoc.educational.framework.content.model.ResourceType;

import java.util.List;

public interface ActivityResourceUseCase {

    ActivityResource createResource(Long activityId, Long topicId, ResourceType resourceType, String path, String metadata);

    List<ActivityResource> listByActivity(Long activityId);

    ActivityResource updateResource(Long id, Long topicId, ResourceType resourceType, String path, String metadata);
}
