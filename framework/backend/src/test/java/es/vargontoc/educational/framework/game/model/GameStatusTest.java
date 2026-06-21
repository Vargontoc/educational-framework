package es.vargontoc.educational.framework.game.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStatusTest {

    @Test
    void gameStatus_doesNotIncludePaused() {
        Set<String> statusNames = Arrays.stream(GameStatus.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

        assertFalse(statusNames.contains("PAUSED"),
            "GameStatus should not include PAUSED for age 3-4 compatibility");
    }

    @Test
    void gameStatus_includesAllRequiredStatuses() {
        Set<String> statusNames = Arrays.stream(GameStatus.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

        assertTrue(statusNames.contains("WAITING"));
        assertTrue(statusNames.contains("STARTING"));
        assertTrue(statusNames.contains("IN_PROGRESS"));
        assertTrue(statusNames.contains("COMPLETED"));
        assertTrue(statusNames.contains("ABANDONED"));
    }
}
