package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TracingPatternJpaRepository extends JpaRepository<TracingPatternJpaEntity, Long> {

    List<TracingPatternJpaEntity> findByTopicId(Long topicId);

    List<TracingPatternJpaEntity> findByStatus(String status);
}
