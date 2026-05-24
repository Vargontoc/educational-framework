package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityResourceJpaRepository extends JpaRepository<ActivityResourceJpaEntity, Long> {

    List<ActivityResourceJpaEntity> findByActivityId(Long activityId);
}
