package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.CuriosityViewed;

import java.util.List;

public interface GetViewedCuriositiesUseCase {

    List<CuriosityViewed> getViewedCuriosities(Long childProfileId, Long topicId);

    List<CuriosityViewed> getViewedCuriosities(Long childProfileId, Long topicId, int cycleNumber);
}
