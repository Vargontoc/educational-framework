# SPRINT-062 — Integración en escenas

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-059, SPRINT-060, SPRINT-061
- **Impacto estimado:** Integra la reproducción de audio en las escenas del juego (WELCOME, FAREWELL) y maneja casos de error. Cierra la feature de integración de audio.

## Objetivo

Integrar la reproducción de audio en las escenas del juego (WELCOME en LoadingScene, FAREWELL en BaseStateScene/FarewellScene), manejar casos de error con fallbacks, y realizar limpieza de recursos.

## Contexto

Los sprints anteriores crearon la infraestructura de audio (WebSocket, AudioService, AudioCache, recursos estáticos). Este sprint integra la reproducción de audio en las escenas del juego:

- **WELCOME:** Se reproduce en LoadingScene al recibir evento `WELCOME`
- **FAREWELL:** Se reproduce en BaseStateScene/FarewellScene al recibir evento `FAREWELL`

El audio puede ser estático (WAV) o dinámico (TTS). Si el audio dinámico no está disponible, se hace fallback a estático.

## Diseño funcional-técnico

### 1. Reproducir audio WELCOME en LoadingScene

**Archivo:** `framework/frontend/app/src/components/game/LoadingScene.ts`

**Cambio en `handleAvatarEvent()`:**
```typescript
handleAvatarEvent(event: AvatarEvent) {
  switch(event.eventType) {
    case 'WELCOME':
      this.handleWelcomeEvent(event)
      break
    case 'FAREWELL':
      this.handleFarewellEvent(event)
      break
    default:
      break
  }
}

handleWelcomeEvent(event: AvatarEvent) {
  const audioService = this.registry.get('audioService') as AudioService
  
  if (event.audioAvailable && event.audioId) {
    // Audio dinámico (TTS) - esperar a que llegue binary frame
    const handleAudioReceived = (audioId: string) => {
      if (audioId === event.audioId) {
        audioService.playDynamic(audioId)
        audioService.off('audio-received', handleAudioReceived)
      }
    }
    audioService.on('audio-received', handleAudioReceived)
    
    // Timeout de seguridad (10 segundos)
    setTimeout(() => {
      if (!audioService.isCurrentlyPlaying()) {
        console.warn('Audio dinámico no recibido, usando fallback estático')
        audioService.playStatic('welcome')
      }
    }, 10000)
  } else {
    // Audio estático (WAV)
    audioService.playStatic('welcome')
  }
}
```

### 2. Reproducir audio FAREWELL en BaseStateScene

**Archivo:** `framework/frontend/app/src/components/game/BaseStateScene.ts`

**Cambio en `handleAvatarEvent()`:**
```typescript
handleFarewellEvent(event: AvatarEvent) {
  const audioService = this.registry.get('audioService') as AudioService
  
  if (event.audioAvailable && event.audioId) {
    // Audio dinámico (TTS)
    const handleAudioReceived = (audioId: string) => {
      if (audioId === event.audioId) {
        audioService.playDynamic(audioId)
        audioService.off('audio-received', handleAudioReceived)
      }
    }
    audioService.on('audio-received', handleAudioReceived)
    
    // Timeout de seguridad
    setTimeout(() => {
      if (!audioService.isCurrentlyPlaying()) {
        console.warn('Audio dinámico no recibido, usando fallback estático')
        audioService.playStatic('farewell')
      }
    }, 10000)
  } else {
    // Audio estático (WAV)
    audioService.playStatic('farewell')
  }
  
  // Transición a FarewellScene después de reproducir audio
  audioService.once('audio-completed', () => {
    this.goToFarewell()
  })
  
  // Timeout de seguridad para transición
  setTimeout(() => {
    this.goToFarewell()
  }, 8000)
}
```

### 3. Reproducir audio en FarewellScene

**Archivo:** `framework/frontend/app/src/components/game/FarewellScene.ts`

**Cambio en `create()`:**
```typescript
create() {
  const cx = 640
  const cy = 360
  
  // ... código visual existente ...
  
  // Reproducir audio de despedida
  this.playFarewellAudio()
  
  this.time.delayedCall(this.farewellDuration, () => {
    router.replace({ name: 'Home' })
  })
}

private playFarewellAudio() {
  const audioService = this.registry.get('audioService') as AudioService
  
  if (!audioService) {
    console.warn('AudioService no disponible')
    return
  }
  
  // Verificar configuración de audio
  const npcEnabled = this.registry.get('npcEnabled') as boolean ?? true
  const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? true
  const voiceEnabled = this.registry.get('voiceEnabled') as boolean ?? true
  
  if (npcEnabled && ttsEnabled && voiceEnabled) {
    audioService.playStatic('farewell')
  }
}
```

### 4. Manejo de errores y fallbacks

**Estrategia de fallback:**
```typescript
private playAudioWithFallback(
  audioService: AudioService,
  dynamicAudioId: string | undefined,
  staticKey: string
) {
  if (dynamicAudioId && audioService.getAudioCache().has(dynamicAudioId)) {
    try {
      audioService.playDynamic(dynamicAudioId)
    } catch (error) {
      console.error('Error reproduciendo audio dinámico, usando estático:', error)
      audioService.playStatic(staticKey)
    }
  } else {
    audioService.playStatic(staticKey)
  }
}
```

**Casos de error a manejar:**
1. AudioContext no disponible → fallback a solo visual
2. Binary frame no llega → timeout y fallback a estático
3. Error en decodificación → fallback a estático
4. Error en reproducción → log y continuar

### 5. Limpieza de recursos

**Cambio en todas las escenas:**
```typescript
shutdown() {
  const audioService = this.registry.get('audioService') as AudioService
  if (audioService) {
    audioService.stop()
  }
}

destroy() {
  const audioService = this.registry.get('audioService') as AudioService
  if (audioService) {
    audioService.dispose()
  }
  
  const audioCache = this.registry.get('audioCache') as AudioCache
  if (audioCache) {
    audioCache.clear()
  }
}
```

## Contratos y dependencias externas

### Dependencias internas

| Dependencia | Estado | Impacto |
|-------------|--------|---------|
| SPRINT-059 | Pendiente | Infraestructura WebSocket |
| SPRINT-060 | Pendiente | AudioService, AudioCache |
| SPRINT-061 | Pendiente | Recursos estáticos cargados |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Sincronización audio-animación incorrecta | MEDIA | Usar eventos de Phaser, ajustar tiempos |
| R2 | Audio no se reproduce en algunos dispositivos | MEDIA | Fallback a solo visual, testing en múltiples dispositivos |
| R3 | Memory leaks por AudioCache | BAJA | Limpieza explícita, profiling con DevTools |
| R4 | Condiciones de carrera con binary frames | MEDIA | Timeouts, validación de estado |

---

## Tareas del sprint

### Tarea 62.1: Reproducir audio WELCOME en LoadingScene

**Archivo:** `LoadingScene.ts`

**Criterios de aceptación:**
- Audio WELCOME se reproduce al recibir evento `WELCOME`.
- Audio dinámico (TTS) se reproduce si está disponible.
- Fallback a audio estático si dinámico no está disponible.

### Tarea 62.2: Reproducir audio FAREWELL en BaseStateScene

**Archivo:** `BaseStateScene.ts`

**Criterios de aceptación:**
- Audio FAREWELL se reproduce al recibir evento `FAREWELL`.
- Transiciones sincronizadas con audio.
- Timeout de seguridad para evitar bloqueo.

### Tarea 62.3: Reproducir audio en FarewellScene

**Archivo:** `FarewellScene.ts`

**Criterios de aceptación:**
- FarewellScene reproduce audio de despedida.
- Configuración de audio del usuario respetada.

### Tarea 62.4: Manejo de errores y fallbacks

**Archivos:** Todos los que usan AudioService

**Criterios de aceptación:**
- Errores manejados gracefully sin bloquear la aplicación.
- Fallbacks funcionando correctamente.

### Tarea 62.5: Limpieza de recursos

**Archivos:** Todas las escenas

**Criterios de aceptación:**
- Recursos limpiados correctamente al cambiar de escena.
- No hay memory leaks.

### Tarea 62.6: Pruebas de integración

**Criterios de aceptación:**
- Flujo completo WELCOME funcionando.
- Flujo completo FAREWELL funcionando.
- Fallbacks funcionando correctamente (simular errores).

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/components/game/LoadingScene.ts` | Modificación |
| `framework/frontend/app/src/components/game/BaseStateScene.ts` | Modificación |
| `framework/frontend/app/src/components/game/FarewellScene.ts` | Modificación |

## Estimación

- **Duración:** 3-4 días
- **Complejidad:** Media-Alta
- **Riesgo:** Medio

## Criterios de aceptación del sprint

1. Audio WELCOME se reproduce al recibir evento `WELCOME`.
2. Audio FAREWELL se reproduce al recibir evento `FAREWELL`.
3. Audio dinámico (TTS) se reproduce si está disponible.
4. Fallback a audio estático si dinámico no está disponible.
5. Transiciones sincronizadas con audio.
6. Errores manejados gracefully sin bloquear la aplicación.
7. Recursos limpiados correctamente al cambiar de escena.
8. Configuración de audio del usuario respetada.

## Dependencias bloqueantes

- [ ] SPRINT-059 completado (infraestructura WebSocket).
- [ ] SPRINT-060 completado (AudioService, AudioCache).
- [ ] SPRINT-061 completado (recursos estáticos cargados).

## Handoffs a otras capas

Ninguno.

## Notas adicionales

Este sprint cierra la feature de integración de audio. Con la reproducción de audio integrada en las escenas del juego, la experiencia de Nubi cobra vida con audio de bienvenida y despedida.

Los futuros sprints de World Map y minijuegos podrán usar AudioService para reproducir audio de motivación, instrucciones, feedback, etc.

## Resumen de archivos afectados (integración de audio completa)

| Archivo | Tipo | Sprint |
|---------|------|--------|
| `game-avatar-event.yaml` | Modificación | 059 |
| `GameEvent.ts` | Modificación | 059 |
| `websocket.ts` | Modificación | 059 |
| `BinaryFrameParser.ts` | Nuevo | 059 |
| `MessageRouter.ts` | Nuevo | 059 |
| `AudioCache.ts` | Nuevo | 060 |
| `AudioDecoder.ts` | Nuevo | 060 |
| `AudioService.ts` | Nuevo | 060 |
| `AudioContextManager.ts` | Nuevo | 060 |
| `welcome.wav` | Nuevo | 061 |
| `farewell.wav` | Nuevo | 061 |
| `assets-manifest.json` | Modificación | 061 |
| `LoadingScene.ts` | Modificación | 061, 062 |
| `BaseStateScene.ts` | Modificación | 061, 062 |
| `FarewellScene.ts` | Modificación | 061, 062 |
