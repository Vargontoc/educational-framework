package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.Curiosity;

import java.util.List;
import java.util.Optional;

public interface CuriosityRepository {

    Optional<Curiosity> findById(Long id);

    List<Curiosity> findAll();

    List<Curiosity> findByTopicId(Long topicId);

    List<Curiosity> findActiveByFilters(Long topicId, Integer age, String locale);

    Curiosity save(Curiosity curiosity);
}
