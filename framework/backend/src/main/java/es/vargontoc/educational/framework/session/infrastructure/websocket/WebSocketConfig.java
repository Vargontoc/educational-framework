package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.avatar.service.AvatarLifecycleService;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.session.infrastructure.websocket.stomp.StompSubscribeInterceptor;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.session.ports.in.FamilySessionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.WebSocketHttpRequestHandler;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final FamilySessionUseCase familySessionUseCase;
    private final ChildSessionUseCase childSessionUseCase;
    private final ObjectMapper objectMapper;
    private final StompSubscribeInterceptor stompSubscribeInterceptor;
    private final AvatarLifecycleService avatarLifecycleService;
    private final GameOrchestrator gameOrchestrator;
    private final GameStateRegistry gameStateRegistry;

    public WebSocketConfig(
            FamilySessionUseCase familySessionUseCase,
            ChildSessionUseCase childSessionUseCase,
            ObjectMapper objectMapper,
            StompSubscribeInterceptor stompSubscribeInterceptor,
            AvatarLifecycleService avatarLifecycleService,
            GameOrchestrator gameOrchestrator,
            GameStateRegistry gameStateRegistry) {
        this.familySessionUseCase = familySessionUseCase;
        this.childSessionUseCase = childSessionUseCase;
        this.objectMapper = objectMapper;
        this.stompSubscribeInterceptor = stompSubscribeInterceptor;
        this.avatarLifecycleService = avatarLifecycleService;
        this.gameOrchestrator = gameOrchestrator;
        this.gameStateRegistry = gameStateRegistry;
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

    @Bean
    public HandlerMapping nativeWebSocketHandlerMapping() {
        DefaultHandshakeHandler handshakeHandler = new DefaultHandshakeHandler() {
            @Override
            protected boolean isValidOrigin(ServerHttpRequest request) {
                return true;
            }
        };
        WebSocketHttpRequestHandler handler = new WebSocketHttpRequestHandler(gameWebSocketHandler(), handshakeHandler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of("/ws/game", handler));
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public GameWebSocketHandler gameWebSocketHandler() {
        return new GameWebSocketHandler(childSessionUseCase, objectMapper, avatarLifecycleService,
            gameOrchestrator, gameStateRegistry);
    }
}
