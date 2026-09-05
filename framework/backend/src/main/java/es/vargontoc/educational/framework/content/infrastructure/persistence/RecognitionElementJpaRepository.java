package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecognitionElementJpaRepository extends JpaRepository<RecognitionElementJpaEntity, Long> {

    List<RecognitionElementJpaEntity> findByTopicIdAndStatus(Long topicId, String status);
}
