package es.vargontoc.educational.framework.agents.ports.in;

import es.vargontoc.educational.framework.shared.exception.ValidationException;

public interface SendMessageChatbotUseCase {
    
    String sendMessage(String message) throws ValidationException;
}
