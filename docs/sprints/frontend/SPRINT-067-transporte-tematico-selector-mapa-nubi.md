# SPRINT-067 — Transporte temático y selector visual de destino (mapa con Nubi)

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-11
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-066 (consumo de `worldWidth`/posición real); SPRINT-092 backend (contrato de selección de destino); Contenido (ilustración de Nubi+mapa y stickers por bioma)
- **Impacto estimado:** Habilita el único mecanismo de navegación entre biomas descrito en ADR-026/FEAT-012: el transporte junto al punto de inicio de cada bioma.

## Objetivo

Nuevo elemento de transporte visible junto al punto de inicio de Nubi en cada bioma. Al tocarlo, se abre un overlay a pantalla completa con Nubi sosteniendo un mapa con un sticker por cada uno de los 6 biomas disponibles. Tocar un sticker inicia directamente la transición a ese bioma.

## Contexto

**Decisión confirmada por el usuario (2026-09-11):** el selector es un overlay a pantalla completa (no un panel lateral que conviva con el paisaje). La ilustración es Nubi mostrando un mapa con stickers de los biomas; no hay paso de confirmación intermedio entre tocar el sticker y empezar la transición.

Requisitos de FEAT-012/ADR-026 que este sprint debe respetar estrictamente:
- Los 6 destinos se presentan siempre disponibles, sin candados, orden de preferencia, insignias de progreso ni indicación de visitas previas (FEAT-012 §3.11, criterio de aceptación §13).
- El transporte debe ser reconocible como elemento de viaje sin depender exclusivamente de texto, color o sonido (§3.10).
- Elegir un destino no debe presentarse como salto de nivel ni premio (§3.12).

## Diseño funcional-técnico

### `TransportLayer` (nueva capa)

- Mismo patrón que el resto de capas de `worldmap/layers/`: constructor con `Scene`, `create()`/`destroy()`, profundidad propia (por encima de `GroundLayer`, junto a la posición de inicio de Nubi del bioma activo).
- Un único elemento interactivo por bioma (no six-in-one): el icono de transporte temático del bioma actual (globo aerostático en Pradera, tractor en Granja, etc. — a definir por Contenido).
- `zone` interactiva con `useHandCursor`, mismo patrón que `InteractiveLayer.createElement`.

### Overlay de selección (`BiomeSelectorLayer` o escena superpuesta)

- Se evalúa como capa adicional dentro de `WorldMapScene` (consistente con el resto de capas) en vez de una `Scene` de Phaser separada, para reutilizar el mismo `registry`/eventos sin gestionar transferencia de estado entre escenas.
- Contenido: ilustración de Nubi + mapa (asset de Contenido) con 6 stickers posicionados, cada uno representando un bioma mediante icono + forma + color (nunca solo color, por accesibilidad — mismo criterio que `InteractiveLayer`).
- Ningún sticker se muestra distinto por progreso, visitas previas o recomendación. El orden de presentación de los stickers es el orden lineal de biomas (dato de SPRINT-091 backend), pero solo como disposición visual del mapa, nunca como bloqueo de acceso a los posteriores.
- Tocar un sticker: cierra el overlay y dispara directamente el flujo de transición (ver SPRINT-068) hacia ese bioma — sin pantalla de confirmación.
- Cerrar el overlay sin elegir (tocar fuera, o un control de cierre) debe ser posible y no penalizar ni registrar nada.

### Accesibilidad

- El overlay debe ser navegable/comprensible en `prefers-reduced-motion` (sin animación de apertura brusca si está activo).
- Objetivos táctiles ≥ `WORLD_MAP_CONFIG.minHitAreaSize` (80px), igual que el resto de elementos interactivos.

## Contratos y dependencias externas

| Dependencia | Tipo | Estado |
|---|---|---|
| Orden lineal de biomas (para disposición visual de stickers) | Deseable, no bloqueante | Pendiente (SPRINT-091 backend) — sin él, se puede fijar un orden local temporal |
| Acción de "solicitar destino" hacia backend | Requerida para completar la transición | Pendiente (SPRINT-092 backend) |
| Ilustración Nubi+mapa y 6 stickers | Requerida para producción real | Pendiente — dependencia de Contenido, fuera de esta capa |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El selector de 6 destinos se percibe como menú adulto (riesgo ya señalado en FEAT-012 §7) | ALTA | Validar layout con Contenido antes de dar por cerrado el diseño visual; no es una decisión que frontend cierre solo |
| R2 | Sin el contrato de SPRINT-092, no hay forma real de completar la transición tras tocar un sticker | MEDIA | Implementar el flujo hasta el punto de disparo del evento de selección; dejar la integración real como tarea acoplada a SPRINT-092 |
| R3 | Confundir "orden lineal de biomas" (dato de producto) con "orden de desbloqueo" (prohibido) | ALTA | Revisión explícita: ningún sticker debe renderizarse deshabilitado o con candado bajo ninguna condición |

## Tareas del sprint

### Tarea 67.1: `TransportLayer`

**Archivo (nuevo):** `framework/frontend/app/src/game/worldmap/layers/TransportLayer.ts`

**Criterios de aceptación:**
- Un elemento de transporte visible junto al punto de inicio de Nubi en el bioma activo.
- Objetivo táctil ≥ 80px, reconocible sin depender solo de color.
- Al tocarlo, emite un evento de escena (p. ej. `'transport-touched'`) que `WorldMapScene` escucha para abrir el selector.

### Tarea 67.2: Overlay de selección de destino

**Archivo (nuevo):** `framework/frontend/app/src/game/worldmap/layers/BiomeSelectorLayer.ts`

**Criterios de aceptación:**
- Overlay a pantalla completa con Nubi + mapa + 6 stickers, todos visualmente equivalentes y disponibles.
- Tocar un sticker cierra el overlay y emite el bioma elegido (p. ej. evento `'destination-selected'` con el id de bioma).
- Se puede cerrar sin elegir ningún destino.
- Ningún sticker depende exclusivamente de color para diferenciarse.

### Tarea 67.3: Integración en `WorldMapScene`

**Archivo:** `framework/frontend/app/src/game/WorldMapScene.ts`

**Criterios de aceptación:**
- `TransportLayer` se crea/destruye junto al resto de capas del bioma activo (se reconstruye si el bioma cambia, igual que `NubiLayer`).
- Al recibir `'destination-selected'`, se dispara el flujo de transición de SPRINT-068 (aunque su implementación completa sea de ese sprint).

## Notas

- Este sprint no implementa la pausa de llegada ni la señal de proximidad — solo el mecanismo de elegir destino. La transición real (fundido, reconstrucción de capas, pausa) se conecta en SPRINT-068.
- La disposición exacta de los 6 stickers (fila, círculo, mapa ilustrado con posiciones fijas) depende del asset final de Contenido; no fijar coordenadas definitivas hasta tener el arte.

## Review

### Developer implementation — Evidencias

(Pendiente de implementación)

### Reviewer verification

(Pendiente de revisión)
