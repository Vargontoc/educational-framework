package es.vargontoc.educational.framework.family.infrastructure.persistence;

import es.vargontoc.educational.framework.family.model.Family;
import es.vargontoc.educational.framework.family.ports.out.FamilyRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FamilyPersistenceAdapter implements FamilyRepository {

    private final FamilyJpaRepository jpaRepository;

    public FamilyPersistenceAdapter(FamilyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Family> findFamily() {
        return jpaRepository.findAll().stream()
            .findFirst()
            .map(FamilyPersistenceAdapter::toDomain);
    }

    @Override
    public boolean exists() {
        return jpaRepository.count() > 0;
    }

    @Override
    public Family save(Family family) {
        var saved = jpaRepository.save(toJpa(family));
        return toDomain(saved);
    }

    private static Family toDomain(FamilyJpaEntity source) {
        var target = new Family();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setPinHash(source.getPinHash());
        target.setAudioGeneralEnabled(source.isAudioGeneralEnabled());
        target.setAudioGeneralVolume(source.getAudioGeneralVolume());
        target.setNpcEnabled(source.isNpcEnabled());
        target.setNpcVoiceEnabled(source.isNpcVoiceEnabled());
        target.setNpcVoiceVolume(source.getNpcVoiceVolume());
        target.setNarrativeVoiceEnabled(source.isNarrativeVoiceEnabled());
        target.setNarrativeVoiceVolume(source.getNarrativeVoiceVolume());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static FamilyJpaEntity toJpa(Family source) {
        var target = new FamilyJpaEntity();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setPinHash(source.getPinHash());
        target.setAudioGeneralEnabled(source.isAudioGeneralEnabled());
        target.setAudioGeneralVolume(source.getAudioGeneralVolume());
        target.setNpcEnabled(source.isNpcEnabled());
        target.setNpcVoiceEnabled(source.isNpcVoiceEnabled());
        target.setNpcVoiceVolume(source.getNpcVoiceVolume());
        target.setNarrativeVoiceEnabled(source.isNarrativeVoiceEnabled());
        target.setNarrativeVoiceVolume(source.getNarrativeVoiceVolume());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
