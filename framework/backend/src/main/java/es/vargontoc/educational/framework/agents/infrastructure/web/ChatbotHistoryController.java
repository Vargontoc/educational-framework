package es.vargontoc.educational.framework.agents.infrastructure.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.vargontoc.educational.framework.agents.infrastructure.dto.CommandResponse;
import es.vargontoc.educational.framework.agents.infrastructure.dto.ConversationResponse;
import es.vargontoc.educational.framework.agents.infrastructure.dto.MessageResponse;
import es.vargontoc.educational.framework.agents.infrastructure.dto.UpdateConversationTitleDto;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotHistoryUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/agents/conversations")
@Tag(name =  "Chatbot History", description = "Historial de conversaciones del chatbot parental")
public class ChatbotHistoryController {
    
    private final ChatbotHistoryUseCase historyUseCase;

    public ChatbotHistoryController(ChatbotHistoryUseCase historyUseCase) {
        this.historyUseCase = historyUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar conversaciones de la familia autenticada")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> listConversations(@RequestParam(defaultValue = "10") int limit, @RequestAttribute("familyId") Long familyId) {
        List<ChatbotConversation> conversations = historyUseCase.listConversations(familyId, limit);
        List<ConversationResponse> responses = conversations.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PatchMapping
    @Operation(summary = "Actualizar titulo de una conversacion")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateTitle(@Valid @RequestBody UpdateConversationTitleDto request, @RequestAttribute("familyId") Long familyId){
        return ResponseEntity.ok(ApiResponse.ok(toResponse(historyUseCase.updateConversation(request, familyId))));
    }

    @DeleteMapping("/{conversationId}")
    @Operation(summary = "Elimina una conversacion completa")
    public ResponseEntity<ApiResponse<Boolean>> deleteConversation(@PathVariable UUID conversationId, @RequestAttribute("familyId") Long familyId)
    {
        historyUseCase.deleteConversation(conversationId, familyId);
        return ResponseEntity.ok(ApiResponse.ok(Boolean.TRUE));
    }



    @GetMapping("/{conversationId}")
    @Operation(summary = "Obtiener conversación específica con todos los mensajes")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(@PathVariable UUID conversationId, @RequestAttribute("familyId") Long familyId)
    {
        ChatbotConversation conversation = historyUseCase.getConversation(conversationId, familyId).orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada"));
        return ResponseEntity.ok(ApiResponse.ok((toResponse(conversation))));
    }

    @GetMapping("/commands")
    @Operation(summary = "Listar comandos disponibles en el aplicativo")
    public ResponseEntity<ApiResponse<List<CommandResponse>>> getCommands() {
        return ResponseEntity.ok(ApiResponse.ok(historyUseCase.getCommands()));
    }

    private ConversationResponse toResponse(ChatbotConversation conversation)  {
        List<MessageResponse> messages = conversation.getMessages().stream().map(m -> new MessageResponse(m.getRole(), m.getContent(), m.getCreatedAt())).toList();
        return new ConversationResponse(conversation.getConversationId(), conversation.getTitle(), conversation.getStartedAt(), conversation.getLastMessageAt(), messages);
    }
}
