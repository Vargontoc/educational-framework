package es.vargontoc.educational.framework.agents.infrastructure.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.vargontoc.educational.framework.agents.infrastructure.dto.AgentStatusResponseDto;
import es.vargontoc.educational.framework.agents.service.AgentsService;
import es.vargontoc.educational.framework.shared.api.ApiResponse;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentsController {

    private final AgentsService service;
    public AgentsController(AgentsService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> responseChatbot(@RequestParam("message") String message){
        return ResponseEntity.ok(ApiResponse.ok(service.sendMessage(message)));
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<List<AgentStatusResponseDto>>> checkStatus(){
        List<AgentStatusResponseDto> result = new ArrayList<>();
        service.checkAllAvailableModels().forEach(m -> result.add(new AgentStatusResponseDto(m.model(), m.status().name())));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
