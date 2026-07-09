package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.AccessibleColor;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccessibleColorPersistenceAdapter implements AccessibleColorRepository {

    private final AccessibleColorJpaRepository jpaRepository;

    public AccessibleColorPersistenceAdapter(AccessibleColorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AccessibleColor> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AccessibleColor> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<AccessibleColor> findByStatus(String status) {
        return jpaRepository.findByStatus(status).stream().map(this::toDomain).toList();
    }

    @Override
    public AccessibleColor save(AccessibleColor accessibleColor) {
        return toDomain(jpaRepository.save(toJpa(accessibleColor)));
    }

    @Override
    public boolean existsByConceptualIdentity(String conceptualIdentity) {
        return jpaRepository.existsByConceptualIdentity(conceptualIdentity);
    }

    private AccessibleColor toDomain(AccessibleColorJpaEntity source) {
        var target = new AccessibleColor();
        target.setId(source.getId());
        target.setConceptualIdentity(source.getConceptualIdentity());
        target.setLabelKey(source.getLabelKey());
        target.setShapeIcon(source.getShapeIcon());
        target.setSymbol(source.getSymbol());
        target.setStatus(ContentStatus.valueOf(source.getStatus()));
        target.setSortOrder(source.getSortOrder());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private AccessibleColorJpaEntity toJpa(AccessibleColor source) {
        var target = new AccessibleColorJpaEntity();
        target.setId(source.getId());
        target.setConceptualIdentity(source.getConceptualIdentity());
        target.setLabelKey(source.getLabelKey());
        target.setShapeIcon(source.getShapeIcon());
        target.setSymbol(source.getSymbol());
        target.setStatus(source.getStatus().name());
        target.setSortOrder(source.getSortOrder());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
