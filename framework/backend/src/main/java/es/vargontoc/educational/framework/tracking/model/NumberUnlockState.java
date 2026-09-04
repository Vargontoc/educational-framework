package es.vargontoc.educational.framework.tracking.model;

public record NumberUnlockState(
        Long childProfileId,
        boolean unlocked,
        boolean letterMastered,
        boolean shapeMastered) {
}
