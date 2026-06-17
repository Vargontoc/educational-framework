package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.CuriosityViewed;

import java.util.List;
import java.util.Optional;

public interface CuriosityViewedRepository {

    List<CuriosityViewed> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId);

    List<CuriosityViewed> findByChildProfileIdAndTopicIdAndCycleNumber(Long childProfileId, Long topicId, int cycleNumber);

    Optional<CuriosityViewed> findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(
            Long childProfileId, Long topicId, Long curiosityId, int cycleNumber);

    CuriosityViewed save(CuriosityViewed curiosityViewed);

    Optional<Integer> findMaxCycleNumber(Long childProfileId, Long topicId);
}
