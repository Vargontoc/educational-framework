# FEAT-004 - Dev Content Locales And Copy

## Status

state: proposal
user_history: Gestionar textos localizados, curiosidades y mensajes del avatar desde la mini-app de desarrollo
depends_on: FEAT-002-Dev-Content-Catalog-Core, backend/FEAT-003-Content-Module
owned_by: frontend
scope: frontend integration with development content API for locales, curiosities and avatar event catalog. No backend implementation is included in this feature.
test: Validar listado, creación, edición, filtros por entidad/topic/event type/tone/locale y errores API.

## Description

El objetivo de esta feature es gestionar contenido textual usado por la experiencia educativa:
traducciones/locales, curiosidades y mensajes predefinidos del avatar.

Estos textos pueden ser usados por TTS, agentes o fallback del avatar, por lo que la interfaz debe
facilitar edición clara, revisión rápida y control de estados, sin generar contenido automáticamente
ni ejecutar lógica de agentes.

## Scope

In scope:

- Listar locales mediante `GET /api/v1/dev/content/locales` con filtros por `entityType` y `entityId`.
- Crear locales mediante `POST /api/v1/dev/content/locales`.
- Editar locales mediante `PUT /api/v1/dev/content/locales/{id}`.
- Listar curiosities mediante `GET /api/v1/dev/content/curiosities`.
- Filtrar curiosities por `topicId`, `age` y `locale` cuando aplique.
- Crear y editar curiosities.
- Listar avatar event catalog entries mediante `GET /api/v1/dev/content/avatar-events`.
- Filtrar avatar events por `eventType`, `tone` y `locale` cuando aplique.
- Crear y editar avatar event catalog entries.
- Mostrar errores API inline.

Out of scope:

- Generación automática de textos con agentes.
- Invocación de TTS o previsualización de audio.
- Diagnósticos psicológicos o recomendaciones clínicas.
- Gestión de story catalog si no está en el contrato frontend disponible.
- Delete de entidades.

## Acceptance Criteria

- La sección Locales permite seleccionar `entityType` y `entityId` antes de listar locales.
- Se puede crear y editar un locale para una entidad soportada por contrato.
- La sección Curiosities lista curiosidades y permite filtrar por topic, age y locale si el contrato lo permite.
- Se puede crear y editar una curiosity asociada a un topic.
- La sección Avatar Events lista mensajes del catálogo y permite filtrar por event type, tone y locale.
- Se puede crear y editar un avatar event catalog entry.
- Los textos largos son editables en campos adecuados, no en inputs de una sola línea.
- Los errores `400`, `404` y `409` se muestran sin perder el estado del formulario.
- No se llama a servicios de agente ni TTS desde esta feature.

## Technical Notes

- Para V1, `es-ES` es el locale principal esperado.
- Los formularios deben dejar claro si un texto está orientado a TTS, pero la validación final pertenece al backend.
- Reutilizar datos cargados de categories/topics/activities para ayudar a seleccionar entidades relacionadas.
- Si una entidad todavía no existe en el contrato, no crear modelos locales provisionales.

## Risks and Mitigations

- Risk: Usar la mini-app para generar texto libre con agentes.
  Mitigation: Esta feature solo edita contenido; generación automática queda fuera de alcance.

- Risk: Textos demasiado largos o poco adecuados para TTS.
  Mitigation: Mostrar ayuda visual de longitud y recomendaciones, sin bloquear salvo reglas del contrato.

- Risk: Confundir locales de contenido con i18n de la interfaz.
  Mitigation: Documentar que content locales son datos de catálogo; Vue i18n solo gestiona textos de la UI.

## References

- ADR-011: Development Content Manager
- ADR-004: TTS Service
- Backend FEAT-003: Content Module
- `docs/contracts/api/openapi.json`
