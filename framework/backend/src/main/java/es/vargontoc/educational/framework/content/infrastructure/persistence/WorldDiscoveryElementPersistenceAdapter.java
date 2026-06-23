package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.InteractionCueType;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import es.vargontoc.educational.framework.content.ports.out.WorldDiscoveryElementRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class WorldDiscoveryElementPersistenceAdapter implements WorldDiscoveryElementRepository {

    private final WorldDiscoveryElementJpaRepository jpaRepository;

    public WorldDiscoveryElementPersistenceAdapter(WorldDiscoveryElementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<WorldDiscoveryElement> findByCode(String code) {
        return jpaRepository.findAll().stream()
                .filter(entity -> entity.getCode().equals(code))
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public WorldDiscoveryElement save(WorldDiscoveryElement worldDiscoveryElement) {
        WorldDiscoveryElementJpaEntity entity = toJpa(worldDiscoveryElement);
        WorldDiscoveryElementJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    private WorldDiscoveryElement toDomain(WorldDiscoveryElementJpaEntity source) {
        WorldDiscoveryElement target = new WorldDiscoveryElement();
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setDisplayName(source.getDisplayName());
        target.setElementType(ElementType.valueOf(source.getElementType()));
        target.setBiome(Biome.valueOf(source.getBiome()));
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        target.setVisualAssetKey(source.getVisualAssetKey());
        target.setInteractionCueType(
                source.getInteractionCueType() != null
                        ? InteractionCueType.valueOf(source.getInteractionCueType())
                        : null);
        target.setSortOrder(source.getSortOrder());
        return target;
    }

    private WorldDiscoveryElementJpaEntity toJpa(WorldDiscoveryElement source) {
        WorldDiscoveryElementJpaEntity target = new WorldDiscoveryElementJpaEntity();
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setDisplayName(source.getDisplayName());
        target.setElementType(source.getElementType().name());
        target.setBiome(source.getBiome().name());
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(source.getStatus().name());
        target.setActivityId(source.getActivityId());
        target.setTopicId(source.getTopicId());
        target.setVisualAssetKey(source.getVisualAssetKey());
        target.setInteractionCueType(
                source.getInteractionCueType() != null
                        ? source.getInteractionCueType().name()
                        : null);
        target.setSortOrder(source.getSortOrder());
        return target;
    }
}
