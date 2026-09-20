package es.vargontoc.educational.framework.game.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalGroupServiceTest {

    private final AnimalGroupService service = AnimalGroupService.fromSeed();

    @Test
    void getAnimalsInSameGroup_bull_returnsCowDeerGoat() {
        assertEquals(List.of("cow", "deer", "goat"), service.getAnimalsInSameGroup("bull"));
    }

    @Test
    void getAnimalsInSameGroup_animalInSeveralGroups_mergesThem() {
        // deer: horns + equine
        List<String> mates = service.getAnimalsInSameGroup("deer");

        assertEquals(Set.of("bull", "cow", "goat", "donkey", "horse"), Set.copyOf(mates));
        assertFalse(mates.contains("deer"));
    }

    @Test
    void getAnimalsInSameGroup_otherSeedGroups() {
        assertEquals(Set.of("goose", "chicken"), Set.copyOf(service.getAnimalsInSameGroup("duck")));
        assertEquals(List.of("goat"), service.getAnimalsInSameGroup("sheep"));
    }

    @Test
    void getAnimalsInSameGroup_animalWithoutGroupOrUnknown_isEmpty() {
        assertTrue(service.getAnimalsInSameGroup("bee").isEmpty());
        assertTrue(service.getAnimalsInSameGroup("dragon").isEmpty());
        assertTrue(service.getAnimalsInSameGroup(null).isEmpty());
    }

    @Test
    void getAnimalsByBiome_farm_returnsOnlyFarmAnimals() {
        List<String> farm = service.getAnimalsByBiome("FARM");

        assertTrue(farm.containsAll(List.of("bull", "cow", "cat", "pig", "sheep", "dog", "duck")));
        assertFalse(farm.contains("bee"));
        assertFalse(farm.contains("deer"));
        assertFalse(farm.contains("frog"));
    }

    @Test
    void getAnimalsByBiome_animalInSeveralBiomes_appearsInEach() {
        assertTrue(service.getAnimalsByBiome("MEADOW").contains("bull"));
        assertTrue(service.getAnimalsByBiome("FARM").contains("bull"));
    }

    @Test
    void getAnimalsByBiome_isCaseInsensitive_andUnknownBiomeIsEmpty() {
        assertEquals(service.getAnimalsByBiome("FARM"), service.getAnimalsByBiome("farm"));
        assertTrue(service.getAnimalsByBiome("SPACE").isEmpty());
        assertTrue(service.getAnimalsByBiome(null).isEmpty());
        assertTrue(service.getAnimalsByBiome(" ").isEmpty());
    }

    @Test
    void filterByBiome_keepsOrderAndDropsOtherBiomesAndUnknownCodes() {
        List<String> result = service.filterByBiome(List.of("frog", "cow", "bee", "pig", "dragon"), "FARM");

        assertEquals(List.of("cow", "pig"), result);
    }

    @Test
    void seedAnimalsAreAllCoveredByMeadowOrFarm() {
        Set<String> covered = new java.util.HashSet<>(service.getAnimalsByBiome("MEADOW"));
        covered.addAll(service.getAnimalsByBiome("FARM"));

        assertEquals(22, covered.size());
    }

    @Test
    void missingSeed_givesEmptyServiceInsteadOfFailing() {
        AnimalGroupService empty = AnimalGroupService.fromSeed("/seeds/does-not-exist.json");

        assertTrue(empty.getAnimalsByBiome("FARM").isEmpty());
        assertFalse(empty.isKnown("bull"));
    }
}
