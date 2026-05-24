package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ActivityResource;
import es.vargontoc.educational.framework.content.model.ResourceType;
import es.vargontoc.educational.framework.content.ports.out.ActivityResourceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ActivityResourcePersistenceAdapter implements ActivityResourceRepository {

    private final ActivityResourceJpaRepository jpaRepository;

    public ActivityResourcePersistenceAdapter(ActivityResourceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ActivityResource> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ActivityResourcePersistenceAdapter::toDomain);
    }

    @Override
    public List<ActivityResource> findByActivityId(Long activityId) {
        return jpaRepository.findByActivityId(activityId).stream()
            .map(ActivityResourcePersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public ActivityResource save(ActivityResource resource) {
        var saved = jpaRepository.save(toJpa(resource));
        return toDomain(saved);
    }

    private static ActivityResource toDomain(ActivityResourceJpaEntity source) {
        var target = new ActivityResource();
        target.setId(source.getId());
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        target.setResourceType(ResourceType.valueOf(source.getResourceType()));
        target.setPath(source.getPath());
        target.setMetadata(source.getMetadata());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static ActivityResourceJpaEntity toJpa(ActivityResource source) {
        var target = new ActivityResourceJpaEntity();
        target.setId(source.getId());
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        target.setResourceType(source.getResourceType().name());
        target.setPath(source.getPath());
        target.setMetadata(source.getMetadata());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
