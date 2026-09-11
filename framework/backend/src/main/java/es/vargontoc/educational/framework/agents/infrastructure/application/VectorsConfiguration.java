package es.vargontoc.educational.framework.agents.infrastructure.application;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import es.vargontoc.educational.framework.agents.application.ports.out.ResourceLoaderPort;
import es.vargontoc.educational.framework.agents.infrastructure.utils.AgentsConstants;

@Configuration
public class VectorsConfiguration {
    
    @Value("classpath:prompts/empty_context.st")
    private Resource emptyContextTemplate;
    @Value("classpath:prompts/context_template.st")
    private Resource contextTemplate;


    @Bean("content-vector")
    public RetrievalAugmentationAdvisor chatbotVector(ChatClient.Builder builder, ResourceLoaderPort loader, JdbcTemplate template, EmbeddingModel model) {
        VectorStore v = PgVectorStore.builder(template, model)
            .vectorTableName("chatbot_knowledge_base")
            .dimensions(1024)
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .initializeSchema(true)
            .build();
        return RetrievalAugmentationAdvisor.builder()
            .queryTransformers(TranslationQueryTransformer.builder()
            .chatClientBuilder(builder.clone()
                .defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.CONTENT_MODEL)))
            .targetLanguage("spanish").build())
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                .vectorStore(v)
                .topK(3)
                .similarityThreshold(0.5)
                .build())
            .queryAugmenter(ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .promptTemplate(new PromptTemplate(contextTemplate))
                .emptyContextPromptTemplate(new PromptTemplate(emptyContextTemplate))
                .build())
            .build();
    }
    
    @Bean("chatbot-vector")
    public RetrievalAugmentationAdvisor contentVector(ChatClient.Builder builder, ResourceLoaderPort loader, JdbcTemplate template, EmbeddingModel model) {
        
        VectorStore v = PgVectorStore.builder(template, model)
            .vectorTableName("content_generated")
            .dimensions(1024)
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .initializeSchema(true)
            .build();

        loader.loadResourcesForChatbot(v);
        return RetrievalAugmentationAdvisor.builder()
            .queryTransformers(TranslationQueryTransformer.builder()
            .chatClientBuilder(builder.clone()
                .defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.CHATBOT_MODEL)))
            .targetLanguage("spanish").build())
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                .vectorStore(v)
                .topK(3)
                .similarityThreshold(0.5)
                .build())
            .queryAugmenter(ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .promptTemplate(new PromptTemplate(contextTemplate))
                .emptyContextPromptTemplate(new PromptTemplate(emptyContextTemplate))
                .build())
            .build();
    }

    
}
