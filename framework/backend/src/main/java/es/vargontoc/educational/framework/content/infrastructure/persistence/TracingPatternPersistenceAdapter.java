package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.TracingPattern;
import es.vargontoc.educational.framework.content.ports.out.TracingPatternRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class TracingPatternPersistenceAdapter implements TracingPatternRepository {

    private final TracingPatternJpaRepository jpaRepository;

    public TracingPatternPersistenceAdapter(TracingPatternJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<TracingPattern> findById(Long id) {
        return jpaRepository.findById(id)
            .map(TracingPatternPersistenceAdapter::toDomain);
    }

    @Override
    public List<TracingPattern> findAll() {
        return jpaRepository.findAll().stream()
            .map(TracingPatternPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<TracingPattern> findByTopicId(Long topicId) {
        return jpaRepository.findByTopicId(topicId).stream()
            .map(TracingPatternPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public TracingPattern save(TracingPattern tracingPattern) {
        var saved = jpaRepository.save(toJpa(tracingPattern));
        return toDomain(saved);
    }

    private static TracingPattern toDomain(TracingPatternJpaEntity source) {
        var target = new TracingPattern();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setPatternType(source.getPatternType());
        target.setPoints(parsePoints(source.getPoints()));
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static TracingPatternJpaEntity toJpa(TracingPattern source) {
        var target = new TracingPatternJpaEntity();
        target.setId(source.getId());
        target.setTopicId(source.getTopicId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setPatternType(source.getPatternType());
        target.setPoints(serializePoints(source.getPoints()));
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static List<List<Double>> parsePoints(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(";"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(pair -> {
                String[] coords = pair.split(",");
                if (coords.length != 2) {
                    throw new IllegalArgumentException("Invalid point format: " + pair);
                }
                return List.of(Double.parseDouble(coords[0].trim()), Double.parseDouble(coords[1].trim()));
            })
            .toList();
    }

    static String serializePoints(List<List<Double>> points) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        return points.stream()
            .map(p -> p.get(0) + "," + p.get(1))
            .reduce((a, b) -> a + ";" + b)
            .orElse(null);
    }
}
