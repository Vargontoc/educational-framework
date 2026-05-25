package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.AvatarEventCatalogRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class AvatarEventCatalogPersistenceAdapter implements AvatarEventCatalogRepository {

    private final AvatarEventCatalogJpaRepository jpaRepository;

    public AvatarEventCatalogPersistenceAdapter(AvatarEventCatalogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AvatarEventCatalog> findById(Long id) {
        return jpaRepository.findById(id)
            .map(AvatarEventCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public List<AvatarEventCatalog> findAll() {
        return jpaRepository.findAll().stream()
            .map(AvatarEventCatalogPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<AvatarEventCatalog> findByEventType(AvatarEventType eventType) {
        return jpaRepository.findByEventType(eventType.name()).stream()
            .map(AvatarEventCatalogPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public List<AvatarEventCatalog> findActiveByFilters(AvatarEventType eventType, AvatarTone tone, String locale) {
        if (eventType == null || tone == null || locale == null) {
            return Collections.emptyList();
        }
        return jpaRepository.findByStatusAndEventTypeAndToneAndLocale(
                ContentStatus.ACTIVE.name(), eventType.name(), tone.name(), locale).stream()
            .map(AvatarEventCatalogPersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public AvatarEventCatalog save(AvatarEventCatalog event) {
        var saved = jpaRepository.save(toJpa(event));
        return toDomain(saved);
    }

    private static AvatarEventCatalog toDomain(AvatarEventCatalogJpaEntity source) {
        var target = new AvatarEventCatalog();
        target.setId(source.getId());
        target.setEventType(AvatarEventType.valueOf(source.getEventType()));
        target.setTone(AvatarTone.valueOf(source.getTone()));
        target.setLocale(source.getLocale());
        target.setMessageText(source.getMessageText());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static AvatarEventCatalogJpaEntity toJpa(AvatarEventCatalog source) {
        var target = new AvatarEventCatalogJpaEntity();
        target.setId(source.getId());
        target.setEventType(source.getEventType().name());
        target.setTone(source.getTone().name());
        target.setLocale(source.getLocale());
        target.setMessageText(source.getMessageText());
        target.setStatus(source.getStatus().name());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
