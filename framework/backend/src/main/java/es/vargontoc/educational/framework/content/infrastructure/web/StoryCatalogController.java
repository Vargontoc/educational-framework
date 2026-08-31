package es.vargontoc.educational.framework.content.infrastructure.web;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.vargontoc.educational.framework.content.model.StoryCatalog;
import es.vargontoc.educational.framework.content.model.StoryCatalogEntry;
import es.vargontoc.educational.framework.content.service.StoryCatalogService;


@RestController
@RequestMapping("/api/v1/stories")
public class StoryCatalogController {
    
    private final StoryCatalogService service;
    public StoryCatalogController(StoryCatalogService service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<List<StoryCatalog>> getStories() {
        return ResponseEntity.ok(service.listCatalog());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryCatalogEntry> getStory(@PathVariable("id") String idStory) {
        return ResponseEntity.ok(service.findById(idStory));
    }

    @GetMapping("{id}/cover")
    public ResponseEntity<InputStreamResource> getCover(@PathVariable("id") String idStory) {
        
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(service.loadResource(idStory, 0, false));
    }
    
    @GetMapping("{id}/pages/{page}/image")
    public ResponseEntity<InputStreamResource> getImagePage(@PathVariable("id") String idStory, @PathVariable("page") int page) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(service.loadResource(idStory, page, false));
    }
    
    @GetMapping("{id}/pages/{page}/audio")
    public ResponseEntity<InputStreamResource> getAudioPage(@PathVariable("id") String idStory, @PathVariable("page") int page) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("audio/mpeg")).body(service.loadResource(idStory, page, true));
    }

    
    
}
