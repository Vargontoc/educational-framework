package es.vargontoc.educational.framework.agents.application;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.vargontoc.educational.framework.agents.utils.AgentsConstants;

@Configuration
public class AgentsApplication {
    
    @Bean(name = "chatbot-agent")
    public ChatClient chatbotAgent(ChatClient.Builder builder) {

        return builder
        .defaultOptions(OllamaOptions.builder().model(AgentsConstants.CHATBOT_MODEL).build())
        .build();
    }

    @Bean(name = "npc-agent")
    public ChatClient npcAgent(ChatClient.Builder builder) {

        return builder
        .defaultOptions(OllamaOptions.builder().model(AgentsConstants.NPC_MODEL).build())
        .build();
    }
}
