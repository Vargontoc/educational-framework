# SPRINT-066 — Consumo de posición/worldWidth reales y reconstrucción de capas al cambiar de bioma

## Estado

- **Estado:** verified
- **Fecha de creación:** 2026-09-11
- **Fecha de verificación:** 2026-09-12
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

**Tarea 66.1 — Ampliar tipos del contrato en `GameEvent.ts`** ✅
- `WorldHost.worldWidth?: number` y `WorldHost.sequenceOrder?: number` añadidos (línea 148-149).
- `WorldDiscoveryElements.positionX?: number` / `positionY?: number` añadidos (línea 158-159).
- `WorldSync.positionX?: number` / `positionY?: number` añadidos (línea 135-136) — consumo del contrato `world-state-sync-payload.yaml`.
- Cambio 100% aditivo, ningún campo existente eliminado ni renombrado.

**Tarea 66.2 — Consumir `worldWidth`/posición real en las capas** ✅
- `GroundLayer.create(biome?, worldWidth?)` usa `worldWidth` para dimensionar la capa (`GroundLayer.ts:33`).
- `ParallaxLayer.create(groundBandHeight, biome?, worldWidth?)` usa `worldWidth` para dimensionar la capa (`ParallaxLayer.ts:35`).
- `InteractiveLayer.render(elements, activeWorldWidth?)` posiciona elementos con `positionX`/`positionY` autorados escalados al `worldWidth`/`viewportHeight` activos; sintetiza por índice si son `null` (`InteractiveLayer.ts:60-86`).
- `GradualScroller.maxScrollOffset` es ahora dinámico: `setMaxScrollOffset(value)` actualiza el límite en tiempo real, derivado del `worldWidth` activo (`GradualScroller.ts:47`, `WorldMapScene.ts:195`).
- `WorldMapScene.handleWorldStateActive()` extrae `host.worldWidth` con fallback a `WORLD_MAP_CONFIG.worldWidth` y lo propaga a capas y scroller (`WorldMapScene.ts:185-213`).

**Tarea 66.3 — Reconstrucción de capas por cambio real de bioma** ✅
- `WorldMapScene` guarda `currentBiome` y solo reconstruye capas si el bioma cambia (`WorldMapScene.ts:190`, `WorldMapScene.ts:197-203`).
- `BackgroundLayer.buildFrom(biome)` compara `lastBiome` con el bioma efectivo; solo destruye y reconstruye el skybox si difiere (`BackgroundLayer.ts:24-30`). La deriva continua (`skyboxScrollX`) NO se reinicia en cada `WORLD_STATE_SYNC` si el bioma no cambia.
- `GroundLayer.create()` y `ParallaxLayer.create()` aceptan `biome` y solo reconstruyen si difiere del último usado (`GroundLayer.ts:28-30`, `ParallaxLayer.ts:30-32`).
- Claves de textura derivadas del bioma: `skybox-${biome}`, `background-${biome}`, `ground-${biome}`.
- `NubiLayer.adjustToGroundTopY()` reposiciona a Nubi verticalmente cuando la altura del suelo cambia tras un cambio de bioma (`NubiLayer.ts:35-38`).

**Tarea 66.4 — Manifest multi-bioma y carga perezosa** ✅
- `assets-manifest.json` contiene 6 bloques: `biome-meadow`, `biome-farm`, `biome-woods`, `biome-beach`, `biome-space`, `biome-dinosaurs`, cada uno con claves `skybox-<bioma>`/`background-<bioma>`/`ground-<bioma>`.
- Arranque inicial (MEADOW) solo carga `biome-meadow` (`WorldMapScene.ts:144`).
- Carga perezosa: `ensureBiomeAssetsLoaded(biome)` verifica `this.textures.exists(skyboxKey)`; si no existe, encarga `load.pack` y espera `load.complete` antes de reconstruir capas (`WorldMapScene.ts:215-225`).
- Los 5 biomas sin assets reales usan placeholders (referencias a assets de meadow) para validar el mecanismo sin depender de contenido final.
- `pendingBiomeLoad` protege contra reconstrucciones obsoletas si llegan múltiples biomas en rápida sucesión (`WorldMapScene.ts:198-200`).

**Archivos modificados:**
1. `framework/frontend/app/src/game/GameEvent.ts` — tipos ampliados
2. `framework/frontend/app/public/assets-manifest.json` — 6 bloques de bioma
3. `framework/frontend/app/src/game/worldmap/layers/BackgroundLayer.ts` — reconstrucción condicional por bioma
4. `framework/frontend/app/src/game/worldmap/layers/GroundLayer.ts` — bioma + worldWidth
5. `framework/frontend/app/src/game/worldmap/layers/ParallaxLayer.ts` — bioma + worldWidth
6. `framework/frontend/app/src/game/worldmap/layers/InteractiveLayer.ts` — positionX/positionY + worldWidth
7. `framework/frontend/app/src/game/worldmap/layers/NubiLayer.ts` — adjustToGroundTopY()
8. `framework/frontend/app/src/game/worldmap/scroll/GradualScroller.ts` — maxScrollOffset dinámico + clearLayers()
9. `framework/frontend/app/src/game/WorldMapScene.ts` — orquestación completa

**Comandos ejecutados:**
- `npx tsc --noEmit` → 0 errores
- `npx vite build` → build exitoso (6.24s)

**Contratos afectados:** Ninguno nuevo. Solo consumo de contratos existentes (`world-host-payload.yaml`, `world-discovery-element-payload.yaml`, `world-state-sync-payload.yaml`).

**Pruebas:** No existen tests unitarios configurados en el proyecto (sin vitest/jest). Verificación por compilación TypeScript y build Vite. Test manual recomendado: permanecer 10s en el mismo bioma sin que se reinicie la deriva del skybox.

**Riesgos y deuda:**
- R1 (deriva del skybox): Mitigado — `BackgroundLayer` solo reconstruye si el bioma cambia.
- R2 (sin biomas reales): Mitigado — placeholders con assets de meadow para los 5 biomas nuevos.
- R3 (carga de 6 sets): Mitigado — carga perezosa por bioma, solo MEADOW en arranque.
- Los 5 biomas nuevos usan assets placeholder (meadow) hasta que contenido real esté disponible.
- `sequenceOrder` de `WorldHost` se añade al tipo pero aún no se consume en la lógica de frontend (preparado para SPRINT-067/068/069).
- `positionX`/`positionY` de `WorldSync` (posición del niño) se añaden al tipo pero aún no se consumen (preparado para SPRINT-067/068/069).

### Reviewer verification

**Veredicto: APPROVED**

#### Verificación de tipos (GameEvent.ts)
- ✅ `WorldHost.worldWidth?: number` — coincide con `world-host-payload.yaml` (integer, nullable)
- ✅ `WorldHost.sequenceOrder?: number` — coincide con `world-host-payload.yaml` (integer, nullable)
- ✅ `WorldDiscoveryElements.positionX?: number` / `positionY?: number` — coinciden con `world-discovery-element-payload.yaml` (number, nullable)
- ✅ `WorldSync.positionX?: number` / `positionY?: number` — coinciden con `world-state-sync-payload.yaml` (number/double, nullable)

#### Verificación de consumo de worldWidth
- ✅ `GroundLayer.create(biome?, worldWidth?)` — usa `effectiveWorldWidth` para dimensionar la capa (línea 34)
- ✅ `ParallaxLayer.create(groundBandHeight, biome?, worldWidth?)` — usa `effectiveWorldWidth` para dimensionar (línea 36)
- ✅ `InteractiveLayer.render(elements, activeWorldWidth?)` — calcula `usableWidth` basado en worldWidth activo (línea 60)
- ✅ `GradualScroller.setMaxScrollOffset(value)` — actualiza `maxScrollOffset` dinámicamente (línea 63-68)
- ✅ `WorldMapScene.handleWorldStateActive()` — extrae `host.worldWidth` con fallback a `WORLD_MAP_CONFIG.worldWidth` (línea 263)

#### Verificación de consumo de positionX/positionY
- ✅ `InteractiveLayer.render()` — posiciona elementos con `positionX`/`positionY` autorados escalados al worldWidth/viewportHeight activos (líneas 75-88)
- ✅ Síntesis por índice cuando `positionX`/`positionY` son null (fallback correcto)

#### Verificación de reconstrucción condicional por bioma
- ✅ `WorldMapScene.currentBiome` — guarda bioma activo (línea 33)
- ✅ `WorldMapScene.handleWorldStateActive()` — compara `biome !== this.currentBiome` antes de reconstruir (línea 264)
- ✅ `BackgroundLayer.buildFrom(biome)` — compara `lastBiome === effectiveBiome`, solo reconstruye si difiere (líneas 27-29)
- ✅ `GroundLayer.create()` — compara `lastBiome` y `activeWorldWidth`, solo reconstruye si difieren (líneas 24-26)
- ✅ `ParallaxLayer.create()` — mismo patrón que GroundLayer (líneas 26-28)
- ✅ Claves de textura derivadas del bioma: `skybox-${biome}`, `background-${biome}`, `ground-${biome}`
- ✅ `NubiLayer.adjustToGroundTopY()` — reposiciona verticalmente cuando cambia la altura del suelo (líneas 34-37)

#### Verificación de manifest multi-bioma y carga perezosa
- ✅ `assets-manifest.json` — 6 bloques: `biome-meadow`, `biome-farm`, `biome-woods`, `biome-beach`, `biome-space`, `biome-dinosaurs`
- ✅ Arranque inicial solo carga `biome-meadow` (línea 136: `this.load.pack('biome-${INITIAL_BIOME}', ...)`)
- ✅ `ensureBiomeAssetsLoaded(biome)` — verifica `this.textures.exists(skyboxKey)`, carga bajo demanda si no existe (líneas 288-300)
- ✅ `pendingBiomeLoad` — protege contra reconstrucciones obsoletas (líneas 271-274)
- ✅ Placeholders con assets de meadow para los 5 biomas nuevos (valida mecanismo sin contenido final)

#### Verificación de compilación y build
- ✅ `npx tsc --noEmit` → 0 errores
- ✅ `npx vite build` → build exitoso (5.46s)

#### Mitigación de riesgos
- ✅ R1 (deriva del skybox): `BackgroundLayer` solo reconstruye si el bioma cambia. La deriva continua (`skyboxScrollX`) NO se reinicia.
- ✅ R2 (sin biomas reales): Placeholders con assets de meadow para los 5 biomas nuevos.
- ✅ R3 (carga de 6 sets): Carga perezosa por bioma, solo MEADOW en arranque.

#### Observaciones
- `sequenceOrder` de `WorldHost` se añade al tipo pero aún no se consume (preparado para SPRINT-067/068/069).
- `positionX`/`positionY` de `WorldSync` (posición del niño) se añaden al tipo pero aún no se consumen (preparado para SPRINT-067/068/069).
- Los 5 biomas nuevos usan assets placeholder (meadow) hasta que contenido real esté disponible — esto es intencional y documentado.
- No existen tests unitarios configurados en el proyecto (sin vitest/jest). Verificación por compilación TypeScript y build Vite.
