package es.vargontoc.educational.framework.avatar.ports.in;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.NarrateStorytellerRequest;
import es.vargontoc.educational.framework.avatar.model.StorytellerResult;

public interface NarrateStorytellerUseCase {
    StorytellerResult narrate(NarrateStorytellerRequest request);
}