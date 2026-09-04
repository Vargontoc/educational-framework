package es.vargontoc.educational.framework.content.infrastructure.filesystem;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.content.model.StoryCatalog;
import es.vargontoc.educational.framework.content.model.StoryCatalogEntry;
import es.vargontoc.educational.framework.content.model.StoryPageEntry;
import es.vargontoc.educational.framework.content.ports.out.StoryCatalogPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StoryCatalogFilesystemAdapter implements StoryCatalogPort {

    
    @Value("classpath:/stories")
    private Resource storiesPath;

    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public List<StoryCatalog> loadCatalog() {
        List<StoryCatalog> catalog = new ArrayList<>();
        try {
            Path basePath = Path.of(storiesPath.getURI());
            if(Files.exists(basePath)) {
                Files.list(basePath).iterator().forEachRemaining(d -> {
                    if(Files.isDirectory(d)){
                        try {
                            readDirectory(catalog, d);
                        }catch(IOException e) { log.error("No se pudo leer el directorio {}", d, e.getMessage(), e);}
                    }
                });
            }

        }catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        
        return catalog;
    }

    private void readDirectory(List<StoryCatalog> catalog, Path directory) throws IOException {
        String id = directory.getFileName().toString();
        List<String> filenames = new ArrayList<>();

        Files.list(directory).iterator().forEachRemaining(d -> {
                    if(!Files.isDirectory(d)){
                        filenames.add(d.getFileName().toString());
                    }
                });

        if(!filenames.contains("story.json") || !filenames.contains("cover.png")) {
            log.warn("Directorio '{}' no contiene story.json o cover.png");
            return;
        }

        byte[] bytes =  Files.readAllBytes(directory.resolve("story.json"));
        
        StoryCatalogEntry entry = mapper.readValue(bytes, StoryCatalogEntry.class);
        if(entry.title() != null && !entry.title().isBlank() && entry.pages() != null && !entry.pages().isEmpty()){

            for (StoryPageEntry pageEntry : entry.pages()) {
                if(pageEntry.text() == null || pageEntry.text().isBlank()){
                    log.warn("El texto de la pagina {} esta vacio", pageEntry.page());
                    return;
                }

                if(!filenames.contains(String.format("page_%d.png", pageEntry.page()))
                    ||  !filenames.contains(String.format("page_%d.mp3", pageEntry.page())))
                {
                    log.warn("No existen la imagen o el audio de la página {}", pageEntry.page());
                    return;
                }
            }


            catalog.add(new StoryCatalog(id, entry.title()));
        }

    }

    @Override
    public StoryCatalogEntry loadStory(String directory) throws IOException {
    
        Path path = Path.of(storiesPath.getURI()).resolve(directory).resolve("story.json");
        byte[] bytes =  Files.readAllBytes(path);
    
        return mapper.readValue(bytes, StoryCatalogEntry.class);
    }

    @Override
    public InputStreamResource loadImage(String directory, int page) throws IOException {
        Path path = Path.of(storiesPath.getURI()).resolve(directory).resolve(page == 0 ? "cover.png" : String.format("page_%d.png", page));
        return new InputStreamResource(Files.newInputStream(path));
    }

    @Override
    public InputStreamResource loadAudio(String directory, int page) throws IOException {
        Path path = Path.of(storiesPath.getURI()).resolve(directory).resolve(String.format("page_%d.mp3", page));
        return new InputStreamResource(Files.newInputStream(path));
    }

    
    
}
