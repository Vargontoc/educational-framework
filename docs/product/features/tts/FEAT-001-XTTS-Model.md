# FEAT-001 - TTS: Migrate to XTTS v2 model

## Status

state: accepted
user_history: La pronunciación del modelo base en español peninsular no es suficientemente clara para niños de 3-8 años.
owned_by: agents, infrastructure
depends_on: ADR-007-TTS-Service.md
test: El modelo XTTS v2 sintetiza frases del catálogo de curiosidades (agents/FEAT-004) en español peninsular con pronunciación comprensible para niños de 3 años.

## Description

El objetivo de esta feature es sustituir el modelo base `tts_models/es/css10/vits` por `tts_models/multilingual/multi-dataset/xtts_v2` como motor de síntesis principal. XTTS v2 está entrenado con más datos en español peninsular, soporta múltiples idiomas y ofrece voice cloning con un audio de referencia de 6 segundos sin necesidad de fine-tuning.

Esta feature es el primer paso antes de plantear fine-tuning (FEAT-002). Si la pronunciación resultante es aceptable para el vocabulario de la app, FEAT-002 puede no ser necesaria.

## Análisis

XTTS v2 frente al modelo base:

- `tts_models/es/css10/vits`: dataset CSS10 con acento latinoamericano, un único hablante adulto, vocabulario genérico.
- `tts_models/multilingual/multi-dataset/xtts_v2`: entrenado con múltiples datasets, español peninsular incluido, soporte de voice cloning con audio de referencia corto.

El cambio de modelo implica ajustes en el comando de arranque del contenedor Coqui, en la configuración del servidor y en el contrato de llamada del backend (parámetro `language_idx`).

## Requerimientos por capas

### Infrastructure

- Actualizar el comando del servicio `educational-coqui` en `docker-compose.yml`:
  - Cambiar `--model_name tts_models/es/css10/vits` por `--model_name tts_models/multilingual/multi-dataset/xtts_v2`
  - Añadir `--language_idx es` para fijar español como idioma por defecto
  - El volumen `coqui_models` persiste el modelo descargado; el primer arranque requiere descarga (~1.8GB)
- Actualizar `coqui.env.example` con las nuevas variables si se parametrizan modelo e idioma
- Validar que el contenedor arranca y el health check responde tras la descarga del modelo

### Backend

- Actualizar la llamada al endpoint `/v1/audio/speech` para incluir el parámetro `voice` con el speaker de referencia si se usa voice cloning
- Verificar que el parámetro `language_idx` se respeta en las respuestas del servidor
- Validar que el formato de audio devuelto (`response_format`) sigue siendo compatible con el frontend
- Regenerar la caché de audio completa tras el cambio de modelo — el mismo texto con XTTS v2 produce audio diferente al de VITS
- Actualizar el step de pre-warm para usar los nuevos parámetros del endpoint

### Agents

- No hay cambios en el contrato de salida del agente (`content_text`, `tone`, `locale`)
- Verificar que los textos del catálogo (curiosidades, muletillas, motivación) no superan los límites de tokens de XTTS v2 por segmento

### Frontend

- No hay cambios funcionales
- Validar en staging que el audio recibido se reproduce correctamente en los navegadores objetivo (formato MP3 por defecto en XTTS v2)

## Riesgos y mitigaciones

Riesgo: XTTS v2 tiene mayor latencia de síntesis que VITS en primera inferencia.
- Mitigación: La caché de audio absorbe el impacto para contenido de catálogo; el pre-warm en arranque calienta el modelo antes de la primera sesión.

Riesgo: El modelo pesa ~1.8GB; el primer arranque del contenedor requiere descarga.
- Mitigación: El volumen `coqui_models` persiste la descarga; solo ocurre una vez.

Riesgo: La voz por defecto de XTTS v2 puede no ser adecuada para el personaje sin audio de referencia.
- Mitigación: XTTS v2 soporta voice cloning con 6 segundos de audio; si la voz por defecto no convence, se puede pasar un audio de referencia sin re-entrenar.

Riesgo: Invalidación completa de la caché de audio existente.
- Mitigación: Vaciar el volumen de caché antes del primer arranque con el nuevo modelo; el pre-warm la reconstruye automáticamente.

## Tests y criterios de aceptación

- Infrastructure: `docker compose up educational-coqui` arranca sin errores y el health check responde en `http://educational-coqui:5002/health`.
- Backend: llamada a `/v1/audio/speech` con texto de prueba en español devuelve audio MP3 no vacío.
- Pronunciación: escucha manual de las 10 primeras entradas del catálogo de curiosidades; pronunciación comprensible para adulto evaluador.
- Caché: tras pre-warm, las peticiones de catálogo devuelven `cache_hit=true`.
- Regresión: el contrato de salida del agente no ha cambiado; los tests de schema existentes siguen pasando.

## Notas

- Si la pronunciación con la voz por defecto de XTTS v2 no es suficiente, el siguiente paso es pasar un audio de referencia de 6 segundos como parámetro `voice` antes de plantear fine-tuning completo (FEAT-008).
- La decisión de continuar con FEAT-008 se toma después de validar esta feature con el usuario final (el niño).