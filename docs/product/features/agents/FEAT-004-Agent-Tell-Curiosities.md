# Feat-004 - Agent Child: Tone reponse

## Status

state: proposal
user_history: El agente según ciertos eventos del juego contará una curiosidad divertida y educativa.
owned_by: agents
depends_on: FEAT-001-Agent-Child-Modelfile.md
test: El agente debe dar una curiosidad divertida

## Description

El objetivo de esta feature el agente según ciertos eventos del juego contará una curiosidad divertida y educativa relacionada con el tema que venga del evento por ejemplo si la respuesta del evento es "Perro" el agente dirá "¿Sabías que los perros son conocidos como el mejor amigo del hombre?"

Las curiosidades tienen que ser cortas para niños de a partir de 3 años. El objetivo es mantener el juego divertido y despertar la cueriosidad del niño para que explore fuera de la aplicación.

Estas curiosidades también son configurables por el panel de control de los padres, activo/ desactivo, frequencia del evento de curiosidad, etc.

## Análisis

Resumen: el backend mantiene un catálogo curado de curiosidades (locale-aware) que el servicio de juego selecciona según el evento y el rango de edad del niño. El agente recibe únicamente la curiosidad seleccionada (texto + metadata mínima) para convertirla en respuesta estructurada y/o SSML si procede.

Requerimientos clave:
- Seguridad y privacidad: no incluir PII ni referencias locales sensibles.
- Brevedad: textos aptos desde 3 años; preferir 1–2 frases simples.
- Localización: cada entrada lleva `locale` y `tags` para filtrado temático.
- Controles parentales: encendido/apagado y frecuencia configurable desde el panel de padres.

## Esquema de datos (catalogo)

Cada entrada del catálogo (JSON) debe incluir al menos estos campos:

- `id` (string): identificador único, ej. "c001".
- `text` (string): curiosidad corta en el idioma indicado.
- `age_min` (integer): edad mínima recomendada.
- `age_max` (integer): edad máxima recomendada.
- `tags` (array[string]): temas para filtrar (ej. "animales", "espacio").
- `locale` (string): código de localización, ej. "es-ES".
- `phonetic_hint` (string, opcional): ayuda para TTS pronunciación.
- `length_chars` (integer, opcional): longitud en caracteres.

Ejemplo: ver el catálogo de muestra en `docs/product/features/agents/curiosities_catalog_sample_es.json`.

## Flujo de eventos (resumen)

1. El juego genera un evento (ej. `topic_answered: { topic: "perro" }`) y solicita una curiosidad.
2. Backend valida permisos parentales y filtros de frecuencia; consulta el catálogo y selecciona una entrada adecuada (edad, locale, tags).
3. Backend envía al agente la carga mínima: `{curiosity: { id, text, locale, phonetic_hint } , truncated_context_id }`.
4. El agente formatea la respuesta siguiendo el Modelfile (voz/tone constraints) y devuelve JSON validado al contrato.
5. Backend registra el evento y, si procede, reproduce TTS o notifica al frontend.

## Riesgos y mitigaciones

Riesgo: Contenido inapropiado o no verificado (información errónea para niños).
- Mitigación: Curación humana inicial del catálogo; reglas de revisión de fuentes; marcar entrada con `reviewed_by` y `review_date` (implementación futura).

Riesgo: PII o referencias locales en textos generados dinámicamente.
- Mitigación: Solo usar entradas del catálogo curado; cualquier texto generado por LLM parafraseado debe pasar por redactor/validator en backend que elimine PII.

Riesgo: Frecuencia excesiva que interrumpe la experiencia de juego.
- Mitigación: Control por parents (panel), rate limiter en backend y opción de pausa por sesión.

Riesgo: Tono o vocabulario no apropiado para la edad.
- Mitigación: Filtrado por `age_min`/`age_max` y parámetros deterministas del modelo (baja temperatura); pruebas unitarias por rango de edad.

Riesgo: Problemas de pronunciación en ciertos TTS locales.
- Mitigación: `phonetic_hint` opcional y selección de TTS compatible con SSML; probar con TTS opensource (Coqui/OpenTTS) en staging.

## Requerimientos por capas

- Backend: almacenar y servir catálogo, validar permisos parentales, rate limiting, auditoría (input_hash, curiosity_id).
- Agent / Modelfile: recibir sólo texto curado y metadata mínima; no realizar búsquedas externas ni pedir información personal.
- Frontend: controles parentales UI (activar/desactivar, frecuencia) y opciones de reproducción (texto/voz).
- Ops: despliegue de catálogo en storage accesible, backups y proceso de revisión of entries.

## Testing y criterios de aceptación

- Pruebas unitarias: validación de esquema JSON (cada entrada tiene campos requeridos).
- Pruebas funcionales: flujo completo desde evento hasta respuesta del agente con el contrato esperado.
- Aceptación: el agente responde con una curiosidad válida y apropiada para la edad, reproducible mediante TTS en staging.

## Auditoría y monitoreo

- Registrar `curiosity_id`, `event_type`, `truncated_context_id` y marca temporal en logs para auditoría.
- Métricas: tasa de uso de curiosidades, rechazos por edad, feedback parental (si existe).

## Referencias

- Catálogo de muestra: `docs/contracts/schemas/curiosities_catalog_sample_es.json`.

## Notas futuras

- Añadir campos opcionales: `source`, `reviewed_by`, `safety_flags`.
- Plantear UI para revisión y edición por curadores desde el panel de contenidos.
