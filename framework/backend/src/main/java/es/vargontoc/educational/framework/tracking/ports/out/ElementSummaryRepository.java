package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ElementSummary;

import java.util.List;
import java.util.Optional;

public interface ElementSummaryRepository {

    Optional<ElementSummary> findByChildProfileIdAndElementId(Long childProfileId, Long elementId);

    List<ElementSummary> findByChildProfileId(Long childProfileId);

    ElementSummary save(ElementSummary summary);
}
