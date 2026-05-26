package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Story;
import es.vargontoc.educational.framework.content.ports.out.StoryRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class StoryPersistenceAdapter implements StoryRepository {

    private final StoryJpaRepository jpaRepository;

    public StoryPersistenceAdapter(StoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Story> findById(Long id) {
        return jpaRepository.findById(id)
            .map(StoryPersistenceAdapter::toDomain);
    }

    @Override
    public List<Story> findAll() {
        return jpaRepository.findAll().stream()
            .map(StoryPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<Story> findByStatus(ContentStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
            .map(StoryPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public Story save(Story story) {
        var saved = jpaRepository.save(toJpa(story));
        return toDomain(saved);
    }

    private static Story toDomain(StoryJpaEntity source) {
        var target = new Story();
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setEstimatedDurationMinutes(source.getEstimatedDurationMinutes());
        target.setTopicIds(parseTopicIds(source.getTopicIds()));
        target.setBackgroundMusicResourceRef(source.getBackgroundMusicResourceRef());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static StoryJpaEntity toJpa(Story source) {
        var target = new StoryJpaEntity();
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setEstimatedDurationMinutes(source.getEstimatedDurationMinutes());
        target.setTopicIds(joinTopicIds(source.getTopicIds()));
        target.setBackgroundMusicResourceRef(source.getBackgroundMusicResourceRef());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    static List<Long> parseTopicIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .toList();
    }

    static String joinTopicIds(List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return null;
        }
        return topicIds.stream()
            .map(String::valueOf)
            .reduce((a, b) -> a + "," + b)
            .orElse(null);
    }
}
