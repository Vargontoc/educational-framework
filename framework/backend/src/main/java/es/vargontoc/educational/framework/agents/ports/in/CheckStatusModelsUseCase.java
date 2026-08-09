package es.vargontoc.educational.framework.agents.ports.in;

import java.util.List;

import es.vargontoc.educational.framework.agents.model.AgentStatus;

public interface CheckStatusModelsUseCase {

    List<AgentStatus> checkAllAvailableModels();
}
