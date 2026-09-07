# SPRINT-088 — Ajustes backend para integración de audio

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-07
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** FEAT-010, FEAT-011
- **Impacto estimado:** Ajustes menores en backend para soportar la integración de audio en frontend: reducir timeout de Chatterbox, actualizar contrato AsyncAPI, poblar campo `text` en GameAvatarEvent, y actualizar documentación.

## Objetivo

Realizar ajustes menores en backend para alinear la arquitectura de audio con las decisiones de producto confirmadas: reducir timeout de Chatterbox, actualizar contrato AsyncAPI (WELCOME/FAREWELL), siempre enviar `text` en GameAvatarEvent, y eliminar referencia desactualizada a `framework/tts/`.

## Contexto

La integración de audio en frontend requiere ajustes menores en backend:

| Decisión | Impacto Backend |
|----------|-----------------|
| Reducir timeout Chatterbox | De 90s a 15-20s |
| Actualizar contrato AsyncAPI | `WELCOME`/`FAREWELL` en lugar de `SESSION_CONNECTED`/`SESSION_DISCONNECTED` |
| Siempre enviar `text` | Poblar `GameAvatarEvent.text` con mensaje del catálogo |
| Eliminar referencia `framework/tts/` | Actualizar `AGENTS.md` |

## Diseño funcional-técnico

### 1. Reducir timeout de Chatterbox

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/audio/infrastructure/adapters/out/ChatterboxAdapter.java`

**Cambio:** Reducir timeout de 90s a 15-20s.

**Justificación:** 90s es excesivo para juego infantil. Si TTS no responde en 15-20s, es mejor hacer fallback a audio estático.

### 2. Actualizar contrato AsyncAPI

**Archivo:** `docs/contracts/api/asyncapi/messages/game-avatar-event.yaml`

**Cambio:** Renombrar `SESSION_CONNECTED` → `WELCOME` y `SESSION_DISCONNECTED` → `FAREWELL`.

**Justificación:** Alinear contrato con decisiones de producto.

### 3. Siempre enviar `text` en GameAvatarEvent

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/avatar/infrastructure/service/AvatarService.java`

**Cambio:** Poblar `GameAvatarEvent.text` con el mensaje del catálogo, independientemente de si hay audio disponible.

**Justificación:** Frontend puede usar `text` como fallback si audio falla (aunque actualmente no se renderice para niños de 3-4 años).

### 4. Actualizar tipos en backend

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/avatar/domain/enums/AvatarEventType.java`

**Cambio:** Verificar que el enum tenga `WELCOME` y `FAREWELL` (no `SESSION_CONNECTED`/`SESSION_DISCONNECTED`).

### 5. Eliminar referencia a `framework/tts/`

**Archivo:** `AGENTS.md`

**Cambio:** Eliminar o actualizar la referencia a `framework/tts/` que no existe.

**Justificación:** Documentación desactualizada.

## Contratos y dependencias externas

### Contratos afectados

| Contrato | Cambio |
|----------|--------|
| `game-avatar-event.yaml` | Renombrar `SESSION_CONNECTED` → `WELCOME`, `SESSION_DISCONNECTED` → `FAREWELL` |

### Dependencias de otras capas

| Capa | Dependencia | Impacto |
|------|-------------|---------|
| Frontend | Frontend debe actualizar tipos TypeScript para alinearse con nuevo contrato | Medio |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Reducir timeout causa fallos en TTS lento | MEDIA | Monitorear métricas de TTS, ajustar si es necesario |
| R2 | Cambio de contrato rompe frontend | BAJA | Coordinar con frontend para actualizar tipos simultáneamente |
| R3 | `text` siempre enviado aumenta tamaño de mensajes | BAJA | Impacto mínimo (texto corto) |

---

## Tareas del sprint

### Tarea 88.1: Reducir timeout de Chatterbox

**Archivo:** `ChatterboxAdapter.java`

**Criterios de aceptación:**
- Timeout reducido de 90s a 15-20s.
- Tests actualizados para reflejar nuevo timeout.

### Tarea 88.2: Actualizar contrato AsyncAPI

**Archivo:** `game-avatar-event.yaml`

**Criterios de aceptación:**
- `eventType` enum actualizado con `WELCOME` y `FAREWELL`.
- Descripción actualizada.

### Tarea 88.3: Siempre enviar `text` en GameAvatarEvent

**Archivo:** `AvatarService.java`

**Criterios de aceptación:**
- `GameAvatarEvent.text` siempre poblado con mensaje del catálogo.
- Tests actualizados.

### Tarea 88.4: Actualizar tipos en backend

**Archivo:** `AvatarEventType.java`

**Criterios de aceptación:**
- Enum tiene `WELCOME` y `FAREWELL`.
- Código que usa estos tipos actualizado.

### Tarea 88.5: Eliminar referencia a `framework/tts/`

**Archivo:** `AGENTS.md`

**Criterios de aceptación:**
- Referencia eliminada o actualizada.

### Tarea 88.6: Pruebas

**Criterios de aceptación:**
- Tests unitarios pasando.
- Tests de integración pasando.

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|----------------|
| `framework/backend/src/main/java/es/vargontoc/educational/framework/audio/infrastructure/adapters/out/ChatterboxAdapter.java` | Modificación (timeout) |
| `docs/contracts/api/asyncapi/messages/game-avatar-event.yaml` | Modificación (enum) |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/avatar/infrastructure/service/AvatarService.java` | Modificación (text field) |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/avatar/domain/enums/AvatarEventType.java` | Verificación |
| `AGENTS.md` | Modificación (documentación) |

## Estimación

- **Duración:** 1 día
- **Complejidad:** Baja
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. Timeout de Chatterbox reducido a 15-20s.
2. Contrato AsyncAPI actualizado con `WELCOME`/`FAREWELL`.
3. `GameAvatarEvent.text` siempre poblado.
4. Tipos en backend actualizados.
5. Documentación actualizada.
6. Tests pasando.

## Dependencias bloqueantes

- [ ] Ninguna.

## Handoffs a otras capas

| Capa | Handoff | Prioridad |
|------|---------|-----------|
| Frontend | Actualizar tipos TypeScript para alinearse con nuevo contrato | Alta |

## Notas adicionales

Este sprint realiza ajustes menores en backend para soportar la integración de audio en frontend. Los cambios son coordinados con el equipo de frontend para evitar incompatibilidades.
