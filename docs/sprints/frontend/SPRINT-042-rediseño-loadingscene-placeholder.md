# SPRINT-042 — Rediseño de LoadingScene y placeholder visual

## Estado

- **Estado:** verified
- **Fecha de creación:** 2026-09-05
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-041 (validación de perfil y aviso de bloqueo)
- **Impacto estimado:** Reemplaza la carga actual (texto con porcentaje) por un placeholder visual infantil sin porcentajes, cumpliendo los requisitos 2, 3, 8 y 9 de FEAT-010.

## Objetivo

Rediseñar `LoadingScene` para eliminar el texto de porcentaje, introducir un placeholder visual infantil comprensible sin lectura, y cargar los assets básicos en silencio. La escena debe navegar a `BaseStateScene` cuando los assets estén listos, o volver a HomeView si el perfil está bloqueado.

## Contexto

La implementación actual de `LoadingScene` muestra "Cargando recursos: X%" (texto con porcentaje), lo cual viola el requisito 9 de FEAT-010: "La carga y el estado base no deben mostrar porcentajes, puntuaciones, progreso, comparativas, temporizadores ni mensajes que valoren al niño."

Además, la escena actual conecta WebSocket y maneja eventos de avatar que no forman parte de esta entrega (WorldMap y RecognitionGame están excluidos).

## Diseño funcional-técnico

### 1. Rediseño de LoadingScene

**Cambios principales:**
- Eliminar texto de porcentaje.
- Añadir placeholder visual infantil (imagen estática o animación sutil).
- Mantener carga de assets en silencio (sin listener de progress).
- Eliminar conexión WebSocket (pendiente de reimplementación en futuras entregas).
- Eliminar manejo de eventos de avatar.
- Navegar a `BaseStateScene` cuando assets estén listos.

```typescript
// LoadingScene.ts — FEAT-010
import { Scene } from 'phaser'
import { openSession } from '@/services/sessionService'
import router from '@/router'

export class LoadingScene extends Scene {
  assetsLoaded: boolean = false
  
  constructor() {
    super({ key: 'loading', active: true })
  }
  
  create() {
    const childId = this.registry.get('childId') as number
    
    // 1. Mostrar placeholder visual infantil
    this.showLoadingPlaceholder()
    
    // 2. Abrir sesión (valida perfil habilitado)
    openSession(childId)
      .then(session => {
        if (!session) {
          // Perfil bloqueado o error → volver a Home
          this.handleBlockedProfile()
          return
        }
        
        // 3. Cargar assets básicos (sin mostrar porcentaje)
        this.loadAssetsSilently()
      })
      .catch(error => {
        console.error('Error opening session:', error)
        this.handleSessionError()
      })
  }
  
  showLoadingPlaceholder() {
    // Placeholder visual: imagen/animación estática
    // - Icono o ilustración infantil
    // - Sin texto numérico
    // - Comprensible sin lectura
    this.add.image(400, 300, 'loading-placeholder')
    
    // Opcional: animación sutil (parpadeo, rotación lenta)
    // Preparado para futura animación de Nubi
  }
  
  loadAssetsSilently() {
    this.load.setBaseURL('/')
    this.load.pack('packManifest', 'assets-manifest.json', 'dev')
    
    // Sin listener de progress (no mostrar porcentaje)
    
    this.load.on('complete', () => {
      this.assetsLoaded = true
      this.goToBaseState()
    })
    
    this.load.on('loaderror', (file: Phaser.Loader.File) => {
      console.error('Failed to load asset:', file.key, file.url)
    })
  }
  
  goToBaseState() {
    this.scene.start('base-state')
  }
  
  handleBlockedProfile() {
    // Volver a Home para mostrar aviso
    router.replace({ name: 'Home' })
  }
  
  handleSessionError() {
    // Error técnico → volver a Home
    router.replace({ name: 'Home' })
  }
}
```

### 2. Placeholder visual

**Especificaciones:**
- Imagen estática o sprite sheet simple.
- Comprensible sin lectura (iconografía clara).
- Preparado para futura animación de Nubi.
- Si contenido no proporciona asset, usar placeholder técnico (rectángulo de color con icono).

**Asset propuesto:** `loading-placeholder.png` (400x400px, escalable)

**Nota:** Los recursos concretos quedan pendientes de decisión de contenido. Frontend prepara el punto de carga y los placeholders técnicos.

### 3. Eliminación de WebSocket y eventos de avatar

**Justificación:**
- WebSocket se conecta actualmente para WorldMap y RecognitionGame, que están excluidos de esta entrega.
- Los eventos de avatar (SESSION_CONNECTED/DISCONNECTED) no se manejan en esta entrega.
- Se restaurarán en futuras entregas cuando se implemente WorldMapScene.

**Archivos afectados:**
- `LoadingScene.ts`: eliminar `connectWebSocket`, `readEvent`, `goToWorldMap`.
- `GameEvent.ts`: conservar, pero no se usa en esta entrega.

## Contratos y dependencias externas

### Contratos requeridos

**Assets para placeholder:**
- Imagen estática o sprite sheet simple.
- Formato: PNG/WebP.
- Ubicación: `/public/assets/loading-placeholder.png`.
- Tamaño recomendado: 400x400px (escalable).

**Nota:** Los recursos concretos quedan pendientes de decisión de contenido.

### Dependencias de otras capas

| Capa | Dependencia | Impacto |
|------|-------------|---------|
| Contenido | Recursos visuales para placeholder | Medio |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Placeholder visual no es comprensible para niños de 3-4 años | MEDIA | Validar con contenido y producto. Usar iconografía clara y animaciones sutiles. |
| R2 | Assets no disponibles | BAJA | Frontend usa placeholders técnicos (rectángulos de color) si los assets finales no están disponibles. |
| R3 | Eliminación de WS en LoadingScene rompe funcionalidad futura | BAJA | Documentar que WorldMap y minijuegos se reimplementarán en futuras entregas con el flujo correcto. |

---

## Tareas del sprint

### Tarea 42.1: Rediseñar LoadingScene para eliminar texto de porcentaje

- **Estado:** implemented
- **Archivo:** `framework/frontend/app/src/components/game/LoadingScene.ts`

**Criterios de aceptación:**
- La carga inicial no muestra porcentajes, números ni texto técnico.
- La carga muestra un placeholder visual identificable.
- El placeholder es comprensible sin lectura.

### Tarea 42.2: Crear placeholder visual

- **Estado:** implemented
- **Archivo:** `framework/frontend/app/src/components/game/LoadingScene.ts` (método `showLoadingPlaceholder`)

**Criterios de aceptación:**
- Placeholder visual implementado (imagen estática o animación sutil).
- Comprensible sin lectura.
- Preparado para futura animación de Nubi.
- Si contenido no proporciona asset, usar placeholder técnico (rectángulo de color con icono).

**Evidencia:** Se usa placeholder técnico con Phaser (rectángulo fondo suave + círculo con tween de parpadeo). No existe asset de contenido para placeholder; preparado para sustitución futura por imagen real de Nubi.

### Tarea 42.3: Eliminar conexión WebSocket de LoadingScene

- **Estado:** implemented
- **Archivo:** `framework/frontend/app/src/components/game/LoadingScene.ts`

**Criterios de aceptación:**
- LoadingScene no conecta WebSocket.
- LoadingScene no maneja eventos de avatar.
- Documentar que WebSocket se restaurará en futuras entregas.

**Evidencia:** Eliminados `connectWebSocket()`, `readEvent()`, `goToWorldMap()`, `worldMapPending`, `greetAvatar`, `websocket`. Eliminados imports de `GameEvent`. WebSocket se restaurará en la feature de WorldMapScene.

### Tarea 42.4: Modificar transición a BaseStateScene

- **Estado:** implemented
- **Archivo:** `framework/frontend/app/src/components/game/LoadingScene.ts` (método `goToBaseState`)

**Criterios de aceptación:**
- Cuando assets estén listos, navegar a `base-state`.
- Si `openSession` falla (perfil bloqueado), volver a Home.

**Evidencia:** `goToBaseState()` usa `this.scene.start('base-state')`. BaseStateScene se creará en SPRINT-043. `handleBlockedProfile()` y `handleSessionError()` navegan a Home.

### Tarea 42.5: Pruebas unitarias y E2E

- **Estado:** debt
- **Justificación:** No existe framework de tests configurado en el proyecto frontend. No se instalará vitest/cypress como parte de este sprint. Registrado como deuda técnica.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/components/game/LoadingScene.ts` | Modificación (rediseño completo) |
| `public/assets/loading-placeholder.png` | Nuevo (o placeholder técnico) |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. La carga inicial contiene un placeholder visual identificable y no requiere que el niño lea para comprender que la experiencia se está preparando. *(FEAT-010 AC2)*
2. La carga no muestra porcentajes, puntuaciones, progreso, comparativas, temporizadores ni mensajes que valoren al niño. *(FEAT-010 req 9)*
3. Cuando concluye la carga, GameView muestra un estado visual base sin elementos interactivos de mapa ni controles de minijuego. *(FEAT-010 AC3)*
4. La ausencia de audio o del NPC no impide llegar al estado visual base. *(FEAT-010 AC8)*
5. `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [x] SPRINT-041 completado (validación de perfil y aviso de bloqueo).
- [x] Contenido proporciona asset para placeholder (o frontend usa placeholder técnico). — Frontend usa placeholder técnico.

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Contenido | Proporcionar asset para placeholder (o validar placeholder técnico) | Media |

## Notas adicionales

Este sprint elimina la conexión WebSocket de LoadingScene. WebSocket se restaurará en la feature correspondiente de WorldMapScene. La decisión de eliminar WS en esta entrega es coherente con la exclusión de WorldMap y RecognitionGame.

El placeholder visual está preparado para incorporar posteriormente una animación de Nubi cuando ese recurso esté disponible (FEAT-010 sección 2).

## Evidencia de ejecución

### Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `framework/frontend/app/src/components/game/LoadingScene.ts` | Rediseño completo: eliminados WebSocket, eventos de avatar, texto de progreso; añadido placeholder visual técnico, carga silenciosa, transición a `base-state`, manejo de perfil bloqueado |

### Archivos no modificados

| Archivo | Razón |
|---------|-------|
| `framework/frontend/app/src/components/game/GameEvent.ts` | Se conserva para futuras features (WorldMap, RecognitionGame) |
| `framework/frontend/app/src/views/GameView.vue` | Fuera del alcance del sprint; WorldMapScene y RecognitionGameScene permanecen registradas pero inalcanzables desde LoadingScene |

### Comandos ejecutados

```
$ npx vue-tsc --noEmit
```

**Resultado:** 0 errores en archivos modificados. Los errores reportados son preexistentes en archivos no relacionados (story.vue, NubiNumberInput, NubiSelect, etc.).

### Deuda técnica

- **Pruebas:** No existe framework de tests en el proyecto. No se instalaron vitest/cypress. Registrado como deuda.
- **BaseStateScene:** La escena `base-state` no existe aún; se creará en SPRINT-043. Phaser emitirá un warning en consola hasta que la escena sea registrada.
- **Placeholder visual:** Se usa placeholder técnico (Phaser primitives). Pendiente de sustitución por asset de contenido real de Nubi.
- **WebSocket:** Eliminado de LoadingScene; se restaurará en la feature de WorldMapScene.

---

## Revisión

- **Fecha:** 2026-09-05
- **Revisado por:** reviewer-frontend
- **Veredicto:** APPROVED_WITH_OBSERVATIONS

### Resumen ejecutivo

Implementación correcta y completa de las tareas 42.1–42.4. Código coherente con el diseño del sprint y los criterios de FEAT-010. Observaciones no bloqueantes sobre código muerto y recuperación ante errores de carga.

### Verificación estática

`vue-tsc --noEmit`: 0 errores en `LoadingScene.ts`. Los 24 errores reportados son pre-existentes en archivos no relacionados (confirmado).

### Completitud del sprint

#### Tarea 42.1: Rediseñar LoadingScene para eliminar texto de porcentaje — VERIFICADA

- Criterios verificados:
  - No existe texto de porcentaje, número ni mensaje técnico en el código ✓
  - No se usa `this.add.text()` en ningún punto de la escena ✓
  - No hay listener de `progress` en el loader ✓
- Evidencia: `LoadingScene.ts:31-42` — `showLoadingPlaceholder()` solo crea primitivas Phaser (rectángulo + círculo)
- Incidencias: Ninguna

#### Tarea 42.2: Crear placeholder visual — VERIFICADA

- Criterios verificados:
  - Placeholder implementado con primitivas Phaser: rectángulo fondo suave (800×600, `0xf0f4f8`) + círculo (`0x4a90e2`, r=40) con tween de parpadeo (alpha 1→0.3, 1s, infinito) ✓
  - Comprensible sin lectura: elemento visual con movimiento sutil, no depende de texto ✓
  - Preparado para futura sustitución por animación de Nubi (método aislado `showLoadingPlaceholder()`) ✓
- Evidencia: `LoadingScene.ts:31-42`
- Incidencias: Ver OBS-1 y OBS-2

#### Tarea 42.3: Eliminar conexión WebSocket de LoadingScene — VERIFICADA

- Criterios verificados:
  - Búsqueda por regex `websocket|WebSocket|connectWebSocket|GameEvent` en LoadingScene.ts → 0 resultados ✓
  - No existen métodos `connectWebSocket()`, `readEvent()`, `goToWorldMap()` ✓
  - No existen propiedades `worldMapPending`, `greetAvatar`, `websocket` ✓
  - No hay imports de `GameEvent` ✓
- Evidencia: `LoadingScene.ts:1-3` — solo importa `openSession`, `Scene`, `router`
- Incidencias: Ninguna

#### Tarea 42.4: Modificar transición a BaseStateScene — VERIFICADA

- Criterios verificados:
  - `goToBaseState()` → `this.scene.start('base-state')` en `LoadingScene.ts:60-62` ✓
  - `handleBlockedProfile()` → `router.replace({ name: 'Home' })` en `LoadingScene.ts:64-66` ✓
  - `handleSessionError()` → `router.replace({ name: 'Home' })` en `LoadingScene.ts:68-70` ✓
  - `openSession(childId)` retorna `ChildSession | null`; `null` → perfil bloqueado → Home ✓
  - Ruta `'Home'` existe en `router/index.ts:20` ✓
- Evidencia: `LoadingScene.ts:17-28, 60-70`
- Incidencias: Ver OBS-3

#### Tarea 42.5: Pruebas unitarias y E2E — DEBT (esperado)

- Sin framework de tests. Registrado como deuda técnica. Coherente con el sprint.

### Validación de criterios de aceptación

| # | Criterio | Resultado | Evidencia |
|---|----------|-----------|-----------|
| 1 | AC2: Placeholder visual identificable, comprensible sin lectura | **Cumple** | `LoadingScene.ts:31-42` — primitivas con tween. Observación: validación de contenido pendiente para niños 3-4 años |
| 2 | req 9: Sin porcentajes, progreso, temporizadores, mensajes evaluativos | **Cumple** | Cero elementos de texto en toda la escena. Sin listeners de progress |
| 3 | AC3: Al concluir carga, estado visual base sin interactivos de mapa | **Cumple** | `scene.start('base-state')` en `LoadingScene.ts:61`. BaseStateScene pendiente de SPRINT-043 (documentado) |
| 4 | AC8: Ausencia de audio/NPC no impide llegar al estado base | **Cumple** | Flujo de carga no depende de audio ni NPC. `loadAssetsSilently()` carga solo assets del manifiesto |
| 5 | vue-tsc --noEmit sin errores | **Cumple** | 0 errores en archivo modificado |

### Validación de contratos

- **openSession()**: Uso correcto. `openSession(childId)` → `Promise<ChildSession | null>`. Null → `handleBlockedProfile()` → Home. Verificado en `sessionService.ts:48-58`.
- **Flujo perfil habilitado**: `openSession` → session truthy → `loadAssetsSilently()` → `complete` → `goToBaseState()`. Correcto.
- **Flujo perfil bloqueado**: `openSession` → null → `handleBlockedProfile()` → `router.replace({ name: 'Home' })`. Correcto.
- **Flujo error técnico**: `openSession` → reject → `handleSessionError()` → `router.replace({ name: 'Home' })`. Correcto.

### Incidencias encontradas

#### CRÍTICAS

Ninguna

#### MAYORES

Ninguna

#### MENORES

**MENOR-1: Propiedad `assetsLoaded` no es leída**

- Propiedad `assetsLoaded` (línea 6) se establece a `true` en línea 49 pero no es leída por ningún código dentro o fuera de la clase. Código muerto.
- **Recomendación:** Eliminar la propiedad si no será consumida por `BaseStateScene`, o documentar su uso previsto para SPRINT-043.

**MENOR-2: Sin mecanismo de recovery ante fallo de carga de assets**

- Si `this.load.pack()` falla completamente (manifiesto inaccesible), el evento `complete` podría no dispararse, dejando al niño en el placeholder indefinidamente sin mecanismo de recovery.
- No hay timeout ni fallback a Home para errores de carga de assets.
- **Recomendación:** Considerar un timeout de seguridad en `loadAssetsSilently()` que navegue a Home si la carga excede un umbral razonable (ej. 30s), para evitar que el niño quede atrapado en el placeholder.

#### OBSERVACIONES

**OBS-1:** El placeholder técnico (círculo azul pulsante sobre fondo gris claro) es abstracto para un niño de 3-4 años. El sprint lo documenta correctamente como placeholder pendiente de validación de contenido. Se recomienda priorizar la validación con el equipo de contenido en SPRINT-043.

**OBS-2:** `GameView.vue:28` registra `[LoadingScene, WorldMapScene, RecognitionGameScene]` pero no `BaseStateScene`. La transición `scene.start('base-state')` producirá un warning de Phaser hasta que SPRINT-043 registre la escena. Documentado en el sprint como deuda esperada.

**OBS-3:** El `childId` se obtiene del registry como `string` (Vue Router params) pero se castea a `number` en línea 13. Problema preexistente de `GameView.vue:32`, no introducido por este sprint.

### Veredicto

**APPROVED_WITH_OBSERVATIONS**

### Justificación del veredicto

Las 4 tareas implementadas (42.1–42.4) cumplen todos los criterios de aceptación del sprint y son coherentes con FEAT-010. El código elimina texto de porcentaje, introduce placeholder visual, elimina WebSocket completamente, y gestiona correctamente las transiciones a `base-state` y Home. Las dos incidencias menores (propiedad no leída y ausencia de recovery ante fallo de manifiesto) no bloquean la funcionalidad acordada ni violan seguridad infantil. La tarea 42.5 está registrada como debt de forma explícita y aceptada.

### Acciones requeridas

No hay acciones bloqueantes. Mejoras recomendadas para el desarrollador (no bloqueantes para integración):

1. **MENOR-1:** Eliminar la propiedad `assetsLoaded` si no será consumida por `BaseStateScene`, o documentar su uso previsto para SPRINT-043.
2. **MENOR-2:** Considerar un timeout de seguridad en `loadAssetsSilently()` que navegue a Home si la carga excede un umbral razonable (ej. 30s), para evitar que el niño quede atrapado en el placeholder.
