package es.vargontoc.educational.framework.world.infrastructure.persistence;

import es.vargontoc.educational.framework.world.model.WorldExplorationState;
import es.vargontoc.educational.framework.world.ports.out.WorldExplorationStateRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaWorldExplorationStateRepository implements WorldExplorationStateRepository {

    private final WorldExplorationStateJpaRepository jpaRepository;

    public JpaWorldExplorationStateRepository(WorldExplorationStateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<WorldExplorationState> findByChildProfileId(Long childProfileId) {
        return jpaRepository.findById(childProfileId).map(this::toDomain);
    }

    @Override
    public void save(WorldExplorationState state) {
        WorldExplorationStateJpaEntity entity = toEntity(state);
        jpaRepository.save(entity);
    }

    private WorldExplorationState toDomain(WorldExplorationStateJpaEntity entity) {
        WorldExplorationState domain = new WorldExplorationState();
        domain.setChildProfileId(entity.getChildProfileId());
        domain.setBiome(entity.getBiome());
        domain.setPositionX(entity.getPositionX());
        domain.setPositionY(entity.getPositionY());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    private WorldExplorationStateJpaEntity toEntity(WorldExplorationState domain) {
        WorldExplorationStateJpaEntity entity = new WorldExplorationStateJpaEntity();
        entity.setChildProfileId(domain.getChildProfileId());
        entity.setBiome(domain.getBiome());
        entity.setPositionX(domain.getPositionX());
        entity.setPositionY(domain.getPositionY());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
