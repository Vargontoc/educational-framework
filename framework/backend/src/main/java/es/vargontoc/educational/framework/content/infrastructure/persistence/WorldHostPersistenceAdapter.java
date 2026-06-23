package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldHost;
import es.vargontoc.educational.framework.content.ports.out.WorldHostRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class WorldHostPersistenceAdapter implements WorldHostRepository {

    private final WorldHostJpaRepository jpaRepository;

    public WorldHostPersistenceAdapter(WorldHostJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<WorldHost> findByCode(String code) {
        return jpaRepository.findAll().stream()
                .filter(entity -> entity.getCode().equals(code))
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public WorldHost save(WorldHost worldHost) {
        WorldHostJpaEntity entity = toJpa(worldHost);
        WorldHostJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    private WorldHost toDomain(WorldHostJpaEntity source) {
        WorldHost target = new WorldHost();
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setDisplayName(source.getDisplayName());
        target.setBiome(Biome.valueOf(source.getBiome()));
        target.setDescription(source.getDescription());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setSortOrder(source.getSortOrder());
        target.setVisualAssetKey(source.getVisualAssetKey());
        return target;
    }

    private WorldHostJpaEntity toJpa(WorldHost source) {
        WorldHostJpaEntity target = new WorldHostJpaEntity();
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setDisplayName(source.getDisplayName());
        target.setBiome(source.getBiome().name());
        target.setDescription(source.getDescription());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(source.getStatus().name());
        target.setSortOrder(source.getSortOrder());
        target.setVisualAssetKey(source.getVisualAssetKey());
        return target;
    }
}
