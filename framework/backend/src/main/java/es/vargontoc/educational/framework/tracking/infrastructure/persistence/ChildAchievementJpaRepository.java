package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildAchievementJpaRepository extends JpaRepository<ChildAchievementJpaEntity, Long> {

    List<ChildAchievementJpaEntity> findByChildProfileId(Long childProfileId);

    List<ChildAchievementJpaEntity> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId);

    List<ChildAchievementJpaEntity> findByChildProfileIdAndTopicId(Long childProfileId, Long topicId);

    List<ChildAchievementJpaEntity> findByChildProfileIdAndActivityIdAndTopicId(
            Long childProfileId, Long activityId, Long topicId);

    @Query("SELECT e FROM ChildAchievementJpaEntity e " +
           "WHERE e.childProfileId = :childProfileId " +
           "AND e.achievementCode = :achievementCode " +
           "AND ((:activityId IS NULL AND e.activityId IS NULL) OR e.activityId = :activityId) " +
           "AND ((:topicId IS NULL AND e.topicId IS NULL) OR e.topicId = :topicId)")
    Optional<ChildAchievementJpaEntity> findByChildProfileIdAndAchievementCodeAndActivityIdAndTopicId(
            @Param("childProfileId") Long childProfileId,
            @Param("achievementCode") String achievementCode,
            @Param("activityId") Long activityId,
            @Param("topicId") Long topicId);
}
