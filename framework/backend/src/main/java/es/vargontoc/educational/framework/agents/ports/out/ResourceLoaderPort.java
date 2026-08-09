package es.vargontoc.educational.framework.agents.ports.out;

import org.springframework.ai.vectorstore.VectorStore;

public interface ResourceLoaderPort {
    
    void loadResourcesForChatbot(VectorStore vector);
}
