package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.TopicGroupedByPerformance;

public interface ClassifyTopicsByPerformanceUseCase {

    TopicGroupedByPerformance classifyTopics(Long childProfileId);
}
