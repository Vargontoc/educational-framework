package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.TopicSummary;

import java.util.Optional;

public interface TopicSummaryRepository {

    Optional<TopicSummary> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId);

    TopicSummary save(TopicSummary summary);
}
