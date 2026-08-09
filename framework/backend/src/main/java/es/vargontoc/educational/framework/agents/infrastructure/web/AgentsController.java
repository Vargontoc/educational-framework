package es.vargontoc.educational.framework.agents.infrastructure.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.vargontoc.educational.framework.agents.infrastructure.dto.AgentRequestDto;
import es.vargontoc.educational.framework.agents.infrastructure.dto.AgentStatusResponseDto;
import es.vargontoc.educational.framework.agents.ports.in.CheckStatusModelsUseCase;
import es.vargontoc.educational.framework.agents.ports.in.SendMessageChatbotUseCase;
import es.vargontoc.educational.framework.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentsController {

    private final SendMessageChatbotUseCase send;
    private final CheckStatusModelsUseCase checker;
    
    public AgentsController(SendMessageChatbotUseCase sender, CheckStatusModelsUseCase checker) {
        this.send = sender;
        this.checker = checker;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> responseChatbot(@RequestBody AgentRequestDto request){
        return ResponseEntity.ok(ApiResponse.ok(send.sendMessage(request.message())));
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<List<AgentStatusResponseDto>>> checkStatus(){
        List<AgentStatusResponseDto> result = new ArrayList<>();
        checker.checkAllAvailableModels().forEach(m -> result.add(new AgentStatusResponseDto(m.model(), m.status().name())));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
