package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;

import java.util.List;

public interface ActivityUseCase {

    Activity createActivity(String name, String description, String gameEngineType, ContentStatus status, Integer minAge, Integer maxAge, List<Long> topicIds);

    Activity getActivity(Long id);

    Activity getGameReadyActivity(Long activityId);

    List<Activity> listActivities();

    List<Activity> listActivitiesByTopic(Long topicId);

    Activity updateActivity(Long id, String name, String description, String gameEngineType, ContentStatus status, Integer minAge, Integer maxAge, List<Long> topicIds);
}
