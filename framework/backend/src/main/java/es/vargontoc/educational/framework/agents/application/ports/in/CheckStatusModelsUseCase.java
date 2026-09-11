package es.vargontoc.educational.framework.agents.application.ports.in;

import java.util.List;

import es.vargontoc.educational.framework.agents.domain.AgentStatus;

public interface CheckStatusModelsUseCase {

    List<AgentStatus> checkAllAvailableModels();
}
