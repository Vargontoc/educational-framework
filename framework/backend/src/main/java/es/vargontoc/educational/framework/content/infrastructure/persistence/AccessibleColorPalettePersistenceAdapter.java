package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.AccessibleColorPalette;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorPaletteRepository;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccessibleColorPalettePersistenceAdapter implements AccessibleColorPaletteRepository {

    private final AccessibleColorPaletteJpaRepository jpaRepository;

    public AccessibleColorPalettePersistenceAdapter(AccessibleColorPaletteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AccessibleColorPalette> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AccessibleColorPalette> findByAccessibleColorId(Long accessibleColorId) {
        return jpaRepository.findByAccessibleColorId(accessibleColorId).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public List<AccessibleColorPalette> findByColorVisionMode(ColorVisionMode colorVisionMode) {
        return jpaRepository.findByColorVisionMode(colorVisionMode).stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public AccessibleColorPalette save(AccessibleColorPalette palette) {
        return toDomain(jpaRepository.save(toJpa(palette)));
    }

    private AccessibleColorPalette toDomain(AccessibleColorPaletteJpaEntity source) {
        var target = new AccessibleColorPalette();
        target.setId(source.getId());
        target.setAccessibleColorId(source.getAccessibleColorId());
        target.setColorVisionMode(source.getColorVisionMode());
        target.setAccessibleColorValue(source.getAccessibleColorValue());
        target.setAccessibleLabelKey(source.getAccessibleLabelKey());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private AccessibleColorPaletteJpaEntity toJpa(AccessibleColorPalette source) {
        var target = new AccessibleColorPaletteJpaEntity();
        target.setId(source.getId());
        target.setAccessibleColorId(source.getAccessibleColorId());
        target.setColorVisionMode(source.getColorVisionMode());
        target.setAccessibleColorValue(source.getAccessibleColorValue());
        target.setAccessibleLabelKey(source.getAccessibleLabelKey());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
