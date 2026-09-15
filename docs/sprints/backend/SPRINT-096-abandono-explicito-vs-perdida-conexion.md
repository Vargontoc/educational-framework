# Sprint 096 - backend
# -----------------------------------------------

## Goal
Separar el abandono explícito del niño (doble toque → `game_abandon`) de la pérdida de conexión / expiración de sesión. Solo el abandono explícito registra una señal de abandono en tracking; la pérdida de conexión descarta el estado sin rastro. Implementa las decisiones 11 y 13 de FEAT-013.

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-14):

- **FEAT-013 D11**: "Si se abandona antes, los resultados de rondas previas no se consolidan; el abandono sí queda disponible como señal informativa no evaluativa."
- **FEAT-013 D13**: "Si la app pasa a segundo plano o se cierra la conexión websocket con un minijuego activo, no cuenta ni como resultado de tracking ni como abandono; es una señal de plataforma distinta de la inactividad dentro del juego y no comparte contador con la decisión 12."
- **Estado actual**: existen dos métodos de abandono en `GameOrchestrator`:
  - `abandonGame(Long gameId)`: llamado por el cliente vía `game_abandon`. Actualmente **no registra** `GameSessionSummary` (bug conocido desde SPRINT-043).
  - `abandonGameForSession(Long childSessionId)`: llamado por `ChildSessionService` en caso de expiración por inactividad o expulsión parental. Actualmente **sí registra** `GameSessionSummary(ABANDONED)`.
- **Gap crítico**: la pérdida de conexión (WebSocket cierra → heartbeats dejan de llegar → `SessionExpirationJob` detecta inactividad → `expireInactiveSessions` → `abandonGameForSession`) registra un abandono en tracking, violando D13. FEAT-013 exige que la pérdida de conexión **no cuente como abandono**.
- **Gap conocido**: `abandonGame()` (cliente) no registra `GameSessionSummary`. FEAT-013 D11 exige que el abandono explícito **sí quede como señal informativa**. Este sprint corrige ambos gaps.
- **World state**: `WorldGameCompletionListener` reacciona a `GameSessionCompletedEvent`. Con el nuevo diseño, `discardGameForSession()` (pérdida de conexión) no publica evento. **Decisión confirmada (2026-09-14)**: tratar como abandono a efectos de world state (limpiar `narrativeCompletionStatus`), pero sin registrar tracking ni publicar evento. La limpieza de world state se mueve a `discardGameForSession()` directamente.
- **`GameSessionSummary`**: SPRINT-095 ya añadió `isRepetition`. Este sprint añade `abandonReason` para distinguir el tipo de abandono.
- **Migración Liquibase**: SPRINT-095 usa `039`. La siguiente es `040`.

## Status
status: proposed
started_at:
closed_at:
verified_at:
blocked_by: SPRINT-095
waiting_for:

## Decisiones confirmadas (2026-09-14)

1. **Abandono explícito (`game_abandon`) registra señal de abandono.** Confirmado — `abandonGame(gameId)` registra `GameSessionSummary(ABANDONED, abandonReason=CLIENT_REQUESTED)`. Los intentos buffer (SPRINT-095) se descartan.
2. **Pérdida de conexión / expiración no registra nada.** Confirmado — `discardGameForSession(childSessionId)` limpia el estado en memoria y el world state, pero no registra `GameSessionSummary` ni `ActivityAttempt`. No publica `GameSessionCompletedEvent`.
3. **World state en pérdida de conexión: como abandono, sin tracking.** Confirmado — `discardGameForSession()` limpia `narrativeCompletionStatus` igual que `abandonGame()`, pero sin publicar evento ni registrar tracking.
4. **`abandonReason` solo para abandonos explícitos.** Confirmado — el campo `abandonReason` es nullable y solo se popula cuando `finalStatus=ABANDONED` y el abandono fue explícito (`CLIENT_REQUESTED`). Para completados, es null.

## Diseño propuesto

### 1. Renombrar `abandonGameForSession()` → `discardGameForSession()`

El método actual `abandonGameForSession(Long childSessionId)` se renombra para reflejar su nuevo propósito: descartar el estado sin registrar tracking.

```java
// GameOrchestrator.java (puerto)
void discardGameForSession(Long childSessionId);

// GameOrchestratorService.java (implementación)
public void discardGameForSession(Long childSessionId) {
    var gameState = gameStateRegistry.findByChildSessionId(childSessionId).orElse(null);
    if (gameState == null) {
        log.debug("No active game found for childSessionId={}", childSessionId);
        return;
    }
    Long gameId = gameState.getGameId();
    ReentrantLock lock = getLock(gameId);
    lock.lock();
    try {
        var state = gameStateRegistry.findByGameId(gameId).orElse(null);
        if (state == null || !isActive(state.getStatus())) {
            return;
        }
        // Limpiar world state sin publicar evento
        cleanupWorldState(state.getChildSessionId());
        // Eliminar del registro sin registrar summary
        gameStateRegistry.remove(gameId);
        log.info("Game {} discarded for childSessionId={} (no tracking)", gameId, childSessionId);
    } finally {
        lock.unlock();
    }
}
```

`cleanupWorldState(childSessionId)`: método privado que busca el `WorldState` por `childSessionId` y establece `narrativeCompletionStatus = NO_PENDING` (igual que hace `WorldGameCompletionListener` para abandonos). No publica `GameSessionCompletedEvent`.

### 2. Modificar `abandonGame()` para registrar summary

El método actual `abandonGame(Long gameId)` no registra `GameSessionSummary`. Se modifica para registrar la señal de abandono:

```java
// GameOrchestratorService.java
public GameState abandonGame(Long gameId) {
    ReentrantLock lock = getLock(gameId);
    lock.lock();
    try {
        GameState state = gameStateRegistry.findByGameId(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId));
        if (!isActive(state.getStatus())) {
            throw new InvalidStateTransitionException(state.getStatus(), GameStatus.ABANDONED);
        }
        state.setStatus(GameStatus.ABANDONED);
        state.setLastActivityAt(LocalDateTime.now());
        
        // Registrar summary solo si no es repetición
        if (!state.isRepetition()) {
            registerAbandonmentSummary(state);
        }
        
        // Limpiar world state
        cleanupWorldState(state.getChildSessionId());
        
        gameStateRegistry.remove(gameId);
        publishGameCompletedEvent(gameId, state.getChildSessionId(), state.getActivityId(), GameSessionFinalStatus.ABANDONED);
        return state;
    } finally {
        lock.unlock();
    }
}

private void registerAbandonmentSummary(GameState state) {
    GameSessionSummary summary = new GameSessionSummary();
    summary.setChildProfileId(state.getChildProfileId());
    summary.setChildSessionId(state.getChildSessionId());
    summary.setActivityId(state.getActivityId());
    summary.setDifficultyLevelStartId(state.getDifficultyLevelId());
    summary.setDifficultyLevelEndId(state.getDifficultyLevelId());
    summary.setScore(0);
    summary.setTotalAttempts(state.getAttempts());
    summary.setTotalCorrect(state.getCorrectAttempts());
    summary.setTotalIncorrect(state.getIncorrectAttempts());
    summary.setTotalTimeouts(state.getTimeoutAttempts());
    summary.setStartedAt(state.getStartedAt());
    summary.setEndedAt(LocalDateTime.now());
    summary.setFinalStatus(GameSessionFinalStatus.ABANDONED);
    summary.setRepetition(false);
    summary.setAbandonReason("CLIENT_REQUESTED");
    registerGameSessionSummaryUseCase.register(summary);
}
```

### 3. Actualizar `ChildSessionService`

Los llamadores de `abandonGameForSession()` se actualizan para llamar a `discardGameForSession()`:

```java
// ChildSessionService.java
public void expireInactiveSessions(LocalDateTime cutoff) {
    // ...
    gameOrchestrator.discardGameForSession(childSessionId);  // era abandonGameForSession
    // ...
}

public void expelChild(Long id) {
    // ...
    gameOrchestrator.discardGameForSession(childSessionId);  // era abandonGameForSession
    // ...
}
```

### 4. Migración Liquibase

`040__add_abandon_reason_to_game_session_summary.xml`:

```xml
<changeSet id="040-1" author="sprint-096">
    <addColumn tableName="game_session_summary">
        <column name="abandon_reason" type="VARCHAR(30)">
            <constraints nullable="true"/>
        </column>
    </addColumn>
</changeSet>
```

### 5. Modelo `GameSessionSummary`

Se añade el campo `abandonReason` (String, nullable):

```java
// GameSessionSummary.java
private String abandonReason;  // nullable, solo para ABANDONED

// GameSessionSummaryJpaEntity.java
@Column(name = "abandon_reason", length = 30)
private String abandonReason;
```

### 6. Validación

`GameSessionSummaryValidator` se actualiza para validar:
- Si `finalStatus == ABANDONED` y `!isRepetition`, entonces `abandonReason` no debe ser null.
- Si `finalStatus == COMPLETED`, entonces `abandonReason` debe ser null.

### 7. Impacto en `WorldGameCompletionListener`

El listener existente sigue funcionando para abandonos explícitos (que publican `GameSessionCompletedEvent`). Para pérdidas de conexión, `discardGameForSession()` limpia el world state directamente sin publicar evento. No se modifica el listener.

## Contratos y dependencias externas

| Dependencia | Tipo | Estado |
|---|---|---|
| `docs/contracts/api/asyncapi/` | Sin cambios | El protocolo WebSocket no se modifica |
| `docs/contracts/api/openapi/schemas/tracking/` | Sin cambios (diferido a dashboard) | `abandonReason` no se expone aún en API REST |
| SPRINT-095 (consolidación diferida) | Requerida | Verificado — este sprint asume buffer/flush y `isRepetition` |
| SPRINT-043 (game session abandonment, cerrado) | Reemplazado parcialmente | Este sprint corrige el gap de `abandonGame()` y renombra `abandonGameForSession()` |
| Frontend (FEAT-013) | Consumidor | El frontend envía `game_abandon` al doble-toque (ya existe). Sin cambio de contrato |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Renombrar `abandonGameForSession()` → `discardGameForSession()` rompe llamadores existentes | BAJA | Solo hay 2 llamadores (`ChildSessionService.expireInactiveSessions` y `expelChild`), ambos se actualizan en este sprint. El compilador detecta cualquier omisión. |
| R2 | `cleanupWorldState()` duplica lógica de `WorldGameCompletionListener` | BAJA | Aceptable: la limpieza es simple (establecer `narrativeCompletionStatus = NO_PENDING`). Se extrae a un método privado reutilizable. |
| R3 | `abandonGame()` ahora registra summary, lo que puede fallar si tracking no está disponible | BAJA | El registro de summary ya se realiza en otros flujos (completado). Si falla, se lanza excepción y el abandono no se procesa. Aceptable. |
| R4 | La limpieza de world state en `discardGameForSession()` sin publicar evento puede dejar el world state inconsistente si hay otros listeners | BAJA | Actualmente solo hay un listener (`WorldGameCompletionListener`). Si se añaden más en el futuro, deberán suscribirse a un evento de descarte o `discardGameForSession()` deberá invocarlos directamente. |

## Handoffs a otras capas (no diseñados en detalle aquí)

- **Frontend**: no requiere cambios de contrato. El frontend envía `game_abandon` al doble-toque (ya existe). El comportamiento observable es el mismo: el niño vuelve a WorldMap.
- **Agents (npc-game)**: no aplica. Las intervenciones de Nubi se resuelven con frases pre-generadas.
- **World**: `WorldGameCompletionListener` no recibe evento en pérdida de conexión, pero el world state se limpia directamente en `discardGameForSession()`. Transparente para el módulo world.

## Tareas del sprint

### Modelo y persistencia
- [ ] Migración Liquibase `040__add_abandon_reason_to_game_session_summary.xml`: columna `abandon_reason VARCHAR(30) NULL`.
- [ ] Añadir campo `abandonReason` (String, nullable) a `GameSessionSummary` (modelo).
- [ ] Añadir campo `abandonReason` a `GameSessionSummaryJpaEntity` y mapeo `toDomain`/`toJpa`.
- [ ] Actualizar `GameSessionSummaryResult` (record) con `abandonReason`.
- [ ] Actualizar `GameSessionSummaryValidator`: validar que `abandonReason` es obligatorio para `ABANDONED` no repetición, y null para `COMPLETED`.

### Orquestador
- [ ] Renombrar `abandonGameForSession()` → `discardGameForSession()` en `GameOrchestrator` (puerto) y `GameOrchestratorService` (implementación).
- [ ] Modificar `discardGameForSession()`: eliminar la llamada a `registerGameSessionSummary()`. Añadir `cleanupWorldState(childSessionId)` que establece `narrativeCompletionStatus = NO_PENDING` sin publicar evento.
- [ ] Modificar `abandonGame()`: añadir registro de `GameSessionSummary(ABANDONED, abandonReason=CLIENT_REQUESTED)` solo si `!isRepetition`. Añadir `cleanupWorldState(childSessionId)`.
- [ ] Crear método privado `registerAbandonmentSummary(GameState)` en `GameOrchestratorService`.
- [ ] Crear método privado `cleanupWorldState(Long childSessionId)` en `GameOrchestratorService`.

### Session
- [ ] Actualizar `ChildSessionService.expireInactiveSessions()`: llamar a `discardGameForSession()` en lugar de `abandonGameForSession()`.
- [ ] Actualizar `ChildSessionService.expelChild()`: llamar a `discardGameForSession()` en lugar de `abandonGameForSession()`.

### Tests
- [ ] Unit test: `game_abandon` (cliente) → verificar que `registerGameSessionSummary()` se llama con `finalStatus=ABANDONED` e `abandonReason=CLIENT_REQUESTED`.
- [ ] Unit test: `game_abandon` de una repetición → verificar que `registerGameSessionSummary()` NO se llama.
- [ ] Unit test: expiración por inactividad → verificar que `registerGameSessionSummary()` NO se llama.
- [ ] Unit test: expulsión parental → verificar que `registerGameSessionSummary()` NO se llama.
- [ ] Unit test: `discardGameForSession()` limpia el world state (`narrativeCompletionStatus = NO_PENDING`) sin publicar `GameSessionCompletedEvent`.
- [ ] Unit test: `abandonGame()` limpia el world state y publica `GameSessionCompletedEvent(ABANDONED)`.
- [ ] Unit test: `GameSessionSummaryValidator` rechaza un summary `ABANDONED` sin `abandonReason`.
- [ ] Unit test: `GameSessionSummaryValidator` rechaza un summary `COMPLETED` con `abandonReason` no null.
- [ ] Integration test: flujo completo de abandono explícito → verificar persistencia en `game_session_summary` con `abandon_reason=CLIENT_REQUESTED`.
- [ ] Integration test: flujo completo de expiración → verificar que no se registra nada en `game_session_summary`.

## Manual Tests
- Con backend levantado y un niño con sesión activa: iniciar un minijuego, realizar 2 rondas, abandonar (doble toque). Verificar en la tabla `game_session_summary` que se ha registrado una fila con `final_status=ABANDONED` y `abandon_reason=CLIENT_REQUESTED`. Verificar en `activity_attempt` que no se han registrado las 2 rondas previas (SPRINT-095).
- Forzar expiración de sesión (dejar de enviar heartbeats): verificar que no se registra ninguna fila en `game_session_summary`.
- Expulsar al niño desde el panel parental: verificar que no se registra ninguna fila en `game_session_summary`.

## Dependencies
- SPRINT-095 (consolidación diferida, propuesto) — punto de partida (buffer/flush, `isRepetition`).
- SPRINT-043 (game session abandonment, cerrado) — reemplazado parcialmente.
- SPRINT-050 (game session completed event, verificado) — `publishGameCompletedEvent` ya existe.
- SPRINT-032 (game session summary, verificado) — `registerGameSessionSummaryUseCase` ya existe.
- Módulo `game` existente (`GameOrchestratorService`) — se extiende, no se reemplaza.
- Módulo `world` existente (`WorldStateRegistry`) — se usa para limpieza directa.

## Agent Instruction
- No modificar `WorldGameCompletionListener` — la limpieza de world state en `discardGameForSession()` es independiente.
- El comportamiento observable de `game_abandon` para el cliente WebSocket no debe cambiar: el niño vuelve a WorldMap.
- Código, comentarios y nombres en inglés.
- Los tests deben cubrir tanto el flujo de abandono explícito como el de pérdida de conexión.

## Notes
- Este sprint asume que SPRINT-095 ya añadió `isRepetition` al modelo.
- La migración `040` añade `abandon_reason` (SPRINT-095 usó `039` para `is_repetition`).
- `discardGameForSession()` no publica `GameSessionCompletedEvent` — la limpieza de world state es directa.
