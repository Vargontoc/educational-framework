package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.AccessibleColorPalette;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;

import java.util.List;
import java.util.Optional;

public interface AccessibleColorPaletteRepository {

    Optional<AccessibleColorPalette> findById(Long id);

    List<AccessibleColorPalette> findByAccessibleColorId(Long accessibleColorId);

    List<AccessibleColorPalette> findByColorVisionMode(ColorVisionMode colorVisionMode);

    AccessibleColorPalette save(AccessibleColorPalette palette);
}
