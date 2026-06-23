package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldHost;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorldHostPersistenceAdapterTest {

    @Mock
    private WorldHostJpaRepository jpaRepository;

    private WorldHostPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorldHostPersistenceAdapter(jpaRepository);
    }

    @Test
    void save_persistsWorldHost() {
        var worldHost = buildDomain();
        var captor = ArgumentCaptor.forClass(WorldHostJpaEntity.class);
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.save(worldHost);

        verify(jpaRepository).save(captor.capture());
        assertEquals("MEADOW_DOG", captor.getValue().getCode());
        assertEquals("Dog", captor.getValue().getDisplayName());
        assertEquals("MEADOW", captor.getValue().getBiome());
        assertEquals("ACTIVE", captor.getValue().getStatus());
    }

    @Test
    void findByCode_existingCode_returnsWorldHost() {
        when(jpaRepository.findByCode("MEADOW_DOG")).thenReturn(Optional.of(buildJpaEntity()));

        var result = adapter.findByCode("MEADOW_DOG");

        assertTrue(result.isPresent());
        assertEquals("MEADOW_DOG", result.get().getCode());
        assertEquals(Biome.MEADOW, result.get().getBiome());
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
        when(jpaRepository.existsByCode("MEADOW_DOG")).thenReturn(true);

        var result = adapter.existsByCode("MEADOW_DOG");

        assertTrue(result);
    }

    private WorldHost buildDomain() {
        var worldHost = new WorldHost();
        worldHost.setCode("MEADOW_DOG");
        worldHost.setDisplayName("Dog");
        worldHost.setBiome(Biome.MEADOW);
        worldHost.setDescription("A friendly dog");
        worldHost.setMinAge(3);
        worldHost.setMaxAge(4);
        worldHost.setStatus(ContentStatus.ACTIVE);
        worldHost.setSortOrder(1);
        worldHost.setVisualAssetKey("host_dog_meadow");
        worldHost.setCreatedAt(LocalDateTime.now());
        return worldHost;
    }

    private WorldHostJpaEntity buildJpaEntity() {
        var entity = new WorldHostJpaEntity();
        entity.setId(1L);
        entity.setCode("MEADOW_DOG");
        entity.setDisplayName("Dog");
        entity.setBiome("MEADOW");
        entity.setDescription("A friendly dog");
        entity.setMinAge(3);
        entity.setMaxAge(4);
        entity.setStatus("ACTIVE");
        entity.setSortOrder(1);
        entity.setVisualAssetKey("host_dog_meadow");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
