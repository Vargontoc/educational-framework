package es.vargontoc.educational.framework.avatar.application.ports.in;


import es.vargontoc.educational.framework.avatar.domain.AvatarEventRequest;
import es.vargontoc.educational.framework.avatar.infrastructure.service.AvatarLifecycleService.AvatarLifecycleResult;

public interface AvatarUseCase {
    
    AvatarLifecycleResult  processEvent(AvatarEventRequest request);
}
