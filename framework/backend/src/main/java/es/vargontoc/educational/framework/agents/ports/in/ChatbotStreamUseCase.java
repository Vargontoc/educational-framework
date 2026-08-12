package es.vargontoc.educational.framework.agents.ports.in;

import es.vargontoc.educational.framework.shared.exception.ValidationException;

public interface ChatbotStreamUseCase {
    
    void sendMessage(String message, Long familyId) throws ValidationException;
}
