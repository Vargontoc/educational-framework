package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameWebSocketHandlerTest {

    @Mock
    private ChildSessionUseCase childSessionUseCase;

    @Mock
    private WebSocketSession session;

    private GameWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GameWebSocketHandler(childSessionUseCase, new ObjectMapper());
        lenient().when(session.getId()).thenReturn("test-session-id");
        lenient().when(session.getAttributes()).thenReturn(new HashMap<>());
    }

    @Test
    void auth_validActiveSession_bindsSessionAndSendsAuthAck() throws IOException {
        var childSession = childSession(1L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(1L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":1}"));

        assertEquals(1L, session.getAttributes().get("childSessionId"));
        assertTrue(handler.hasActiveSession(1L));
        verify(session).sendMessage(argThat(msg ->
            ((TextMessage) msg).getPayload().contains("AUTH_ACK")));
    }

    @Test
    void auth_sessionNotFound_closesWithPolicyViolation() throws IOException {
        when(childSessionUseCase.getSession(99L)).thenThrow(new ResourceNotFoundException("not found"));

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":99}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        assertFalse(handler.hasActiveSession(99L));
    }

    @Test
    void auth_sessionNotActive_closesWithPolicyViolation() throws IOException {
        var childSession = childSession(2L, ChildSessionStatus.EXPIRED);
        when(childSessionUseCase.getSession(2L)).thenReturn(childSession);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":2}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        assertFalse(handler.hasActiveSession(2L));
    }

    @Test
    void auth_missingChildSessionId_closesWithPolicyViolation() throws IOException {
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\"}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    @Test
    void heartbeat_beforeAuth_closesWithPolicyViolation() throws IOException {
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"heartbeat\"}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(childSessionUseCase, never()).recordHeartbeat(any());
    }

    @Test
    void heartbeat_afterAuth_callsRecordHeartbeatAndSendsAck() throws IOException {
        var childSession = childSession(3L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(3L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":3}"));
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"heartbeat\"}"));

        verify(childSessionUseCase).recordHeartbeat(3L);
        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(2)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("HEARTBEAT_ACK")));
    }

    @Test
    void afterConnectionClosed_unauthSession_doesNotThrow() {
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertFalse(handler.hasActiveSession(1L));
    }

    @Test
    void afterConnectionClosed_authSession_removesFromMap() throws IOException {
        var childSession = childSession(4L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(4L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":4}"));
        assertTrue(handler.hasActiveSession(4L));

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertFalse(handler.hasActiveSession(4L));
    }

    private ChildSession childSession(Long id, ChildSessionStatus status) {
        var s = new ChildSession();
        s.setId(id);
        s.setStatus(status);
        return s;
    }
}
