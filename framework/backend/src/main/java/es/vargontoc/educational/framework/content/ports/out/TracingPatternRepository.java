package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.TracingPattern;

import java.util.List;
import java.util.Optional;

public interface TracingPatternRepository {

    Optional<TracingPattern> findById(Long id);

    List<TracingPattern> findAll();

    List<TracingPattern> findByTopicId(Long topicId);

    TracingPattern save(TracingPattern tracingPattern);
}
