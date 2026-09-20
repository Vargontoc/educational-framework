package es.vargontoc.educational.framework.game.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * In-memory catalogue of the {@code group} and {@code biome} metadata of the animal recognition elements.
 * <p>
 * The metadata is static, so it is loaded once from the animals seed
 * ({@value #DEFAULT_SEED_RESOURCE}) and cached. Animals are identified by their element {@code code}
 * (e.g. {@code "bull"}). An animal may belong to several groups and several biomes.
 */
public class AnimalGroupService {

    public static final String DEFAULT_SEED_RESOURCE = "/seeds/20-recognition-elements-animals.json";

    private static final Logger log = LoggerFactory.getLogger(AnimalGroupService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** Metadata of one animal. Biomes are stored upper-case, groups as written in the seed. */
    public record AnimalMetadata(String code, Set<String> biomes, Set<String> groups) {

        public AnimalMetadata {
            biomes = biomes == null ? Set.of() : Set.copyOf(upperCase(biomes));
            groups = groups == null ? Set.of() : Set.copyOf(groups);
        }

        private static Set<String> upperCase(Set<String> values) {
            Set<String> result = new LinkedHashSet<>();
            for (String value : values) {
                result.add(value.toUpperCase(Locale.ROOT));
            }
            return result;
        }
    }

    // Seed order is preserved so results are deterministic.
    private final Map<String, AnimalMetadata> animalsByCode = new LinkedHashMap<>();

    public AnimalGroupService(Collection<AnimalMetadata> animals) {
        for (AnimalMetadata animal : animals) {
            animalsByCode.put(animal.code(), animal);
        }
    }

    /**
     * Loads the metadata from the animals seed on the classpath. If the seed cannot be read the service is
     * empty, which makes the distractor selection degrade to random instead of failing the game.
     */
    public static AnimalGroupService fromSeed() {
        return fromSeed(DEFAULT_SEED_RESOURCE);
    }

    public static AnimalGroupService fromSeed(String classpathResource) {
        try (InputStream in = AnimalGroupService.class.getResourceAsStream(classpathResource)) {
            if (in == null) {
                log.error("Animal seed not found on the classpath: {}", classpathResource);
                return new AnimalGroupService(List.of());
            }
            return new AnimalGroupService(parse(OBJECT_MAPPER.readTree(in)));
        } catch (IOException | JacksonException e) {
            log.error("Failed to load animal metadata from {}: {}", classpathResource, e.getMessage());
            return new AnimalGroupService(List.of());
        }
    }

    private static List<AnimalMetadata> parse(JsonNode root) {
        List<AnimalMetadata> animals = new ArrayList<>();
        if (root == null || !root.isArray()) {
            return animals;
        }
        for (JsonNode node : root) {
            JsonNode code = node.get("code");
            if (code == null || code.isNull()) {
                continue;
            }
            animals.add(new AnimalMetadata(code.asString(), textValues(node.get("biome")), textValues(node.get("group"))));
        }
        return animals;
    }

    private static Set<String> textValues(JsonNode array) {
        Set<String> values = new LinkedHashSet<>();
        if (array != null && array.isArray()) {
            for (JsonNode value : array) {
                if (!value.isNull()) {
                    values.add(value.asString());
                }
            }
        }
        return values;
    }

    /** @return true when the code belongs to a known animal. */
    public boolean isKnown(String code) {
        return code != null && animalsByCode.containsKey(code);
    }

    /**
     * @return the codes of the animals that share at least one group with the target, excluding the target
     *         itself. Empty when the target has no groups or is unknown.
     */
    public List<String> getAnimalsInSameGroup(String targetCode) {
        AnimalMetadata target = animalsByCode.get(targetCode);
        if (target == null || target.groups().isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (AnimalMetadata animal : animalsByCode.values()) {
            if (!animal.code().equals(targetCode) && sharesGroup(target, animal)) {
                result.add(animal.code());
            }
        }
        return result;
    }

    /** @return the codes of the animals whose {@code biome} list contains the given biome (case-insensitive). */
    public List<String> getAnimalsByBiome(String biome) {
        if (biome == null || biome.isBlank()) {
            return List.of();
        }
        String normalized = biome.toUpperCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (AnimalMetadata animal : animalsByCode.values()) {
            if (animal.biomes().contains(normalized)) {
                result.add(animal.code());
            }
        }
        return result;
    }

    /**
     * Keeps the candidates that are valid for the biome, preserving their order. Codes that are not known
     * animals are dropped.
     */
    public List<String> filterByBiome(List<String> candidates, String biome) {
        Set<String> valid = Set.copyOf(getAnimalsByBiome(biome));
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (valid.contains(candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private static boolean sharesGroup(AnimalMetadata a, AnimalMetadata b) {
        for (String group : a.groups()) {
            if (b.groups().contains(group)) {
                return true;
            }
        }
        return false;
    }
}
