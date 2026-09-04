package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.NumberUnlockState;

public interface NumberUnlockReadinessUseCase {

    NumberUnlockState evaluateNumberUnlock(Long childProfileId);
}
