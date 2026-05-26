package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.LearningPath;
import es.vargontoc.educational.framework.content.ports.out.LearningPathRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LearningPathPersistenceAdapter implements LearningPathRepository {

    private final LearningPathJpaRepository jpaRepository;

    public LearningPathPersistenceAdapter(LearningPathJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LearningPath> findById(Long id) {
        return jpaRepository.findById(id)
            .map(LearningPathPersistenceAdapter::toDomain);
    }

    @Override
    public List<LearningPath> findAll() {
        return jpaRepository.findAll().stream()
            .map(LearningPathPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<LearningPath> findByStatus(ContentStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
            .map(LearningPathPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public LearningPath save(LearningPath learningPath) {
        var saved = jpaRepository.save(toJpa(learningPath));
        return toDomain(saved);
    }

    private static LearningPath toDomain(LearningPathJpaEntity source) {
        var target = new LearningPath();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setLocale(source.getLocale());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static LearningPathJpaEntity toJpa(LearningPath source) {
        var target = new LearningPathJpaEntity();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setLocale(source.getLocale());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
