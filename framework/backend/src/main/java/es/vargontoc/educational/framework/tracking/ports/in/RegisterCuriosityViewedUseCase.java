package es.vargontoc.educational.framework.tracking.ports.in;

public interface RegisterCuriosityViewedUseCase {

    void registerView(Long childProfileId, Long topicId, Long curiosityId, int cycleNumber);
}
