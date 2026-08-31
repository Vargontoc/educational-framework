package es.vargontoc.educational.framework.content.ports.out;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;

import es.vargontoc.educational.framework.content.model.StoryCatalog;
import es.vargontoc.educational.framework.content.model.StoryCatalogEntry;

public interface StoryCatalogPort {

    List<StoryCatalog> loadCatalog();

    StoryCatalogEntry loadStory(String directory) throws IOException;

    InputStreamResource loadImage(String directory, int page) throws IOException;

    InputStreamResource loadAudio(String directory, int page) throws IOException;
}
