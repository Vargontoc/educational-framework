# SPRINT-061 — Recursos estáticos y carga

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-060 (AudioService y AudioCache)
- **Impacto estimado:** Carga recursos de audio estático (WAV) en la escena de carga y prepara el sistema para reproducción.

## Objetivo

Cargar recursos de audio estático (WAV) en la escena de carga, actualizar el assets-manifest, inicializar AudioService en escenas, e integrar MessageRouter en el manejo de WebSocket.

## Contexto

Los recursos de audio estático (welcome.wav, farewell.wav) deben cargarse en LoadingScene para estar disponibles inmediatamente en las escenas posteriores. AudioService se inicializa una vez y se comparte entre escenas vía registry de Phaser.

## Diseño funcional-técnico

### 1. Crear recursos de audio estático

**Archivos nuevos:**
- `framework/frontend/app/public/audio/welcome.wav`
- `framework/frontend/app/public/audio/farewell.wav`

**Especificaciones:**
- Formato: WAV (PCM 16-bit, 44.1kHz, mono)
- Duración: 2-5 segundos
- Tamaño: < 500KB cada uno
- Contenido: Audio de bienvenida/despedida de Nubi

**Nota:** Los archivos deben ser proporcionados por el equipo de diseño/contenido. Si no están disponibles, usar placeholders temporales.

### 2. Actualizar assets-manifest.json

**Archivo:** `framework/frontend/app/public/assets-manifest.json`

**Cambio:** Agregar sección de audio.

```json
{
  "dev": {
    "images": [...],
    "spritesheets": [...],
    "audio": [
      { "key": "welcome", "url": "audio/welcome.wav", "type": "wav" },
      { "key": "farewell", "url": "audio/farewell.wav", "type": "wav" }
    ]
  }
}
```

### 3. Modificar LoadingScene para cargar audio

**Archivo:** `framework/frontend/app/src/components/game/LoadingScene.ts`

**Cambio en método `loadAssetsSilently()`:**
```typescript
loadAssetsSilently() {
  this.load.setBaseURL('/')
  this.load.pack('packManifest', 'assets-manifest.json', 'dev')
  
  // Cargar audio estático
  this.load.audio('welcome', 'audio/welcome.wav')
  this.load.audio('farewell', 'audio/farewell.wav')
  
  this.load.on('complete', () => {
    this.goToBaseState()
  })
  
  this.load.on('loaderror', (file: Phaser.Loader.File) => {
    console.error('Failed to load asset:', file.key, file.url)
  })
  
  this.load.start()
}
```

### 4. Inicializar AudioService en escenas

**Archivos a modificar:**
- `LoadingScene.ts`
- `BaseStateScene.ts`
- `FarewellScene.ts`

**Cambio:**
```typescript
// En cada escena, agregar:
import { AudioService } from '@/services/AudioService'
import { AudioCache } from '@/services/AudioCache'
import { AudioDecoder } from '@/services/AudioDecoder'
import { AudioContextManager } from '@/services/AudioContextManager'

// En create():
const audioContextManager = AudioContextManager.getInstance()
const audioDecoder = new AudioDecoder()
const audioCache = new AudioCache()
const audioService = new AudioService({
  scene: this,
  audioCache,
  audioDecoder
})

// Guardar en registry para compartir entre escenas
this.registry.set('audioService', audioService)
this.registry.set('audioCache', audioCache)
```

### 5. Integrar MessageRouter en escenas

**Archivos a modificar:**
- `LoadingScene.ts`
- `BaseStateScene.ts`

**Cambio en `setupWebSocketHandlers()`:**
```typescript
setupWebSocketHandlers(ws: WebSocket) {
  ws.onmessage = (msg) => {
    MessageRouter.route(
      msg.data,
      (jsonData) => {
        // Manejar mensaje JSON
        this.readEvent(JSON.parse(jsonData as string))
      },
      (binaryData) => {
        // Manejar binary frame
        const audioService = this.registry.get('audioService') as AudioService
        audioService.handleBinaryFrame(binaryData)
      }
    )
  }
  
  ws.onclose = () => {
    clearWebSocketHeartbeat(ws)
  }
}
```

## Contratos y dependencias externas

### Dependencias internas

| Dependencia | Estado | Impacto |
|-------------|--------|---------|
| SPRINT-059 | Pendiente | Proporciona MessageRouter |
| SPRINT-060 | Pendiente | Proporciona AudioService, AudioCache, AudioDecoder |

### Dependencias de contenido

| Elemento | Responsable | Estado |
|----------|-------------|--------|
| welcome.wav | Contenido/diseño | Pendiente — usar placeholder |
| farewell.wav | Contenido/diseño | Pendiente — usar placeholder |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Archivos WAV no proporcionados a tiempo | ALTA | Usar archivos placeholder temporalmente |
| R2 | Tamaño de archivos WAV muy grande | MEDIA | Comprimir/optimizar archivos |
| R3 | Fallo en carga de audio bloquea escena | BAJA | Manejar errores de carga, continuar sin audio |

---

## Tareas del sprint

### Tarea 61.1: Crear recursos de audio estático (placeholders)

**Archivos:** `welcome.wav`, `farewell.wav`

**Criterios de aceptación:**
- Archivos WAV creados y ubicados en `public/audio/`.
- Si contenido no proporciona archivos, usar placeholders.

### Tarea 61.2: Actualizar assets-manifest.json

**Archivo:** `assets-manifest.json`

**Criterios de aceptación:**
- assets-manifest.json actualizado con referencias a audio.

### Tarea 61.3: Modificar LoadingScene para cargar audio

**Archivo:** `LoadingScene.ts`

**Criterios de aceptación:**
- LoadingScene carga audio estático correctamente.
- Si falla la carga, continuar con el resto de assets.

### Tarea 61.4: Inicializar AudioService en escenas

**Archivos:** `LoadingScene.ts`, `BaseStateScene.ts`, `FarewellScene.ts`

**Criterios de aceptación:**
- AudioService inicializado en todas las escenas relevantes.
- AudioService compartido vía registry de Phaser.

### Tarea 61.5: Integrar MessageRouter en escenas

**Archivos:** `LoadingScene.ts`, `BaseStateScene.ts`

**Criterios de aceptación:**
- MessageRouter integrado en manejo de WebSocket.
- Binary frames procesados correctamente.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/public/audio/welcome.wav` | Nuevo (placeholder) |
| `framework/frontend/app/public/audio/farewell.wav` | Nuevo (placeholder) |
| `framework/frontend/app/public/assets-manifest.json` | Modificación |
| `framework/frontend/app/src/components/game/LoadingScene.ts` | Modificación |
| `framework/frontend/app/src/components/game/BaseStateScene.ts` | Modificación |
| `framework/frontend/app/src/components/game/FarewellScene.ts` | Modificación |

## Estimación

- **Duración:** 2-3 días
- **Complejidad:** Media
- **Riesgo:** Medio

## Criterios de aceptación del sprint

1. Archivos WAV creados y ubicados en `public/audio/` (placeholders si es necesario).
2. assets-manifest.json actualizado con referencias a audio.
3. LoadingScene carga audio estático correctamente.
4. AudioService inicializado en todas las escenas relevantes.
5. MessageRouter integrado en manejo de WebSocket.
6. Audio estático disponible para reproducción después de carga.
7. Binary frames recibidos y procesados correctamente.

## Dependencias bloqueantes

- [ ] SPRINT-059 completado (MessageRouter).
- [ ] SPRINT-060 completado (AudioService, AudioCache, AudioDecoder).

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Contenido | Proporcionar archivos WAV reales (reemplazar placeholders) | Media |

## Notas adicionales

Este sprint carga los recursos de audio estático y prepara el sistema para reproducción. Los siguientes sprints integrarán la reproducción de audio en las escenas del juego.
