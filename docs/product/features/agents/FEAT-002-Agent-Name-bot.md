# Feat-002 - Agent Child: name bot

## Status

state: proposal
user_history: Padres pueden dar un nombre especifico al bot según las preferencias de cada hijo.
owned_by: agents
depends_on: FEAT-001-Agent-Child-Modelfile.md
test: El agente debe ser capaz de recibir el nombre del bot y utilizarlo en todas sus respuestas. Por ejemplo un evento de bienvenida presentandose.

## Description

El objetivo de esta feature es permitir a los padres nombrar al bot según las preferencias de cada hijo. 
Recibirá un paramertro llamado `agent_name` en el contexto por defecto es `Nubi`

## Mitigaciones (por capa)

### Agents (modulo del agente)

- Uso seguro del `agent_name`: el agente debe recibir únicamente la versión `sanitized_agent_name` proporcionada por el backend; nunca debe confiar en entrada cruda.
- No ejecutar ni interpretar instrucciones contenidas en `agent_name` (defensa en profundidad): cualquier intento de prompt injection en el nombre debe ser tratado como texto plano.
- Limitar dónde aparece el nombre: usarlo en saludos y TTS cortos; evitar incluirlo en every system prompt o en inputs a `tool_calls` automáticos.
- Escapado y normalización: el agente debe tratar `agent_name` como un placeholder inyectado por el backend y no concatenarlo sin escape en plantillas del sistema.

### Backend (servicio que procesa eventos y contrata al agente)

- Validación y sanitización estricta antes de almacenar/inyectar:
  - Longitud máxima configurable (recomendado: 1–32 caracteres).
  - Chars permitidos: letras Unicode, dígitos, espacio, guion, apóstrofo. Rechazar saltos de línea, controles, URLs y direcciones de email.
  - Regex sugerida: `^[A-Za-zÀ-ÖØ-öø-ÿ0-9 '\\-]{1,32}$` (ajustar por locales).
- Filtro de profanidad y heurística de seguridad: rechazo automático o bloqueo para revisión humana si hay coincidencias fuertes.
- Aprobación explícita: requerir `agent_name_approved` antes de que el nombre sea inyectado en los eventos que lleguen al agente.
- Almacenamiento seguro y auditoría:
  - Guardar `sanitized_agent_name` como valor operativo.
  - Opcional: almacenar `original_name_hash` (SHA256) para auditoría en lugar del raw.
  - No loguear el nombre en texto claro; si se registra, registrar solo la versión redacted o el hash.
- Inyección segura en prompts: usar placeholders (`{{agent_name_safe}}`) y escape por el backend; nunca construir system prompts con raw user input.
- Validaciones TTS: verificar que el nombre no rompa la síntesis (longitud/duración/emoji) y solicitar `phonetic_hint` opcional al padre.
- Gateo de `tool_calls`: backend debe validar/permitir cualquier `tool_call` solicitado por el agente; `send_parent_notification` requiere consentimiento en `child_profile`.

### Frontend (UI de padres)

- UX de entrada: campo único con ayuda inline ("Máx 32 caracteres; sin URLs ni insultos"). Mostrar preview de uso: "Hola, soy Nubi" → "Hola, soy <nombre>".
- Confirmación y consentimiento: paso de confirmación donde el padre acepta el nombre y activa `agent_name_approved`.
- Feedback inmediato: mostrar motivos de rechazo con instrucciones (ej. "El nombre contiene una URL; elige otro").
- Acciones: permitir `Cambiar nombre`, `Restablecer a Nubi`, y `Eliminar nombre` con re-aprobación requerida.
- Internacionalización: permitir locales y mostrar guidelines por idioma (acentos, caracteres especiales).

## Flujos propuestos (registrar)

1. Set Name (flujo normal)
	- Padre envía `agent_name` desde UI → Backend valida y sanitiza.
	- Si validación falla → backend responde `{accepted:false, reason}` y UI muestra mensaje.
	- Si validación pasa → backend guarda `sanitized_agent_name` y responde `{accepted:true, approved:false}` y muestra pantalla de confirmación.
	- Padre confirma → backend marca `agent_name_approved=true`, emite evento `agent_name_set` y opcionalmente notifica auditoría.

2. Uso en eventos (runtime)
	- Backend recibe evento (ej. `activity_completed`), sólo inyecta `sanitized_agent_name` en `truncated_context` si `agent_name_approved=true`.
	- Backend llama al agente con el payload validado; agente usa el nombre solo para el `content_text` de saludo o TTS corto.

3. Cambio o borrado
	- Padre solicita cambio → repetir validación/confirmación; si nuevo nombre rechazado, mantener el anterior o fallback a `Nubi`.
	- Padre borra nombre → backend elimina `sanitized_agent_name`, registra `agent_name_deleted`, y notifica al agente que use `Nubi`.

## Tests y QA mínimos

- Unit tests de validación backend: casos válidos (Ana, Tom, O'Neil, Léo), casos inválidos (URLs, emails, control chars, >32 chars, palabras de la blacklist).
- Seguridad: fuzz tests con Unicode/emoji/payloads para asegurar que el backend normaliza y el agente no ejecuta instrucciones.
- E2E: set → approve → evento → ver que el `content_text` del agente contiene el nombre sanitizado.
- TTS manual: chequear pronunciación y duración para nombres populares.

## Notas de privacidad y auditoría

- Registrar eventos: `agent_name_set`, `agent_name_approved`, `agent_name_deleted` con `request_id`, `actor_id`, `sanitized_agent_name` y `original_name_hash` (no almacenar raw en logs).
- Permitir borrado y rotación bajo petición del padre.

## Conclusión

La validación y la aprobación centralizada en el backend minimizan riesgos. El agente debe mantener la regla de no confiar en entrada cruda y sólo usar el `sanitized_agent_name` aprobado.
