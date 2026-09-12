package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorldHostPayloadContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize_withSequenceOrder_fieldIsPopulated() throws Exception {
        String json = """
            {
              "id": 1,
              "code": "MEADOW_DOG",
              "displayName": "Dog",
              "visualAssetKey": "host_dog_meadow",
              "worldWidth": 2560,
              "sequenceOrder": 1
            }
            """;

        WorldHostPayload payload = objectMapper.readValue(json, WorldHostPayload.class);

        assertNotNull(payload);
        assertEquals(1L, payload.id());
        assertEquals("MEADOW_DOG", payload.code());
        assertEquals("Dog", payload.displayName());
        assertEquals("host_dog_meadow", payload.visualAssetKey());
        assertEquals(2560, payload.worldWidth());
        assertEquals(1, payload.sequenceOrder());
    }

    @Test
    void deserialize_withoutSequenceOrder_fieldIsNull() throws Exception {
        String json = """
            {
              "id": 1,
              "code": "MEADOW_DOG",
              "displayName": "Dog",
              "visualAssetKey": "host_dog_meadow",
              "worldWidth": 2560
            }
            """;

        WorldHostPayload payload = objectMapper.readValue(json, WorldHostPayload.class);

        assertNotNull(payload);
        assertEquals(1L, payload.id());
        assertEquals("MEADOW_DOG", payload.code());
        assertNull(payload.sequenceOrder());
    }

    @Test
    void deserialize_withNullSequenceOrder_fieldIsNull() throws Exception {
        String json = """
            {
              "id": 1,
              "code": "MEADOW_DOG",
              "displayName": "Dog",
              "visualAssetKey": null,
              "worldWidth": null,
              "sequenceOrder": null
            }
            """;

        WorldHostPayload payload = objectMapper.readValue(json, WorldHostPayload.class);

        assertNotNull(payload);
        assertNull(payload.sequenceOrder());
    }
}
