# SPRINT-066 — Consumo de posición/worldWidth reales y reconstrucción de capas al cambiar de bioma

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-11
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-012; ADR-026; SPRINT-090 backend (contrato `worldWidth`/`positionX`/`positionY` ya implementado); SPRINT-091 backend (catálogo de biomas conectados)
- **Impacto estimado:** Prerrequisito técnico de todo FEAT-012 en frontend. Sin esto, un segundo bioma con `worldWidth` distinto rompe el layout inmediatamente.

## Objetivo

`WorldMapScene` deja de usar `WORLD_MAP_CONFIG.worldWidth` fijo y posición sintética por índice, consumiendo `positionX`/`positionY`/`host.worldWidth` reales del contrato `WORLD_STATE_SYNC`. Además, `BackgroundLayer`/`ParallaxLayer`/`GroundLayer` pasan a reconstruirse solo cuando el `biome` recibido cambia de verdad, no en cada resincronización (~1/s vía heartbeat).

## Contexto

Verificado directamente sobre el código en esta sesión de análisis:

- `GameEvent.ts` (`WorldHost`, `WorldDiscoveryElements`) no declara `worldWidth`, `positionX` ni `positionY`, aunque el contrato backend (`world-host-payload.yaml`, `world-discovery-element-payload.yaml`, SPRINT-090) ya los expone como campos aditivos `nullable`.
- `GroundLayer.ts:10`, `ParallaxLayer.ts:16` e `InteractiveLayer.ts:59` usan `WORLD_MAP_CONFIG.worldWidth` (3560, fijo) para dimensionar capas y layout de elementos por índice.
- `BackgroundLayer.buildFrom(biome)` ignora hoy el parámetro `biome` a propósito: es idempotente tras la primera construcción, para no reiniciar `tilePositionX` (deriva continua del skybox) en cada `WORLD_STATE_SYNC`. Con biomas reales cambiando en runtime (FEAT-012), esa idempotencia total ya no es correcta — debe ser "idempotente mientras el bioma no cambie", no "idempotente siempre".

**Decisiones técnicas confirmadas (2026-09-11):** SPRINT-090 backend está implementado; este sprint es el trabajo de frontend pendiente que su propia sección `next_sprint_suggestions` ya señalaba.

## Diseño funcional-técnico

### Contrato consumido

- `GameEvent.ts`: añadir `worldWidth?: number` a `WorldHost`, y `positionX?: number` / `positionY?: number` a `WorldDiscoveryElements`.
- Si `worldWidth` llega `null`/`undefined` (host sin autorar), usar `WORLD_MAP_CONFIG.worldWidth` como fallback local — igual que ya hace el backend con su propio `DEFAULT_WORLD_WIDTH`.
- Si `positionX`/`positionY` de un elemento llegan `null`, `InteractiveLayer` sintetiza su posición como hoy (índice), tal como ya contempla el propio backend (elementos sin posición autorada "siempre cumplen separación" y siguen siendo elegibles).

### Reconstrucción de capas por cambio real de bioma

- `WorldMapScene` guarda el `biome` activo (nuevo campo privado, p. ej. `currentBiome?: string`).
- En el `case 'WORLD_STATE_SYNC'`, antes de llamar a `backgroundLayer.buildFrom(...)`, comparar `event.payload.destination.biome` contra `this.currentBiome`. Solo si difieren: actualizar `this.currentBiome` y dejar que `BackgroundLayer`/`ParallaxLayer`/`GroundLayer` reconstruyan sus texturas.
- `BackgroundLayer.buildFrom(biome)` pasa de "si ya existe `this.skybox`, no tocar nada" a "si `biome` es el mismo que la última construcción, no tocar nada; si cambia, destruir y reconstruir con la clave de textura del nuevo bioma". Mismo patrón para `ParallaxLayer`/`GroundLayer` (hoy no aceptan `biome` en absoluto — hay que añadirlo a sus `create()`).
- Las claves de textura pasan de fijas (`'skybox'`, `'background'`, `'ground'`) a derivadas del bioma (p. ej. `` `skybox-${biome}` ``), coordinado con el manifest ampliado (ver tareas).

### Manifest multi-bioma

- `assets-manifest.json` pasa de un único bloque `biome-meadow` a seis bloques (`biome-meadow`, `biome-farm`, `biome-woods`, `biome-beach`, `biome-space`, `biome-dinosaurs`), cada uno con sus claves `skybox-<bioma>`/`background-<bioma>`/`ground-<bioma>`.
- Carga perezosa: `WorldMapScene.preload()` no carga los 6 de golpe. Al recibir un `biome` nuevo en `WORLD_STATE_SYNC`, si su textura aún no existe (`this.textures.exists(...)`), encolar su `load.pack` y esperar a `load.complete` antes de reconstruir capas (mismo patrón de `this.load.on('complete', ...)` ya usado en `preload()`).

## Contratos y dependencias externas

| Dependencia | Tipo | Estado |
|---|---|---|
| `worldWidth` en `world-host-payload.yaml` | Requerida | Implementado (SPRINT-090) |
| `positionX`/`positionY` en `world-discovery-element-payload.yaml` | Requerida | Implementado (SPRINT-090) |
| Catálogo de 6 biomas activos en backend | Requerida | Pendiente (SPRINT-091 backend) — sin esto, este sprint solo se puede probar con MEADOW |
| Assets de arte de los 5 biomas nuevos | Requerida (para pruebas reales) | Pendiente — dependencia de Contenido, no de esta capa |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Repetir el bug ya sufrido esta sesión: reconstruir el skybox en cada sync rompe su deriva continua | ALTA | Test manual explícito: "permanecer en el mismo bioma 10s no reinicia `tilePositionX`" |
| R2 | Sin biomas reales en backend, este sprint solo se prueba con MEADOW y un bioma sintético/mock | MEDIA | Añadir un segundo bioma mínimo de prueba en el manifest (assets placeholder) para validar el cambio de textura sin depender de SPRINT-091 |
| R3 | Cargar 6 sets de assets de golpe penaliza el arranque | MEDIA | Carga perezosa por bioma (ver diseño) |

## Tareas del sprint

### Tarea 66.1: Ampliar tipos del contrato en `GameEvent.ts`

**Archivo:** `framework/frontend/app/src/game/GameEvent.ts`

**Criterios de aceptación:**
- `WorldHost.worldWidth?: number` añadido.
- `WorldDiscoveryElements.positionX?: number` / `positionY?: number` añadidos.
- No se eliminan ni renombran campos existentes (cambio aditivo).

### Tarea 66.2: Consumir `worldWidth`/posición real en las capas

**Archivos:** `GroundLayer.ts`, `ParallaxLayer.ts`, `InteractiveLayer.ts`, `GradualScroller.ts`, `WorldMapScene.ts`

**Criterios de aceptación:**
- El ancho de mundo usado por scroll/capas proviene de `destination.host.worldWidth` cuando existe; cae a `WORLD_MAP_CONFIG.worldWidth` si es `null`/`undefined`.
- `InteractiveLayer` posiciona un elemento con `positionX`/`positionY` autorados usando esos valores (escalados al `worldWidth`/`viewportHeight` activos) en vez de índice, cuando existen; sintetiza por índice si son `null`.
- `GradualScroller.maxScrollOffset` se deriva del `worldWidth` activo, no queda fijo en 1280 si el `worldWidth` del bioma es distinto.

### Tarea 66.3: Reconstrucción de capas por cambio real de bioma

**Archivos:** `WorldMapScene.ts`, `BackgroundLayer.ts`, `ParallaxLayer.ts`, `GroundLayer.ts`

**Criterios de aceptación:**
- `WorldMapScene` no llama a reconstrucción de capas si `biome` no cambió respecto al último `WORLD_STATE_SYNC` procesado.
- `BackgroundLayer`/`ParallaxLayer`/`GroundLayer` exponen la misma interfaz pública (`buildFrom`/`create`) pero aceptan `biome` y solo reconstruyen si difiere del último usado.
- Test manual: permanecer en el mismo bioma 10s no reinicia la deriva del skybox (regresión del bug de esta sesión).

### Tarea 66.4: Manifest multi-bioma y carga perezosa

**Archivos:** `assets-manifest.json`, `WorldMapScene.ts`

**Criterios de aceptación:**
- Seis bloques de manifest, uno por bioma, con claves de textura por bioma.
- Al recibir un bioma cuya textura no está cargada, se carga bajo demanda antes de reconstruir capas (sin dejar un frame con textura ausente/placeholder roto).
- El arranque inicial (bioma MEADOW) no carga assets de los otros 5 biomas.

## Notas

- Este sprint no incluye transporte, selector de destino, señal de proximidad ni pausa de llegada — esos son SPRINT-067/068/069.
- Si SPRINT-091 backend no está listo cuando se implemente este sprint, usar un segundo bloque de manifest sintético (biome de prueba con assets placeholder) para no bloquear la validación de la reconstrucción de capas.

## Review

### Developer implementation — Evidencias

(Pendiente de implementación)

### Reviewer verification

(Pendiente de revisión)
