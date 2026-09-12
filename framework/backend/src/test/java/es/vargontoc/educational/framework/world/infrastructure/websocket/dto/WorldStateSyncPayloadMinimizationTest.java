package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldStateSyncPayloadMinimizationTest {

    @Test
    void syncPayload_onlyExposesBiomeAndPositionFields_noAdditionalData() {
        Set<String> componentNames = Set.of("status", "destination", "positionX", "positionY");

        RecordComponent[] components = WorldStateSyncPayload.class.getRecordComponents();
        Set<String> actualNames = java.util.Arrays.stream(components)
            .map(RecordComponent::getName)
            .collect(Collectors.toSet());

        assertEquals(componentNames, actualNames,
            "WorldStateSyncPayload must only contain status, destination, positionX, positionY (minimization)");
    }

    @Test
    void syncPayload_positionFields_areNullable() {
        WorldStateSyncPayload payload = new WorldStateSyncPayload("ACTIVE", null, null, null);

        assertTrue(payload.positionX() == null, "positionX must be nullable");
        assertTrue(payload.positionY() == null, "positionY must be nullable");
    }

    @Test
    void syncPayload_backwardCompatibleConstructor_omitsPosition() {
        WorldStateSyncPayload payload = new WorldStateSyncPayload("ACTIVE", null);

        assertTrue(payload.positionX() == null, "backward-compatible constructor must leave positionX null");
        assertTrue(payload.positionY() == null, "backward-compatible constructor must leave positionY null");
    }
}
