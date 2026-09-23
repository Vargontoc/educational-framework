package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.content.infrastructure.seed.SeedData.WorldDiscoveryElementSeed;
import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.WorldDiscoveryElement;
import es.vargontoc.educational.framework.content.ports.out.WorldDiscoveryElementRepository;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class WorldDiscoveryElementPersistenceAdapter implements WorldDiscoveryElementRepository {



    @Value("classpath:/seeds/14-world-discovery-elements.json")
    private Resource jsonFile;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final  List<WorldElement> interactiveElements = new ArrayList<>();
    
    
    @PostConstruct
    private void load() throws IOException
    {
        if(!interactiveElements.isEmpty())
            return;

        log.info("Loading World Discovery Elements");
        try {
            Path path =  jsonFile.getFilePath();
            if(!Files.exists(path))
                return;
            
            byte[] data =  Files.readAllBytes(path);
            var items = mapper.readValue(data, new TypeReference<List<WorldDiscoveryElementSeed>>() {});
            long count = 0L;
            for(WorldDiscoveryElementSeed seed: items) {
                try {
                    Biome biome =  Biome.valueOf(seed.biome());
                    if(biome != null)
                        interactiveElements.add(new WorldElement(++count, biome, seed.code(), seed.visualAssetKey(), seed.activityId(), seed.minAge(), seed.maxAge(), seed.positionX(), seed.positionY()));
    
                }catch(Exception e) {
                    log.error("Error reading seed", e.getMessage(), e);
                }
            }
        }catch(IOException e) {
            log.error("Error loading: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<WorldDiscoveryElement> findByStatusAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Integer targetAge) {
    
        return  interactiveElements.stream().
            filter(x -> targetAge >= x.minAge &&  targetAge <= x.maxAge).map(this::toDomain).toList();
    }

    @Override
    public List<WorldDiscoveryElement> findByStatusAndBiomeAndMinAgeLessThanEqualAndMaxAgeGreaterThanEqual(
        ContentStatus status, Biome biome, Integer targetAge) {
        
        return  interactiveElements.stream().
            filter(x -> x.biome() == biome &&  targetAge >= x.minAge &&  targetAge <= x.maxAge).map(this::toDomain).toList();
    }




    private WorldDiscoveryElement toDomain(WorldElement source) {
        WorldDiscoveryElement target = new WorldDiscoveryElement();
        target.setId(source.id());
        target.setCode(source.code());
        target.setBiome(source.biome());
        target.setMinAge(source.minAge());
        target.setMaxAge(source.maxAge());
        target.setStatus(ContentStatus.ACTIVE);
        target.setActivityId(source.activity());

        target.setVisualAssetKey(source.asset());
        target.setSortOrder(0);
        target.setPositionX(source.posX());
        target.setPositionY(source.posY());
        return target;
    }


    private record WorldElement(Long id, Biome biome, String code, String asset, Long activity, Integer minAge, Integer maxAge, Double posX, Double posY) {}

}
