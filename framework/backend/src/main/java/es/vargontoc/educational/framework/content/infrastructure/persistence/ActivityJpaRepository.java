package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityJpaRepository extends JpaRepository<ActivityJpaEntity, Long> {

    Optional<ActivityJpaEntity> findByIdAndStatus(Long id, String status);

    @Query("SELECT a FROM ActivityJpaEntity a JOIN ActivityTopicJpaEntity at ON a.id = at.activityId " +
           "WHERE at.topicId = :topicId AND a.status = :status AND a.minAge <= :targetAge AND a.maxAge >= :targetAge")
    List<ActivityJpaEntity> findByStatusAndTopicId(@Param("topicId") Long topicId, @Param("status") String status, @Param("targetAge") Integer targetAge);
}
