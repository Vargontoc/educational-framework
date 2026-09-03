package es.vargontoc.educational.framework.agents.application;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import es.vargontoc.educational.framework.agents.infrastructure.advisors.CommandInterceptorAdvisor;
import es.vargontoc.educational.framework.agents.infrastructure.commands.CommandRouter;
import es.vargontoc.educational.framework.agents.infrastructure.tools.ChatbotTools;
import es.vargontoc.educational.framework.agents.ports.out.ResourceLoaderPort;
import es.vargontoc.educational.framework.agents.utils.AgentsConstants;

@Configuration
public class AgentsConfiguration {

    @Value("classpath:prompts/empty_context.st")
    private Resource emptyContextTemplate;
    @Value("classpath:prompts/context_template.st")
    private Resource contextTemplate;
    @Value("classpath:prompts/system_support_context.st")
    private Resource systemSupport;
    @Bean(name = "chatbot-agent")
    public ChatClient chatbotAgent(ChatClient.Builder builder, ChatbotTools tools, CommandRouter router,  RetrievalAugmentationAdvisor vectorAdvisor) {
        
        return builder
        .defaultTools(tools)
        .defaultAdvisors(List.of(
           new CommandInterceptorAdvisor(router),
            vectorAdvisor,
            new SimpleLoggerAdvisor()
        )).defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.CHATBOT_MODEL))
        .build();
    }

    @Bean(name = "npc-agent")
    public ChatClient npcAgent(ChatClient.Builder builder) {

        return builder
        .defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.NPC_MODEL))
        .build();
    }

    @Bean(name = "support-agent")
    public ChatClient supportAgent(ChatClient.Builder builder) {
        
        return builder
        .defaultSystem(systemSupport)
        .defaultAdvisors(new SimpleLoggerAdvisor())
        .defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.CHATBOT_MODEL))
        .build();
    }


    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vector, ChatClient.Builder chatBuilder,  ResourceLoaderPort loader) {
        loader.loadResourcesForChatbot(vector);
        return RetrievalAugmentationAdvisor.builder()
            .queryTransformers(TranslationQueryTransformer.builder()
            .chatClientBuilder(chatBuilder.clone()
                .defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.CHATBOT_MODEL)))
            .targetLanguage("spanish").build())
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                .vectorStore(vector)
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

    @Bean("list-agents")
    public Map<String, ChatClient> getAgents(@Qualifier("npc-agent") ChatClient agent, @Qualifier("chatbot-agent") ChatClient chatbot) {
        return Map.of(
            AgentsConstants.CHATBOT_MODEL, chatbot,
            AgentsConstants.NPC_MODEL, agent
        );
    }
}
