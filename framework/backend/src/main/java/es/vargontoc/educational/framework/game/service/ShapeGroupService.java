package es.vargontoc.educational.framework.game.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * In-memory catalogue of the {@code similarityGroup} metadata of the shape recognition elements.
 * <p>
 * The metadata is static, so it is loaded once from the shapes seed ({@value #DEFAULT_SEED_RESOURCE}) and
 * cached. Shapes are identified by their element {@code code} (e.g. {@code "circle"}). A shape may belong to
 * several groups.
 */
public class ShapeGroupService {

    public static final String DEFAULT_SEED_RESOURCE = "/seeds/23-recognition-elements-shapes.json";

    private static final Logger log = LoggerFactory.getLogger(ShapeGroupService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Map<String, ShapeMetadata> shapesByCode = new LinkedHashMap<>();

    public record ShapeMetadata(String code, Set<String> groups){

        public ShapeMetadata{
            groups = groups == null ? Set.of() : Set.copyOf(groups);
        }
    }

    public ShapeGroupService(Collection<ShapeMetadata> shapes) {
        shapes.forEach(e -> {
            shapesByCode.put(e.code(), e);
        });
    }

    /**
     * Loads the metadata from the shapes seed on the classpath. If the seed cannot be read the service is
     * empty, which makes the distractor selection degrade to random instead of failing the game.
     */
    public static ShapeGroupService fromSeed() {
        return fromSeed(DEFAULT_SEED_RESOURCE);
    }

    public static ShapeGroupService fromSeed(String resource) {
        try(InputStream in = ShapeGroupService.class.getResourceAsStream(resource)){
            if(in == null){
                log.error("Shape seed not found on the classpath: {}", resource);
                return new ShapeGroupService(List.of());
            }
            return new ShapeGroupService(parse(OBJECT_MAPPER.readTree(in)));
        }catch(IOException | JacksonException e) {
            log.error("Failed to load shape metadata from {}: {}", resource, e.getMessage(), e);
            return new ShapeGroupService(List.of());
        }
    }

    static List<ShapeMetadata> parse(JsonNode root){
        List<ShapeMetadata> shapes = new ArrayList<>();
        if(root == null || !root.isArray())
            return shapes;

        for(JsonNode node : root){
            JsonNode code = node.get("code");
            if(code == null || code.isNull())
                continue;
            // The seed's per-element field is "similarityGroup" (an array), not "group".
            shapes.add(new ShapeMetadata(code.asString(), textValues(node.get("similarityGroup"))));
        }
        return shapes;
    }

    static Set<String> textValues(JsonNode array){
        Set<String> values = new LinkedHashSet<>();
        if(array != null && array.isArray()){
            for(JsonNode value : array){
                if(!value.isNull())
                    values.add(value.asString());
            }
        }
        return values;
    }

    public boolean isKnown(String code) {
        return code != null && shapesByCode.containsKey(code);
    }

    public List<String> getShapesInSameGroup(String targetCode) {
        ShapeMetadata target = shapesByCode.get(targetCode);
        if(target == null || target.groups().isEmpty())
            return List.of();
        List<String> result = new ArrayList<>();
        for(ShapeMetadata shape : shapesByCode.values()){
            if(!shape.code().equals(targetCode) && shareGroup(target, shape))
                result.add(shape.code());
        }
        return result;
    }

    static boolean shareGroup(ShapeMetadata a, ShapeMetadata b){
        for(String g : a.groups()){
            if(b.groups().contains(g))
                return true;
        }
        return false;
    }
}
