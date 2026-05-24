package es.vargontoc.educational.framework.content.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.in.ActivityUseCase;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import es.vargontoc.educational.framework.content.validation.ActivityValidator;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class ActivityService implements ActivityUseCase {

    private final ActivityRepository activityRepository;
    private final TopicRepository topicRepository;
    private final ActivityValidator activityValidator;

    public ActivityService(ActivityRepository activityRepository, TopicRepository topicRepository) {
        this.activityRepository = activityRepository;
        this.topicRepository = topicRepository;
        this.activityValidator = new ActivityValidator();
    }

    @Override
    public Activity createActivity(String name, String description, String gameEngineType, ContentStatus status, Integer minAge, Integer maxAge, List<Long> topicIds) {
        activityValidator.validateForCreate(name, status, minAge, maxAge);

        if (topicIds != null) {
            for (Long topicId : topicIds) {
                if (!topicRepository.findById(topicId).isPresent()) {
                    throw new ResourceNotFoundException("Topic not found with id: " + topicId);
                }
            }
        }

        var activity = new Activity();
        activity.setName(name);
        activity.setDescription(description);
        activity.setGameEngineType(gameEngineType);
        activity.setStatus(status);
        activity.setMinAge(minAge);
        activity.setMaxAge(maxAge);
        activity.setTopicIds(topicIds != null ? topicIds : new ArrayList<>());
        activity.setCreatedAt(LocalDateTime.now());

        return activityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Activity getActivity(Long id) {
        return activityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Activity> listActivities() {
        return activityRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Activity> listActivitiesByTopic(Long topicId) {
        return activityRepository.findByTopicId(topicId);
    }

    @Override
    public Activity updateActivity(Long id, String name, String description, String gameEngineType, ContentStatus status, Integer minAge, Integer maxAge, List<Long> topicIds) {
        activityValidator.validateForUpdate(name, status, minAge, maxAge);

        var existing = activityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        if (topicIds != null) {
            for (Long topicId : topicIds) {
                if (!topicRepository.findById(topicId).isPresent()) {
                    throw new ResourceNotFoundException("Topic not found with id: " + topicId);
                }
            }
        }

        existing.setName(name);
        existing.setDescription(description);
        existing.setGameEngineType(gameEngineType);
        existing.setStatus(status);
        existing.setMinAge(minAge);
        existing.setMaxAge(maxAge);
        existing.setTopicIds(topicIds != null ? topicIds : new ArrayList<>());
        existing.setUpdatedAt(LocalDateTime.now());

        return activityRepository.save(existing);
    }
}
