package es.vargontoc.educational.framework.agents.infrastructure.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import es.vargontoc.educational.framework.agents.infrastructure.dto.AgentRequestDto;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotStreamUseCase;
import es.vargontoc.educational.framework.shared.exception.ValidationException;

@Controller
public class ChatbotStompController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ChatbotStompController.class);   
    private static final String ATTR_FAMILY_ID = "familyId";
    private final ChatbotStreamUseCase streamUseCase;

    public ChatbotStompController(ChatbotStreamUseCase useCase){
        this.streamUseCase = useCase;
    }
    

    @MessageMapping("/chatbot/send")
    public void handleChatbotMessage(AgentRequestDto request, SimpMessageHeaderAccessor accessor){
        try {

            Long familyId = (Long)accessor.getSessionAttributes().get(ATTR_FAMILY_ID);
            if(familyId == null){
                LOG.warn("Attributo 'familyId' no encontrado en sesión STOMP");
                return;
            }

            LOG.info("Mensaje de chatbot recibido de familyId={}", familyId);
            streamUseCase.sendMessage(request.message(), familyId);

        }catch(ValidationException e) {
            LOG.warn("Validación fallida: {}", e.getMessage());
        }catch(Exception e){
            LOG.error("Error procesando mensaje de chatbot: {}", e.getMessage(), e);
        }
    }
}
