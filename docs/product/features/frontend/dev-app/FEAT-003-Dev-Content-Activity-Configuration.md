# FEAT-003 - Dev Content Activity Configuration

## Status

state: proposal
user_history: Gestionar actividades, dificultades y recursos desde la mini-app de desarrollo
depends_on: FEAT-002-Dev-Content-Catalog-Core, backend/FEAT-003-Content-Module
owned_by: frontend
scope: frontend integration with development content API for activities, difficulty levels and resources. No backend implementation is included in this feature.
test: Validar listado, creación, edición, filtros por topic/activity, errores API y que no se añade lógica de juego al frontend.

## Description

El objetivo de esta feature es permitir que el equipo configure contenido jugable desde la mini-app

Esta feature debe mantener el principio de arquitectura frontend: el frontend no implementa lógica de
juego ni interpreta parámetros de motor más allá de renderizar y enviar los campos definidos por el
contrato.

## Scope

In scope:

- Listar actividades mediante `GET /api/v1/dev/content/activities`.
- Filtrar actividades por `topicId` cuando aplique.
- Crear actividades mediante `POST /api/v1/dev/content/activities`.
- Editar actividades mediante `PUT /api/v1/dev/content/activities/{id}`.
- Listar difficulty levels por `activityId`.
- Crear y editar difficulty levels.
- Listar activity resources por `activityId`.
- Crear y editar activity resources.
- Mostrar errores de validación del API de forma inline.
- Reutilizar selectores de topics y activities cargados desde API real.

Out of scope:

- Ejecutar o previsualizar motores de juego.
- Validar semántica de parámetros de motor en frontend.
- Gestionar assets físicos o uploads si el contrato no lo define.
- Delete de entidades.
- Tracking, logros o progreso infantil.

## Acceptance Criteria

- La sección Activities lista actividades existentes.
- Activities puede filtrarse por topic cuando el usuario selecciona uno.
- Se puede crear una actividad con los campos requeridos por contrato.
- Se puede editar una actividad existente.
- La sección Difficulty Levels requiere seleccionar una actividad antes de listar o crear.
- Se puede crear y editar un difficulty level asociado a una actividad.
- La sección Activity Resources requiere seleccionar una actividad antes de listar o crear.
- Se puede crear y editar un resource asociado a una actividad.
- Las relaciones parent-child se seleccionan desde datos reales cargados por API.
- El frontend no introduce lógica de juego ni transforma parámetros de motor salvo serialización básica requerida por el contrato.

## Technical Notes

- Mantener estado de selección explícito: `selectedTopicId` y `selectedActivityId`.
- Si el contrato usa metadata o parameters flexibles, renderizar controles simples de texto/JSON validando únicamente formato básico.
- Los errores de JSON inválido en campos flexibles deben mostrarse antes de enviar la petición.
- Cualquier endpoint ausente en `openapi.json` bloquea esa parte de la feature hasta actualización de contrato.

## Risks and Mitigations

- Risk: Convertir el frontend en una fuente de reglas de motor.
  Mitigation: El frontend solo edita configuración; la validez de dominio pertenece al backend.

- Risk: Campos flexibles difíciles de editar correctamente.
  Mitigation: Usar editor simple de JSON/texto en V1 y dejar editores especializados para futuras features.

- Risk: Crear resources que referencian assets inexistentes.
  Mitigation: Mostrar claramente que V1 gestiona referencias de recursos, no archivos físicos, salvo que el contrato añada upload.

## References

- ADR-011: Development Content Manager
- ADR-010: Frontend Layer Architecture
- Backend FEAT-003: Content Module
- `docs/contracts/api/openapi.json`
