package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ElementSummary;

import java.util.List;

public interface ElementProgressPort {

    List<ElementSummary> getElementSummariesForChildInTopic(Long childProfileId, Long topicId);
}
