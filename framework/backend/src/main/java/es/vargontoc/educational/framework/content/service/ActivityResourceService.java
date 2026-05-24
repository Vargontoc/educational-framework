package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.ActivityResource;
import es.vargontoc.educational.framework.content.model.ResourceType;
import es.vargontoc.educational.framework.content.ports.in.ActivityResourceUseCase;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.ActivityResourceRepository;
import es.vargontoc.educational.framework.content.validation.ActivityResourceValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class ActivityResourceService implements ActivityResourceUseCase {

    private final ActivityResourceRepository activityResourceRepository;
    private final ActivityRepository activityRepository;
    private final ActivityResourceValidator activityResourceValidator;

    public ActivityResourceService(ActivityResourceRepository activityResourceRepository, ActivityRepository activityRepository) {
        this.activityResourceRepository = activityResourceRepository;
        this.activityRepository = activityRepository;
        this.activityResourceValidator = new ActivityResourceValidator();
    }

    @Override
    public ActivityResource createResource(Long activityId, Long topicId, ResourceType resourceType, String path, String metadata) {
        activityResourceValidator.validateForCreate(activityId, resourceType, path);

        if (!activityRepository.findById(activityId).isPresent()) {
            throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        }

        var resource = new ActivityResource();
        resource.setActivityId(activityId);
        resource.setTopicId(topicId);
        resource.setResourceType(resourceType);
        resource.setPath(path);
        resource.setMetadata(metadata);
        resource.setCreatedAt(LocalDateTime.now());

        return activityResourceRepository.save(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResource> listByActivity(Long activityId) {
        return activityResourceRepository.findByActivityId(activityId);
    }

    @Override
    public ActivityResource updateResource(Long id, Long topicId, ResourceType resourceType, String path, String metadata) {
        var existing = activityResourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ActivityResource not found with id: " + id));

        activityResourceValidator.validateForUpdate(existing.getActivityId(), resourceType, path);

        existing.setTopicId(topicId);
        existing.setResourceType(resourceType);
        existing.setPath(path);
        existing.setMetadata(metadata);
        existing.setUpdatedAt(LocalDateTime.now());

        return activityResourceRepository.save(existing);
    }
}
