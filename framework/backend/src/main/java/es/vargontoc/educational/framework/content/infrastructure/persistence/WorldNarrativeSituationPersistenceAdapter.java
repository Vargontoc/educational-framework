package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.SituationType;
import es.vargontoc.educational.framework.content.model.Tone;
import es.vargontoc.educational.framework.content.model.WorldNarrativeSituation;
import es.vargontoc.educational.framework.content.ports.out.WorldNarrativeSituationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class WorldNarrativeSituationPersistenceAdapter implements WorldNarrativeSituationRepository {

    private final WorldNarrativeSituationJpaRepository jpaRepository;

    public WorldNarrativeSituationPersistenceAdapter(WorldNarrativeSituationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<WorldNarrativeSituation> findByCode(String code) {
        return jpaRepository.findAll().stream()
                .filter(entity -> entity.getCode().equals(code))
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public WorldNarrativeSituation save(WorldNarrativeSituation worldNarrativeSituation) {
        WorldNarrativeSituationJpaEntity entity = toJpa(worldNarrativeSituation);
        WorldNarrativeSituationJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    private WorldNarrativeSituation toDomain(WorldNarrativeSituationJpaEntity source) {
        WorldNarrativeSituation target = new WorldNarrativeSituation();
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setDisplayText(source.getDisplayText());
        target.setSituationType(SituationType.valueOf(source.getSituationType()));
        target.setTone(source.getTone() != null ? Tone.valueOf(source.getTone()) : null);
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setSortOrder(source.getSortOrder());
        return target;
    }

    private WorldNarrativeSituationJpaEntity toJpa(WorldNarrativeSituation source) {
        WorldNarrativeSituationJpaEntity target = new WorldNarrativeSituationJpaEntity();
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setDisplayText(source.getDisplayText());
        target.setSituationType(source.getSituationType().name());
        target.setTone(source.getTone() != null ? source.getTone().name() : null);
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setStatus(source.getStatus().name());
        target.setSortOrder(source.getSortOrder());
        return target;
    }
}
