# FEAT-009 - Backend: Recognition Engine Module

## Status

state: proposal — revisión 2 (incorpora ronda de observaciones de integración con World y Game Shell)
user_history: Primer motor de minijuego — reconocimiento visual de letras, números, formas, colores y animales mediante selección táctil simple, con coherencia narrativa por bioma
depends_on: game (FEAT-007), content, tracking, world (FEAT-008)
future_depends_on: avatar (eventos recomendados y refuerzo sonoro/visual del hint, sin invocación directa)
blocked_by: none
test: unit + integration + contract
sprints:

## Description

Se implementa `RecognitionEngine`, el primer motor concreto que extiende el contrato `GameEngine` definido en FEAT-007. La mecánica es reconocimiento por selección: se presenta al niño un elemento objetivo (letra, número, forma, color o animal) y debe tocar la opción correcta entre 2-3 mostradas. Es el primer minijuego jugable de la aplicación y sienta el patrón de implementación que seguirán los motores cognitivos posteriores (`MemoryEngine`, `SequenceEngine`, mencionados en FEAT-007).

Esta revisión añade el mecanismo de contexto de lanzamiento (`launchContext`) para que `world` pueda informar a `game` de la procedencia narrativa de la partida sin acoplar el motor a `world`, detalla el flujo de anti-repetición coordinado por `GameOrchestrator`, resuelve la aplicación diferida de cambios de dificultad durante retries activos, y corrige la modelización de persistencia de intentos.

## Feature source of truth

Esta feature es la fuente de verdad para el motor `RecognitionEngine` en v1. `backend_design_v1.docx` y `design_decisions_v1.docx` se consideran esquemas generales de la aplicación y pueden quedar desactualizados frente a decisiones más concretas tomadas aquí y en FEAT-007/FEAT-008. En particular, esta feature confirma que el sistema de biomas narrativos (vía `world`) es parte de v1 y no de v1.2+ como sugería `design_decisions_v1.docx` (ADR-001, ADR-003) — la decisión vigente es la de FEAT-008.

## Principios para niños de 3-4 años y escalabilidad

Heredados de FEAT-007 y reafirmados/ajustados para este motor concreto:

- El prompt del elemento objetivo es siempre visual por defecto — nunca depende de que el audio esté activo (coherente con ADR-002: ningún dato se transmite únicamente por audio). El avatar/sonido narrando el objetivo es una capa opcional que respeta `audioEnabled`/`avatarEnabled` del `ChildProfile`. No existe `promptType` de dominio dentro del motor.
- No existe partida fallida: una ronda incorrecta se reintenta sobre el mismo elemento hasta acierto, nunca avanza con un fallo pendiente ni penaliza de forma visible.
- Tras 2 fallos consecutivos en la misma ronda se activa una señal de ayuda (hint), visual siempre, con refuerzo sonoro/avatar si está habilitado.
- **No existe timeout de motor.** No hay cronómetro visible ni invisible por ronda ni por pregunta — coherente con el mismo principio que llevó a descartar el modo dinámico de burbujas. La única vía de terminar una partida sin respuesta es la inactividad prolongada gestionada por el shell (`SYSTEM_INACTIVITY → ABANDONED`, FEAT-007), ajena a la lógica específica de reconocimiento.
- Las métricas de intentos, tiempos y dificultad son datos para tracking/dashboard adulto, nunca presión visible para el niño.
- El diseño de contenido y payload debe permitir escalar a 8 años (variantes futuras, ver Roadmap) sin rediseñar el contrato del motor.

## Scope and responsibilities

### Backend responsibilities

- Implementación de `RecognitionEngine` conforme al contrato `GameEngine` de FEAT-007 (`initGame`, `processAction`, `getNextElement`, `isGameComplete`, `buildSummary`).
- Payload específico del motor (`RecognitionState`), extendiendo `GameState.payload`.
- Anti-repetición intra-partida dentro de `RecognitionState` (`roundsShownElementIds`).
- Construcción del estado de ronda con `targetElementId`, `optionIds`, contadores de intentos, flags de hint y timestamps necesarios para tracking/dashboard adulto.
- Retry hasta acierto: una respuesta incorrecta mantiene la misma ronda abierta con el mismo `targetElementId` y `optionIds`.
- Activación lógica de `hintActive` tras 2 fallos consecutivos en la misma ronda.
- Aplicación diferida de cambios de dificultad adaptativa durante retries activos mediante `currentDifficultyLevel` y `pendingDifficultyLevel`.
- Esquema de `RecognitionAttemptContext` para el detalle fino persistido vía `ActivityAttempt`.
- Reglas de fin de partida (5 rondas, retry hasta acierto, sin timeout de motor) y de puntuación (`STARS`, mínimo garantizado 1, fórmula concreta).
- Ausencia explícita de `topicId`, `promptType`, `biomeCode`, `totalTimeouts` y cualquier timeout propio del motor.
- Roadmap documentado de variantes futuras de la familia "Reconocimiento" (fuera de implementación en esta feature).

### Frontend responsibilities

- Renderizar el prompt visual obligatorio del elemento objetivo; ningún dato crítico debe depender solo de audio.
- Renderizar las 2-3 opciones táctiles recibidas desde backend y enviar la selección del niño como acción de partida.
- Mantener la misma ronda visible tras una respuesta incorrecta, sin penalización visible ni avance de opciones hasta que backend indique acierto.
- Mostrar el hint visual cuando `hintActive` esté activo, incluyendo el refuerzo visual del elemento correcto.
- Aplicar audio/avatar como capa opcional cuando esté habilitada por perfil y por los eventos/capas correspondientes; el motor no define `promptType`.
- Mostrar al niño únicamente el resultado en estrellas, nunca porcentajes, tiempos, precisión ni detalle de intentos.
- Arte, fondos, skins visuales, animaciones y feedback por bioma.
- Adaptación responsive/mobile de la interacción táctil del minijuego.
- Aplicar el checklist base de accesibilidad visual/táctil: contraste alto, áreas táctiles generosas, ausencia de parpadeos rápidos, tolerancia a imprecisión de toque y animaciones interrumpibles.
- Renderizar la categoría COLOR usando paletas accesibles y diferenciadores no-cromáticos según el `colorVisionMode` resuelto por backend/content.

### Out of scope

- Modo dinámico de "explotar burbuja/hoja" con movimiento y temporizador — descartado explícitamente para v1 y para el escalado a 8 años.
- Variantes de reconocimiento auditivo y reconocimiento por categoría — ver Roadmap, no se implementan en esta feature.
- Subdivisión del topic "animales" en sub-topics por hábitat — v1 usa un único topic.
- Persistencia de anti-repetición entre sesiones de días distintos (alcance C) — v1 solo cubre intra-partida e intra-sesión.
- `biomeCode` como campo independiente — se deriva siempre de `world_host.biome`, nunca se duplica en `launchContext` ni en `RecognitionState`.
- `topicId` propio dentro de `RecognitionState` o `RecognitionAttemptContext` — se deriva siempre de la referencia a `Activity` ya presente en `GameState` base.
- Arte, fondos y skins visuales de bioma como implementación backend o lógica del motor — pertenecen al frontend.
- Ajuste parental de probabilidad de motor/skin por niño — ya anticipado como feature futura independiente en FEAT-008.
- Toggle parental para desactivar la anti-repetición (perfiles sensoriales que prefieren repetición) — anotado como candidato futuro, no implementado en esta feature.
- Soporte de tecnologías de asistencia física avanzada (switch access, lectores de pantalla) — fuera de alcance de esta feature.

## Priority changes outside RecognitionEngine

Esta feature pertenece al motor `RecognitionEngine`, pero requiere cambios prioritarios en módulos cercanos para que el motor sea integrable sin acoplamientos indebidos. Estos cambios no son lógica interna del motor y deben planificarse/revisarse en sus features o sprints correspondientes.

### Game Shell / Game Module

- Ampliar `GameOrchestrator.startGame(...)` con `launchContext?` opcional.
- Resolver candidatos antes de inicializar el motor, combinando `activityId`, categoría, `launchContext.habitatTag` cuando aplique y disponibilidad de contenido.
- Incorporar el registro runtime de anti-repetición intra-sesión, hermano de `GameStateRegistry`: `Map<childSessionId + topicId, elementIds recientes>`.
- Registrar cada `targetElementId` acertado al cerrar la ronda para evitar repetición en partidas posteriores de la misma `ChildSession`.
- Promover `pendingDifficultyLevel` a `currentDifficultyLevel` solo al cerrar ronda con acierto o al finalizar partida, nunca durante un retry activo.
- Mantener la inactividad prolongada como responsabilidad del shell (`SYSTEM_INACTIVITY → ABANDONED`), no del motor.
- Respetar el estado de desbloqueo de categoría antes de iniciar una partida; `RecognitionEngine` solo recibe categorías ya permitidas.

### Content Module

- Crear el catálogo inicial de letras, números, formas, colores y animales.
- Añadir metadato de hábitat a animales para permitir filtrado narrativo sin crear sub-topics por hábitat.
- Definir los 3 biomas iniciales (Granja, Selva, Mar) como datos, preparados para extensión posterior.
- Exponer consulta de candidatos filtrable por `category` y, cuando aplique, por `habitatTag`.
- Mantener la extensibilidad data-driven: añadir biomas o hábitats no debe requerir cambios de código en `RecognitionEngine`.
- Modelar colores por identidad conceptual separada de su renderizado visual.
- Proveer paletas accesibles y diferenciadores no-cromáticos para la categoría COLOR según `colorVisionMode`.

### World Module

- Construir `launchContext` cuando la partida nace desde una situación narrativa de `world`.
- En categoría `ANIMAL`, derivar `habitatTag` desde el destino narrativo activo para priorizar coherencia animal-bioma.
- Enviar `worldHostId`, `habitatTag`, `discoveryElementId` y/o `narrativeContextId` como datos opacos para `game`.
- No introducir `biomeCode` duplicado en `launchContext`; el bioma se deriva de `world_host.biome`.
- No acoplar `RecognitionEngine` a consultas directas sobre `world`.

### Tracking Module

- Persistir `RecognitionAttemptContext` dentro de `ActivityAttempt` como detalle atómico de cada intento.
- Mantener `GameSessionSummary` como agregado de partida, sin detalle fino intento a intento.
- Devolver recomendaciones de dificultad sin exigir aplicación inmediata durante una ronda activa.
- Calcular el estado de desbloqueo de categoría NÚMERO a partir del histórico de aciertos en LETRA y FORMA.
- Mantener el umbral de desbloqueo como configuración, no hardcodeado.
- Exponer datos para dashboard adulto sin filtrar esas métricas hacia la experiencia visible del niño.

### Family Module

- Añadir `colorVisionMode` en `ChildProfile` para configurar necesidades de visión de color del niño.
- Hacer el campo editable por el padre en la configuración del niño.
- Mantener `colorVisionMode` como preferencia de perfil, no como regla interna del `RecognitionEngine`.

### Frontend Module

- Implementar la UI del minijuego como consumidor del estado emitido por backend, sin duplicar reglas de dominio del motor.
- Preparar assets visuales por bioma y categoría sin convertirlos en contrato backend.
- Mostrar estados de retry, hint y resultado de estrellas de forma positiva y no punitiva.
- Aplicar las paletas accesibles y diferenciadores no-cromáticos disponibles para la categoría COLOR.
- Cumplir el checklist base de accesibilidad aplicable a interacción, animación, feedback visual y ausencia de dependencia del audio.

## Contrato de inicio de partida — `launchContext`

`GameOrchestrator.startGame(...)` se amplía con un contexto opcional de lanzamiento:

```
startGame(childSessionId, activityId, launchContext?)

launchContext {
  worldHostId?
  habitatTag?
  discoveryElementId?
  narrativeContextId?
}
```

Criterio de arquitectura:

- `world` decide el contexto narrativo y lo entrega al iniciar partida.
- `game` recibe ese contexto como dato de entrada opaco — no interpreta su significado narrativo, solo lo usa como parámetro de filtrado hacia `content` cuando aplica (p. ej. `habitatTag` para categoría `ANIMAL`).
- `RecognitionEngine` no consulta `world` en ningún momento; el motor no recibe `launchContext` directamente, solo el conjunto de candidatos ya resuelto por el orquestador (ver flujo siguiente).
- `biomeCode` no existe como campo independiente: el esquema de `world_host` ya contiene la propiedad `biome`, por lo que se deriva de `worldHostId` sin duplicidad.
- El bioma no modifica el contrato base del motor; solo condiciona qué candidatos llegan a `initGame`.

## Flujo de selección de contenido y anti-repetición

```
GameOrchestrator
  → obtiene candidatos desde content (filtrados por category y, si aplica, por habitatTag de launchContext)
  → consulta el registro de elementos recientes de la ChildSession (anti-repetición intra-sesión)
  → descarta de los candidatos los elementos mostrados recientemente
  → inicializa RecognitionEngine con el conjunto de candidatos ya filtrado
```

- El registro de anti-repetición intra-sesión vive en el módulo `game`, como infraestructura runtime hermana de `GameStateRegistry`: `Map<childSessionId + topicId, elementIds recientes>`. Se descarta al cerrarse la `ChildSession`, mismo patrón que el ajuste de engagement de `world` (FEAT-008).
- El motor no consulta este registro ni conoce `ChildSession` más allá del `GameState` recibido — decide el siguiente elemento (`getNextElement()`) únicamente dentro del conjunto de candidatos ya permitido.
- Al finalizar cada ronda con acierto, el orquestador registra el `targetElementId` mostrado en el registro de sesión, para que no se repita en partidas posteriores de la misma sesión.
- La anti-repetición intra-partida (no repetir el elemento objetivo de la ronda anterior dentro de la misma partida) se resuelve dentro del propio `RecognitionState` (`roundsShownElementIds`), sin depender del registro de sesión.

## Payload `RecognitionState` (extensión de `GameState.payload`)

```
RecognitionState {
  recognitionCategory        // LETRA | NUMERO | FORMA | COLOR | ANIMAL
  roundIndex
  totalRounds                // fijo en 5 para v1, configurable como dato
  targetElementId
  optionIds                  // 2-3 ids mostrados en la ronda actual
  selectedOptionId?
  roundsShownElementIds       // anti-repetición intra-partida
  currentRoundAttemptCount
  currentRoundConsecutiveFailures
  totalIncorrectAttempts
  totalCorrectFirstTry
  hintActive
  hintTriggeredAtAttempt?
  roundStartedAt
  lastActionAt
  currentDifficultyLevel
  pendingDifficultyLevel?
}
```

Notas:

- Sin `topicId` propio — se deriva de la referencia a `Activity` ya presente en `GameState` base.
- Sin `totalTimeouts` ni ningún campo relacionado con timeout — no existe esa noción en el motor.
- Sin `promptType` — la presentación visual/audio/avatar es responsabilidad de capas fuera del dominio del motor.
- `recognitionCategory` mantiene el nombre revisado en la ronda de observaciones; el nombre definitivo (`category` vs `recognitionCategory` vs otra alternativa) queda como revisión abierta no bloqueante, a resolver en diseño técnico final.

## Dificultad adaptativa diferida

Una ronda incorrecta no avanza — el niño reintenta sobre el mismo `targetElementId` y `optionIds` hasta acertar. Si `tracking` devuelve un cambio de dificultad durante ese retry, aplicarlo de inmediato podría alterar una ronda ya iniciada.

Regla:

```
Los cambios de dificultad devueltos por tracking durante una ronda activa
se aplican después de cerrar esa ronda (con acierto) o al finalizar el minijuego,
nunca en mitad de un retry activo.
```

Implementación vía `currentDifficultyLevel` (aplicado a la ronda en curso) y `pendingDifficultyLevel` (devuelto por tracking, pendiente de promoción). El `GameOrchestrator` promueve `pendingDifficultyLevel` a `currentDifficultyLevel` al cerrar la ronda con acierto, antes de generar la siguiente vía `getNextElement()`.

## `RecognitionAttemptContext`

Concreción del `attemptContext` genérico ya previsto en el puerto de tracking (FEAT-007):

```
RecognitionAttemptContext {
  engineType: RECOGNITION
  recognitionCategory
  roundIndex
  targetElementId
  selectedOptionId
  optionIds
  isFirstTry
  hintActive
  hintTriggeredBeforeAnswer
  attemptNumberInRound
  responseTimeMs
}
```

Sin `topicId` propio, por el mismo criterio que en `RecognitionState`.

## Persistencia: `ActivityAttempt` vs `GameSessionSummary`

- `ActivityAttempt` (vía `RecognitionAttemptContext`): detalle atómico de cada intento — tiempos, aciertos, hints, número de intento dentro de la ronda.
- `GameSessionSummary`: resumen agregado de la partida completa, sin el detalle fino intento a intento.
- El dashboard adulto puede combinar ambos modelos si necesita detalle y resumen a la vez.
- El niño solo ve el resultado en estrellas, nunca el detalle fino.

## Fin de partida, reintentos y puntuación

- **Comportamiento ante error**: `processAction()` devuelve `CORRECT` o `INCORRECT` — no existe `TIMEOUT` como resultado del motor. Ante `INCORRECT`, la ronda permanece abierta con el mismo `targetElementId`/`optionIds`; el intento se registra igualmente vía `tracking.registerAttempt(...)`.
- **Hint**: tras 2 intentos fallidos consecutivos en la misma ronda (`currentRoundConsecutiveFailures >= 2`) se activa `hintActive`. Visual siempre (resalte del elemento correcto), con refuerzo sonoro/avatar si está habilitado — el visual nunca es condicional a que el audio esté apagado.
- **Condición de fin (`isGameComplete`)**: 5 rondas fijas por partida, configurables como dato. Al garantizar acierto final por ronda (retry), la partida siempre se completa con éxito — nunca hay partida fallida, solo `ABANDONED` por inactividad prolongada gestionada por el shell.
- **Puntuación (`metricType: STARS`)**, mínimo garantizado de 1 estrella:

```
3 estrellas:
  - 4 o más de las 5 rondas acertadas al primer intento
  - y tiempo medio de respuesta dentro de un umbral considerado bueno

2 estrellas:
  - partida completada
  - y media de intentos por ronda <= 2

1 estrella:
  - partida completada, aunque haya muchos retries (mínimo garantizado)
```

El niño solo ve las estrellas — nunca porcentajes, precisión ni tiempos. El dashboard adulto accede al detalle vía `ActivityAttempt`/`GameSessionSummary`.

## Roadmap de la familia "Reconocimiento" (anticipado para features futuras)

No se implementa en esta feature. Las variantes futuras serán motores propios cuando cambie la semántica de evaluación — no ramas internas activadas por flags dentro de `RecognitionEngine`. Podrán compartir infraestructura común (registro anti-repetición, catálogo de `content`, coherencia por bioma en `world`).

| Variante | Mecánica | Aporte al escalado 3-4 → 8 años |
|---|---|---|
| `RecognitionEngine` (esta feature) | Prompt visual + tap sobre 1 de 2-3 opciones | Base v1 |
| Reconocimiento auditivo | Se reproduce el sonido/palabra del elemento sin icono de referencia inicial; el niño busca la imagen | Introduce el canal auditivo como reto, no solo como apoyo |
| Reconocimiento por categoría | Prompt de categoría en vez de elemento concreto (p. ej. "toca el animal que vive en el mar"), usando el metadato de hábitat ya modelado en `content` | Escalado más barato: mismo motor base y mismo dataset, solo cambia la construcción del prompt. No confirmada para v1 — prioridad v1 es prompt concreto ("toca la vaca") |

## Historial de decisiones

### Sesión 1 — Diseño del motor

| # | Tema | Decisión |
|---|------|----------|
| 1 | Mecánica core | Selección por prompt: elemento objetivo + 2-3 opciones, tap simple. Se descartan "intruso" y "emparejar con modelo". |
| 1 | Número de opciones en v1 | 2-3. |
| 1 | Sub-minijuego dinámico de burbujas/hojas | Descartado para v1 y para el roadmap de escalado a 8 años. |
| 2 | Presentación del prompt | Visual siempre y obligatorio. Audio/avatar opcional según `ChildProfile`. Sin campo `promptType`. |
| 3 | Categorías de contenido v1 | Letras, números, formas, colores, animales (con metadato de hábitat). |
| 3 | Granularidad de topic animales | Único topic "animales" en v1, con metadato de hábitat disponible para subdividir en el futuro. |
| 3 | Mezcla de topics en una partida | No — un único topic por partida. |
| 4 | Alcance anti-repetición | Intra-partida + intra-sesión. Sin persistencia inter-sesión en v1. |
| 5 | Biomas v1 | Granja, Selva, Mar. Bosque y Mascotas quedan para siguiente iteración de contenido. |
| 5 | Extensibilidad de biomas | Data-driven en `content`, sin lógica hardcodeada; añadir bioma no requiere cambios de código en backend. |
| 5 | Coherencia narrativa animal-bioma | `world` prioriza animal cuyo hábitat coincide con el destino activo, sin sub-topics ni impacto en el contrato del motor. |
| 6 | Modelo técnico de variantes futuras | Motores propios, no ramas internas por flag. |
| 7 | Fin de partida y puntuación (versión inicial) | 5 rondas, retry hasta acierto, hint tras 2 fallos, `STARS` con mínimo 1 — revisado y corregido en Sesión 2. |

### Sesión 2 — Revisión de integración con World y Game Shell

| # | Tema | Decisión |
|---|------|----------|
| 1 | Contexto de lanzamiento desde `world` | `launchContext` opcional en `startGame(...)`: `worldHostId`, `habitatTag`, `discoveryElementId`, `narrativeContextId`. Sin `biomeCode` — se deriva de `world_host.biome`. |
| 2 | Flujo de anti-repetición intra-sesión | `GameOrchestrator` filtra candidatos de `content` por elementos recientes de sesión antes de inicializar el motor; el motor solo elige dentro del conjunto permitido. |
| 3 | Prompt visual/audio/avatar | Confirmado sin cambios: sin `promptType`, visual obligatorio, resto opcional vía WebSocket. |
| 4 | Ampliación de `RecognitionState` | Aceptada con ajustes: se añade `roundIndex`, `totalRounds`, `selectedOptionId?`, contadores de intentos/fallos, `hintActive`, timestamps y campos de dificultad diferida. Se descarta `totalTimeouts` (no hay timeout de motor) y `topicId` propio (se deriva de `Activity`). |
| 5 | Aplicación de cambios de dificultad | Diferida — nunca se aplica en mitad de un retry activo; se promueve `pendingDifficultyLevel` al cerrar ronda con acierto. |
| 6 | `RecognitionAttemptContext` | Esquema aceptado para el detalle de intento persistido en `ActivityAttempt`. |
| 7 | `ActivityAttempt` vs `GameSessionSummary` | Corrección aplicada: el detalle fino vive en `ActivityAttempt`, no en `GameSessionSummary` (solo agregado). |
| 8 | Timeout de motor | Eliminado — `RecognitionEngine` no genera `TIMEOUT` propio; solo `CORRECT`/`INCORRECT`. La inactividad prolongada la gestiona el shell. |
| 9 | Fórmula de `STARS` | Concretada con umbrales de rondas al primer intento y media de intentos por ronda. |
| 10 | Nombre de `category` / `recognitionCategory` | Abierto, no bloqueante — a resolver en diseño técnico final. |
| 11 | Variantes futuras de la familia Reconocimiento | Confirmado el criterio de motores propios; reconocimiento por categoría queda en roadmap, no comprometido para v1. |
| — | `biomeCode` en `launchContext` | Eliminado como campo independiente — se deriva de `world_host.biome`. |
| — | `topicId` duplicado en payload | Descartado — se deriva siempre de la referencia a `Activity` en `GameState` base. |

### Sesión 3 — Fricciones cognitivas y accesibilidad

| # | Tema | Decisión |
|---|------|----------|
| 1 | Categoría NÚMERO en v1 | Se mantiene en el catálogo pero bloqueada por defecto; se desbloquea cuando `tracking` detecta índice alto de aciertos en LETRA (todas las dificultades) y FORMA. Umbral configurable, no hardcodeado. |
| 2 | Configuración de daltonismo | Entra en el alcance de esta feature como prerrequisito de módulos superiores: `colorVisionMode` en `family`, paleta accesible y diferenciador no-cromático en `content`. Sin impacto en el contrato del motor. |
| 3 | Checklist de accesibilidad 3-4 años | Documentado como referencia base no exhaustiva (contraste, sin codificación solo por color, sin parpadeos rápidos, área táctil generosa, juego 100% jugable sin audio, tap simple, sin presión de tiempo, feedback predecible, animaciones interrumpibles). |
| 3 | Preferencia por repetición en ciertos perfiles sensoriales | Identificada como fricción no resuelta frente al mecanismo de anti-repetición — anotada como candidata a toggle parental futuro, no bloqueante para v1. |
| 3 | Tecnologías de asistencia física avanzada | Explícitamente fuera de alcance de esta feature. |

## Notas cruzadas pendientes

Estas notas son referencias de trazabilidad; el detalle operativo queda separado en `Priority changes outside RecognitionEngine`.

- **FEAT-003 (Content)**: catálogo inicial, metadatos de hábitat, biomas iniciales, consulta filtrable por `category`/`habitatTag` y soporte de color accesible por `colorVisionMode`.
- **FEAT-006 (Tracking)**: recibe `RecognitionAttemptContext`, aplica dificultad diferida y calcula el desbloqueo de NÚMERO desde el histórico de LETRA/FORMA.
- **FEAT-007 (Game Shell Module)**: incorpora anti-repetición intra-sesión, `launchContext?` y filtrado previo a la inicialización del motor.
- **FEAT-008 (World Module)**: construye `launchContext` con `habitatTag` y datos narrativos opacos cuando la partida nace desde `world`.
- **FEAT-002/Family (perfil del niño)**: incorpora `colorVisionMode` en `ChildProfile`, editable por el padre.

## Estado del debate

Todos los puntos identificados en las tres sesiones (diseño inicial, revisión de integración, y fricciones cognitivas/accesibilidad) quedan resueltos y documentados en el Historial de decisiones. No quedan puntos pendientes bloqueantes, salvo dos anotaciones explícitamente no bloqueantes: el nombre definitivo de `recognitionCategory` (Sesión 2, punto 10) y el posible toggle parental de anti-repetición para perfiles sensoriales que prefieren la repetición (Sesión 3).

## Progresión y desbloqueo de categoría NÚMERO

Riesgo identificado: el reconocimiento simbólico numérico (asociar el símbolo con la cantidad) es más tardío y variable en el desarrollo infantil que letras, formas o colores. Introducirlo al mismo nivel que el resto puede generar fricción, especialmente en el extremo más joven del rango (3 años).

Decisión: NÚMERO permanece en el catálogo v1, pero **bloqueada por defecto**. Se desbloquea solo cuando el niño demuestra un índice alto de aciertos en LETRA (en todas sus dificultades) y FORMA. El umbral concreto es un parámetro configurable, no hardcodeado (mismo patrón que `engagementThresholdConfig` de `world`).

Mecanismo:

- `tracking` calcula el estado de desbloqueo agregando aciertos-al-primer-intento de `ActivityAttempt` en categorías LETRA y FORMA a través de todas las dificultades ya vistas por ese niño.
- `TopicSelectionService` respeta este estado al proponer categoría candidata: si NÚMERO no está desbloqueado, no entra en el conjunto de categorías candidatas.
- `RecognitionEngine` no conoce el bloqueo — recibe únicamente categorías ya filtradas, mismo patrón ya usado para el filtro de hábitat.
- Beneficio adicional: el desbloqueo es un hito visible y positivo en el dashboard parental ("números desbloqueados"), reforzando la narrativa de progresión sin exponer al niño a fracaso.

## Accesibilidad — modo de visión de color (`colorVisionMode`)

Se prioriza como trabajo de prerrequisito en los módulos superiores, antes/junto con la implementación de `RecognitionEngine`.

- Nuevo campo en `ChildProfile` (`family`): `colorVisionMode` — enum `NONE | PROTANOPIA | DEUTERANOMALIA... | DEUTERANOPIA | TRITANOPIA | ACHROMATOPSIA` (naming exacto a definir en diseño técnico, alcance funcional ya fijado).
- `content` modela cada color por identidad conceptual (p. ej. "rojo") separada de su valor de renderizado, con variantes de paleta accesible por `colorVisionMode`.
- Regla transversal: ningún dato del juego se codifica únicamente por matiz de color — la categoría COLOR incorpora un diferenciador no-cromático (forma/icono/etiqueta) para seguir siendo jugable incluso con `ACHROMATOPSIA`.
- `RecognitionEngine` no cambia — sigue pidiendo/recibiendo `elementId` abstracto; la resolución visual vive en `content` + frontend.

## Consideraciones de accesibilidad (checklist base, no exhaustivo)

Referencia normativa razonable para el rango 3-4 años; no sustituye una auditoría de accesibilidad completa ni cubre todos los perfiles posibles.

| Ámbito | Consideración | Estado |
|---|---|---|
| Visual | Contraste alto (referencia WCAG AA/AAA) | A verificar en diseño visual, fuera del alcance de backend |
| Visual | Nunca codificar información solo por color | Regla transversal — ya aplicada a COLOR, extensible al resto |
| Visual | Sin parpadeos/destellos rápidos (máx. 3/s) | Aplica a animación de hint y celebraciones — nota para frontend |
| Visual | Área táctil mayor que el icono visible | Nota para frontend/diseño de interacción |
| Auditivo | Juego 100% jugable con audio apagado | Ya decidido, extendido explícitamente a hint y feedback |
| Auditivo | Sin sonidos súbitos o muy fuertes | Nota para frontend/avatar |
| Motor | Solo tap simple, sin gestos complejos | Ya decidido (ADR-005) |
| Motor | Tolerancia a imprecisión de toque | Nota para frontend |
| Cognitivo | Sin presión de tiempo ni derrota visible | Ya decidido |
| Cognitivo | Feedback predecible y consistente | Ya decidido (ADR-003) |
| Cognitivo | Animaciones interrumpibles | Ya decidido (ADR-003) |
| Cognitivo | Preferencia por repetición en ciertos perfiles sensoriales vs. anti-repetición del motor | Abierto — candidato a toggle parental futuro, no bloqueante para v1 |
| Fuera de alcance | Tecnologías de asistencia física avanzada (switch access, lectores de pantalla) | Explícitamente fuera de esta feature |
