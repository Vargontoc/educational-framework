package es.vargontoc.educational.framework.agents.application.ports.in;

import java.util.List;

import es.vargontoc.educational.framework.agents.domain.request.GenerateGameAvatarRequest;
import es.vargontoc.educational.framework.content.model.AvatarEventCatalog;

public interface ContentGenerationUseCase {
    
    List<AvatarEventCatalog> generateAvatarEvents(GenerateGameAvatarRequest request);
}
