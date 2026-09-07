package es.vargontoc.educational.framework.avatar.domain;

import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;

/**
 *
 * AvatarEvent
 */
public record AvatarEvent(AvatarEventType type, AvatarPhrase text) {}
