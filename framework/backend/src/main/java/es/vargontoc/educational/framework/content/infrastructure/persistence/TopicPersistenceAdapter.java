package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.out.TopicRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class TopicPersistenceAdapter implements TopicRepository {

    private final TopicJpaRepository jpaRepository;

    public TopicPersistenceAdapter(TopicJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Topic> findById(Long id) {
        return jpaRepository.findById(id)
            .map(TopicPersistenceAdapter::toDomain);
    }

    @Override
    public List<Topic> findAll() {
        return jpaRepository.findAll().stream()
            .map(TopicPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<Topic> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId).stream()
            .map(TopicPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public Topic save(Topic topic) {
        var saved = jpaRepository.save(toJpa(topic));
        return toDomain(saved);
    }

    @Override
    public boolean existsByNameAndCategoryId(String name, Long categoryId) {
        return jpaRepository.findByNameAndCategoryId(name, categoryId).isPresent();
    }

    private static Topic toDomain(TopicJpaEntity source) {
        var target = new Topic();
        target.setId(source.getId());
        target.setCategoryId(source.getCategoryId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setCompatibleVariants(parseVariants(source.getCompatibleVariants()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static TopicJpaEntity toJpa(Topic source) {
        var target = new TopicJpaEntity();
        target.setId(source.getId());
        target.setCategoryId(source.getCategoryId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setStatus(source.getStatus().name());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setCompatibleVariants(joinVariants(source.getCompatibleVariants()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static List<String> parseVariants(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    private static String joinVariants(List<String> variants) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return String.join(",", variants);
    }
}
