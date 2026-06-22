# FEAT-008 - Backend: World Module

## Status

state: proposal
user_history: Orquestación backend del World Map — destino narrativo, selección de actividad, señales de engagement
depends_on: content, tracking, game, session
future_depends_on: avatar (eventos recomendados, sin invocación directa)
blocked_by: none
test: unit + integration + contract
sprints:

## Origen

Este documento formaliza el debate de arquitectura del módulo `world`. Se origina en la sección "Backend Responsibilities" extraída del análisis de FEAT-011 (World Map, frontend), que enumera responsabilidades de backend sin un módulo dueño definido. Este FEAT se implementa después de FEAT-007 (Game Shell Module), del que depende para arrancar partidas.

Texto origen (FEAT-011, Backend Responsibilities):

> The backend remains the source of truth for progression, content selection, and descriptive tracking. The backend is responsible for: deciding the current destination and next destination; selecting host, narrative situation, object, and associated activity; managing the LearningPath that remains invisible to the child; deciding which engine or minigame each discovery element contains; managing consistent patterns of ignored elements, abandoned activities, or engagement when applicable; defining what constitutes a consistent pattern and how it temporarily affects engine priority; defining inactivity thresholds and automatic minigame exit; recording started, ignored, abandoned, and completed activities; providing descriptive data for the parent dashboard; avoiding diagnoses or automatic pedagogical conclusions from isolated signals.

## Description

El módulo `world` orquesta la experiencia de paseo/descubrimiento del World Map: decide qué destino narrativo mostrar al niño en cada momento, qué actividad asociada corresponde a cada elemento de descubrimiento, y gestiona señales de engagement (elementos ignorados, actividades abandonadas) sin convertirlas en diagnóstico pedagógico.

Es una capa de orquestación nueva, distinta de `game` (que opera dentro del ciclo de vida de una partida individual) y distinta de `content` (que posee el catálogo estático). `world` consume ambos para tomar decisiones de secuencia narrativa en tiempo real.

## Feature source of truth

Esta feature es la fuente de verdad para el módulo `world` en v1. El documento `docs/design/backend_design_v1.docx` se considera un esquema general de la aplicación y puede quedar desactualizado frente a decisiones más concretas tomadas en las features. Cuando haya diferencias, prevalecen las decisiones de esta feature y las features dependientes (`content`, `tracking`, `game`, `session`) una vez actualizadas.

FEAT-008 se apoya en el estado real del backend tras los sprints archivados de FEAT-007: `GameSessionSummary` existe desde Sprint 032, el game shell está implementado hasta Sprint 044, y `GameOrchestrator` ya expone flujo de inicio de partida usando `activityId` como entrada principal. `world` debe integrarse con ese contrato real en vez de redefinir un inicio paralelo.

## Principios para niños de 3-4 años y escalabilidad

La v1 está optimizada para niños de 3-4 años, pero el diseño debe poder escalar hasta 8 años sin rehacer la arquitectura. La escalabilidad debe venir de contenido, dificultad, narrativa, selección de actividad y configuración, no de exponer al niño estados de progreso o mecánicas de presión.

- El World Map es un paseo narrativo, no un selector de niveles.
- Los discovery elements son siempre opcionales. Ignorarlos no es un fallo.
- Abandonar una actividad, no tocar un elemento o repetir una acción son comportamientos normales en niños pequeños.
- No se muestran diagnósticos, etiquetas, porcentajes de engagement, progreso interno, locks ni estados de dificultad al niño.
- El ajuste de prioridad por engagement es temporal, suave, reversible y limitado a la `ChildSession` activa.
- La selección de actividad puede variar por edad y dificultad, pero la experiencia infantil sigue siendo de descubrimiento.
- El backend nunca devuelve al frontend infantil labels child-facing como `ignored`, `abandoned`, `low engagement` o equivalentes diagnósticos.
- Si una actividad no puede iniciarse o se abandona, el paseo continúa con una salida amable y sin error técnico visible.

## Scope

In scope:

- Estado runtime del paseo en memoria (destino actual, host actual, elementos mostrados en la sesión de paseo activa).
- Lógica de selección narrativa: próximo destino, host, situación narrativa, elemento de descubrimiento.
- Lógica de patrón temporal de engagement (abandonos de partida concentrados en un mismo `engineType` dentro de la `ChildSession` activa) y su efecto en ajuste temporal de prioridad de motor, con umbrales configurables vía `engagementThresholdConfig(childProfileId)`.
- Timeout de inactividad de paseo, con campo propio `lastWorldActivityAt` y evento `WORLD_HEARTBEAT` distintos del mecanismo de inactividad de partida ya cerrado en FEAT-007.
- Integración con `game` vía `GameOrchestrator.startGame(...)` para arrancar partidas cuando un elemento de descubrimiento es punto de entrada a actividad, y suscripción al evento de dominio de finalización de partida.
- Integración con `tracking` para registrar señales de interacción/exploración con valor de dashboard.
- Integración con `TopicSelectionService` (tracking) para obtener el topic pedagógicamente recomendado antes de elegir actividad y narrativa.
- Decisión del momento exacto de avance de `LearningPathStep`, tras completar la secuencia narrativa de llegada al destino (no automáticamente al recibir `GAME_COMPLETED`).
- Registro del ciclo started/ignored de propuestas de actividad (puntos de entrada a minijuego), vía nuevos puertos de tracking.
- Aporte de datos para una nueva query de dashboard parental sobre actividades iniciadas/ignoradas por tipo de motor, expuesta por tracking (no por world).

Out of scope:

- Catálogo estático de `LearningPath`, hosts, narrativa, elementos de descubrimiento (pertenece a `content`).
- Progreso persistente del niño en el `LearningPath` (pertenece a `tracking`).
- Motores de minijuego y ciclo de vida de partida (pertenece a `game`).
- Invocación directa de TTS/avatar (pertenece a `avatar`).
- Diagnóstico o conclusiones pedagógicas persistentes sobre el niño.
- Motor de paseo en frontend / precarga de múltiples destinos resueltos por adelantado (ver Decisión de Arquitectura más abajo).
- Criterio de selección pedagógica de topics (clasificación WEAK/MEDIUM/STRONG y distribución por dificultad) — pertenece en exclusiva a `TopicSelectionService` en `tracking`, no se reimplementa en `world`.
- Persistencia directa de `ChildLearningProgress`/`ChildLearningCompletedStep` — `world` decide cuándo avanzar, pero la escritura siempre pasa por un puerto de `tracking`, nunca por acceso directo a esas tablas.
- Persistencia de elementos decorativos/simple-interactivos ignorados — quedan en memoria de `world`, sin trascender a tracking.

## Decisiones de Arquitectura (registro de sesión de debate)

| # | Tema | Decisión |
|---|------|----------|
| 1 | Nombre del módulo | `world`. |
| 1 | Qué posee `world` | Estado runtime del paseo en memoria (análogo a `GameState` pero de paseo, no de partida, incluyendo `lastWorldActivityAt` — ver punto 7), lógica de selección narrativa de destino/host/elemento, lógica de patrón temporal de engagement, timeout de inactividad de paseo. |
| 1 | Qué NO posee `world` | Catálogo estático (vive en `content`), progreso persistente del niño (vive en `tracking`), motores de juego (viven en `game`), invocación TTS/avatar (vive en `avatar`). |
| 1 | Dependencias de `world` | `content`, `tracking`, `game`, `session`. Dependencia unidireccional: ningún otro módulo conoce `world`. |
| 1 | Integración con `tracking` | Mismo patrón ya establecido para `game` en FEAT-007: puertos explícitos invocados por `world`, nunca tablas propias de `world` que el dashboard necesite leer directamente. |
| 1 | Integración con `game` | `world` decide qué actividad jugar y llama a `GameOrchestrator.startGame(...)` (contrato detallado en punto 4). `game` no conoce `world`, mantiene su agnosticismo respecto a cómo se llegó a jugar. |
| 1 | Integración con `avatar` | `world` recomienda eventos de avatar como dato de dominio (mismo patrón que `avatarEventType` en `ActionResult` de FEAT-007), nunca invoca avatar directamente. |
| 2 | Motor de paseo en frontend / precarga de destinos | Descartado. La dificultad adaptativa es reactiva (FEAT-006): el progreso del niño cambia tras cada intento, por lo que no tiene sentido resolver con antelación una secuencia de destinos/actividades que podría quedar desactualizada antes de mostrarse. `world` sigue siendo la única fuente de decisión destino-a-destino, evaluada en el momento, no precomputada en lote. |
| 2 | Qué sí puede vivir en frontend sin tocar este principio | El esqueleto estático del `LearningPath` (estructura ordenada de pasos, dato de `content`) puede cachearse en cliente para continuidad visual, igual que el catálogo decorativo (hosts, elementos no narrativos). Esto no incluye qué actividad corresponde a cada paso ni cuándo presentarlo — eso sigue siendo decisión de `world` en tiempo real. |
| 2b | Quién decide qué topic reforzar pedagógicamente | `TopicSelectionService` (tracking), reutilizado tal cual por `world`. No se reimplementa criterio pedagógico propio en `world`. |
| 2b | Quién decide qué actividad concreta entre las compatibles con ese topic | `world`, consultando `content` para conocer las `Activity` compatibles con el topic devuelto por tracking. `TopicSelectionService` resuelve el topic, no la actividad. |
| 2b | Quién decide host/situación narrativa/objeto | `world` en exclusiva — decisión puramente narrativa, sin dependencia de datos de progreso del niño. |
| 2b | Dónde se aplica el ajuste temporal de prioridad de motor (patrón de engagement, ver punto 5) | Sobre la elección de actividad concreta dentro de `world`, nunca sobre la selección de topic de tracking. La selección de topic permanece neutral y gobernada exclusivamente por tracking. |
| 2b | Flujo de la cadena de decisión | `world` llama a `TopicSelectionService.selectTopic(...)` → recibe `topicId` → consulta `content` por `Activity` compatibles con ese topic → aplica criterio narrativo y ajuste temporal de prioridad para elegir la actividad concreta → decide host/situación narrativa → construye el destino. |
| 3 | Descomposición de "managing the LearningPath" | Se separa en cuatro operaciones con dueños distintos: lectura de progreso (tracking), lectura de catálogo estático (content), decisión de cuándo avanzar (world), persistencia del avance (tracking). "Managing" no implica propiedad de datos por parte de `world`. |
| 3 | Quién decide el momento de avance de un `LearningPathStep` | `world`, tras completar la secuencia narrativa completa de llegada al destino (microinteracción + celebración, según FEAT-011) — no automáticamente al recibir `GAME_COMPLETED` de `game`. |
| 3 | Quién persiste el avance | `tracking`, vía nuevo puerto `registerLearningPathStepProgress(childProfileId, learningPathId, learningPathStepId)`, que actualiza `ChildLearningProgress` y `ChildLearningCompletedStep` y devuelve el nuevo estado de progreso. |
| 3 | `world` escribe directamente `ChildLearningProgress`/`ChildLearningCompletedStep` | No, nunca. Mismo principio de propiedad ya aplicado a `GameSessionSummary` en FEAT-007. |
| 3 | Cómo se relaciona con el flujo de `GameOrchestrator` | `game` sigue sin conocer `world`. Es `world` quien invoca `GameOrchestrator.startGame(...)` y por tanto puede recibir la finalización de esa partida concreta vía evento de dominio (mecanismo resuelto en punto 4). |
| 4 | Contrato expuesto por `GameOrchestrator` hacia `world` | `startGame(childSessionId, activityId) -> { gameId, status: STARTED \| REJECTED, rejectionReason? }`, alineado con el game shell ya implementado hasta Sprint 044. `world` decide la actividad, no la dificultad; la dificultad vigente la resuelve `game`/`tracking` según FEAT-007. |
| 4 | Motivos válidos de rechazo (`REJECTED`) | Perfil bloqueado (`ChildProfile.estado == BLOCKED`), actividad con `status != ACTIVE` en content, partida ya en curso para esa `childSessionId` en `GameStateRegistry`. |
| 4 | Reacción de `world` ante `REJECTED` | Buscar actividad alternativa compatible con el mismo topic recomendado por tracking, o presentar el destino sin elemento jugable (puramente narrativo/decorativo). Nunca falla silenciosamente ni bloquea el paseo del niño. |
| 4 | Mecanismo de notificación de finalización de partida | Evento de dominio interno (`ApplicationEventPublisher` de Spring, ya disponible en el stack sin infraestructura nueva): `GameOrchestrator` publica `GameSessionCompletedEvent(gameId, childSessionId, activityId, finalStatus)` sin conocer quién escucha; `world` se suscribe como uno de los posibles oyentes. |
| 4 | Por qué no se usa retorno asíncrono ni callback | Retorno asíncrono no es viable porque una partida puede tardar minutos en completarse (el niño juega activamente). Callback introduce acoplamiento inverso encubierto: `GameOrchestrator` tendría que aceptar y almacenar una referencia de un módulo que, por diseño, no debe conocer. |
| 4 | Reacción de `world` al evento `GameSessionCompletedEvent` | Si `finalStatus == COMPLETED` y la partida corresponde a un `LearningPathStep`: reproduce la secuencia narrativa de llegada y llama a `tracking.registerLearningPathStepProgress(...)` (punto 3). Si `finalStatus == ABANDONED`: decide continuidad del paseo sin avanzar el step. |
| 5 | Qué señales cuentan para el "patrón consistente" | Solo abandonos de partida (`ABANDONED`) afectan el ajuste temporal de prioridad en v1. Elementos de descubrimiento ignorados (`IGNORED`) se registran y pueden mostrarse como dato descriptivo al adulto, pero no reducen prioridad de motor en v1 porque son una señal demasiado débil por sí sola. |
| 5 | Ventana y condición de patrón | Últimas N interacciones de inicio de actividad dentro de la `ChildSession` activa; patrón si M de N terminan en `ABANDONED` concentrados en el mismo `engineType`. Mismo principio de ventana deslizante + mínimo de muestras ya usado en dificultad adaptativa (FEAT-006), reutilizado en vez de inventar un mecanismo distinto. |
| 5 | Valores iniciales de N y M | N=3, M=2 como valores de fábrica. Un solo abandono (1 de N) nunca es suficiente — coherente con la prohibición de FEAT-011 de interpretar "isolated signals". |
| 5 | Efecto del patrón | Ajuste *temporal* de probabilidad de selección del motor afectado en la cadena de decisión del punto 2b. Nunca exclusión total — el motor sigue siendo elegible. |
| 5 | Alcance temporal del ajuste | Limitado a la `ChildSession` activa; se resetea al cerrar sesión. Nunca persiste entre sesiones — es la salvaguarda principal contra construir un perfil acumulativo no autorizado del niño. Si el patrón se repite sesión tras sesión, es dato para el dashboard parental (punto 8), no acción autónoma y persistente del sistema. |
| 5 | Configurabilidad de N y M | Configuración propia de `world` en v1, expuesta internamente como `engagementThresholdConfig(childProfileId)`. Debe usar defaults globales por datos/configuración, no constantes dispersas en lógica de negocio. |
| 5 | Resolución de la configuración (v1 vs futuro) | En v1, `engagementThresholdConfig` devuelve valores globales por defecto (N=3, M=2). En una feature futura de ajuste parental por niño se añadirá resolución con herencia nullable — mismo patrón usado en `family`/`ChildProfile` para `audioEnabled`/`avatarEnabled`. |
| 6 | Elementos decorativos ignorados (nube, pájaro, etc.) | No se persisten en tracking. Quedan como estado efímero en memoria de `world`, sin valor de dashboard ni pedagógico a largo plazo — coherente con que su único efecto definido (punto 5) es, como mucho, variación visual menor. |
| 6 | Puntos de entrada a actividad ignorados | Sí se persisten en tracking. Tienen valor descriptivo de dashboard (punto 8) y son la señal "ignored" que el patrón de engagement (punto 5) distingue de `ABANDONED`, aunque solo `ABANDONED` cuenta para el umbral de ajuste de prioridad. |
| 6 | Por qué no encaja en `ActivityAttempt` ni en `GameSessionSummary` | `ActivityAttempt` requiere un `result` real (`CORRECT`/`INCORRECT`/`TIMEOUT`) que no aplica si nunca hubo intento. `GameSessionSummary` requiere `finalStatus` (`COMPLETED`/`ABANDONED`) que no aplica si nunca existió `GameState`. Forzar "ignorado" en cualquiera de las dos rompería su semántica ya cerrada. |
| 6 | Nueva entidad en tracking | `ActivityProposalLog`: `id`, `childProfileId`, `childSessionId`, `activityId`, `topicId`, `outcome` (`STARTED`, `IGNORED`), `proposedAt`, `resolvedAt`, `createdAt`, `updatedAt`. Complementaria a `GameSessionSummary`, no redundante: cubre la fase de propuesta anterior a que exista partida; `GameSessionSummary` sigue siendo la única fuente de completed/abandoned. |
| 6 | Quién escribe `ActivityProposalLog` | `world`, vía dos nuevos puertos de tracking: `registerActivityProposal(childProfileId, childSessionId, activityId, topicId)` al presentar el elemento, y `resolveActivityProposal(proposalId, outcome)` cuando se determina si el niño interactuó o no. Mismo patrón de puertos explícitos ya usado en toda la integración `world`→tracking. |
| 6 | Flujo completo | `world` presenta elemento de descubrimiento punto de entrada a actividad → `registerActivityProposal(...)` → ventana de exploración (timer de "interaction pause" de FEAT-011) → si el niño interactúa y `world` llama a `GameOrchestrator.startGame(...)`: `resolveActivityProposal(proposalId, STARTED)`; si el paseo avanza sin interacción: `resolveActivityProposal(proposalId, IGNORED)`. |
| 6 | Resolución automática de propuestas pendientes | Si existe una propuesta pendiente y se crea una nueva, se cierra la anterior como `IGNORED`. Si la sesión se cierra, expulsa, bloquea o expira por inactividad, toda propuesta pendiente se cierra como `IGNORED`. Implementación v1 puede ser simple; debe dejarse un warning técnico para hardening posterior de reintentos/concurrencia. |
| 7 | Mecanismo de inactividad de partida (FEAT-007) | Sin cambios — `lastActivityAt`/`GAME_HEARTBEAT` siguen siendo exclusivos del ciclo de vida de `GameState`, no se tocan ni se reutilizan para el paseo. |
| 7 | Mecanismo de inactividad de paseo (nuevo, en `world`) | Campo `lastWorldActivityAt` en el estado runtime de paseo (en memoria, análogo a `GameState`), con evento propio `WORLD_HEARTBEAT` (distinto de `GAME_HEARTBEAT`) y umbral propio configurable, más permisivo que el de partida. `WORLD_HEARTBEAT` también actualiza `ChildSession.lastActivityAt` vía el caso de uso de `session`, para mantener viva la sesión infantil mientras no hay partida activa. |
| 7 | Por qué no compartir heartbeat/campo con `game` | `GameState` no existe la mayor parte del tiempo que el niño pasa en el mapa (no hay partida activa mientras explora). Forzar la dependencia introduciría acoplamiento incorrecto, y reutilizar el mismo evento WebSocket obligaría al frontend a desambiguar su sentido según haya o no partida activa. |
| 7 | Por qué el umbral de paseo debe ser más permisivo | El paseo incluye pausas narrativas legítimas (transición de destino de 2-3s, ventana de exploración tras tocar un elemento, según FEAT-011) que no deben confundirse con inactividad real del niño. |
| 7 | Evento de sistema al dispararse | Se reutiliza `SYSTEM_INACTIVITY` (mismo catálogo ya existente, documento general sección 13.1), con disparador y consecuencia distintos según el contexto: partida activa → flujo ya cerrado en FEAT-007; sin partida activa → nuevo flujo en `world`. |
| 7 | Consecuencia en contexto de paseo (sin partida activa) | Cierre del estado runtime de paseo en memoria; si existía un `ActivityProposalLog` con `outcome` pendiente (punto 6), se resuelve como `IGNORED`; no se genera `GameSessionSummary` porque nunca hubo partida; se emite el evento WebSocket al niño con el mismo patrón que el resto de eventos de sistema. |
| 8 | Tablas fuente de la nueva query de dashboard | `ActivityProposalLog` (started/ignored, punto 6) y `GameSessionSummary` (completed/abandoned, definido en el debate de FEAT-007). No se crea ninguna tabla nueva — la query es una composición de lectura sobre datos ya definidos. |
| 8 | Qué expone la query | `ActivityEngagementSummary`: conteos por `engineType` sobre actividades, con `startedCount`, `ignoredCount`, `completedCount` y `abandonedCount`. Se construye a partir de `ActivityProposalLog` y `GameSessionSummary`. |
| 8 | Qué NO expone la query | Ninguna telemetría de navegación del paseo en sí (duración de sesión, número de destinos visitados) — no aporta valor descriptivo real al padre, es información de UX interna, no de progreso o comportamiento del niño. Tampoco ninguna etiqueta, recomendación automática, ni score compuesto de engagement. |
| 8 | Campo `origin` en `GameSessionSummary` | Descartado. El World Map es la única vía de juego del niño — no existe otra forma de iniciar una partida, por lo que no hay ambigüedad de origen que distinguir. Se retira esta pieza de la propuesta inicial. |
| 8 | Dónde vive la query | `tracking`, como query adicional de dashboard, mismo patrón que las ocho ya existentes en FEAT-006. `world` nunca expone lectura de dashboard directamente, coherente con la decisión del punto 1. |

## Estado Del Debate

Los ocho puntos identificados al inicio del debate del módulo `world` quedan resueltos y documentados en la tabla de Decisiones de Arquitectura. No quedan puntos pendientes de esta sesión.

## Anticipado Para Feature Futura (fuera de alcance, pero condiciona el diseño actual)

- **Ajuste parental de probabilidad de motor/skin por niño.** Mencionado durante el debate del punto 5 como una feature futura, posterior a que los motores estén funcionando y probados: el padre podrá configurar, por niño, la probabilidad de aparición de un tipo de minijuego, e incluso la skin visual de ese minijuego. No se implementa en este FEAT ni en FEAT-007, pero condiciona el diseño del umbral de patrón de engagement (punto 5): en vez de constantes de código, `world` consulta un puerto `engagementThresholdConfig(childProfileId)` que en v1 siempre devuelve valores globales, preparado para resolver overrides por niño cuando esa feature se especifique, sin requerir cambios en la lógica de evaluación de `world`. Mismo patrón de herencia nullable ya usado en `family`/`ChildProfile` para `audioEnabled`/`avatarEnabled`.

## Pruebas manuales de usabilidad

Además de tests unitarios, integración y contrato, esta feature requiere validación manual del flujo completo porque el objetivo principal es que el niño perciba el World Map como paseo narrativo, no como sistema de niveles.

- Verificar que un discovery element no parece un botón, misión, nivel ni objetivo obligatorio.
- Verificar que ignorar un discovery element no produce presión, repetición insistente ni feedback negativo.
- Verificar que una propuesta `IGNORED` se resuelve sin mostrar esa etiqueta al niño.
- Verificar que una actividad rechazada por `game` no muestra error técnico y el paseo continúa con alternativa o elemento decorativo.
- Verificar que una actividad abandonada vuelve al paseo de forma tranquila, sin sensación de fallo.
- Verificar que las pausas narrativas de 2-3 segundos no se interpretan como inactividad.
- Verificar que el frontend infantil no muestra `ignored`, `abandoned`, `low engagement`, porcentajes ni conclusiones diagnósticas.
- Verificar que `WORLD_HEARTBEAT` mantiene viva la sesión infantil mientras el niño explora sin partida activa.

## Preparación por módulos superiores

Antes de implementar la orquestación propia de `world`, deben ejecutarse sprints pequeños en los módulos dueños de datos o contratos necesarios:

- `content`: añadir catálogo estático mínimo para World Map (`WorldHost`, situaciones narrativas, objetos/elementos de descubrimiento o modelo equivalente), manteniendo `LearningPath` como catálogo estático.
- `tracking`: añadir `ActivityProposalLog`, puertos `registerActivityProposal(...)`/`resolveActivityProposal(...)`, endpoint/query `ActivityEngagementSummary`, y confirmar que `registerLearningPathStepProgress(...)` cubre el avance decidido por `world`.
- `game`: validar si falta publicar `GameSessionCompletedEvent(gameId, childSessionId, activityId, finalStatus)` al completar o abandonar una partida. `GameSessionSummary` ya existe desde Sprint 032 y el shell está implementado hasta Sprint 044.
- `session`: reutilizar el caso de uso de heartbeat ya existente para que `WORLD_HEARTBEAT` actualice `ChildSession.lastActivityAt` sin acceso directo a persistencia.

## Notas Cruzadas Pendientes

- **FEAT-003 (Content)**: necesitará incorporar el catálogo estático de World Map que hoy no existe en la feature: hosts, situaciones narrativas, objetos/elementos de descubrimiento y metadatos necesarios para construir destinos. El progreso y la selección runtime no pertenecen a content.
- **FEAT-006 (Tracking)**: necesitará la nueva entidad `ActivityProposalLog` y sus puertos `registerActivityProposal(...)`/`resolveActivityProposal(...)` para registrar started/ignored de propuestas de actividad (punto 6), confirmar o ampliar `registerLearningPathStepProgress(...)` para persistir el avance de `LearningPathStep` decidido por `world` (punto 3), y una query de dashboard — `ActivityEngagementSummary` (punto 8) — con `startedCount`, `ignoredCount`, `completedCount` y `abandonedCount` por `engineType`, construida sobre `ActivityProposalLog` y `GameSessionSummary`. `TopicSelectionService` ya existente debe quedar accesible como puerto consumible por `world`, no exclusivo de los motores de minijuego.
- **FEAT-007 (Game Shell Module)**: `GameSessionSummary` ya está implementado desde Sprint 032 y el game shell está implementado hasta Sprint 044. FEAT-008 debe alinearse con el contrato real de inicio de partida (`activityId` como input principal, sesión inferida o pasada según el adaptador interno). Queda validar si existe o falta publicar `GameSessionCompletedEvent` con `ApplicationEventPublisher` al completarse o abandonarse una partida, sin acoplar `game` a `world`.
- **FEAT-011 (World Map, frontend)**: ya establece correctamente que el frontend no implementa lógica de progresión ni selección de actividad; este documento confirma y refuerza esa decisión desde el lado backend, descartando explícitamente la alternativa de mover esa lógica al cliente.
