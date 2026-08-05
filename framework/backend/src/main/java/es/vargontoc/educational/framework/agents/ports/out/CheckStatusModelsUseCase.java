package es.vargontoc.educational.framework.agents.ports.out;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;

import es.vargontoc.educational.framework.agents.model.AgentStatus;

public interface CheckStatusModelsUseCase {
    
    AgentStatus checkStatus(ChatClient client);

    List<AgentStatus> checkAllAvailableModels();
}
