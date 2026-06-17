package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityAttemptJpaRepository extends JpaRepository<ActivityAttemptJpaEntity, Long> {

    @Query("SELECT a FROM ActivityAttemptJpaEntity a WHERE a.childProfileId = :childProfileId AND a.activityId = :activityId ORDER BY a.createdAt DESC LIMIT :limit")
    List<ActivityAttemptJpaEntity> findRecentByChildAndActivity(
            @Param("childProfileId") Long childProfileId,
            @Param("activityId") Long activityId,
            @Param("limit") int limit);
}
