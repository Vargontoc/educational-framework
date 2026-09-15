# Sprint 096 - backend
# -----------------------------------------------

## Goal
Separar el abandono explícito del niño (doble toque → `game_abandon`) de la pérdida de conexión / expiración de sesión. Solo el abandono explícito registra una señal de abandono en tracking; la pérdida de conexión descarta el estado sin rastro. Implementa las decisiones 11 y 13 de FEAT-013.

## Contexto

Verificado por análisis técnico (`analyser-backend`, 2026-09-14):

- **FEAT-013 D11**: "Si se abandona antes, los resultados de rondas previas no se consolidan; el abandono sí queda disponible como señal informativa no evaluativa."
- **FEAT-013 D13**: "Si la app pasa a segundo plano o se cierra la conexión websocket con un minijuego activo, no cuenta ni como resultado de tracking ni como abandono; es una señal de plataforma distinta de la inactividad dentro del juego y no comparte contador con la decisión 12."
- **Estado actual (corregido tras análisis de implementación, 2026-09-15)**: el doc original asumía que `abandonGame()` "no registra `GameSessionSummary` (bug desde SPRINT-043)". Eso ya no es así — **SPRINT-095 añadió registro de summary a ambos métodos** (`abandonGame` y `abandonGameForSession`, ambos guardados por `!isRepetition()`). El trabajo real de este sprint es más estrecho que el descrito abajo: quitar el registro de `abandonGameForSession()` (renombrada `discardGameForSession()`) y añadir `abandonReason` al de `abandonGame()`.
  - `abandonGame(Long gameId)`: llamado por el cliente vía `game_abandon`. Ya registraba `GameSessionSummary(ABANDONED)` desde SPRINT-095; este sprint le añade `abandonReason=CLIENT_REQUESTED`.
  - `abandonGameForSession(Long childSessionId)` → `discardGameForSession(Long childSessionId)`: llamado por `ChildSessionService` en caso de expiración por inactividad o expulsión parental. Ya registraba `GameSessionSummary(ABANDONED)` desde SPRINT-095; este sprint **elimina ese registro por completo**.
- **Gap crítico**: la pérdida de conexión (WebSocket cierra → heartbeats dejan de llegar → `SessionExpirationJob` detecta inactividad → `expireInactiveSessions` → `abandonGameForSession`) registraba un abandono en tracking, violando D13. FEAT-013 exige que la pérdida de conexión **no cuente como abandono**. Corregido en este sprint.
- **World state**: `WorldGameCompletionListener` reacciona a `GameSessionCompletedEvent`. Con el nuevo diseño, `discardGameForSession()` (pérdida de conexión) no publica evento. **Decisión confirmada (2026-09-14)**: tratar como abandono a efectos de world state (limpiar `narrativeCompletionStatus`), pero sin registrar tracking ni publicar evento. La limpieza de world state se mueve a `discardGameForSession()` directamente.
- **`GameSessionSummary`**: SPRINT-095 ya añadió `isRepetition`. Este sprint añade `abandonReason` para distinguir el tipo de abandono.
- **Migración Liquibase**: SPRINT-095 usa `039`. La siguiente es `040`.

## Status
status: verified
started_at: 2026-09-15
closed_at: 2026-09-15
verified_at: 2026-09-15
blocked_by:
waiting_for:

## Decisiones confirmadas (2026-09-14)

1. **Abandono explícito (`game_abandon`) registra señal de abandono.** Confirmado — `abandonGame(gameId)` registra `GameSessionSummary(ABANDONED, abandonReason=CLIENT_REQUESTED)`. Los intentos buffer (SPRINT-095) se descartan.
2. **Pérdida de conexión / expiración no registra nada.** Confirmado — `discardGameForSession(childSessionId)` limpia el estado en memoria y el world state, pero no registra `GameSessionSummary` ni `ActivityAttempt`. No publica `GameSessionCompletedEvent`.
3. **World state en pérdida de conexión: como abandono, sin tracking.** Confirmado — `discardGameForSession()` limpia `narrativeCompletionStatus` igual que `abandonGame()`, pero sin publicar evento ni registrar tracking.
4. **`abandonReason` solo para abandonos explícitos.** Confirmado — el campo `abandonReason` es nullable y solo se popula cuando `finalStatus=ABANDONED` y el abandono fue explícito (`CLIENT_REQUESTED`). Para completados, es null.
5. **Limpieza de world state en `discardGameForSession()`: nuevo evento en vez de dependencia directa.** Confirmado (2026-09-15, tras análisis de implementación) — el diseño original de este doc proponía inyectar `WorldStateRegistry` directamente en `GameOrchestratorService` para limpiar `narrativeCompletionStatus` sin publicar evento. Se descartó: el módulo `game` no tenía (ni tiene ahora) ninguna dependencia hacia `world`, mientras que `world→game` ya existe (`WorldGameCompletionListener`, `WorldGameStartService`); seguir el diseño original habría creado acoplamiento bidireccional entre módulos. En su lugar, `discardGameForSession()` publica un nuevo evento ligero `GameSessionDiscardedEvent` (game.model.event), distinto de `GameSessionCompletedEvent` (que no se publica en absoluto para este caso, cumpliendo la Decisión 2 tal cual), y se añade un nuevo método `@EventListener onGameSessionDiscarded(...)` a `WorldGameCompletionListener` (sin tocar `onGameSessionCompleted`). Esto reemplaza la instrucción original "No modificar `WorldGameCompletionListener`" (ver Agent Instruction actualizado).

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
- [x] Migración Liquibase `040__add_abandon_reason_to_game_session_summary.xml`: columna `abandon_reason VARCHAR(30) NULL`.
- [x] Añadir campo `abandonReason` a `GameSessionSummary` (modelo). **Desviación**: tipo `GameSessionAbandonReason` (nuevo enum de dominio con único valor `CLIENT_REQUESTED`), no `String` suelto — sigue el patrón ya usado por `GameSessionFinalStatus` (enum de dominio ↔ columna `String` vía `.name()`/`.valueOf()`), consistente con el resto del código.
- [x] Añadir campo `abandonReason` a `GameSessionSummaryJpaEntity` y mapeo `toDomain`/`toJpa`.
- [ ] Actualizar `GameSessionSummaryResult` (record) con `abandonReason`. **No implementado a propósito** (mismo criterio que `isRepetition` en SPRINT-095): nadie consume ese valor de vuelta tras el insert; se mantiene el record minimalista.
- [x] Actualizar `GameSessionSummaryValidator`: `abandonReason` obligatorio para `ABANDONED`, null para `COMPLETED`. **Simplificación real**: como `discardGameForSession()` deja de llamar a `registerGameSessionSummary()` por completo, toda llamada con `ABANDONED` que llega al validador es ya, por construcción, un abandono explícito no-repetición — no hace falta que el validador conozca `isRepetition` para distinguir.

### Orquestador
- [x] Renombrar `abandonGameForSession()` → `discardGameForSession()` en `GameOrchestrator` (puerto) y `GameOrchestratorService` (implementación).
- [x] Modificar `discardGameForSession()`: eliminada la llamada a `registerGameSessionSummary()`. **Desviación** (ver Decisión 5): en vez de `cleanupWorldState(childSessionId)` inyectando `WorldStateRegistry` directamente, se publica un nuevo evento `GameSessionDiscardedEvent` (sustituyendo la llamada a `publishGameCompletedEvent`) que `WorldGameCompletionListener` consume para limpiar `narrativeCompletionStatus = NO_PENDING`. Se mantiene `state.setSystemEventPending(true)` (protege `processAction()` de una acción en vuelo, sin relación con tracking).
- [x] Modificar `abandonGame()`: añadido `GameSessionAbandonReason.CLIENT_REQUESTED` como argumento a la llamada ya existente a `registerGameSessionSummary()` (dentro del `if (!state.isRepetition())` ya existente desde SPRINT-095).
- [ ] `registerAbandonmentSummary(GameState)` como método privado nuevo — **no fue necesario**: `abandonGame()` ya tenía la llamada completa desde SPRINT-095, solo se añadió el nuevo argumento in situ.
- [ ] `cleanupWorldState(Long childSessionId)` en `GameOrchestratorService` — **no implementado, ver desviación arriba**: la limpieza vive en `WorldGameCompletionListener.onGameSessionDiscarded(...)`, no en `GameOrchestratorService`.
- [x] Nuevo método privado `publishGameDiscardedEvent(Long gameId, Long childSessionId, Long activityId)` (no listado originalmente), espejo de `publishGameCompletedEvent(...)`.

### Session
- [x] Actualizar `ChildSessionService.expireInactiveSessions()`: llama a `discardGameForSession()`.
- [x] Actualizar `ChildSessionService.expelChild()`: llama a `discardGameForSession()`.

### World (no listado originalmente — consecuencia de la Decisión 5)
- [x] Añadir método `WorldGameCompletionListener.onGameSessionDiscarded(GameSessionDiscardedEvent)`, sin tocar `onGameSessionCompleted`.

### Tests
- [x] Unit test: `game_abandon` (cliente) → `registerGameSessionSummary()` se llama con `finalStatus=ABANDONED` e `abandonReason=CLIENT_REQUESTED` (`abandonGame_removesFromRegistryAndRegistersSummary`).
- [x] Unit test: `game_abandon` de una repetición → `registerGameSessionSummary()` NO se llama (`abandonGame_repetitionGame_doesNotRegisterSummary`).
- [x] Unit test: expiración por inactividad → `registerGameSessionSummary()` NO se llama (`discardGameForSession_withActiveGame_discardsWithoutRegisteringSummary`, `ChildSessionServiceTest.expireInactiveSessions_callsDiscardGameForSession`).
- [x] Unit test: expulsión parental → `registerGameSessionSummary()` NO se llama (`ChildSessionServiceTest.expelChild_setsStatusExpelledAndDiscardsGame`).
- [x] Unit test: `discardGameForSession()` limpia el world state sin publicar `GameSessionCompletedEvent` (`WorldGameCompletionListenerTest.listener_discardedClearsStatus` + `discardGameForSession_publishesGameSessionDiscardedEvent`).
- [x] Unit test: `abandonGame()` publica `GameSessionCompletedEvent(ABANDONED)` (`abandonGame_publishesGameSessionCompletedEvent`, ya existente).
- [x] Unit test: `GameSessionSummaryValidator` rechaza `ABANDONED` sin `abandonReason` (`registerGameSessionSummary_abandonedWithoutAbandonReason_throwsValidationException`).
- [x] Unit test: `GameSessionSummaryValidator` rechaza `COMPLETED` con `abandonReason` no null (`registerGameSessionSummary_completedWithAbandonReason_throwsValidationException`).
- [ ] Integration test de persistencia real en BD (abandono explícito y expiración). **No implementado**: mismo motivo que SPRINT-095 — no hay Docker disponible en este entorno de desarrollo para Testcontainers. Cubierto por unit tests + `GameOrchestratorServiceTrackingIntegrationTest` (mock-based) + "Manual Tests" abajo.

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
- ~~No modificar `WorldGameCompletionListener`~~ — **superada por la Decisión 5**: sí se modifica, añadiendo un nuevo método `onGameSessionDiscarded(...)` sin tocar el `onGameSessionCompleted` existente, porque el diseño original (dependencia directa `GameOrchestratorService`→`WorldStateRegistry`) habría creado acoplamiento `game↔world` que no existía.
- El comportamiento observable de `game_abandon` para el cliente WebSocket no debe cambiar: el niño vuelve a WorldMap.
- Código, comentarios y nombres en inglés.
- Los tests deben cubrir tanto el flujo de abandono explícito como el de pérdida de conexión.

## Notes
- Este sprint asume que SPRINT-095 ya añadió `isRepetition` al modelo.
- La migración `040` añade `abandon_reason` (SPRINT-095 usó `039` para `is_repetition`).
- `discardGameForSession()` no publica `GameSessionCompletedEvent` — publica `GameSessionDiscardedEvent` (nuevo), consumido por un nuevo método en `WorldGameCompletionListener` (ver Decisión 5; corrige el diseño original de "limpieza directa" vía `WorldStateRegistry` inyectado en `GameOrchestratorService`, descartado por acoplamiento de módulos).

## Verificación
- `mvn -o test-compile`: BUILD SUCCESS.
- `mvn -o test -Dtest="es.vargontoc.educational.framework.game.**,es.vargontoc.educational.framework.session.**,es.vargontoc.educational.framework.tracking.**,es.vargontoc.educational.framework.world.**"`: 489 tests ejecutados, 0 failures. Los 33 errores son los mismos de siempre en este entorno (`AuthControllerTest`, `ChildSessionControllerTest`, `TrackingRetentionPersistenceTest`, `TrackingDashboardControllerTest`, `TrackingSchemaApplicationTest` — `IllegalState: Failed to load ApplicationContext` por falta de Docker/Testcontainers, ya documentado en SPRINT-095, sin relación con los módulos tocados por este sprint).
- Clases tocadas verificadas individualmente: `GameOrchestratorServiceTest` (26/26), `GameOrchestratorServiceTrackingIntegrationTest` (6/6), `GameOrchestratorServiceSprint070Test` (5/5, sin cambios), `ChildSessionServiceTest` (18/18), `GameSessionSummaryServiceTest` (8/8), `WorldGameCompletionListenerTest` (4/4), `GameWebSocketHandlerTest` (43/43, sin cambios).

## Review

### Developer implementation — Evidencias

#### Resumen técnico verificado

1. **Rename limpio**: `abandonGameForSession()` → `discardGameForSession()` en el puerto `GameOrchestrator` y en `GameOrchestratorService`. Grep de `abandonGameForSession` sobre todo `src/` → sin resultados: cero referencias colgantes en producción ni en tests.
2. **`discardGameForSession()` sin tracking**: no llama a `flushBufferedAttempts()`, `registerActivityAttemptUseCase.register()` ni `registerGameSessionSummaryUseCase.registerGameSessionSummary()`. El buffer de intentos (SPRINT-095) se descarta junto con el `GameState` al hacer `gameStateRegistry.remove(gameId)`. Cumple D13 al pie de la letra: ni resultado de tracking ni abandono.
3. **`abandonGame()` con `abandonReason`**: se añadió `GameSessionAbandonReason.CLIENT_REQUESTED` como argumento adicional a la llamada a `registerGameSessionSummary()` ya existente desde SPRINT-095 (dentro del `if (!state.isRepetition())`). `processAction()` en el flujo de completado pasa `null` explícito — confirmado en código y en el test `gameSessionSummary_doesNotContainAttemptDetails` (verifica `isNull()` para `abandonReason` en el caso `COMPLETED`).
4. **Decisión 5 (evento en vez de acoplamiento directo)**: implementada exactamente como se documentó. `discardGameForSession()` publica `GameSessionDiscardedEvent` (nuevo record en `game.model.event`) en vez de `GameSessionCompletedEvent`; `WorldGameCompletionListener` gana `onGameSessionDiscarded(...)` sin tocar `onGameSessionCompleted`. Confirmado que el módulo `game` sigue sin ninguna dependencia hacia `world` (no se inyectó `WorldStateRegistry` en `GameOrchestratorService`) — evita el acoplamiento bidireccional que el diseño original habría introducido. `WorldModuleConfiguration.java` no requirió cambios (mismo bean, mismo constructor), confirmado por `git diff` vacío.
5. **Migración `040`**: `abandon_reason VARCHAR(30) NULL` sobre `game_session_summary`, incluida en `db.changelog-master.xml` tras `039`. `GameSessionSummary`/`GameSessionSummaryJpaEntity`/`GameSessionSummaryPersistenceAdapter` mapean el campo en ambas direcciones vía `GameSessionAbandonReason.valueOf(...)`/`.name()`, siguiendo el mismo patrón que `GameSessionFinalStatus`.
6. **Validador**: `GameSessionSummaryValidator` rechaza `ABANDONED` sin `abandonReason` y `COMPLETED` con `abandonReason` no nulo. La simplificación documentada (el validador no necesita conocer `isRepetition`) es correcta: `discardGameForSession()` nunca llega a invocar `registerGameSessionSummary`, así que cualquier `ABANDONED` que llegue al validador es, por construcción, un abandono explícito no-repetición.
7. **`docs/contracts`, `GameSessionSummaryResult`, DTOs/dashboard**: sin cambios, confirmado por `git diff` vacío y por grep — `abandonReason` solo existe en el modelo/persistencia, no se filtra a ningún DTO expuesto.

#### Pruebas ejecutadas (verificación independiente)

```
mvn -o test -Dtest=GameOrchestratorServiceTest,GameOrchestratorServiceTrackingIntegrationTest,GameOrchestratorServiceSprint070Test,ChildSessionServiceTest,GameSessionSummaryServiceTest,WorldGameCompletionListenerTest,GameWebSocketHandlerTest
Tests run: 110, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```
Coincide exactamente con los recuentos por clase que reporta el developer en "Verificación".

```
mvn -o test -Dtest="es.vargontoc.educational.framework.game.**,es.vargontoc.educational.framework.session.**,es.vargontoc.educational.framework.tracking.**,es.vargontoc.educational.framework.world.**"
Tests run: 489, Failures: 0, Errors: 33, Skipped: 0 — BUILD FAILURE
```
Los 33 errores están confinados a `AuthControllerTest`, `ChildSessionControllerTest`, `TrackingDashboardControllerTest`, `TrackingRetentionPersistenceTest`, `TrackingSchemaApplicationTest` — mismo `IllegalState: Failed to load ApplicationContext` por falta de Docker/Testcontainers ya documentado en SPRINT-095. Ninguna clase tocada por este sprint falla.

#### Criterios de aceptación cubiertos por tests

| Criterio (D11/D13) | Test | Estado |
|---|---|---|
| Abandono explícito registra summary con `abandonReason=CLIENT_REQUESTED` | `abandonGame_removesFromRegistryAndRegistersSummary` | ✅ |
| Abandono explícito de una repetición no registra nada | `abandonGame_repetitionGame_doesNotRegisterSummary` | ✅ |
| Expiración por inactividad no registra summary | `discardGameForSession_withActiveGame_discardsWithoutRegisteringSummary`, `ChildSessionServiceTest.expireInactiveSessions_callsDiscardGameForSession` | ✅ |
| Expulsión parental no registra summary | `ChildSessionServiceTest.expelChild_setsStatusExpelledAndDiscardsGame` | ✅ |
| `discardGameForSession()` limpia world state sin publicar `GameSessionCompletedEvent` | `WorldGameCompletionListenerTest` (`onGameSessionDiscarded`), `discardGameForSession_publishesGameSessionDiscardedEvent` | ✅ |
| `abandonGame()` publica `GameSessionCompletedEvent(ABANDONED)` | `abandonGame_publishesGameSessionCompletedEvent` | ✅ |
| Validador rechaza `ABANDONED` sin `abandonReason` | `registerGameSessionSummary_abandonedWithoutAbandonReason_throwsValidationException` | ✅ |
| Validador rechaza `COMPLETED` con `abandonReason` no nulo | `registerGameSessionSummary_completedWithAbandonReason_throwsValidationException` | ✅ |

### Reviewer verification

**Veredicto: APPROVED**

Implementación completa, correcta y bien acotada. El developer aplicó visiblemente las lecciones de la revisión de SPRINT-095 (decisiones documentadas con motivo, desviaciones explicadas, cero código muerto, cobertura de test simétrica para los distintos flujos). La Decisión 5 (evento `GameSessionDiscardedEvent` en vez de inyectar `WorldStateRegistry` en `GameOrchestratorService`) es la elección arquitectónica correcta: evita crear una dependencia `game→world` cuando `world→game` ya existe, manteniendo el módulo `game` desacoplado. No se detectaron cambios fuera de alcance, código muerto, ni discrepancias entre lo documentado y el código real.

#### Observación mínima (no bloqueante)

No existe un test que ejercite `discardGameForSession()` sobre un `GameState` con intentos ya bufferizados (rondas jugadas antes de la desconexión) para verificar explícitamente que `registerActivityAttemptUseCase.register(...)` nunca se invoca. Hoy esto está garantizado estructuralmente (el método no tiene ninguna ruta que llame a `flushBufferedAttempts()`), así que no hay riesgo real, pero un test explícito lo dejaría blindado ante una futura refactorización. Sugerido como mejora opcional, no requiere acción para cerrar este sprint.

#### Limitaciones de la revisión

- No se ejecutaron los "Manual Tests" (backend levantado + BD real) por falta de Docker en este entorno — mismo límite ya reconocido en SPRINT-095. Cubierto de forma equivalente por `GameOrchestratorServiceTrackingIntegrationTest` (mock-based) y por la inspección directa de la migración `040` y el mapeo de persistencia.
