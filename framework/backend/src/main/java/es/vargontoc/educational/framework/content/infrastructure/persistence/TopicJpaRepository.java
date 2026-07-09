package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicJpaRepository extends JpaRepository<TopicJpaEntity, Long> {

    List<TopicJpaEntity> findByCategoryId(Long categoryId);

    Optional<TopicJpaEntity> findByNameAndCategoryId(String name, Long categoryId);

    List<TopicJpaEntity> findByRecognitionType(RecognitionType recognitionType);

    List<TopicJpaEntity> findByRecognitionTypeAndHabitatTag(RecognitionType recognitionType, Biome habitatTag);
}
