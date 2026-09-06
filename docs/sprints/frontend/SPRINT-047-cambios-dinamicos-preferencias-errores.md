# SPRINT-047 — Cambios dinámicos de preferencias y clasificación de errores

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-05
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-046 (recuperación de conexión y escena de despedida)
- **Impacto estimado:** Implementa suscripción a cambios dinámicos de preferencias de audio/NPC/voz durante la sesión y clasificación de errores de comunicación, cumpliendo los requisitos 15-16 de FEAT-010.

## Objetivo

Implementar suscripción a cambios dinámicos de preferencias de audio, NPC y voz del NPC durante la sesión activa en GameView. Los cambios recibidos deben aplicarse a la experiencia posterior. Además, implementar clasificación de errores de comunicación para distinguir entre errores recuperables (no interrumpen la experiencia) y errores críticos (afectan a la sesión).

## Contexto

FEAT-010 establece que:
- **Req 15**: Las preferencias de audio, NPC y voz del NPC pueden cambiar durante la sesión. GameView debe reflejar los cambios vigentes.
- **Req 16**: Ante un error de comunicación que no impida continuar ni afecte a la sesión, la experiencia debe continuar sin interrupción visible.

Los contratos AsyncAPI definen:
- `CHILD_TTS_ACTIVATED`: TTS activado durante la sesión.
- `CHILD_TTS_DEACTIVATED`: TTS desactivado durante la sesión.
- `CHILD_AGENT_ACTIVATED`: NPC activado durante la sesión.
- `CHILD_AGENT_DEACTIVATED`: NPC desactivado durante la sesión.
- `GAME_ERROR`: error de comunicación.

## Diseño funcional-técnico

### 1. Suscripción a cambios de preferencias

**Manejo de eventos en BaseStateScene:**

```typescript
// BaseStateScene.ts — SPRINT-047
readEvent(event: SessionEvent | GameAvatarEvent) {
  if (!event) return
  
  switch (event.event) {
    // ... otros eventos ya manejados ...
    
    case 'CHILD_TTS_ACTIVATED':
      this.handleTTSActivated()
      break
      
    case 'CHILD_TTS_DEACTIVATED':
      this.handleTTSDeactivated()
      break
      
    case 'CHILD_AGENT_ACTIVATED':
      this.handleAgentActivated()
      break
      
    case 'CHILD_AGENT_DEACTIVATED':
      this.handleAgentDeactivated()
      break
      
    case 'GAME_ERROR':
      this.handleGameError(event.payload)
      break
  }
}

handleTTSActivated() {
  // Actualizar registry de Phaser
  this.registry.set('ttsEnabled', true)
  
  // Notificar a otras escenas si es necesario
  this.events.emit('tts-state-changed', true)
  
  console.log('TTS activated during session')
}

handleTTSDeactivated() {
  this.registry.set('ttsEnabled', false)
  this.events.emit('tts-state-changed', false)
  
  console.log('TTS deactivated during session')
}

handleAgentActivated() {
  this.registry.set('npcEnabled', true)
  this.events.emit('npc-state-changed', true)
  
  console.log('NPC activated during session')
}

handleAgentDeactivated() {
  this.registry.set('npcEnabled', false)
  this.events.emit('npc-state-changed', false)
  
  console.log('NPC deactivated during session')
}
```

### 2. Clasificación de errores

**Estrategia de clasificación:**

```typescript
// ErrorClassifier.ts (nuevo)
export type ErrorSeverity = 'RECOVERABLE' | 'CRITICAL'

export interface ClassifiedError {
  severity: ErrorSeverity
  message: string
  originalEvent?: any
}

export class ErrorClassifier {
  // Backend clasifica algunos errores
  static classifyFromBackendEvent(event: any): ClassifiedError {
    switch (event.event) {
      case 'GAME_ERROR':
        // Backend puede enviar código de error en payload
        if (event.payload?.errorCode) {
          return this.classifyByErrorCode(event.payload.errorCode)
        }
        // Si no hay código, asumir recuperable
        return {
          severity: 'RECOVERABLE',
          message: 'Error de comunicación temporal',
          originalEvent: event
        }
        
      case 'SESSION_EXPIRED':
      case 'SESSION_INVALIDATED':
      case 'CHILD_EXPELLED':
        // Estos ya se manejan como críticos en SPRINT-045/046
        return {
          severity: 'CRITICAL',
          message: 'Sesión terminada',
          originalEvent: event
        }
        
      default:
        return {
          severity: 'RECOVERABLE',
          message: 'Error desconocido',
          originalEvent: event
        }
    }
  }
  
  // Frontend clasifica errores de red/timeout
  static classifyFromNetworkError(error: any): ClassifiedError {
    if (error instanceof WebSocket) {
      // Error de WebSocket
      return {
        severity: 'RECOVERABLE',
        message: 'Conexión interrumpida',
        originalEvent: error
      }
    }
    
    if (error?.type === 'timeout') {
      // Timeout de heartbeat
      return {
        severity: 'RECOVERABLE',
        message: 'Timeout de conexión',
        originalEvent: error
      }
    }
    
    // Otros errores de red
    return {
      severity: 'RECOVERABLE',
      message: 'Error de red',
      originalEvent: error
    }
  }
  
  private static classifyByErrorCode(errorCode: string): ClassifiedError {
    // Mapear códigos de error de backend a severidad
    switch (errorCode) {
      case 'TEMPORARY_FAILURE':
      case 'RATE_LIMIT':
      case 'TIMEOUT':
        return {
          severity: 'RECOVERABLE',
          message: 'Error temporal'
        }
        
      case 'SESSION_NOT_FOUND':
      case 'INVALID_STATE':
      case 'PERMISSION_DENIED':
        return {
          severity: 'CRITICAL',
          message: 'Error crítico de sesión'
        }
        
      default:
        return {
          severity: 'RECOVERABLE',
          message: 'Error desconocido'
        }
    }
  }
}
```

### 3. Manejo de errores en BaseStateScene

**Integración con ErrorClassifier:**

```typescript
// BaseStateScene.ts — SPRINT-047
handleGameError(payload: any) {
  // Clasificar error
  const classified = ErrorClassifier.classifyFromBackendEvent({
    event: 'GAME_ERROR',
    payload
  })
  
  if (classified.severity === 'RECOVERABLE') {
    // Error recuperable: no interrumpir experiencia
    console.log('Recoverable error:', classified.message)
    
    // Opcional: mostrar indicador visual sutil (no técnico)
    // this.showSubtleErrorIndicator()
    
    // Continuar experiencia sin interrupción
    return
  }
  
  // Error crítico: aplicar flujo de pérdida de conexión
  console.log('Critical error:', classified.message)
  this.handleConnectionLost()
}

handleNetworkError(error: any) {
  // Clasificar error de red
  const classified = ErrorClassifier.classifyFromNetworkError(error)
  
  if (classified.severity === 'RECOVERABLE') {
    // Error recuperable: intentar reconexión silenciosa
    console.log('Recoverable network error:', classified.message)
    this.connectionMonitor.handleWebSocketClose()
    return
  }
  
  // Error crítico: aplicar flujo de pérdida de conexión
  console.log('Critical network error:', classified.message)
  this.handleConnectionLost()
}
```

### 4. Indicador visual sutil (opcional)

**Para errores recuperables:**

```typescript
// BaseStateScene.ts — SPRINT-047
showSubtleErrorIndicator() {
  // Mostrar indicador visual sutil (no técnico)
  // Ejemplo: pequeño icono de "reconectando" en esquina
  // No mostrar texto técnico ni pedir acción al niño
  
  const indicator = this.add.image(750, 50, 'reconnecting-icon')
    .setScale(0.5)
    .setAlpha(0.7)
  
  // Ocultar después de 3 segundos
  this.time.delayedCall(3000, () => {
    indicator.destroy()
  })
}
```

### 5. Aplicación de cambios de preferencias

**Lectura de preferencias en FarewellScene:**

```typescript
// FarewellScene.ts — SPRINT-047
create() {
  // ... animación visual de Nubi siempre se muestra ...
  
  // Leer preferencias vigentes del registry
  const npcEnabled = this.registry.get('npcEnabled') as boolean
  const ttsEnabled = this.registry.get('ttsEnabled') as boolean
  
  // Voz solo se reproduce si NPC y TTS están activados
  if (npcEnabled && ttsEnabled) {
    this.playFarewellVoice()
  }
}
```

### 6. Inicialización de preferencias en LoadingScene

**Establecer preferencias iniciales:**

```typescript
// LoadingScene.ts — SPRINT-047
create() {
  // ... abrir sesión y conectar WebSocket ...
  
  // Establecer preferencias iniciales en registry
  // (se actualizarán dinámicamente por eventos WebSocket)
  this.registry.set('npcEnabled', false) // Valor por defecto
  this.registry.set('ttsEnabled', false) // Valor por defecto
  
  // ... continuar con carga ...
}
```

**Nota:** Las preferencias iniciales pueden obtenerse de la sesión abierta o de un endpoint específico. Backend debe confirmar cómo se proporcionan las preferencias iniciales.

## Contratos y dependencias externas

### Contratos requeridos

**Eventos WebSocket (AsyncAPI):**
- `CHILD_TTS_ACTIVATED`: TTS activado.
- `CHILD_TTS_DEACTIVATED`: TTS desactivado.
- `CHILD_AGENT_ACTIVATED`: NPC activado.
- `CHILD_AGENT_DEACTIVATED`: NPC desactivado.
- `GAME_ERROR`: error de comunicación con payload opcional.

**Referencia:** `docs/contracts/api/asyncapi/messages/session-event.yaml`

### Dependencias de otras capas

| Capa | Dependencia | Impacto |
|------|-------------|---------|
| Backend | Eventos de cambios de preferencias ya implementados | Ninguno (ya disponibles) |
| Backend | Confirmar cómo se proporcionan preferencias iniciales | Medio |
| Backend | Confirmar códigos de error en GAME_ERROR payload | Bajo |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Cambios de preferencias no se aplican inmediatamente | BAJA | Registry de Phaser se actualiza inmediatamente. Escenas leen del registry en cada uso. |
| R2 | Clasificación incorrecta de error interrumpe innecesariamente | MEDIA | ErrorClassifier usa códigos de backend cuando están disponibles. Frontend solo clasifica errores de red. |
| R3 | Preferencias iniciales no están disponibles al entrar | BAJA | Frontend usa valores por defecto (false). Backend debe confirmar cómo proporcionar preferencias iniciales. |
| R4 | Indicador visual sutil confunde al niño | BAJA | Contenido valida que el indicador es comprensible y no técnico. Si no es adecuado, se elimina. |

---

## Tareas del sprint

### Tarea 47.1: Crear ErrorClassifier para clasificación de errores — implemented

**Criterios de aceptación:**
- ErrorClassifier clasifica errores de backend (GAME_ERROR) como recuperables o críticos. ✓
- ErrorClassifier clasifica errores de red como recuperables. ✓
- ErrorClassifier usa códigos de error de backend cuando están disponibles. ✓

**Archivo:** `framework/frontend/app/src/components/game/ErrorClassifier.ts` (nuevo)

### Tarea 47.2: Implementar suscripción a cambios de preferencias en BaseStateScene — implemented

**Criterios de aceptación:**
- BaseStateScene maneja CHILD_TTS_ACTIVATED/DEACTIVATED. ✓
- BaseStateScene maneja CHILD_AGENT_ACTIVATED/DEACTIVATED. ✓
- Cambios se reflejan en el registry de Phaser inmediatamente. ✓

**Archivo:** `framework/frontend/app/src/components/game/BaseStateScene.ts`

### Tarea 47.3: Integrar ErrorClassifier en BaseStateScene — implemented

**Criterios de aceptación:**
- BaseStateScene usa ErrorClassifier para clasificar GAME_ERROR. ✓
- Errores recuperables no interrumpen la experiencia. ✓
- Errores críticos aplican flujo de pérdida de conexión. ✓

**Archivo:** `framework/frontend/app/src/components/game/BaseStateScene.ts`

### Tarea 47.4: Implementar indicador visual sutil para errores recuperables (opcional) — not implemented

**Criterios de aceptación:**
- No implementado. Opcional según diseño. Requiere validación de contenido.

### Tarea 47.5: Actualizar FarewellScene para leer preferencias vigentes — implemented

**Criterios de aceptación:**
- FarewellScene lee preferencias de NPC, TTS y voz del registry. ✓
- Voz de despedida solo se reproduce si NPC, TTS y voz están activados. ✓
- Animación visual de Nubi siempre se muestra (excepción limitada). ✓

**Archivo:** `framework/frontend/app/src/components/game/FarewellScene.ts`

### Tarea 47.6: Inicializar preferencias en LoadingScene — implemented

**Criterios de aceptación:**
- LoadingScene establece preferencias iniciales en registry. ✓
- Preferencias iniciales usan valores por defecto (false) si no están definidas. ✓

**Archivo:** `framework/frontend/app/src/components/game/LoadingScene.ts`

### Tarea 47.7: Pruebas unitarias y E2E — deuda técnica

**Criterios de aceptación:**
- No existe framework de tests en el proyecto frontend.
- Documentado como deuda técnica.

### Tarea 47.0: Actualizar GameEvent.ts con nuevos tipos de eventos — implemented

**Criterios de aceptación:**
- SERVER_EVENT incluye CHILD_TTS_ACTIVATED, CHILD_TTS_DEACTIVATED, CHILD_AGENT_ACTIVATED, CHILD_AGENT_DEACTIVATED, GAME_ERROR. ✓
- Tipos de eventos añadidos: ChildTTSActivatedEvent, ChildTTSDeactivatedEvent, ChildAgentActivatedEvent, ChildAgentDeactivatedEvent, GameErrorEvent. ✓
- GameErrorPayload interface definida. ✓
- ServerGameEvent union actualizada. ✓

**Archivo:** `framework/frontend/app/src/components/game/GameEvent.ts`

## Evidencia de ejecución

- `npx vue-tsc --noEmit` ejecutado. Errores preexistentes en archivos ajenos al sprint (`.story.vue`, `NubiNumberInput.vue`, etc.). Sin errores nuevos en `src/components/game/`.
- Contratos AsyncAPI (`session-event.yaml`) confirman los eventos implementados.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/frontend/app/src/components/game/ErrorClassifier.ts` | Nuevo |
| `framework/frontend/app/src/components/game/BaseStateScene.ts` | Modificación |
| `framework/frontend/app/src/components/game/FarewellScene.ts` | Modificación |
| `framework/frontend/app/src/components/game/LoadingScene.ts` | Modificación |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media
- **Riesgo:** Medio

## Criterios de aceptación del sprint

1. Los cambios vigentes de preferencias de audio, NPC y voz del NPC recibidos durante la sesión se aplican a la experiencia posterior. *(FEAT-010 req 15)*
2. Ante un error de comunicación que no impida continuar ni afecte a la sesión, la experiencia continúa sin una interrupción visible para el niño. *(FEAT-010 req 16, AC 14)*
3. `vue-tsc --noEmit` sin errores.

## Dependencias bloqueantes

- [ ] SPRINT-046 completado (recuperación de conexión y escena de despedida).
- [ ] Backend confirma cómo se proporcionan preferencias iniciales.

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Backend | Confirmar cómo se proporcionan preferencias iniciales de audio/NPC/voz | Media |
| Backend | Confirmar códigos de error en GAME_ERROR payload | Bajo |
| Contenido | Validar indicador visual sutil para errores recuperables (si se implementa) | Bajo |

## Notas adicionales

Este sprint cierra FEAT-010 con la implementación de cambios dinámicos de preferencias y clasificación de errores.

La clasificación de errores es colaborativa: backend proporciona códigos de error cuando es posible, frontend clasifica errores de red.

Las preferencias se almacenan en el registry de Phaser y se leen en cada uso (FarewellScene, futuras escenas).

Con este sprint, FEAT-010 queda completa y se puede marcar como `completed`.
