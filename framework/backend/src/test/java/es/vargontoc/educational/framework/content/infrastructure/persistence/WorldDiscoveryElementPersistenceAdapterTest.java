package es.vargontoc.educational.framework.content.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.ports.out.ActivityRepository;

class WorldDiscoveryElementPersistenceAdapterTest {

    private static final String JSON = """
        [
          {
            "code": "DAISY",
            "displayName": "Daisy",
            "elementType": "SIMPLE_INTERACTIVE",
            "biome": "MEADOW",
            "minAge": 3,
            "maxAge": 4,
            "status": "ACTIVE",
            "activityName": null,
            "topicId": null,
            "visualAssetKey": "daisy",
            "interactionCueType": null,
            "sortOrder": 0,
            "positionX": 0.15,
            "positionY": 0.6
          }
        ]
        """;

    private WorldDiscoveryElementPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        clearLoadedElements();
        adapter = new WorldDiscoveryElementPersistenceAdapter(mock(ActivityRepository.class));
    }

    @AfterEach
    void tearDown() {
        clearLoadedElements();
    }

    @Test
    void loadOnMemory_withNonFileResource_loadsElements() {
        // En produccion el seed esta dentro del jar: el Resource no es un fichero del sistema.
        // ByteArrayResource reproduce ese caso (getFile/getFilePath lanzarian FileNotFoundException).
        Resource inJar = new ByteArrayResource(JSON.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(adapter, "jsonFile", inJar);

        adapter.loadOnMemory();

        var elements = adapter.findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, Biome.MEADOW, 3);
        assertEquals(1, elements.size());
        assertEquals("DAISY", elements.get(0).getCode());
    }

    @Test
    void loadOnMemory_withMissingResource_doesNotThrowAndLoadsNothing() {
        Resource missing = new org.springframework.core.io.ClassPathResource("seeds/does-not-exist.json");
        ReflectionTestUtils.setField(adapter, "jsonFile", missing);

        adapter.loadOnMemory();

        assertTrue(adapter.findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
            ContentStatus.ACTIVE, 3).isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static void clearLoadedElements() {
        var list = (List<Object>) ReflectionTestUtils.getField(
            WorldDiscoveryElementPersistenceAdapter.class, "interactiveElements");
        if (list != null) {
            list.clear();
        }
    }
}
