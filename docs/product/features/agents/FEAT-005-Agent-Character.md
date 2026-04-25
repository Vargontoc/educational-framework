# Feat-005 - Agent Child: Character & Muletillas

## Status

state: accepted
user_history: El agente tiene un background de personaje
owned_by: agents
depends_on: FEAT-001-Agent-Child-Modelfile.md
test: El agente puede soltar una muletilla del personaje.

## Description

El objetivo de esta feature es dotar al agente de un `character_profile` que aporte identidad (hobbies, temas de interés y muletillas). Las muletillas son frases cortas y contextuales (ej. "¡Canasta! Muy bien hecho") que se inyectan con baja frecuencia para reforzar la personalidad sin saturar la experiencia.

## Análisis

Resumen: el catálogo de personajes es gestionado por el backend; el backend decide cuándo inyectar una muletilla según contexto del juego, controles parentales y rate limits. El agente recibe únicamente la muletilla curada y metadata mínima para incluirla en la respuesta.

Requerimientos clave:
- Control parental para activar/desactivar personajes y ajustar frecuencia.
- Muletillas curadas y revisadas humanamente antes de su despliegue.
- Filtrado por edad y contexto del minijuego para evitar incoherencias.

## Esquema de datos (character_profile)

Estructura mínima recomendada:

{
	"character_id": "string",
	"display_name": "string",
	"tags": ["string"],
	"age_preference": {"min": integer, "max": integer},
	"muletillas": [
		{"id": "string", "text": "string", "frequency": number, "context_tags": ["string"], "reviewed_by": "string"}
	],
	"safety_flags": ["string"]
}

Notas:
- `frequency`: probabilidad base (0-1) que backend combina con rate-limiter por sesión.
- `context_tags`: ej. "score", "puzzle", "reward" para filtrar aplicabilidad.

## Flujo recomendado

1. Backend valida configuración parental y estado del personaje.
2. Al ocurrir un evento (ej. `score`, `puzzle_solved`) backend filtra muletillas por `age_preference`, `context_tags` y `locale`.
3. Backend aplica rate limiting y decide si inyectar la muletilla; envía solo la frase y metadata mínima al agente.
4. El agente concatena o inserta la muletilla en la respuesta respetando constraints del Modelfile y devuelve JSON al contrato.
5. Backend registra la muletilla enviada para auditoría.

## Riesgos y mitigaciones

Riesgo: Muletillas repetitivas o intrusivas que degradan la experiencia del niño.
- Mitigación: Rate limiter por sesión, frecuencia configurable por parents, pruebas de usabilidad.

Riesgo: Contenido culturalmente inapropiado o confuso.
- Mitigación: Curación humana, `safety_flags`, y filtros automáticos de vocabulario; bloqueo por defecto de muletillas no revisadas.

Riesgo: Personificación excesiva que genere expectativas erróneas sobre capacidades del agente.
- Mitigación: Mantener frases simples y evitar promesas; mostrar en UI que el personaje es ficticio.

Riesgo: Exposición accidental de datos del niño en muletillas (ej. usar nombre, ubicación).
- Mitigación: No incluir PII en muletillas; backend debe sanear cualquier texto que pueda provenir de entradas dinámicas.

## Requerimientos por capas

- Backend: almacenar `character_profile`, aplicar rate limiting, controles parentales, auditoría y endpoints para edición curada.
- Agent/Modelfile: no generar muletillas; aceptar solo la frase curada del backend y aplicar constraints de tono/voz.
- Frontend: UI para activar/desactivar personaje, ajustar frecuencia y mostrar aviso de ficción del personaje.
- Ops: procesos de despliegue y pruebas A/B para calibrar frecuencia y aceptación.

## Testing y criterios de aceptación

- Validación de esquema JSON para `character_profile`.
- Simular eventos y comprobar que la inyección de muletillas respeta `frequency`, `age_preference` y controles parentales.
- Prueba de integración: flujo completo desde evento → backend → agente → frontend.

## Notas y mejoras futuras

- Añadir campos `reviewed_by`, `review_date`, `source` y `locale` para facilitar la gobernanza.
- Soporte para variantes de muletillas (A/B) y métricas de aceptación parental.

## Opción B — Backend templates + paraphrase control (implementación recomendada)

Resumen: Backend envía una plantilla o muletilla origen junto con `paraphrase_constraints` y una bandera `allow_paraphrase: true`. El agente puede generar una paráfrasis controlada para aportar variedad. El backend valida la paráfrasis antes de usarla en producción, con fallback a la muletilla original si la validación falla.

Campos y flags añadidos al `character_profile` / request payload:

- `allow_paraphrase` (boolean): si `true`, el agent puede paraphrasear la muletilla bajo constraints.
- `paraphrase_constraints` (object): reglas para la paráfrasis, por ejemplo `{ "max_length": 60, "forbid_new_facts": true, "max_added_entities": 0 }`.
- `original_id` (string): id de la muletilla origen enviada por backend.
- `last_used` (timestamp, optional): para cooldowns.

Respuesta esperada del agent cuando `allow_paraphrase=true`:

{
	"original_id": "m001",
	"paraphrase_text": "¡Canasta! Buen trabajo, sigamos así.",
	"paraphrase_quality_score": 0.92
}

Validación recomendada en backend (post-paraphrase):
1. Similaridad semántica: embedding(original, paraphrase) >= 0.85.
2. NER check: no PERSON/LOCATION/IDENTIFIER añadidos.
3. Safety filter: bad-words, factual-inconsistency heuristics.
4. Constraints: `paraphrase_text.length <= paraphrase_constraints.max_length`.
5. Si TODO pasa → accept; else → fallback a `original_id` text.

Parámetros del modelo sugeridos para paraphrase (determinismo y seguridad):
- `temperature`: 0.0–0.2
- `top_p`: 0.6
- `max_new_tokens`: pequeño (p. ej. 10–20)

Estrategias anti-repetición (combinadas con Opción B):
- Mantener pool rotatorio en backend y registrar `last_used` por sesión.
- Durante paraphrase, preferir transformaciones menores (sin añadir entidades).

Telemetría y auditoría:
- Registrar `original_id`, `paraphrased` (bool), `validation_passed` y `paraphrase_quality_score`.
- Guardar ejemplos fallidos para revisión humana.

Impacto operativo:
- Añade comprobación de embeddings y NER en backend (librerías/servicios adicionales).
- Incrementa latencia por la validación; planificar en SLAs y caches.

Implementación incremental sugerida:
1. Empezar con `allow_paraphrase=false` por defecto y backend pool + rotation.
2. Habilitar `allow_paraphrase=true` en staging con validación estricta y telemetry.
3. Gradual rollout con A/B y controles parentales.

