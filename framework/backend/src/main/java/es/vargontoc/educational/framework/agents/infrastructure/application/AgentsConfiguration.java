package es.vargontoc.educational.framework.agents.infrastructure.application;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import es.vargontoc.educational.framework.agents.infrastructure.advisors.CommandInterceptorAdvisor;
import es.vargontoc.educational.framework.agents.infrastructure.commands.CommandRouter;
import es.vargontoc.educational.framework.agents.infrastructure.tools.ChatbotTools;
import es.vargontoc.educational.framework.agents.infrastructure.utils.AgentsConstants;

@Configuration
public class AgentsConfiguration {

    @Value("classpath:prompts/empty_context.st")
    private Resource emptyContextTemplate;
    @Value("classpath:prompts/context_template.st")
    private Resource contextTemplate;
    @Value("classpath:prompts/system_support_context.st")
    private Resource systemSupport;




    @Bean(name = "chatbot-agent")
    public ChatClient chatbotAgent(ChatClient.Builder builder, ChatbotTools tools, CommandRouter router, @Qualifier("chatbot-vector") RetrievalAugmentationAdvisor vectorAdvisor) {
        
        return builder
        .defaultTools(tools)
        .defaultAdvisors(List.of(
            new CommandInterceptorAdvisor(router),
            vectorAdvisor,
            new SimpleLoggerAdvisor()
        )).defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.CHATBOT_MODEL))
        .build();
    }

    @Bean(name = "content-agent")
    public ChatClient contentAgent(ChatClient.Builder builder, @Qualifier("content-vector") RetrievalAugmentationAdvisor vectorAdvisor) {
        return builder
                .defaultAdvisors(List.of( new SimpleLoggerAdvisor()))
                .defaultOptions(OllamaChatOptions.builder().model(AgentsConstants.CONTENT_MODEL))
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

    @Bean("list-agents")
    public Map<String, ChatClient> getAgents(@Qualifier("npc-agent") ChatClient agent, @Qualifier("chatbot-agent") ChatClient chatbot) {
        return Map.of(
            AgentsConstants.CHATBOT_MODEL, chatbot,
            AgentsConstants.NPC_MODEL, agent
        );
    }
}
