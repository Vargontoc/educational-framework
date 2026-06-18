package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.TopicSummary;

import java.util.List;
import java.util.Optional;

public interface TopicSummaryRepository {

    Optional<TopicSummary> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId);

    List<TopicSummary> findByChildProfileId(Long childProfileId);

    TopicSummary save(TopicSummary summary);
}
