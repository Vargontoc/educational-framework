package es.vargontoc.educational.framework.avatar.ports.in;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.AvatarEventRequest;
import es.vargontoc.educational.framework.avatar.model.AvatarEventResult;

public interface AvatarUseCase {

    AvatarEventResult processAvatarEvent(AvatarEventRequest request);
}