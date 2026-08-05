# Sprint 002 - Agents

## Goal
Definir y acordar los contratos de entrada/salida del chatbot con backend, integrar las reglas de selección de perfil mediante comandos, y manejar el historial conversacional de 5-8 turnos.

## Status
status: pending
started_at:
closed_at:
blocked_by: SPRINT-001
waiting_for:

## Decisiones confirmadas (2026-08-05)

1. **Selección de perfil**: mediante comandos (p. ej. `/perfil <nombre>`), interpretados por backend antes de invocar al agente. El agente no parsea comandos; backend los intercepta y actualiza el contexto.

2. **Historial conversacional**: 5-8 turnos máximo, gestionado por backend e inyectado como `conversation_history` en contexto. El agente puede referenciar turnos anteriores para mantener coherencia.

3. **Variables de contexto**: backend inyecta `parent_access_valid`, `authorized_profile_count`, `selected_profile_id`, `selected_profile_name`, `orientative_summary`, `corpus_context`, `conversation_history`, `user_message`.

4. **Longitud máxima de respuesta**: 1500 caracteres (coherente con `num_predict 512` y compatibilidad TTS).

## Tasks
- [ ] Crear `docs/contracts/schemas/parent-chat-request.v1.yaml`: solicitud de turno conversacional con campos `schema_version`, `parent_access_valid`, `authorized_profile_count`, `selected_profile_id`, `selected_profile_name`, `orientative_summary`, `corpus_context`, `conversation_history` (array de turnos), `user_message`. `conversation_history` es array de objetos con `role` (user/assistant) y `content`, máximo 8 turnos.
- [ ] Crear `docs/contracts/schemas/parent-chat-response.v1.yaml`: respuesta conversacional con campos `schema_version`, `status`, `message` (máximo 1500 caracteres).
- [ ] Crear `docs/contracts/schemas/parent-chat-error.v1.yaml`: error controlado con campos `schema_version`, `status`, `error_code` (enum: `access_not_authorized`, `invalid_request`, `model_unavailable`, `response_truncated`, `internal_error`), `message`.
- [ ] Crear `docs/contracts/endpoints/parent-chat.v1.yaml`: endpoint `/api/v1/parent/chat`, método POST, autenticación parental. Relacionar schemas de request, response y error.
- [ ] Actualizar system prompt de `agent-educational-parent/Modelfile`: añadir instrucción sobre manejo de `conversation_history` ("Recibes el historial de la sesión actual. Puedes referenciar turnos anteriores para mantener coherencia, pero no extiendas el historial ni inventes turnos previos."), reforzar que el agente no ve comandos; backend los interpreta y actualiza el contexto.
- [ ] Actualizar `framework/agents/agent-educational-parent/smoke-test.ps1`: añadir prueba con `authorized_profile_count > 1` y `selected_profile_id` nulo → agente solicita selección, añadir prueba con `authorized_profile_count = 1` y `selected_profile_id` nulo → agente responde sin solicitar selección, añadir prueba con `selected_profile_id` poblado → agente responde limitado a ese perfil, añadir prueba con `conversation_history` de 3 turnos → agente mantiene coherencia.
- [ ] Coordinar con backend: confirmar nombres de campo de contexto, confirmar formato de inyección en prompt (texto interpolado o JSON estructurado), confirmar mecanismo de comandos (backend intercepta `/perfil` y actualiza contexto), confirmar gestión de historial conversacional (backend trunca a 8 turnos).

## Acceptance Criteria
- **Criterio FEAT-003 §4.1**: sin acceso parental (`parent_access_valid = false`) → agente no responde contenido.
- **Criterio FEAT-003 §4.3**: resumen de actividad distingue hechos de síntesis.
- **Criterio FEAT-003 §4.4**: con >1 perfil y sin selección → solicita selección explícita.
- **Criterio FEAT-003 §4.5**: con 1 perfil → no solicita selección.
- **Criterio FEAT-003 §4.6**: tras seleccionar perfil → respuesta no incluye datos de otro perfil.
- Los 4 contratos YAML están creados y revisados.
- El smoke test cubre los 3 estados de selección de perfil y historial conversacional.
- Acta de acuerdo con backend sobre nombres de campo, formato de inyección y mecanismo de comandos.

## Evidence
- `docs/contracts/schemas/parent-chat-request.v1.yaml` creado.
- `docs/contracts/schemas/parent-chat-response.v1.yaml` creado.
- `docs/contracts/schemas/parent-chat-error.v1.yaml` creado.
- `docs/contracts/endpoints/parent-chat.v1.yaml` creado.
- `framework/agents/agent-educational-parent/Modelfile` actualizado con manejo de `conversation_history`.
- `framework/agents/agent-educational-parent/smoke-test.ps1` actualizado con pruebas de selección de perfil e historial.
- Acta de acuerdo con backend (documento o comentario en PR).

## Risks
- Los contratos YAML pueden requerir ajustes tras coordinación con backend (nombres de campo, formato de inyección).
- El mecanismo de comandos (`/perfil <nombre>`) puede requerir validación adicional en backend para evitar inyección de perfiles no autorizados.
- El historial conversacional de 5-8 turnos puede consumir tokens significativamente si los turnos son largos; backend debe truncar por longitud total, no solo por número de turnos.
- La interpretación de comandos por backend puede generar ambigüedad si el usuario escribe texto que parece un comando pero no lo es (p. ej. "quiero ver el perfil de Juan" vs "/perfil Juan").

## Dependencies
- FEAT-003: Chatbot parental conversacional de Nubi.
- ADR-003: Chatbot parental conversacional de Nubi.
- SPRINT-001: System prompt, guardrails y propuesta de corpus (completado).
- Backend: implementación del endpoint `/api/v1/parent/chat`, gestión de comandos, inyección de variables de contexto, gestión de historial conversacional.
- Frontend: presentación de respuestas, flujo de comandos, presentación de derivaciones.

## Agent Instruction
- Crear exclusivamente los contratos YAML en `docs/contracts/schemas/` y `docs/contracts/endpoints/`.
- No implementar código de backend ni frontend.
- Actualizar el system prompt del Modelfile para manejar `conversation_history` y reforzar que el agente no ve comandos.
- Actualizar el smoke test para cubrir los 3 estados de selección de perfil y historial conversacional.
- Coordinar con backend para confirmar nombres de campo, formato de inyección y mecanismo de comandos.
- Documentar el acuerdo con backend en el sprint o en un documento separado.
- Actualizar tareas, estado y revisión de este sprint con pruebas ejecutadas y bloqueos reales.

## Notes
- Los contratos YAML siguen la convención de `npc-game-event.v1.yaml` (versionado, estructura clara, validación de campos).
- El campo `conversation_history` es un array de objetos con `role` (user/assistant) y `content`. Backend trunca a 8 turnos máximo.
- El agente no parsea comandos; backend intercepta `/perfil <nombre>` y actualiza `selected_profile_id` y `selected_profile_name` en el contexto antes de invocar al agente.
- La longitud máxima de `message` en la respuesta es 1500 caracteres (coherente con `num_predict 512` y compatibilidad TTS).
- Los códigos de error (`access_not_authorized`, `invalid_request`, `model_unavailable`, `response_truncated`, `internal_error`) cubren los escenarios principales de fallo.
- Backend es responsable de validar `parent_access_valid` antes de invocar al agente.
- Backend es responsable de filtrar `orientative_summary` y `corpus_context` al perfil seleccionado.

## Review

### Developer implementation — Evidencias

(Pendiente de implementación por developer-agents)

### Reviewer verification

(Pendiente de revisión por reviewer-agents)

## Design decisions

### 1. Contratos YAML versionados

**Decisión**: Crear contratos YAML versionados (`v1`) siguiendo la convención de `npc-game-event.v1.yaml`.

**Justificación**:
- Consistencia con contratos existentes del proyecto.
- Permite evolución futura sin romper integraciones existentes.
- Facilita validación automática de schemas.

### 2. Historial conversacional como array de turnos

**Decisión**: Inyectar `conversation_history` como array de objetos con `role` (user/assistant) y `content`.

**Justificación**:
- Formato estándar para conversaciones (similar a OpenAI Chat API).
- Backend puede truncar fácilmente por número de turnos o longitud total.
- El agente puede referenciar turnos anteriores para mantener coherencia.

**Alternativas descartadas**:
- Inyectar solo el último turno: pierde contexto conversacional.
- Inyectar historial completo sin truncar: consume tokens innecesariamente.

### 3. Selección de perfil mediante comandos

**Decisión**: Backend interpreta comandos (`/perfil <nombre>`) y actualiza el contexto antes de invocar al agente.

**Justificación**:
- El agente no necesita parsear comandos (reduce complejidad y superficie de ataque).
- Backend tiene control explícito sobre la selección de perfil (validación de autorización).
- Frontend puede sugerir comandos disponibles (autocompletado).

**Alternativas descartadas**:
- Agente parsea comandos: añade complejidad al prompt, riesgo de interpretación errónea.
- Frontend presenta selector visual: requiere interacción adicional, no es conversacional.

### 4. Códigos de error contractuales

**Decisión**: Definir enum de códigos de error (`access_not_authorized`, `invalid_request`, `model_unavailable`, `response_truncated`, `internal_error`).

**Justificación**:
- Backend puede traducir códigos a mensajes apropiados para el usuario.
- Facilita depuración y monitorización.
- Consistente con filosofía de errores contractuales del proyecto.

**Alternativas descartadas**:
- Mensajes de error genéricos: dificulta depuración.
- Errores técnicos detallados: puede exponer información sensible.

## Contract changes

### parent-chat-request.v1

**Nuevo contrato**:
```yaml
$schema: https://json-schema.org/draft/2020-12/schema
$id: parent-chat-request.v1
title: Parent chat request v1
description: >-
  Solicitud de turno conversacional para el chatbot parental. Solo puede ser
  invocada por backend tras validar sesión parental autenticada. No contiene
  datos de menores más allá del identificador y nombre del perfil seleccionado.
type: object
additionalProperties: false
required:
  - schema_version
  - parent_access_valid
  - authorized_profile_count
  - user_message
properties:
  schema_version:
    const: v1
  parent_access_valid:
    type: boolean
  authorized_profile_count:
    type: integer
    minimum: 0
  selected_profile_id:
    type: string
    pattern: '^[A-Za-z0-9_-]{1,64}$'
  selected_profile_name:
    type: string
    minLength: 1
    maxLength: 30
  orientative_summary:
    type: string
    maxLength: 2000
  corpus_context:
    type: string
    maxLength: 4000
  conversation_history:
    type: array
    maxItems: 8
    items:
      type: object
      required:
        - role
        - content
      properties:
        role:
          type: string
          enum: [user, assistant]
        content:
          type: string
          maxLength: 1000
  user_message:
    type: string
    minLength: 1
    maxLength: 1000
```

### parent-chat-response.v1

**Nuevo contrato**:
```yaml
$schema: https://json-schema.org/draft/2020-12/schema
$id: parent-chat-response.v1
title: Parent chat response v1
description: >-
  Respuesta conversacional del chatbot parental. Texto libre en español,
  dirigido exclusivamente a adultos. No contiene datos de menores ni
  metadatos de transporte.
type: object
additionalProperties: false
required:
  - schema_version
  - status
  - message
properties:
  schema_version:
    const: v1
  status:
    const: ok
  message:
    type: string
    minLength: 1
    maxLength: 1500
    description: >-
      Texto de respuesta para el adulto. Puede incluir la frase exacta de
      rechazo si la petición está fuera de alcance, y derivación profesional
      si la consulta lo requiere. Nunca contiene PII de menores.
```

### parent-chat-error.v1

**Nuevo contrato**:
```yaml
$schema: https://json-schema.org/draft/2020-12/schema
$id: parent-chat-error.v1
title: Parent chat error v1
description: >-
  Error controlado del chatbot parental. Sin texto conversacional dirigido
  al usuario; backend traduce el error_code a un mensaje apropiado.
type: object
additionalProperties: false
required:
  - schema_version
  - status
  - error_code
properties:
  schema_version:
    const: v1
  status:
    const: error
  error_code:
    type: string
    enum:
      - access_not_authorized
      - invalid_request
      - model_unavailable
      - response_truncated
      - internal_error
  message:
    type: string
    maxLength: 300
    description: Mensaje técnico opcional, no dirigido al usuario final.
```

### parent-chat.v1

**Nuevo contrato de endpoint**:
```yaml
$id: parent-chat.v1
title: Parent chat endpoint v1
description: >-
  Endpoint conversacional del chatbot parental. Acceso exclusivo para adultos
  autenticados en el panel parental.
path: /api/v1/parent/chat
method: POST
auth: parental_session
request_schema: parent-chat-request.v1
response_schema: parent-chat-response.v1
error_schema: parent-chat-error.v1
```
