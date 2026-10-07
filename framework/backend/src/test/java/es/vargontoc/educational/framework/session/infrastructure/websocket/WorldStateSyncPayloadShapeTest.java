package es.vargontoc.educational.framework.session.infrastructure.websocket;

import tools.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldDestinationPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldDiscoveryElementPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldHostPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldNarrativeSituationPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldStateSyncPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldStateSyncPayloadShapeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void worldStateSyncPayload_preservesContractShape() throws Exception {
        WorldHostPayload host = new WorldHostPayload(1L, "H1", "Host 1", null, 100, 1);
        WorldNarrativeSituationPayload situation = new WorldNarrativeSituationPayload(1L, "S1", "Hello", null);
        WorldDiscoveryElementPayload element = new WorldDiscoveryElementPayload(
            "prop-1", 10L, "E1", "Element 1", "ANIMAL", "asset_key", "GLOW", true, 0.5, 0.3);
        WorldDestinationPayload destination = new WorldDestinationPayload(
            "dest-1", host, situation, "MEADOW", List.of(element));
        WorldStateSyncPayload syncPayload = new WorldStateSyncPayload("ACTIVE", destination);

        Map<String, Object> result = toPayload(syncPayload);

        assertEquals("ACTIVE", result.get("status"));
        assertNotNull(result.get("destination"));

        @SuppressWarnings("unchecked")
        Map<String, Object> dest = (Map<String, Object>) result.get("destination");
        assertEquals("dest-1", dest.get("destinationId"));
        assertEquals("MEADOW", dest.get("biome"));
        assertNotNull(dest.get("host"));
        assertNotNull(dest.get("narrativeSituation"));
        assertNotNull(dest.get("discoveryElements"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elements = (List<Map<String, Object>>) dest.get("discoveryElements");
        assertEquals(1, elements.size());
        Map<String, Object> el = elements.get(0);
        assertEquals("prop-1", el.get("proposalRuntimeId"));
        assertEquals(10L, el.get("discoveryElementId"));
        assertEquals("E1", el.get("code"));
        assertEquals("Element 1", el.get("displayName"));
        assertEquals("ANIMAL", el.get("elementType"));
        assertEquals("asset_key", el.get("visualAssetKey"));
        assertEquals("GLOW", el.get("interactionCueType"));
        assertEquals(true, el.get("hasActivity"));
        assertEquals(0.5, el.get("positionX"));
        assertEquals(0.3, el.get("positionY"));
    }

    @Test
    void worldStateSyncPayload_withoutDestination_preservesStatusOnly() throws Exception {
        WorldStateSyncPayload syncPayload = new WorldStateSyncPayload("INACTIVE_CLOSED", null);

        Map<String, Object> result = toPayload(syncPayload);

        assertEquals("INACTIVE_CLOSED", result.get("status"));
        assertTrue(result.get("destination") == null || !result.containsKey("destination"));
    }

    @Test
    void worldStateSyncPayload_serializesToJsonWithoutError() throws Exception {
        WorldHostPayload host = new WorldHostPayload(1L, "H1", "Host 1", null, 100, 1);
        WorldNarrativeSituationPayload situation = new WorldNarrativeSituationPayload(1L, "S1", "Hello", null);
        WorldDestinationPayload destination = new WorldDestinationPayload(
            "dest-1", host, situation, "MEADOW", List.of());
        WorldStateSyncPayload syncPayload = new WorldStateSyncPayload("ACTIVE", destination);

        Map<String, Object> payload = toPayload(syncPayload);
        String json = objectMapper.writeValueAsString(payload);

        assertNotNull(json);
        assertTrue(json.contains("\"status\":\"ACTIVE\""));
        assertTrue(json.contains("\"destinationId\":\"dest-1\""));
        assertTrue(json.contains("\"biome\":\"MEADOW\""));
    }

    private Map<String, Object> toPayload(WorldStateSyncPayload payload) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", payload.status());
        if (payload.destination() != null) {
            result.put("destination", toPayload(payload.destination()));
        }
        if (payload.positionX() != null) {
            result.put("positionX", payload.positionX());
        }
        if (payload.positionY() != null) {
            result.put("positionY", payload.positionY());
        }
        return result;
    }

    private Map<String, Object> toPayload(WorldDestinationPayload destination) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("destinationId", destination.destinationId());
        result.put("host", toPayload(destination.host()));
        result.put("narrativeSituation", toPayload(destination.narrativeSituation()));
        result.put("biome", destination.biome());
        result.put("discoveryElements", destination.discoveryElements().stream()
            .map(this::toPayload)
            .toList());
        return result;
    }

    private Map<String, Object> toPayload(WorldHostPayload host) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", host.id());
        result.put("code", host.code());
        result.put("displayName", host.displayName());
        if (host.visualAssetKey() != null) {
            result.put("visualAssetKey", host.visualAssetKey());
        }
        if (host.worldWidth() != null) {
            result.put("worldWidth", host.worldWidth());
        }
        if (host.sequenceOrder() != null) {
            result.put("sequenceOrder", host.sequenceOrder());
        }
        return result;
    }

    private Map<String, Object> toPayload(WorldNarrativeSituationPayload situation) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", situation.id());
        result.put("code", situation.code());
        if (situation.displayText() != null) {
            result.put("displayText", situation.displayText());
        }
        if (situation.tone() != null) {
            result.put("tone", situation.tone());
        }
        return result;
    }

    private Map<String, Object> toPayload(WorldDiscoveryElementPayload element) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("proposalRuntimeId", element.proposalRuntimeId());
        result.put("discoveryElementId", element.discoveryElementId());
        result.put("code", element.code());
        result.put("displayName", element.displayName());
        result.put("elementType", element.elementType());
        if (element.visualAssetKey() != null) {
            result.put("visualAssetKey", element.visualAssetKey());
        }
        if (element.interactionCueType() != null) {
            result.put("interactionCueType", element.interactionCueType());
        }
        result.put("hasActivity", element.hasActivity());
        if (element.positionX() != null) {
            result.put("positionX", element.positionX());
        }
        if (element.positionY() != null) {
            result.put("positionY", element.positionY());
        }
        return result;
    }
}
