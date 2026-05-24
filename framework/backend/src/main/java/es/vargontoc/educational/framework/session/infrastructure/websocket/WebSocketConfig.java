package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.session.infrastructure.websocket.stomp.StompSubscribeInterceptor;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.session.ports.in.FamilySessionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final FamilySessionUseCase familySessionUseCase;
    private final ChildSessionUseCase childSessionUseCase;
    private final ObjectMapper objectMapper;
    private final StompSubscribeInterceptor stompSubscribeInterceptor;

    public WebSocketConfig(
            FamilySessionUseCase familySessionUseCase,
            ChildSessionUseCase childSessionUseCase,
            ObjectMapper objectMapper,
            StompSubscribeInterceptor stompSubscribeInterceptor) {
        this.familySessionUseCase = familySessionUseCase;
        this.childSessionUseCase = childSessionUseCase;
        this.objectMapper = objectMapper;
        this.stompSubscribeInterceptor = stompSubscribeInterceptor;
    }

    // ── STOMP (parental channel) ──────────────────────────────────────

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
            .setTaskScheduler(taskScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("ws-broker-");
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/parent")
            .addInterceptors(new WebSocketAuthInterceptor(familySessionUseCase))
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompSubscribeInterceptor);
    }

    // ── Native WebSocket (game channel) ───────────────────────────────

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler(), "/ws/game")
            .addInterceptors(new WebSocketAuthInterceptor(familySessionUseCase))
            .setAllowedOriginPatterns("*");
    }

    @Bean
    public GameWebSocketHandler gameWebSocketHandler() {
        return new GameWebSocketHandler(childSessionUseCase, objectMapper);
    }
}
