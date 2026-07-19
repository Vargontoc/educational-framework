# FEAT-008 - TTS Product Requirements Integration

## Status

state: accepted
user_history: Integración de necesidades de producto del equipo: timeout, fallback, pre-generación, disponibilidad y configuración parental
depends_on: `docs/architecture/decisions/ADR-012-Replain-tts-service.md`, `docs/product/features/tts/FEAT-002-Contracts-API.md`, `docs/product/features/tts/FEAT-006-Chatterbox-Integration.md`
owned_by: tts
scope: tts-service configuration and backend requirements
test: Validate timeout behavior, status endpoint accuracy, and integration contract with backend for fallback and pre-generation.

## Description

El equipo de producto ha definido necesidades específicas para el servicio TTS que afectan tanto a la configuración del servicio como a los requisitos que el backend debe implementar. Esta feature documenta las decisiones confirmadas y establece el contrato de integración entre TTS y backend.

El objetivo es que el niño reciba audio inmediato y coherente, y que los padres tengan control y feedback claro sobre el estado del servicio.

## In Scope

### En el servicio TTS (`tts-educational`)

- Cambiar timeout de generación de 30s a 5s para texto dinámico (nombre del niño).
- Verificar disponibilidad real de Chatterbox en el endpoint `/status`.
- Mantener endpoint `/synthesize` para generación bajo demanda.

### En el servicio backend (`api-educational`)

- Implementar fallback a audio pregrabado genérico cuando TTS falla o excede timeout.
- Implementar pre-generación de audios al registrar/editar nombre del niño.
- Implementar configuración parental de audio (lógica en backend).
- Almacenar y servir audios generados.
- Manejar reconexión tras pérdida de conexión.

## Out Of Scope

- Frontend changes (la implementación visual de controles parentales).
- WebSocket changes.
- Nuevos endpoints en TTS más allá de las modificaciones de status.
- Infraestructura Docker Compose.
- Gestión de múltiples voces por modelo (v1.0 solo 1 por tipo).

## Product Decisions

### Decision 1: Timeout de generación de audio dinámico

**Contexto**: Cuando el padre registra el nombre del niño, el backend envía el texto formateado al TTS. Si la generación tarda demasiado, el niño percibirá demora.

**Decisión**: El timeout máximo de generación es **5 segundos**.

**Comportamiento esperado**:
- Si Chatterbox responde en ≤5s → audio personalizado listo.
- Si Chatterbox excede 5s → backend usa audio pregrabado genérico (ej: "Hola, vamos a jugar").

**Justificación**: La interacción del niño debe ser inmediata. 5 segundos es el máximo aceptable para una generación que ocurre antes de que el niño inicie sesión.

### Decision 2: Fallback a audio pregrabado

**Contexto**: Si el TTS falla o excede timeout, el niño no debe quedarse sin audio.

**Decisión**: El **backend** es responsable de implementar el fallback.

**Comportamiento esperado**:
- Backend almacena audios pregrabados genéricos para cada tipo de interacción.
- Si TTS falla → backend sirve audio pregrabado genérico.
- El niño siempre recibe audio, aunque no sea personalizado.

**Justificación**: El TTS es un servicio de síntesis. La lógica de negocio (¿qué hacer si falla?) pertenece al backend.

### Decision 3: Pre-generación de audios

**Contexto**: Los audios del NPC deben estar listos cuando el niño inicia sesión.

**Decisión**: El **backend** es responsable de pre-generar audios.

**Comportamiento esperado**:
- Padre registra nombre → backend envía texto al TTS → audio generado se almacena.
- Cuando niño inicia sesión → backend sirve audio pregenerado (sin latencia).
- Si TTS falla durante pre-generación → backend usa audio genérico.

**Justificación**: La pre-generación elimina latencia durante el juego. El backend controla el ciclo de vida del audio.

### Decision 4: Verificación de disponibilidad en status

**Contexto**: Los padres necesitan saber si el servicio TTS está funcionando.

**Decisión**: El endpoint `/status` de TTS debe **verificar disponibilidad real** de Chatterbox.

**Comportamiento esperado**:
- `/status` intenta conectar con Chatterbox.
- Si Chatterbox responde → `"state": "ready"`.
- Si Chatterbox no responde → `"state": "unavailable"`.

**Justificación**: Los padres necesitan feedback claro. Un status "ready" cuando Chatterbox está caído genera confusión.

### Decision 5: Configuración parental de audio

**Contexto**: Los padres quieren controlar el audio según el contexto.

**Decisión**: Lógica en **backend**, iteración en **frontend**.

**Comportamiento esperado**:
- Panel global: Control conjunto de ambas voces (npc y storyteller).
- Perfil del niño: Solo control de npc-voice.
- Experiencia lectura: Solo control de narrative-voice.
- Backend almacena preferencias y aplica al servir audio.
- Frontend muestra controles y envía cambios al backend.

**Justificación**: El backend es el único que conoce el contexto de cada audio. El frontend solo necesita mostrar controles y enviar preferencias.

## Actors and Use Cases

### Actor 1: Niño (3-4 años)

| Escenario | Comportamiento esperado |
|-----------|------------------------|
| Inicia sesión y juega con NPC | Escucha audio del NPC inmediatamente (pre-generado o genérico). |
| NPC dice su nombre | Audio personalizado si está generado; genérico si no. |
| Escucha un cuento | Narración controlada por padre, audio claro. |
| TTS no disponible | Juega con animaciones visuales, sin audio (aceptable). |

### Actor 2: Padre/Madre

| Escenario | Comportamiento esperado |
|-----------|------------------------|
| Registra nombre del niño | Backend genera audio personalizado en background. |
| Ve panel parental | Ve estado del TTS (disponible/no disponible). |
| Configura audio | Controla volumen y habilitación por contexto. |
| TTS falla | Ve feedback claro: "Servicio de audio no disponible". |

## Functional Requirements

### TTS Service Requirements

| ID | Requisito | Prioridad |
|----|-----------|-----------|
| TTS-R1 | Endpoint `/status` verifica disponibilidad real de Chatterbox | Alta |
| TTS-R2 | Timeout de generación es 5 segundos (configurable) | Alta |
| TTS-R3 | Endpoint `/synthesize` mantiene comportamiento actual | Media |

### Backend Requirements

| ID | Requisito | Prioridad |
|----|-----------|-----------|
| BE-R1 | Implementar fallback a audio pregrabado genérico | Alta |
| BE-R2 | Pre-generar audios al registrar/editar nombre del niño | Alta |
| BE-R3 | Almacenar audios generados (no en TTS) | Alta |
| BE-R4 | Implementar configuración parental de audio | Media |
| BE-R5 | Manejar reconexión tras pérdida de conexión | Media |
| BE-R6 | Mostrar feedback de estado TTS en panel parental | Media |

## Non-Functional Requirements

| ID | Requisito | Justificación |
|----|-----------|---------------|
| NFR-1 | Audio personalizado disponible en ≤5s | Niño no debe percibir demora |
| NFR-2 | Fallback a audio genérico en ≤1s | Continuidad de experiencia |
| NFR-3 | Status endpoint responde en ≤500ms | Feedback inmediato al padre |
| NFR-4 | Almacenamiento de audio en backend | TTS no almacena (decisión ADR) |

## Acceptance Criteria

### TTS Service

- [ ] GET `/api/v1/tts/status` retorna `"state": "ready"` cuando Chatterbox está disponible.
- [ ] GET `/api/v1/tts/status` retorna `"state": "unavailable"` cuando Chatterbox no responde.
- [ ] POST `/api/v1/tts/synthesize` con timeout de 5s retorna error si Chatterbox excede tiempo.
- [ ] Timeout de 5s es configurable via `TTS_TIMEOUT_MS`.

### Backend

- [ ] Al registrar nombre, backend envía audio al TTS y almacena resultado.
- [ ] Si TTS falla o excede timeout, backend usa audio pregrabado genérico.
- [ ] Cuando niño inicia sesión, backend sirve audio pre-generado sin latencia.
- [ ] Panel parental muestra estado real del TTS.
- [ ] Configuración de audio se almacena por perfil de niño.
- [ ] Cambios de configuración se aplican inmediatamente.

## Privacy, Security and Child Safety

### Privacy

- Nombre del niño se usa solo para generar audio personalizado.
- No se almacenan grabaciones de voz del menor.
- Datos de audio se gestionan en entorno familiar.

### Child Safety

- Contenido de audio es predefinido y controlado.
- No hay interacción de voz del niño con el servicio (TTS es unidireccional).
- Sin audio, el juego sigue funcionando (animaciones visuales).

### Accessibility

- Animaciones del NPC refuerzan visualmente el audio.
- Juego funciona sin audio (aceptado para v1.0, niños de 3-4 años no leen subtítulos).

## Risks and Mitigations

| Riesgo | Mitigación |
|--------|-----------|
| Chatterbox no responde en 5s | Backend usa audio pregrabado genérico |
| Chatterbox caído durante pre-generación | Backend usa audio genérico y notifica al padre |
| Timeout muy corto para cuentos largos | Timeout de 5s es solo para texto dinámico (nombre); cuentos usan timeout mayor |
| Configuración parental compleja | v1.0: controles básicos; versión futura: avanzados |

## Dependencies

- `ADR-012-Replain-tts-service.md` (arquitectura TTS)
- `ADR-013-Chatterbox-unico-proveedor-TTS` (Chatterbox único proveedor)
- `ADR-014-Validacion-texto-locale-backend` (validación en backend)
- `FEAT-002-Contracts-API.md` (contrato TTS)
- `FEAT-006-Chatterbox-Integration.md` (adapter Chatterbox)

## Notes

Esta feature documenta las decisiones de producto del equipo y establece el contrato de integración entre TTS y backend. La implementación técnica correspondiente a cada ámbito (TTS, backend, frontend) se define en features separadas.

**Decisiones confirmadas por producto**:
1. Timeout: 5 segundos ✅
2. Fallback: Responsabilidad del backend ✅
3. Pre-generación: Responsabilidad del backend ✅
4. Status verification: Sí, verificar disponibilidad real ✅
5. Configuración parental: Backend lógica, frontend iteración ✅
