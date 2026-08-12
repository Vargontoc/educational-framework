package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.avatar.service.AvatarLifecycleService;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.session.infrastructure.websocket.stomp.StompConnectAuthInterceptor;
import es.vargontoc.educational.framework.session.infrastructure.websocket.stomp.StompSubscribeInterceptor;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldGameStartUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldHeartbeatUseCase;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;

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

import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChildSessionUseCase childSessionUseCase;
    private final ObjectMapper objectMapper;
    private final StompConnectAuthInterceptor stompConnectAuthInterceptor;
    private final StompSubscribeInterceptor stompSubscribeInterceptor;
    private final AvatarLifecycleService avatarLifecycleService;
    private final GameOrchestrator gameOrchestrator;
    private final GameStateRegistry gameStateRegistry;
    private final WorldHeartbeatUseCase worldHeartbeatUseCase;
    private final WorldGameStartUseCase worldGameStartUseCase;
    private final WorldStateRegistry worldStateRegistry;
    private final WorldOrchestrator worldOrchestrator;
    private final ThreadPoolTaskScheduler webSocketBrokerTaskScheduler;

    public WebSocketConfig(
            ChildSessionUseCase childSessionUseCase,
            ObjectMapper objectMapper,
            StompConnectAuthInterceptor stompConnectAuthInterceptor,
            StompSubscribeInterceptor stompSubscribeInterceptor,
            AvatarLifecycleService avatarLifecycleService,
            GameOrchestrator gameOrchestrator,
            GameStateRegistry gameStateRegistry,
            WorldHeartbeatUseCase worldHeartbeatUseCase,
            WorldGameStartUseCase worldGameStartUseCase,
            WorldStateRegistry worldStateRegistry,
            WorldOrchestrator worldOrchestrator,
            ThreadPoolTaskScheduler webSocketBrokerTaskScheduler) {
        this.childSessionUseCase = childSessionUseCase;
        this.objectMapper = objectMapper;
        this.stompConnectAuthInterceptor = stompConnectAuthInterceptor;
        this.stompSubscribeInterceptor = stompSubscribeInterceptor;
        this.avatarLifecycleService = avatarLifecycleService;
        this.gameOrchestrator = gameOrchestrator;
        this.gameStateRegistry = gameStateRegistry;
        this.worldHeartbeatUseCase = worldHeartbeatUseCase;
        this.worldGameStartUseCase = worldGameStartUseCase;
        this.worldStateRegistry = worldStateRegistry;
        this.worldOrchestrator = worldOrchestrator;
        this.webSocketBrokerTaskScheduler = webSocketBrokerTaskScheduler;
    }

    // ── STOMP (parental channel) ──────────────────────────────────────

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
            .setTaskScheduler(webSocketBrokerTaskScheduler);
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/parent")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompConnectAuthInterceptor, stompSubscribeInterceptor);
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
            gameOrchestrator, gameStateRegistry,
            worldHeartbeatUseCase, worldGameStartUseCase, worldStateRegistry, worldOrchestrator);
    }
}
