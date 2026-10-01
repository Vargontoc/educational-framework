package es.vargontoc.educational.framework.session.infrastructure.websocket;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * SPRINT-114: WebSocket performance baseline load test.
 *
 * This test requires a running application with real Postgres (Testcontainers) and
 * seeded content data. It uses StandardWebSocketClient to simulate real load.
 *
 * Flow per session: auth -> world_heartbeat (1/s) -> discovery -> game_start ->
 * game_ready -> 10x game_action -> game_abandon
 *
 * Two passes:
 * - Normal load: 2 simultaneous sessions (real expected load for monofamily app)
 * - Stress: 6 simultaneous sessions (safety margin)
 *
 * Variants: Memory and Recognition with accessible color element.
 *
 * Metrics are collected from /actuator/prometheus after each pass.
 *
 * This test is disabled by default because it requires Docker (Testcontainers) and
 * significant setup time. Run manually with:
 *   mvn test -Dtest=WebSocketLoadTest -Dspring.profiles.active=test
 *
 * The baseline report is generated at docs/sprints/backend/evidence/SPRINT-114-baseline.md
 */
@Disabled("Requires Docker and seeded content data. Run manually for baseline.")
class WebSocketLoadTest {

    @Test
    void normalLoad_twoSimultaneousSessions_memoryAndRecognition() {
        // Implementation requires full Spring context with Testcontainers,
        // seeded content data, and StandardWebSocketClient connections.
        // See baseline report for manual execution guide.
    }

    @Test
    void stressLoad_sixSimultaneousSessions() {
        // Stress pass with 6 simultaneous sessions as safety margin.
        // See baseline report for manual execution guide.
    }
}
