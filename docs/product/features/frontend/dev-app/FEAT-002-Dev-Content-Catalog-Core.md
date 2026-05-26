# FEAT-002 - Dev Content Catalog Core

## Status

state: proposal
user_history: Gestionar categorías y temas del catálogo desde la mini-app de desarrollo
depends_on: FEAT-001-Dev-Content-App-Shell, backend/FEAT-003-Content-Module
owned_by: frontend
scope: frontend integration with development content API for categories and topics. No backend implementation is included in this feature.
test: Validar listado, creación, edición, filtros por categoría, errores API y estados vacíos.

## Description

El objetivo de esta feature es implementar la primera integración real de la mini-app de contenido:
gestión de categorías y temas.

Estas entidades forman el núcleo del catálogo y sirven como base para actividades, curiosidades,
recursos y textos localizados. La implementación debe consumir únicamente los endpoints de desarrollo
definidos en `docs/contracts/api/openapi.json` bajo `/api/v1/dev/content/**`.

## Scope

In scope:

- Listar categorías mediante `GET /api/v1/dev/content/categories`.
- Crear categorías mediante `POST /api/v1/dev/content/categories`.
- Editar categorías mediante `PUT /api/v1/dev/content/categories/{id}`.
- Listar temas mediante `GET /api/v1/dev/content/topics`.
- Filtrar temas por `categoryId` cuando aplique.
- Crear temas mediante `POST /api/v1/dev/content/topics`.
- Editar temas mediante `PUT /api/v1/dev/content/topics/{id}`.
- Mostrar errores de validación del API de forma inline.
- Derivar tipos desde `docs/contracts/api/openapi.json`.

Out of scope:

- Activity CRUD.
- Difficulty level CRUD.
- Resources CRUD.
- Locales, curiosities y avatar event catalog.
- Delete de categorías o temas.
- Validaciones de dominio duplicadas en frontend.

## Acceptance Criteria

- La sección Categories lista las categorías existentes.
- La sección Categories permite crear una nueva categoría con los campos requeridos por contrato.
- La sección Categories permite editar una categoría existente.
- La sección Topics lista los temas existentes.
- La sección Topics permite filtrar por categoría.
- La sección Topics permite crear un tema asociado a una categoría.
- La sección Topics permite editar un tema existente.
- Los errores `400`, `404` y `409` se muestran de forma comprensible sin romper la vista.
- Los estados loading, empty y error están cubiertos visualmente.
- Los stores llaman a services; los services llaman al cliente Axios compartido.
- No se inventan tipos locales que dupliquen el contrato.

## Technical Notes

- Crear un servicio `devContentService.ts` o equivalente para encapsular llamadas a `/api/v1/dev/content/**`.
- Crear un store específico de dev content si el estado compartido entre secciones lo justifica.
- Mantener parent-child filtering en estado simple: `selectedCategoryId` para topics.
- El frontend no debe convertir errores de contrato en reglas locales rígidas salvo para mejorar UX básica de campos requeridos.

## Risks and Mitigations

- Risk: Drift entre frontend y contrato backend.
  Mitigation: Derivar interfaces desde `docs/contracts/api/openapi.json` y revisar el contrato antes de implementar cada endpoint.

- Risk: El formulario de topics permite seleccionar una categoría inexistente.
  Mitigation: Poblar el selector desde el listado real de categorías.

- Risk: Duplicar validaciones del backend en frontend.
  Mitigation: Validar solo campos requeridos para UX; la consistencia final pertenece al backend.

## References

- ADR-011: Development Content Manager
- Backend FEAT-003: Content Module
- `docs/contracts/api/openapi.json`
