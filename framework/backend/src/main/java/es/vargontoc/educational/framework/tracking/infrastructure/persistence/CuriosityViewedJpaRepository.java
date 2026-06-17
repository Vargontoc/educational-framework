package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuriosityViewedJpaRepository extends JpaRepository<CuriosityViewedJpaEntity, Long> {

    List<CuriosityViewedJpaEntity> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId);

    List<CuriosityViewedJpaEntity> findByChildProfileIdAndTopicIdAndCycleNumber(
            Long childProfileId, Long topicId, Integer cycleNumber);

    Optional<CuriosityViewedJpaEntity> findByChildProfileIdAndTopicIdAndCuriosityIdAndCycleNumber(
            Long childProfileId, Long topicId, Long curiosityId, Integer cycleNumber);

    @Query("SELECT MAX(c.cycleNumber) FROM CuriosityViewedJpaEntity c WHERE c.childProfileId = :childProfileId AND c.topicId = :topicId")
    Optional<Integer> findMaxCycleNumber(@Param("childProfileId") Long childProfileId, @Param("topicId") Long topicId);
}
