package es.vargontoc.educational.framework.agents.infrastructure.advisors;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import es.vargontoc.educational.framework.agents.infrastructure.commands.CommandRouter;
import reactor.core.publisher.Flux;

public class CommandInterceptorAdvisor implements CallAdvisor, StreamAdvisor {
    
    private final CommandRouter router;

    public CommandInterceptorAdvisor(CommandRouter router){
        this.router = router;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return route(chatClientRequest)
            .map(result -> Flux.just(shortCircuit(chatClientRequest, result)))
            .orElseGet(() -> streamAdvisorChain.nextStream(chatClientRequest));
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        return route(chatClientRequest)
            .map(result -> shortCircuit(chatClientRequest, result))
            .orElseGet(() -> callAdvisorChain.nextCall(chatClientRequest));
    }
    
    private Optional<String> route(ChatClientRequest request) {
        String userPrompt = request.prompt().getUserMessage().getText();
        return router.route(userPrompt);
    }

    private ChatClientResponse shortCircuit(ChatClientRequest request, String message) {
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage(message))));
        return ChatClientResponse.builder().chatResponse(response).context(request.context()).build();
    }
}
