# SPRINT-059 — Infraestructura WebSocket para audio binario

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-088 (backend), FEAT-010, FEAT-011
- **Impacto estimado:** Habilita el soporte para binary frames en WebSocket y crea la infraestructura de parsing para audio dinámico.

## Objetivo

Habilitar el soporte para binary frames en WebSocket y crear la infraestructura de parsing para audio dinámico: actualizar contrato AsyncAPI, actualizar tipos TypeScript, modificar WebSocket para soportar `binaryType = 'arraybuffer'`, crear BinaryFrameParser y MessageRouter.

## Contexto

La integración de audio requiere que el WebSocket pueda recibir binary frames (audio data) además de text frames (JSON). El formato de binary frame es:

```
[4 bytes: audioId length (int32 big-endian)]
[N bytes: audioId UTF-8]
[remaining: audio data (MP3 para dinámico)]
```

## Diseño funcional-técnico

### 1. Actualizar contrato AsyncAPI

**Archivo:** `docs/contracts/api/asyncapi/messages/game-avatar-event.yaml`

**Cambio:** Renombrar `SESSION_CONNECTED` → `WELCOME` y `SESSION_DISCONNECTED` → `FAREWELL`.

### 2. Actualizar tipos TypeScript

**Archivo:** `framework/frontend/app/src/components/game/GameEvent.ts`

**Cambio:**
```typescript
export type AVATAR_TYPE_EVENT = 'WELCOME' | 'FAREWELL'
```

### 3. Modificar WebSocket para soportar binary frames

**Archivo:** `framework/frontend/app/src/services/websocket.ts`

**Cambio:**
```typescript
ws.binaryType = 'arraybuffer'
```

### 4. Crear BinaryFrameParser

**Archivo nuevo:** `framework/frontend/app/src/services/BinaryFrameParser.ts`

**Responsabilidad:** Decodificar binary frames del WebSocket.

**Métodos:**
```typescript
interface ParsedBinaryFrame {
  audioId: string
  audioData: ArrayBuffer
}

class BinaryFrameParser {
  static parse(buffer: ArrayBuffer): ParsedBinaryFrame
  static isValidBinaryFrame(data: unknown): data is ArrayBuffer
}
```

### 5. Crear MessageRouter

**Archivo nuevo:** `framework/frontend/app/src/services/MessageRouter.ts`

**Responsabilidad:** Distinguir entre mensajes JSON y binary frames.

**Métodos:**
```typescript
class MessageRouter {
  static route(
    message: MessageEvent['data'],
    onJson: (data: unknown) => void,
    onBinary: (buffer: ArrayBuffer) => void
  ): void
}
```

## Contratos y dependencias externas

### Contratos afectados

| Contrato | Cambio |
|----------|--------|
| `game-avatar-event.yaml` | Renombrar `SESSION_CONNECTED` → `WELCOME`, `SESSION_DISCONNECTED` → `FAREWELL` |

### Dependencias de otras capas

| Capa | Dependencia | Impacto |
|------|-------------|---------|
| Backend | SPRINT-088 debe completar ajustes de contrato | Alto |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Backend no envía binary frames en formato esperado | MEDIA | Coordinar con backend para validar formato |
| R2 | Problemas de compatibilidad con navegadores | BAJA | Verificar soporte de `binaryType` |

---

## Tareas del sprint

### Tarea 59.1: Actualizar contrato AsyncAPI

**Archivo:** `game-avatar-event.yaml`

**Criterios de aceptación:**
- `eventType` enum actualizado con `WELCOME` y `FAREWELL`.

### Tarea 59.2: Actualizar tipos TypeScript

**Archivo:** `GameEvent.ts`

**Criterios de aceptación:**
- `AVATAR_TYPE_EVENT` actualizado.

### Tarea 59.3: Modificar WebSocket para soportar binary frames

**Archivo:** `websocket.ts`

**Criterios de aceptación:**
- `binaryType = 'arraybuffer'` configurado.

### Tarea 59.4: Crear BinaryFrameParser

**Archivo:** `BinaryFrameParser.ts` (nuevo)

**Criterios de aceptación:**
- Parser decodifica correctamente binary frames.
- Tests unitarios pasando.

### Tarea 59.5: Crear MessageRouter

**Archivo:** `MessageRouter.ts` (nuevo)

**Criterios de aceptación:**
- Router distingue correctamente entre JSON y binary frames.
- Tests unitarios pasando.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `docs/contracts/api/asyncapi/messages/game-avatar-event.yaml` | Modificación |
| `framework/frontend/app/src/components/game/GameEvent.ts` | Modificación |
| `framework/frontend/app/src/services/websocket.ts` | Modificación |
| `framework/frontend/app/src/services/BinaryFrameParser.ts` | Nuevo |
| `framework/frontend/app/src/services/MessageRouter.ts` | Nuevo |

## Estimación

- **Duración:** 3-4 días
- **Complejidad:** Media-Alta
- **Riesgo:** Medio

## Criterios de aceptación del sprint

1. Contrato AsyncAPI actualizado con `WELCOME`/`FAREWELL`.
2. Tipos TypeScript actualizados.
3. WebSocket configurado con `binaryType = 'arraybuffer'`.
4. BinaryFrameParser decodifica correctamente binary frames.
5. MessageRouter distingue correctamente entre JSON y binary frames.
6. Tests unitarios pasando.

## Dependencias bloqueantes

- [ ] SPRINT-088 completado (backend).

## Handoffs a otras capas

Ninguno.

## Notas adicionales

Este sprint sienta las bases para la integración de audio. Los siguientes sprints crearán AudioService, AudioCache y la integración en escenas.
