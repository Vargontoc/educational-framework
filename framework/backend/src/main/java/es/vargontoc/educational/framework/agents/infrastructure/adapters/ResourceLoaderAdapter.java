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

import es.vargontoc.educational.framework.agents.ports.out.ResourceLoaderPort;

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

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_store", Integer.class);
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
