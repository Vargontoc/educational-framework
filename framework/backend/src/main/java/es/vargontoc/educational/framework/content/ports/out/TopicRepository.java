package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;

import java.util.List;
import java.util.Optional;

public interface TopicRepository {

    Optional<Topic> findById(Long id);

    List<Topic> findAll();

    List<Topic> findByCategoryId(Long categoryId);

    List<Topic> findByRecognitionType(RecognitionType recognitionType);

    List<Topic> findByRecognitionTypeAndHabitatTag(RecognitionType recognitionType, Biome habitatTag);

    Topic save(Topic topic);

    boolean existsByNameAndCategoryId(String name, Long categoryId);
}
