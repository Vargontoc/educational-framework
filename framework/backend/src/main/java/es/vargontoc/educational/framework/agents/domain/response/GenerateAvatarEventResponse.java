package es.vargontoc.educational.framework.agents.domain.response;

import java.util.List;

import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;

public record GenerateAvatarEventResponse(AvatarEventType type, List<String> phrases) {}
