package es.vargontoc.educational.framework.agents.infrastructure.adapters;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.agents.application.ports.out.ResourceLoaderPort;

@Component
public class ResourceLoaderAdapter implements ResourceLoaderPort {

    @Value("classpath:files/manual_usuario.pdf") 
    Resource resource;

    private final JdbcTemplate jdbcTemplate;

    public ResourceLoaderAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void loadResourcesForChatbot(VectorStore vector) {

        // "content_generated" es la tabla real configurada en VectorsConfiguration para este vector
        // store; "vector_store" es el nombre por defecto de PgVectorStore (de antes de nombrar la
        // tabla explicitamente) y ya no existe, así que la comprobación de "ya cargado" nunca
        // funcionaba.
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM content_generated", Integer.class);
        if (count != null && count > 0) {
            return;
        }

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
        List<Document> raw = pdfReader.read();

        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(100)
            .withMaxNumChunks(400)
            .build();
        List<Document> splitDocs = splitter.split(raw);

        vector.accept(splitDocs);
    }
    
}
