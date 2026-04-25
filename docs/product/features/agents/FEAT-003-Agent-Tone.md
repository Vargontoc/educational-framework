# Feat-004 - Agent Child: Tone reponse

## Status

state: proposal
user_history: El agente puede ser configurado con distintos tonos de voz, e incluso proponer tono de resultado
owned_by: agents
depends_on: FEAT-001-Agent-Child-Modelfile.md
test: El agente debe indicar el tipo de tono del mensaje que da al usuario

## Description

El objetivo de esta feature es que el agente indique el tipo de tono que tiene su mensaje, además de poder recibir por parametro un tipo de tono [calmado, alegre, entusiasta, serio] tonos relacionados con actividades para niños, nunca buscar un tono recriminador, siempre enfocado a la conciliación.

## Riesgos

- Tono inapropiado: un tono puede percibirse como acusatorio, sarcástico o confuso para niños pequeños.
- Inconsistencia TTS/texto: la síntesis de voz puede no coincidir con intención textual (prosodia vs texto plano).
- Minimización de warnings: usar un tono "suave" para temas sensibles puede dar falsa sensación de seguridad.
- Prompt injection vía parámetro de tono: si no se valida, el parámetro puede romper plantillas o prompts.
- I18n/Accesibilidad: interpretación cultural distinta de tonos; problemas de pronunciación y diacríticos.
- Combinación con tool_calls: el tono puede condicionar al agente a sugerir herramientas inapropiadas.

## Mitigaciones (por capa)

### Agents

- Output estructurado: incluir en la salida `tone` (enum limitado) y `tone_reason` breve; no improvisar valores.
- Prioridad de seguridad: si hay `safety_flags`, forzar `tone: serious` o `neutral` y no suavizar el contenido crítico.
- No interpretar `preferred_tone` como instrucciones; tratarlo como etiqueta/placeholder.

### Backend

- Validación estricta: aceptar sólo valores permitidos (`calm`,`joyful`,`enthusiastic`,`serious`,`neutral`) y mapear a presets TTS.
- Gating: override de tone cuando `safety_flags` presentes; registrar `tone_override` en auditoría.
- SSML/phonetics: backend debe generar SSML seguro (escapando texto y aplicando prosody tags) o inyectar `phonetic_hint` cuando sea necesario.
- Fallback: si motor TTS no soporta preset, usar mapping seguro y mostrar preview en UI.

### Frontend

- Controls limitados: exponer sólo presets (no entrada libre), con preview y descripción simple para padres.
- Accesibilidad: permitir subtítulos y ajustes de velocidad; opción de escuchar previa antes de confirmar.

## Cambios contractuales sugeridos

- Output schema: añadir campos `tone` (enum) y `tone_reason` (string, opcional) en `education-framework-agent-child-output-v1`.
- Input schema: permitir `preferred_tone` en metadata con validación backend.

## Flujos propuestos

1. Parent selects `preferred_tone` → Backend validates → marks `preferred_tone_approved`.
2. Event arrives → Backend injects `preferred_tone` only if approved → Agent returns `tone` in output → Backend maps to TTS preset and renders.
3. If `safety_flags` present → Backend forces `serious` and notifies parent.

## Mejoras opcionales y software libre recomendados

- Mejora 1: `phonetic_hint` + tone-aware SSML generation (produce SSML/phoneme hints for clearer TTS). Software libre recomendados:
	- Coqui TTS / TTS (https://github.com/coqui-ai/TTS): open-source, supports phoneme input and prosody controls; works with OpenTTS.
	- MaryTTS (Java): soporta SSML y phonemas, útil para pipelines on-prem.
	- eSpeak NG: excelente para generar phonemas y pruebas rápidas (ligero, multiplataforma).
	- OpenTTS (https://github.com/synesthesiam/opentts): servidor compatible con múltiples motores (facilita integración).

- Mejora 2: presets por edad (ver sección siguiente).

## Tones y presets sugeridos por edad

Notas: los valores son recomendaciones para mapping a parámetros TTS (rate multiplier, pitch semitones, volume) y a la política de uso.

- Ages 3–4 (preescolar)
	- Default: `calm`
	- Rate: 0.80–0.90, Pitch: -1 semitone, Volume: -1dB
	- Uso: instrucciones simples, frases cortas, pausas ligeramente más largas.
	- Permitted tones: `calm`, `joyful` (suave).

- Ages 5–6
	- Default: `joyful`
	- Rate: 0.95–1.00, Pitch: 0 semitones, Volume: 0dB
	- Uso: animación, feedback positivo; frases cortas con entonación ascendente en preguntas.
	- Permitted tones: `joyful`, `calm`, `enthusiastic` (moderado).

- Ages 7–8
	- Default: `enthusiastic`
	- Rate: 1.00–1.10, Pitch: +1 semitone, Volume: +1dB
	- Uso: retos, congratulaciones; mantener claridad y evitar sarcasmo.
	- Permitted tones: `enthusiastic`, `joyful`, `serious` (para seguridad).

Presets example (SSML-like):
	- Calm: `<prosody rate="85%" pitch="-1st">...</prosody>`
	- Joyful: `<prosody rate="100%" pitch="0st">...</prosody>`
	- Enthusiastic: `<prosody rate="110%" pitch="+1st">...</prosody>`
	- Serious: `<prosody rate="95%" pitch="-0.5st">...</prosody>`

## Tests y QA

- Schema tests: validar `tone` y `tone_reason` en la salida.
- Functional: fixtures con `preferred_tone` para cada edad y comprobación de TTS mapping.
- Safety: caso `preferred_tone=joyful` + `out_of_scope_query` → backend override a `serious`.

## Conclusión

Agregar soporte de tono mejora la experiencia pedagógica; hacerlo con presets controlados y validación backend reduce riesgos. Para empezar, usar Coqui TTS + OpenTTS o MaryTTS para pipelines on-prem y generar `phonetic_hint` cuando la pronunciación sea crítica.