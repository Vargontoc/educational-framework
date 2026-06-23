package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.InteractionCueType;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldDiscoveryElementPersistenceAdapterTest {

    @Mock
    private WorldDiscoveryElementJpaRepository jpaRepository;

    private WorldDiscoveryElementPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorldDiscoveryElementPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_persistsWorldDiscoveryElement() {
        var element = buildDomain();
        var captor = ArgumentCaptor.forClass(WorldDiscoveryElementJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(element);

        verify(jpaRepository).save(captor.capture());
        assertEquals("MEADOW_SHINY_FLOWER", captor.getValue().getCode());
        assertEquals("DISCOVERY", captor.getValue().getElementType());
        assertEquals("MEADOW", captor.getValue().getBiome());
        assertEquals("BREATHING_GLOW", captor.getValue().getInteractionCueType());
        assertEquals("ACTIVE", captor.getValue().getStatus());
    }

    @Test
    void save_elementWithoutOptionalFields_persistsWithNulls() {
        var element = buildDomainWithoutOptionals();
        var captor = ArgumentCaptor.forClass(WorldDiscoveryElementJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(element);

        verify(jpaRepository).save(captor.capture());
        assertNull(captor.getValue().getActivityId());
        assertNull(captor.getValue().getTopicId());
        assertNull(captor.getValue().getInteractionCueType());
    }

    @Test
    void findByCode_existingCode_returnsWorldDiscoveryElement() {
        when(jpaRepository.findByCode("MEADOW_SHINY_FLOWER")).thenReturn(Optional.of(buildJpaEntity()));

        var result = adapter.findByCode("MEADOW_SHINY_FLOWER");

        assertTrue(result.isPresent());
        assertEquals("MEADOW_SHINY_FLOWER", result.get().getCode());
        assertEquals(ElementType.DISCOVERY, result.get().getElementType());
        assertEquals(Biome.MEADOW, result.get().getBiome());
        assertEquals(InteractionCueType.BREATHING_GLOW, result.get().getInteractionCueType());
        assertNotNull(result.get().getCreatedAt());
    }

    @Test
    void findByCode_nonExistingCode_returnsEmpty() {
        when(jpaRepository.findByCode("NON_EXISTING")).thenReturn(Optional.empty());

        var result = adapter.findByCode("NON_EXISTING");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByCode_existingCode_returnsTrue() {
        when(jpaRepository.existsByCode("MEADOW_SHINY_FLOWER")).thenReturn(true);

        var result = adapter.existsByCode("MEADOW_SHINY_FLOWER");

        assertTrue(result);
    }

    private WorldDiscoveryElement buildDomain() {
        var element = new WorldDiscoveryElement();
        element.setCode("MEADOW_SHINY_FLOWER");
        element.setDisplayName("Shiny Flower");
        element.setElementType(ElementType.DISCOVERY);
        element.setBiome(Biome.MEADOW);
        element.setMinAge(3);
        element.setMaxAge(4);
        element.setStatus(ContentStatus.ACTIVE);
        element.setActivityId(1L);
        element.setTopicId(null);
        element.setVisualAssetKey("element_shiny_flower");
        element.setInteractionCueType(InteractionCueType.BREATHING_GLOW);
        element.setSortOrder(1);
        element.setCreatedAt(LocalDateTime.now());
        return element;
    }

    private WorldDiscoveryElement buildDomainWithoutOptionals() {
        var element = new WorldDiscoveryElement();
        element.setCode("MEADOW_ROCK");
        element.setDisplayName("Meadow Rock");
        element.setElementType(ElementType.DECORATIVE);
        element.setBiome(Biome.MEADOW);
        element.setMinAge(3);
        element.setMaxAge(4);
        element.setStatus(ContentStatus.ACTIVE);
        element.setActivityId(null);
        element.setTopicId(null);
        element.setVisualAssetKey(null);
        element.setInteractionCueType(null);
        element.setSortOrder(1);
        element.setCreatedAt(LocalDateTime.now());
        return element;
    }

    private WorldDiscoveryElementJpaEntity buildJpaEntity() {
        var entity = new WorldDiscoveryElementJpaEntity();
        entity.setId(1L);
        entity.setCode("MEADOW_SHINY_FLOWER");
        entity.setDisplayName("Shiny Flower");
        entity.setElementType("DISCOVERY");
        entity.setBiome("MEADOW");
        entity.setMinAge(3);
        entity.setMaxAge(4);
        entity.setStatus("ACTIVE");
        entity.setActivityId(1L);
        entity.setTopicId(null);
        entity.setVisualAssetKey("element_shiny_flower");
        entity.setInteractionCueType("BREATHING_GLOW");
        entity.setSortOrder(1);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
