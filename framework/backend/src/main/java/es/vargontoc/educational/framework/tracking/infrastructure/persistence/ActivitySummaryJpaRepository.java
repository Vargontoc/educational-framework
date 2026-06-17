package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivitySummaryJpaRepository extends JpaRepository<ActivitySummaryJpaEntity, Long> {

    Optional<ActivitySummaryJpaEntity> findByChildProfileIdAndActivityId(Long childProfileId, Long activityId);
}
