package es.vargontoc.educational.framework.content.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import es.vargontoc.educational.framework.content.model.StoryCatalog;
import es.vargontoc.educational.framework.content.model.StoryCatalogEntry;
import es.vargontoc.educational.framework.content.ports.out.StoryCatalogPort;
import es.vargontoc.educational.framework.shared.exception.AppException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;

@Service
public class StoryCatalogService {
    
    private static final List<StoryCatalog> catalog = new ArrayList<>();
    private static final Map<String, StoryCatalogEntry> entries = new HashMap<>();

    private final StoryCatalogPort port;

    public StoryCatalogService(StoryCatalogPort filesystem){
        port = filesystem;
        if(catalog.isEmpty()){
            filesystem.loadCatalog().forEach(f -> catalog.add(f));
        }
    }

    public List<StoryCatalog> listCatalog() { return catalog; }

    public StoryCatalogEntry findById(String id) { 
        var found = catalog.stream().filter(x -> x.id().equals(id)).findFirst().orElseThrow(() -> {
            throw new ResourceNotFoundException("No se encontro un cuento con el identificador " + id + " en el catalogo");
        }); 

        try {
            if(entries.containsKey(found.id())) return entries.get(found.id());
            var entry = port.loadStory(found.id());
            entries.put(found.id(), entry);

            return entry;
        }catch(Exception e){
            throw new AppException("Algo fue mal en la carga del cuento", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public InputStreamResource loadResource(String storyId, int page, boolean audio) {
        StoryCatalogEntry entry = findById(storyId);
        if(page < 0) throw new ValidationException("La pagina debe ser un valor numerico superior o igual 0");
        if(page > entry.pages().size()) throw new ResourceNotFoundException("El cuento no contiene la pagina " + page);
        if(audio && page == 0) throw new ValidationException("La portada no contiene pista de audio");

        try {
            
            return audio ? port.loadAudio(storyId, page) : port.loadImage(storyId, page);
        }catch(Exception e)  { throw new AppException("Error en la carga de recurso", HttpStatus.INTERNAL_SERVER_ERROR); }
    }
}
