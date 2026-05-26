package es.vargontoc.educational.framework.content.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningPathJpaRepository extends JpaRepository<LearningPathJpaEntity, Long> {

    List<LearningPathJpaEntity> findByStatus(String status);

    List<LearningPathJpaEntity> findByLocale(String locale);
}
