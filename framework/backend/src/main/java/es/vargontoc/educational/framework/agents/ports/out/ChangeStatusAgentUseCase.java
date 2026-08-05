package es.vargontoc.educational.framework.agents.ports.out;

import org.springframework.ai.chat.client.ChatClient;

public interface ChangeStatusAgentUseCase {
    
    void run(ChatClient client, boolean stream, Integer time);

    void stop(ChatClient client);
}
