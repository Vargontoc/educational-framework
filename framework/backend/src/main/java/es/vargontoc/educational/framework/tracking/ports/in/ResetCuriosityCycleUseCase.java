package es.vargontoc.educational.framework.tracking.ports.in;

public interface ResetCuriosityCycleUseCase {

    int resetCycle(Long childProfileId, Long topicId);
}
