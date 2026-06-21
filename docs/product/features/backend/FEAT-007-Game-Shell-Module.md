# FEAT-007 - Backend: Game Module - Base engine

## Status

state: proposal
user_history: Minigames engine shell with tracking integration
depends_on: tracking, session, content
future_depends_on: 
blocked_by: none
test: unit + integration + contract
sprints:

## Description

Se implementará el motor base que comparten todos los minijuegos. Define el contrato deque cada motor especifico debe cumplir y gestiona todo lo que es común: ciclo de vida de la partida, la comunicación con los demas modulos (tracking) y el canal websocket. Un motor específico solo deberia preocuparse de su lógica propia

## Feature source of truth

Esta feature es la fuente de verdad para el módulo `game` en v1. El documento `docs/design/backend_design_v1.docx` se considera un esquema general de la aplicación y puede quedar desactualizado frente a decisiones más concretas tomadas en las features. Cuando haya diferencias, prevalecen las decisiones de esta feature y las features dependientes (`session`, `content`, `tracking`) una vez actualizadas.

## Principios para niños de 3-4 años

El game shell debe imponer una experiencia segura, simple y amable para niños de 3-4 años. Estas reglas son transversales a todos los motores v1:

- No hay vidas, derrota dura ni pantallas de fallo. El niño siempre puede continuar o volver al mapa de forma clara.
- Los errores no penalizan de forma visible. Se usan para adaptar dificultad, pedir ayuda al avatar o simplificar la siguiente interacción.
- Los `TIMEOUT` se interpretan como señal de apoyo, no como fracaso. El flujo debe responder con ayuda, repetición o reducción de complejidad.
- Las rondas deben ser cortas, con una única intención principal por pantalla y feedback inmediato.
- Las acciones repetidas, taps duplicados, respuestas incompletas o pequeños errores motores deben tolerarse cuando sea posible.
- El feedback del avatar debe ser positivo, breve y concreto: recompensa, ánimo o ayuda, nunca reproche.
- Las métricas de puntuación, precisión o velocidad son datos para adaptación y dashboard adulto; no deben convertirse en presión visible para el niño.
- Toda interrupción del sistema debe resolverse con un estado comprensible para el frontend infantil: volver al mapa, mostrar bloqueo adulto o cerrar la sesión con un mensaje amable.

## Ciclo de vida

Toda partida independientemente del minijuiego pasa por los mismos estados: 
WAITING    ← partida creada, esperando que el niño esté listo
STARTING   ← cuenta atrás o animación de inicio
IN_PROGRESS ← partida en curso
COMPLETED  ← partida finalizada con éxito
ABANDONED  ← partida interrumpida, expulsión, bloqueo parental, timeout de inactividad

Las transiciones entre estados son responsabilidad del motor base. Cada motor especifico opera dentro de IN_PROGRESS

**Decisión de arquitectura:** se elimina `PAUSED` del ciclo de vida en v1. La aplicación no tiene una funcionalidad de pausa reversible. Ninguno de los eventos de sistema documentados (`SYSTEM_EXPELLED`, `SYSTEM_BLOCKED`, `SYSTEM_INACTIVITY`, `SYSTEM_AGENT_DOWN`, `SYSTEM_AUDIO_DISABLED`) implica retomar la partida exactamente donde quedó; todos los casos de interrupción real ya se modelan como `ABANDONED`, incluido el bloqueo parental (`SYSTEM_BLOCKED`), que es persistente hasta que un adulto desbloquea el perfil y no tiene mecanismo de reanudación automática. Si en el futuro surge un caso de uso real de pausa (distinto de bloqueo o expulsión), se añadirá como feature propio con su disparador y su flujo de reanudación definidos explícitamente, no se reutilizará este estado sin contrato.

## Contract

Interfaz o clase abstracta que todos los motores implementan o extienden. El motor es lógica de dominio pura: ningún método de este contrato accede a tracking, avatar, WebSocket ni a ningún otro módulo. El motor solo opera sobre `GameState` y devuelve datos de dominio.

- initGame() -> inicializa estado específico del motor, carga los elementos de la actividad, aplica la dificultad actual del niño (recibida como parámetro, no consultada por el motor)
- processAction() -> Recibe una acción del niño, valida que sea compatible con el estado actual, aplica las reglas específicas del motor, actualiza el estado y devuelve el resultado de dominio (`ActionResult`). No accede directamente a tracking, avatar, WebSocket ni otros módulos.
- getNextElement() -> determina cual es el siguiente elemento a presentar al niño según el estado actual y dificultad
- isGameComplete -> cada motor define su propia condición de fin de partida
- buildSummary -> construye el resumen final de la partida para persistir y mostrar al niño

**Decisión de arquitectura:** `evaluateAdaptativeDifficulty()` no forma parte del contrato del motor. La evaluación de dificultad adaptativa es responsabilidad exclusiva de tracking, invocada únicamente por el `GameOrchestrator` tras recibir el `ActionResult` (ver sección Orchestrator). Ningún motor concreto debe tener una dependencia inyectada hacia el puerto de tracking. Esto mantiene los motores testeables como lógica de dominio pura, sin necesidad de mockear infraestructura en los tests unitarios.

Si el motor necesita conocer la dificultad vigente para decidir el siguiente elemento (`getNextElement`), la recibe a través de `GameState.difficultyLevel`, que el orquestador actualiza tras cada llamada a tracking. El motor nunca consulta la dificultad por su cuenta.

## Schemas

- GameState: el estado completo de una partida en un momento dado. Vive en memoria durante la partida; al completarse, abandonarse o cerrar sesión del niño, se elimina de memoria y su resumen se envía a tracking para persistirse como `GameSessionSummary` (no se persiste `GameState` como tal)
    - gameId -> long
    - referencia a childSession
    - referencia a activity
    - DifficultyLevel actual
    - Estado de la partida
    - Puntuacion actual
    - Número de intentos
    - Elementos presentados y pendientes
    - Timestamp de inicio
    - Último lastActivityAt para el heartbeat
    - sequenceNumber: contador incrementado en cada GAME_STATE_UPDATE/GAME_STATE_SYNC emitido, usado por el protocolo de reconexión para detectar duplicados y gaps
    - systemEventPending: flag booleano que da prioridad a un evento de sistema en curso sobre cualquier acción de juego pendiente o en proceso (ver Protocolo de Reconexión y Resiliencia)
    - Payload especñifico del motor, aqui cada motor extiende con su propio estado

El payload específico del motor es un punto de diseño importante. Igual que con los parámetros de dificultad en content, podría modelarse como un objeto tipado que cada motor define, evitando un json generico que es mas complicado mantener

- ActionResult: loque devuelve processAction() al orquestador del motor base:
    - Resultado: CORRECT, INCORRECT, TIMEOUT
    - Tiempo de respuesta en ms
    - AvatarEventType recomendado, REWARD, ENCORAUGE, HELP (recomendación de dominio del motor; no es una llamada a avatar, el orquestador decide si la respeta, p.ej. según avatarEnabled del niño)
    - Nuevo GameState tras procesar la acción
    - Flag de partida compeltada

Nota: `ActionResult` ya no incluye un flag de cambio de dificultad. Ese dato no lo conoce el motor — lo devuelve tracking al orquestador tras registrar el intento (ver puerto `registerAttempt` más abajo).

- GameSession (estado transitorio en memoria, módulo game): acumula datos de la partida en curso mientras está activa. No se persiste como tabla propia de game — al finalizar la partida (COMPLETED o ABANDONED), el orquestador delega su persistencia en tracking (ver `GameSessionSummary` en el puerto de Tracking, más abajo).
    - Referencia a ChildSession
    - Referencia a Activity
    - DifficultyLevel inicial y final
    - Puntuación
    - Total de intentos, aciertos, timeouts
    - Timestam  incio y fin
    - Estado final: COMPLETED, ABANDONED

**Decisión de arquitectura:** `GameSession` deja de ser una entidad persistida en el módulo `game`. La query de dashboard "Recent sessions: duration and activity by session" (FEAT-006) pertenece a tracking, igual que el resto de read models del dashboard parental. Mantener una tabla `GameSession` en `game` obligaría a tracking a leer de `game` para servir esa query, invirtiendo la dirección de dependencia documentada (`game` depende de `tracking`, no al revés). En su lugar, `GameSession` en memoria es solo el acumulador transitorio del orquestador durante `IN_PROGRESS`; al completarse o abandonarse la partida, su contenido se envía a tracking para persistirse como `GameSessionSummary`.

### Puerto de Tracking consumido por el Orchestrator

El módulo game consume tracking a través de un único puerto combinado, no a través de llamadas separadas para registrar el intento, evaluar dificultad y evaluar logros basados en el intento. Esto coincide con el acceptance criteria de FEAT-006: registrar un intento devuelve si hubo cambio de dificultad, sin emitir eventos WebSocket. Se extiende aquí para incluir también los logros desbloqueables a nivel de intento.

- `registerAttempt(childProfileId, activityId, childSessionId, topicId?, difficultyLevelId, result, responseTimeMs, attemptContext)` -> devuelve `{ attemptRegistered, difficultyChanged, newDifficultyLevel?, unlockedAchievements[] }`

`topicId` es opcional porque no todos los motores están ligados a un topic concreto. Motores cognitivos como `RecognitionEngine`, `MemoryEngine` o `SequenceEngine` normalmente lo enviarán. Motores psicomotrices o de timing como `RhythmEngine`, `MotionEngine` o `AimEngine` pueden enviar `topicId = null` y registrar sus datos específicos en `attemptContext`.

`result` mantiene el resultado normalizado que tracking puede agregar de forma común: `CORRECT`, `INCORRECT`, `TIMEOUT`. Si un motor necesita resultados más expresivos (`EARLY`, `LATE`, `MISSED`, precisión, delta temporal, distancia al objetivo, etc.), los incluye en `attemptContext` sin ampliar el enum normalizado de tracking.

`unlockedAchievements` solo contiene logros cuya condición es evaluable con datos de intento/agregado (ver tabla en la sección Logros / insignias). Logros que dependen de la finalización de la partida se evalúan con un puerto distinto, invocado solo cuando corresponde.

- `evaluateGameCompletionAchievements(childProfileId, activityId)` -> devuelve `{ unlockedAchievements[] }`. Invocado por el orquestador únicamente cuando `GameEngine.isGameComplete()` es true, no tras cada intento.

- `registerGameSessionSummary(childProfileId, childSessionId, activityId, difficultyLevelStart, difficultyLevelEnd, score, totalAttempts, totalCorrect, totalTimeouts, startedAt, endedAt, finalStatus)` -> persiste el resumen de la partida finalizada como `GameSessionSummary`. Invocado por el orquestador al transicionar a `COMPLETED` o `ABANDONED`, con el contenido acumulado del `GameSession` en memoria.

Solo el `GameOrchestrator` invoca estos puertos. Ningún `GameEngine` específico tiene una dependencia hacia tracking.

### GameSessionSummary (nueva entidad, módulo tracking)

Resumen persistido de una partida individual. Distinta de `ActivitySummary`: `ActivitySummary` es el acumulado de por vida del niño en una actividad; `GameSessionSummary` es el detalle de una partida concreta, y es la fuente de la query de dashboard "Recent sessions: duration and activity by session" (FEAT-006).

- `id`
- `childProfileId`
- `childSessionId`
- `activityId`
- `difficultyLevelStartId`
- `difficultyLevelEndId`
- `score`
- `totalAttempts`
- `totalCorrect`
- `totalTimeouts`
- `startedAt`
- `endedAt`
- `finalStatus`: `COMPLETED`, `ABANDONED`
- `createdAt`
- `updatedAt`

**Nota para FEAT-006:** esta entidad y su caso de uso de persistencia (`registerGameSessionSummary`) no estaban documentados allí. La query de dashboard "Recent sessions" no tenía fuente de datos definida hasta esta decisión; debe incorporarse a FEAT-006 como entidad y endpoint de lectura asociado.


## Orchertator

Es el componente que coordina el motor base con el resto de módulos. Los motores especñificos no conocen Websocket, tracking, ni avatar ni otros modulos, eso es responsabilidad del orquestador.

Acción del niño llega por WebSocket
    → recupera GameState de memoria
    → delega en GameEngine.processAction
    → recibe ActionResult (sin información de dificultad ni de logros, el motor no las conoce)
    → orquestador llama a tracking.registerAttempt(...) con el resultado del intento
    → tracking registra el intento, evalúa dificultad adaptativa y logros basados en intento, todo en la misma operación
    → tracking devuelve { attemptRegistered, difficultyChanged, newDifficultyLevel?, unlockedAchievements[] }
    → si difficultyChanged → orquestador actualiza GameState.difficultyLevel con newDifficultyLevel
    → si difficultyChanged → emite GAME_DIFFICULTY_CHANGED con la nueva dificultad aplicada
    → dispara evento avatar (según avatarEventType recomendado por el motor, si avatar está habilitado)
    → emite nuevo GameState por WebSocket (con difficultyLevel ya actualizado si cambió)
    → si unlockedAchievements no está vacío → emite GAME_ACHIEVEMENT_UNLOCKED por cada logro
    → si ActionResult indica partida completa:
        → orquestador llama a tracking.evaluateGameCompletionAchievements(...)
        → si hay logros adicionales desbloqueados → emite GAME_ACHIEVEMENT_UNLOCKED por cada uno
        → orquestador llama a tracking.registerGameSessionSummary(...) con el GameSession acumulado en memoria, finalStatus COMPLETED
        → limpia GameSession de memoria
        → emite GAME_COMPLETED

Cuando llega un evento externo durante la partida como expulsion parental, bloqueo parental o timeout por inactividad, el orquestador debe:
Evento SYSTEM_EXPELLED, SYSTEM_BLOCKED o SYSTEM_INACTIVITY llega
    → GameOrchestrator detecta partida activa para esa sesión
    → transiciona GameState a ABANDONED
    → orquestador llama a tracking.registerGameSessionSummary(...) con el GameSession acumulado en memoria, finalStatus ABANDONED
    → limpia estado de memoria
    → emite evento WebSocket al niño

### Eventos WebSocket requeridos

Esta feature requiere que el contrato WebSocket documente, como mínimo, los siguientes eventos:

- Cliente → backend: `GAME_READY`, `GAME_ACTION`, `GAME_RECONNECT`, `GAME_HEARTBEAT`
- Backend → cliente: `GAME_STATE_UPDATE`, `GAME_STATE_SYNC`, `GAME_DIFFICULTY_CHANGED`, `GAME_ACHIEVEMENT_UNLOCKED`, `GAME_COMPLETED`
- Sistema → cliente: `SYSTEM_EXPELLED`, `SYSTEM_BLOCKED`, `SYSTEM_INACTIVITY`, `SYSTEM_AGENT_DOWN`, `SYSTEM_AUDIO_DISABLED`

Tras implementar endpoints o canales reales, `docs/contracts/api/websocket.json` debe actualizarse con payloads, dirección, campos obligatorios y reglas de secuencia.


## Protocolo de Reconexión y Resiliencia

El `GameStateRegistry` vive en memoria sin persistencia intermedia, por lo que el `GameOrchestrator` es responsable de toda la lógica de reconexión, heartbeat y resolución de condiciones de carrera entre eventos de sistema y acciones de juego. Esto es necesario en v1: sin ello, cualquier corte breve de conexión (habitual en tablets domésticas) abandonaría partidas innecesariamente, y el campo `lastActivityAt` ya presente en `GameState` no tendría ningún flujo que lo mantenga actualizado.

### Compatibilidad con FEAT-002 Session

FEAT-002 documenta reconexión de sesión infantil y restauración de estado. Para v1, esta feature acota la restauración de partida al estado mantenido en memoria por `GameStateRegistry`. Si el backend sigue vivo, `GAME_RECONNECT` recupera la partida activa. Si el proceso backend cae o el estado ya fue limpiado, no se reconstruye una partida desde base de datos; el frontend recibe `GAME_STATE_SYNC` sin partida activa y vuelve al mapa. FEAT-002 debe reflejar esta decisión para no asumir persistencia completa de `GameState`.

### Compatibilidad con FEAT-006 Tracking

FEAT-006 debe incorporar como fuente de verdad de dashboard la entidad `GameSessionSummary`, el puerto `registerGameSessionSummary(...)` y los casos internos de evaluación de logros usados por esta feature. Tracking sigue sin emitir WebSocket ni eventos de avatar; solo devuelve resultados para que `GameOrchestrator` decida qué comunicar al niño.

### Reconexión — GAME_RECONNECT / GAME_STATE_SYNC

```
GAME_RECONNECT llega con childSessionId
    → GameOrchestrator busca en GameStateRegistry por childSessionId
    → si existe GameState activo (WAITING, STARTING o IN_PROGRESS):
        → emite GAME_STATE_SYNC con el GameState completo actual, incluido sequenceNumber vigente
    → si no existe (la partida ya se completó, se abandonó, o nunca existió):
        → emite GAME_STATE_SYNC con estado "sin partida activa", el frontend interpreta esto como retorno al mapa
```

No requiere infraestructura nueva: reutiliza el `GameStateRegistry` ya definido en Memory Store. Es un caso de uso adicional del orquestador, no un componente distinto.

### Heartbeat — GAME_HEARTBEAT

```
GAME_HEARTBEAT llega con childSessionId, cada 30 segundos desde el cliente
    → GameOrchestrator localiza el GameState activo para esa sesión
    → actualiza GameState.lastActivityAt = now()
    → reinicia el timer de abandono por inactividad asociado a esa partida
    → no requiere GAME_STATE_UPDATE de respuesta (ACK ligero opcional, a definir en implementación si la latencia lo justifica)
```

Si `lastActivityAt` no se actualiza durante el periodo configurado de inactividad, se dispara `SYSTEM_INACTIVITY` según el flujo ya definido de eventos externos.

Este heartbeat debe coordinarse con el módulo `session`. Para evitar doble fuente de actividad, `GAME_HEARTBEAT` debe actualizar también `ChildSession.lastActivityAt` mediante el caso de uso de session, o delegar completamente en session la actualización de actividad y usar el resultado para reiniciar el timer de abandono de partida.

### Prioridad de eventos de sistema — systemEventPending

Escenario de carrera a resolver: el padre expulsa la sesión justo cuando el niño envía una `GAME_ACTION`. Sin una guarda explícita, el orquestador podría procesar la acción de juego y emitir `GAME_STATE_UPDATE` antes de aplicar la expulsión, dejando que el niño "complete una jugada" después de haber sido expulsado — experiencia confusa e indeseable.

```
Evento de sistema (SYSTEM_EXPELLED, SYSTEM_BLOCKED, SYSTEM_INACTIVITY) llega
    → GameOrchestrator marca GameState.systemEventPending = true para esa partida ANTES de procesar cualquier GAME_ACTION en cola o en curso
    → cualquier processAction() en curso o pendiente para ese gameId se descarta sin aplicar efectos:
        → no se registra intento en tracking
        → no se emite GAME_STATE_UPDATE
    → procede el flujo normal de transición a ABANDONED ya documentado
```

Toda invocación a `GameEngine.processAction` por parte del orquestador debe comprobar `systemEventPending` inmediatamente antes de aplicar el resultado devuelto, no solo antes de iniciar el procesamiento, para cerrar la ventana de carrera entre el inicio y el fin del procesamiento de la acción.

Además, el orquestador debe procesar de forma serial las acciones de un mismo `gameId`. En v1 basta con una guarda o lock por partida que garantice como máximo una `GAME_ACTION` en curso por `gameId`. Esto evita que dos acciones del niño actualicen el mismo `GameState` a la vez o que un evento de sistema se intercale entre dos efectos parciales.

### Número de secuencia — detección de duplicados y gaps

```
GameState.sequenceNumber se incrementa en cada GAME_STATE_UPDATE o GAME_STATE_SYNC emitido por el orquestador
GAME_ACTION enviado por el niño incluye el sequenceNumber del último GameState que el cliente recibió
    → si coincide con el esperado por el orquestador: se procesa la acción normalmente
    → si es un sequenceNumber ya procesado: se trata como duplicado (reintento de red), se ignora sin error
    → si hay un gap (sequenceNumber menor al esperado en más de un duplicado, o mayor de lo esperado): el orquestador fuerza un GAME_STATE_SYNC en vez de procesar la acción, para resincronizar al cliente antes de aceptar nuevas acciones
```

Esto protege contra reenvíos del cliente por reintentos de red y contra procesar una acción del niño basada en un estado de juego que el cliente ya no tiene actualizado.


## Memory Store

El GameState vive en memoria durante la partida en un Map<gameId, GameState> gestionado por el orquestador. Esto es suficiente para que una aplicación monousuario con máximo 4 sesiones simultaneas. No necesitamos Redis ni ningún mecanismo externo.

Al completarse o abandonarse la partida, el estado se elimina de memoria. El resumen ya no se persiste como tabla propia del módulo game — se envía a tracking vía `registerGameSessionSummary(...)` y se persiste allí como `GameSessionSummary` (ver Schemas).

## Global Session

La childSession es el contendor de todo lo que hace el niño mientras está conectado. Puede jugar varias partidas consecutivas, o estar en modo exploración. el GameOrchestator gestiona partidas individuales dentro de esa sesión, pero la sesión en sí la gestiona session. Esto esta modelado correctamente con la referencia de GameSessionSummary (en tracking) a ChildSession.

## Puntuacion flexible

Cada motor define como la representa, reforzando se esa manera el modelo de payload especifico del motor que se propone en GameState. El motor base solo conoce un concepto abstracto de métrica de progreso y cada motor lo implementa como necesita.

Motor de letras/números  → puntuación numérica acumulada
Motor psicomotriz        → porcentaje de precisión
Motor de memoria         → racha de aciertos consecutivos
Motor de identificación  → estrellas por velocidad y aciertos

El equipo de front end recibe la métrica con un tipo  que le dice como renderizarla

{
  "metricType": "SCORE",
  "value": 150
}
{
  "metricType": "PERCENTAGE",
  "value": 78
}
{
  "metricType": "STREAK",
  "value": 5
}
{
  "metricType": "STARS",
  "value": 2,
  "maxValue": 3
}

## Logros / insignias

- Achievement: Catalogo de logros disponibles en la aplicación. Dato de referencia, vive en content junto al resto de catalogo
    - Nombre e icono
    - Descripcion
    - Tipo de condición: FIRST_CORRECT_STREAK, ACTIVITY_COMPLETED, DIFFICULTY_INCREASED, SESSION_COUNT, etc.
    - Parámetro de la condición, por ejemplo racha de 5 aciertos, completar 3 minijuegos, etc
    - Estado ACTIVE, INACTIVE

- ChildAchievement: logros obtenidos por un niño en concreto. Vive en tracking ya que es un dato de progreso
    - Referencia a ChildProfile
    - Referencia a Achievement
    - earnedAt

La evaluacion se realiza en tracking, no en el GameOrchestrator. El orquestador no conoce las reglas de negocio de los logros — solo dispara la evaluación en el momento adecuado del ciclo de vida y reacciona a la lista de logros desbloqueados que tracking le devuelve. Esto es coherente con que tracking es dueño de todo el progreso runtime del niño (FEAT-006).

No todas las condiciones de logro se evalúan en el mismo momento. Se distinguen tres categorías:

| Tipo de condición | Cuándo se evalúa | Quién la dispara | Mecanismo |
|---|---|---|---|
| Basada en métricas de intento (`FIRST_CORRECT_STREAK` y similares) | Tras cada intento | `GameOrchestrator`, como parte de `registerAttempt(...)` | Incluido en `unlockedAchievements` de la respuesta combinada |
| `DIFFICULTY_INCREASED` | Cuando `registerAttempt(...)` ya detecta `difficultyChanged = true` | Tracking, en la misma llamada | Incluido en `unlockedAchievements` de la respuesta combinada |
| `ACTIVITY_COMPLETED` | Cuando `GameEngine.isGameComplete()` es true | `GameOrchestrator`, llamada puntual tras completar la partida | Puerto `evaluateGameCompletionAchievements(childProfileId, activityId)` |
| `SESSION_COUNT` | Al iniciar una `ChildSession` | Módulo `session`, no `game` | Fuera de alcance de FEAT-007. `SESSION_COUNT` no es un evento de partida sino de sesión global del niño; debe evaluarse donde se crea la `ChildSession`, no en el `GameOrchestrator`. Pendiente de incorporar a FEAT-006 o a un futuro feature de session. |

Si se desbloquea un logro se emite un evento Websocket especial que el frontend renderiza como celebración y el avatar lanza un evento REWARD enriquecido.

GAME_ACHIEVEMENT_UNLOCKED
{
  "achievementId": "xxx",
  "name": "Primera estrella",
  "icon": "star"
}

**Nota para FEAT-006:** tracking necesita un nuevo caso de uso interno de "evaluar logros" — hoy solo documenta "registrar achievement" y "obtener achievements del niño". La evaluación de condiciones (incluida la lectura de `Achievement` desde content para conocer el tipo y parámetro de condición) debe añadirse explícitamente como caso de uso de tracking, ya que hoy se asumía implícitamente desde game.

## Operaciones adicionales

- Consultar logros del niño para el dashboard de padres
- Consultar logros recientes para mostrar en el perfil del niño

## Decisiones de Arquitectura (registro de sesión)

| # | Tema | Decisión |
|---|------|----------|
| 1 | Quién llama a tracking para evaluar dificultad adaptativa | Solo el `GameOrchestrator`, nunca el `GameEngine`. Se elimina `evaluateAdaptativeDifficulty()` del contrato del motor. |
| 1 | Cómo se entera el motor de un cambio de dificultad | El orquestador actualiza `GameState.difficultyLevel` tras la respuesta de tracking; el motor lo recibe en `GameState`, nunca lo consulta directamente. |
| 1 | Forma del puerto de tracking consumido por game | Llamada combinada `registerAttempt(...)` que registra el intento y evalúa dificultad en una sola operación, devolviendo si hubo cambio y la nueva dificultad. |
| 1 | Comunicación de cambio de dificultad al frontend | Si tracking devuelve `difficultyChanged = true`, el orquestador emite `GAME_DIFFICULTY_CHANGED` además de incluir la dificultad actualizada en `GAME_STATE_UPDATE`. |
| 1 | `topicId` en intentos | Opcional. Motores ligados a topic lo envían; motores psicomotrices o de timing pueden enviar `null` y usar `attemptContext`. |
| 1 | Resultados específicos de motor | Tracking mantiene `CORRECT`, `INCORRECT`, `TIMEOUT` como resultado normalizado; resultados como `EARLY`, `LATE` o `MISSED` viven en `attemptContext`. |
| 3 | Quién evalúa las condiciones de logro | Tracking, no el `GameOrchestrator`. El orquestador solo dispara la evaluación en el momento correcto del ciclo de vida. |
| 3 | Cómo se evalúan logros basados en intento (incluido `DIFFICULTY_INCREASED`) | Incluidos en la misma respuesta de `registerAttempt(...)`, campo `unlockedAchievements[]`. |
| 3 | Cómo se evalúan logros basados en finalización de partida (`ACTIVITY_COMPLETED`) | Puerto nuevo `evaluateGameCompletionAchievements(...)`, invocado solo cuando `isGameComplete()` es true. |
| 3 | `SESSION_COUNT` | Fuera de alcance de FEAT-007. Pertenece al ciclo de vida de `ChildSession` en el módulo `session`, no al `GameOrchestrator`. Nota cruzada pendiente para FEAT-006/session. |
| 4 | Dónde se persiste el resumen final de una partida | En tracking, no en `game`. Se crea la nueva entidad `GameSessionSummary` en tracking. |
| 4 | Qué queda en `game` | Solo el estado transitorio en memoria (`GameSession` dentro de `GameState`/Memory Store), sin tabla persistida propia. |
| 4 | Cómo se persiste | Nuevo puerto `registerGameSessionSummary(...)`, invocado por el orquestador al transicionar a `COMPLETED` o `ABANDONED`. |
| 4 | Relación con `ActivitySummary` | Sin solapamiento: `ActivitySummary` es el acumulado de por vida; `GameSessionSummary` es el detalle por partida individual, fuente de la query "Recent sessions" del dashboard. |
| 5 | Estado `PAUSED` en el ciclo de vida | Eliminado de v1. La aplicación no tiene funcionalidad de pausa reversible; ningún evento de sistema documentado lo dispara ni lo resuelve. Todos los casos de interrupción se modelan como `ABANDONED`. |
| 6 | Reconexión tras corte de red | Documentado en nueva sección "Protocolo de Reconexión y Resiliencia". `GAME_RECONNECT`/`GAME_STATE_SYNC` reutilizan `GameStateRegistry`, sin infraestructura nueva ni persistencia completa de `GameState`. |
| 6 | Heartbeat | `GAME_HEARTBEAT` cada 30s actualiza `GameState.lastActivityAt`, se coordina con `ChildSession.lastActivityAt` en session y reinicia el timer de abandono por inactividad. |
| 6 | Prioridad de eventos de sistema sobre acciones de juego | Nuevo campo `GameState.systemEventPending`; toda aplicación de `ActionResult` debe comprobarlo inmediatamente antes de aplicar efectos, no solo al iniciar el procesamiento. |
| 6 | Serialización por partida | El orquestador procesa como máximo una `GAME_ACTION` en curso por `gameId` para evitar carreras sobre el mismo `GameState`. |
| 6 | Duplicados y gaps de mensajes | Nuevo campo `GameState.sequenceNumber`; duplicados se ignoran, gaps fuerzan `GAME_STATE_SYNC` antes de aceptar nuevas acciones. |
| 7 | Enfoque 3-4 años | El shell impone reglas transversales: sin vidas, sin derrota dura, timeouts como ayuda, rondas cortas, feedback positivo y tolerancia a errores motores o acciones repetidas. |

Todos los puntos identificados en esta sesión de debate quedan resueltos y documentados.
