package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessibleColorPaletteJpaRepository extends JpaRepository<AccessibleColorPaletteJpaEntity, Long> {

    List<AccessibleColorPaletteJpaEntity> findByAccessibleColorId(Long accessibleColorId);

    List<AccessibleColorPaletteJpaEntity> findByColorVisionMode(ColorVisionMode colorVisionMode);
}
