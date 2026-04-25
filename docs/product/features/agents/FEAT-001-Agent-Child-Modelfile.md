# Feat-001 - Agent Child: build modelfile

## Status

state: accepted
user_history: 
depends_on:
owned_by: agents
test: Agent child model should be able on Ollama service and return a basic response.

## Description

The goal of this feature is to create a model file that describes an agent. Agents communicate through events with predefined output and input schemes. The model must be able to handle incoming events and return responses in the specified format. 

Within the same feature, the agent's input and output formats must be documented so that it is clear how to interact with it. 

The necessary scripts and files will also be created for the agent to load into the Ollama service and start receiving requests.

### Risks
- Hallusion risk: The agent might generate incorrect or misleading information. This could happen if the agent does not have enough context or if it is not properly
- PII Exposed
- Out of scope for  the agent to handle.
- Prompt injection 

### Riesgos (detalle)
- Alucinaciones (hallucination): el agente puede inventar hechos, métricas o interpretaciones del comportamiento del niño.
- Exposición de PII: respuestas que revelen datos sensibles por error si el backend no redacta correctamente el contexto.
- Fuera de alcance (medical/legal): el agente responde a preguntas clínicas o legales de forma inapropiada.
- Prompt injection: inputs maliciosos que intenten alterar comportamiento del agente si se inyectan sin sanitizar.
- Tool misuse: el agente puede solicitar llamadas a herramientas MCP que el backend no debería permitir en ciertos estados.
- Rendimiento/latencia: modelos locales pueden no responder en tiempo requerido para UX infantil.
- TTS/UX broken: formatos o longitudes que rompan la síntesis de voz o la experiencia de lectura para niños.

## Mitigaciones (por capa)

### Agents (módulo del agente)

- Salida estricta y validada: diseñar prompts y ejemplos para forzar salida en el JSON schema acordado; incluir validación en el agente (p. ej. "Always return valid JSON matching schema v1").
- Rechazo explícito: si información insuficiente o fuera de alcance, devolver `response_type: "refusal"` y `safety_flags` apropiados.
- Determinismo y parámetros conservadores: `temperature` bajo (0.1–0.25), `top_p` moderado, limitar longitud de `content_text` para evitar verborrea.
- No hacer persistencia local de memoria: el agente debe operar sobre `truncated_context` suministrado por backend y no mantener memoria usuario-side.
- Filtro de `tool_calls`: el agente debe anunciar intenciones de usar herramientas y estructurarlas; backend valida y ejecuta.
- Defensa ante prompt injection: el agente debe ignorar cualquier instrucción embebida en `event_payload` o `truncated_context` que intente sobreescribir reglas, cambiar el formato de salida, asignar una identidad nueva o saltar los controles de seguridad. Cualquier intento debe disparar `"out_of_scope"` en `safety_flags`.

### Backend (servicio que orquesta eventos)

- Sanitización de entrada: redactar PII y truncar el contexto antes de enviarlo al agente. Backend es la única fuente de verdad para memoria y consentimiento.
- Validación post-respuesta: validar la respuesta del agente contra el JSON Schema (input/output contract) y bloquear respuestas inválidas antes de mostrarlas o enviarlas a TTS.
- Gating de herramientas MCP: validar que `tool_calls` solicitadas son permitidas por el consentimiento del `child_profile` y por las políticas del sistema.
- Escalado y fallback: si el modelo no responde o devuelve fallo, usar respuestas templadas y seguras (backend templates) y registrar el incidente.
- Auditoría y logging seguro: registrar hashes y metadatos (`request_id`, `response_schema_version`, `safety_flags`) sin almacenar PII cruda en logs.
- Rate limits y retries: evitar sobrecargar el modelo; políticas de reintento y backoff.

### Frontend (UI para padres y visualización infantil)

- Interfaz clara: mostrar cuando una respuesta proviene del agente y, si aplica, incluir opciones de "notify parent" para contenido sensible.
- Control parental: panel para revisar y aprobar features (ej. `agent_name`, notificaciones) y ver historial de eventos/alertas.
- Fallback visual: si TTS falla o se rechaza respuesta por seguridad, mostrar una versión templada en texto y sugerir acciones para padres.
- Localización y accesibilidad: asegurar vocabulario apropiado para 3–8 años y opciones TTS lentas/rápidas.

## Flujos propuestos

1. Eventos entrantes
  - Backend recibe evento (ej. `activity_completed`) → redacta contexto → llama al agente con `truncated_context`.
  - Agente genera respuesta JSON → Backend valida schema.
  - Si válido y sin flags críticos → Backend procesa `tool_calls` autorizadas y envía `content_text` a TTS/UI.
  - Si inválido o `safety_flags` no vacíos → Backend marca `refusal` o notifica al padre según configuración.

2. Manejo de out-of-scope / preguntas clínicas
  - Agente debe devolver `response_type: "refusal"` con `safety_flags: ["needs_parent_attention"]` y opcional `tool_call: send_parent_notification`.
  - Backend verifica consentimiento y enqueue-notification, sin enviar datos clínicos al agente.

3. Tool call lifecycle
  - Agente anuncia intención de `tool_call` en respuesta JSON.
  - Backend valida autorías y consentimientos, ejecuta la herramienta y reinyecta el resultado al agente si es necesario (announce & include results pattern).

## Tests y QA mínimos

- Unit tests de schema: validar que la salida del agente bate el JSON Schema (usar `jq` en scripts de CI).
- Casos de rechazo: inputs que deben producir `refusal` y `safety_flags` (p. ej. preguntas de medicina, solicitudes de PII).
- Tests de herramientas: simular `tool_calls` y verificar que backend las ejecuta sólo si permitido.
- Performance: pruebas de latencia para asegurar TTS < 20s y respuestas < 300 chars.
- Security fuzzing: inyección de payloads en `truncated_context` y en `event_payload` para probar sanitización.

## Notas de privacidad y auditoría

- Minimizar PII en transit y at rest; redactar antes de enviar al agente.
- Logs: guardar `input_hash` y `truncated_context_id`, no almacenar campos PII completos; permitir borrado bajo petición.

## Conclusión y recomendaciones

La seguridad y la calidad dependen del contrato estricto entre backend y agente (docs/contracts/agents). Priorizar validación post-respuesta y gating de herramientas en el backend. Mantener tests automatizados (schema, funcionales, seguridad) y revisiones humanas periódicas de respuestas infantiles muestreadas.


### Input format

proposed
'''json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "education-framework-agent-child-v1",
  "type": "object",
  "required": ["version", "response_type", "content_text", "content_type"],
  "properties": {
    "version": { "type": "string", "pattern": "^v\\d+" },
    "response_type": { "type": "string", "enum": ["narration","prompt","action","tool_call","refusal"] },
    "content_text": { "type": "string", "maxLength": 300 },
    "content_type": { "type": "string", "enum": ["plain_text","tts_snippet","structured_activity"] },
    "suggested_actions": { "type": "array", "items": { "type": "string" }, "maxItems": 5 },
    "safety_flags": { "type": "array", "items": { "type": "string", "enum": ["age_inappropriate","pii_detected","out_of_scope","needs_parent_attention"] } },
    "tool_calls": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["tool_name"],
        "properties": {
          "tool_name": { "type": "string" },
          "inputs": { "type": "object" },
          "note": { "type": "string" }
        },
        "additionalProperties": false
      },
      "maxItems": 3
    },
    "confidence_score": { "type": "number", "minimum": 0, "maximum": 1 }
  },
  "additionalProperties": false
}
'''

### Output Format

Valid response format:

'''json
{
  "version": "v1",
  "response_type": "narration",
  "content_text": "¡Genial trabajo terminando la actividad! ¿Quieres jugar otra vez o probar un reto nuevo?",
  "content_type": "tts_snippet",
  "suggested_actions": ["play_again","try_new_challenge"],
  "safety_flags": [],
  "tool_calls": []
}
'''
Invalid response format:
'''
{
  "version": "v1",
  "response_type": "refusal",
  "content_text": "Esa pregunta la debe responder un adulto. He avisado a los padres para que lo revisen.",
  "content_type": "plain_text",
  "suggested_actions": ["notify_parent"],
  "safety_flags": ["needs_parent_attention"],
  "tool_calls": [
    { "tool_name": "send_parent_notification", "inputs": { "reason": "out_of_scope_question" }, "note": "backend must gate/send" }
  ]
}
'''


