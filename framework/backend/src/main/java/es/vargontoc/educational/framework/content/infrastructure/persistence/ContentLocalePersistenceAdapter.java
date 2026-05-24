package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.ContentLocale;
import es.vargontoc.educational.framework.content.model.EntityType;
import es.vargontoc.educational.framework.content.ports.out.ContentLocaleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ContentLocalePersistenceAdapter implements ContentLocaleRepository {

    private final ContentLocaleJpaRepository jpaRepository;

    public ContentLocalePersistenceAdapter(ContentLocaleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ContentLocale> findById(Long id) {
        return jpaRepository.findById(id)
            .map(ContentLocalePersistenceAdapter::toDomain);
    }

    @Override
    public List<ContentLocale> findByEntityTypeAndEntityId(EntityType entityType, Long entityId) {
        return jpaRepository.findByEntityTypeAndEntityId(entityType.name(), entityId).stream()
            .map(ContentLocalePersistenceAdapter::toDomain)
            .toList();
    }

    @Override
    public ContentLocale save(ContentLocale contentLocale) {
        var saved = jpaRepository.save(toJpa(contentLocale));
        return toDomain(saved);
    }

    @Override
    public void deleteByEntityTypeAndEntityIdAndLocaleCode(EntityType entityType, Long entityId, String localeCode) {
        jpaRepository.deleteByEntityTypeAndEntityIdAndLocaleCode(entityType.name(), entityId, localeCode);
    }

    private static ContentLocale toDomain(ContentLocaleJpaEntity source) {
        var target = new ContentLocale();
        target.setId(source.getId());
        target.setEntityType(EntityType.valueOf(source.getEntityType()));
        target.setEntityId(source.getEntityId());
        target.setLocaleCode(source.getLocaleCode());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static ContentLocaleJpaEntity toJpa(ContentLocale source) {
        var target = new ContentLocaleJpaEntity();
        target.setId(source.getId());
        target.setEntityType(source.getEntityType().name());
        target.setEntityId(source.getEntityId());
        target.setLocaleCode(source.getLocaleCode());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
