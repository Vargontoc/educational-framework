# SPRINT-060 — AudioService y AudioCache

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-059 (infraestructura WebSocket)
- **Impacto estimado:** Crea el módulo de audio de Phaser y el sistema de cache para audio dinámico.

## Objetivo

Crear el módulo de audio de Phaser (AudioService) y el sistema de cache para audio dinámico (AudioCache, AudioDecoder, AudioContextManager).

## Contexto

AudioService es el módulo centralizado para reproducción de audio en Phaser. Debe soportar:
- Audio estático (WAV cargado en Phaser cache)
- Audio dinámico (MP3 decodificado y almacenado en AudioCache)
- Cola de reproducción para evitar solapamientos
- Eventos para sincronización con animaciones

## Diseño funcional-técnico

### 1. Crear AudioCache

**Archivo nuevo:** `framework/frontend/app/src/services/AudioCache.ts`

**Responsabilidad:** Almacenar AudioBuffer de audio dinámico recibido.

**Estructura:**
```typescript
class AudioCache {
  private cache: Map<string, AudioBuffer>
  
  set(audioId: string, buffer: AudioBuffer): void
  get(audioId: string): AudioBuffer | undefined
  has(audioId: string): boolean
  delete(audioId: string): void
  clear(): void
  getSize(): number
}
```

**Consideraciones:**
- Cache en memoria (no persistencia)
- Limitar tamaño máximo (50 AudioBuffer)
- Estrategia LRU si se excede el límite
- Limpieza automática al destruir escena

### 2. Crear AudioDecoder

**Archivo nuevo:** `framework/frontend/app/src/services/AudioDecoder.ts`

**Responsabilidad:** Decodificar ArrayBuffer a AudioBuffer usando Web Audio API.

**Métodos:**
```typescript
class AudioDecoder {
  private audioContext: AudioContext
  
  constructor()
  
  async decode(audioData: ArrayBuffer): Promise<AudioBuffer>
  
  dispose(): void
}
```

### 3. Crear AudioService (módulo de Phaser)

**Archivo nuevo:** `framework/frontend/app/src/services/AudioService.ts`

**Responsabilidad:** Reproducir audio estático y dinámico, gestionar cola.

**Estructura:**
```typescript
interface AudioServiceConfig {
  scene: Phaser.Scene
  audioCache: AudioCache
  audioDecoder: AudioDecoder
}

class AudioService extends Phaser.Events.EventEmitter {
  constructor(config: AudioServiceConfig)
  
  // Audio estático (WAV cargado en Phaser)
  playStatic(key: string): void
  
  // Audio dinámico (MP3 decodificado)
  async playDynamic(audioId: string): Promise<void>
  
  // Cola de reproducción
  enqueue(type: 'static' | 'dynamic', key: string): void
  playNext(): void
  
  // Control
  stop(): void
  pause(): void
  resume(): void
  
  // Estado
  isCurrentlyPlaying(): boolean
  
  // Lifecycle
  dispose(): void
}
```

**Eventos emitidos:**
- `'audio-started'` → cuando inicia reproducción
- `'audio-completed'` → cuando termina reproducción
- `'audio-error'` → cuando hay error
- `'audio-received'` → cuando llega audio dinámico

### 4. Integrar AudioService con BinaryFrameParser

**Método nuevo en AudioService:**
```typescript
async handleBinaryFrame(buffer: ArrayBuffer): Promise<void> {
  const { audioId, audioData } = BinaryFrameParser.parse(buffer)
  const audioBuffer = await this.audioDecoder.decode(audioData)
  this.audioCache.set(audioId, audioBuffer)
  this.emit('audio-received', audioId)
}
```

### 5. Crear AudioContext Manager

**Archivo nuevo:** `framework/frontend/app/src/services/AudioContextManager.ts`

**Responsabilidad:** Gestionar AudioContext global y resolver problemas de autoplay.

**Métodos:**
```typescript
class AudioContextManager {
  private static instance: AudioContextManager
  private audioContext: AudioContext | null = null
  
  static getInstance(): AudioContextManager
  
  async getAudioContext(): Promise<AudioContext>
  
  async resumeIfNeeded(): Promise<void>
  
  dispose(): void
}
```

## Contratos y dependencias externas

### Dependencias internas

| Dependencia | Estado | Impacto |
|-------------|--------|---------|
| SPRINT-059 | Pendiente | Proporciona BinaryFrameParser y MessageRouter |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | AudioContext bloqueado por política de autoplay | ALTA | AudioContextManager con resume después de interacción |
| R2 | Latencia alta en decodificación de MP3 | MEDIA | Precargar AudioContext, usar Web Workers si es necesario |
| R3 | Consumo excesivo de memoria con AudioCache | MEDIA | Limitar tamaño de cache (50 buffers), implementar LRU |
| R4 | Sincronización audio-animación | MEDIA | Usar eventos de Phaser para sincronizar |

---

## Tareas del sprint

### Tarea 60.1: Crear AudioCache

**Archivo:** `AudioCache.ts` (nuevo)

**Criterios de aceptación:**
- AudioCache almacena y recupera AudioBuffer correctamente.
- Tests unitarios pasando.

### Tarea 60.2: Crear AudioDecoder

**Archivo:** `AudioDecoder.ts` (nuevo)

**Criterios de aceptación:**
- AudioDecoder decodifica MP3 a AudioBuffer sin errores.
- Tests unitarios pasando.

### Tarea 60.3: Crear AudioService

**Archivo:** `AudioService.ts` (nuevo)

**Criterios de aceptación:**
- AudioService reproduce audio estático (WAV).
- AudioService reproduce audio dinámico (MP3 decodificado).
- Cola de reproducción funciona correctamente.
- Tests unitarios pasando.

### Tarea 60.4: Integrar AudioService con BinaryFrameParser

**Archivo:** `AudioService.ts`

**Criterios de aceptación:**
- AudioService puede procesar binary frames.
- Audio decodificado se almacena en AudioCache.

### Tarea 60.5: Crear AudioContextManager

**Archivo:** `AudioContextManager.ts` (nuevo)

**Criterios de aceptación:**
- AudioContextManager resuelve problemas de autoplay.
- Tests unitarios pasando.

### Tarea 60.6: Tests de integración

**Criterios de aceptación:**
- Tests de integración para flujo completo (binary frame → cache → reproducción).

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/services/AudioCache.ts` | Nuevo |
| `framework/frontend/app/src/services/AudioDecoder.ts` | Nuevo |
| `framework/frontend/app/src/services/AudioService.ts` | Nuevo |
| `framework/frontend/app/src/services/AudioContextManager.ts` | Nuevo |

## Estimación

- **Duración:** 4-5 días
- **Complejidad:** Alta
- **Riesgo:** Medio-Alto

## Criterios de aceptación del sprint

1. AudioCache almacena y recupera AudioBuffer correctamente.
2. AudioDecoder decodifica MP3 a AudioBuffer sin errores.
3. AudioService reproduce audio estático (WAV).
4. AudioService reproduce audio dinámico (MP3 decodificado).
5. Cola de reproducción funciona correctamente.
6. AudioContextManager resuelve problemas de autoplay.
7. Tests unitarios y de integración pasando.

## Dependencias bloqueantes

- [ ] SPRINT-059 completado (infraestructura WebSocket).

## Handoffs a otras capas

Ninguno.

## Notas adicionales

Este sprint crea los componentes centrales para la integración de audio. Los siguientes sprints integrarán estos componentes en las escenas Phaser.
