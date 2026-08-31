# SPRINT-082 — Retirada de Story/StoryPage administrados en base de datos

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-26
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** ADR-024 (aceptada), FEAT-003 (amendment 2026-08-26), FEAT-008
- **Impacto estimado:** Elimina por completo el módulo `Story`/`StoryPage` basado en BD (modelo, persistencia, servicios, validadores, controllers, DTOs, migración). Sin capa de compatibilidad: no hay datos ni consumidores en producción ni en desarrollo.

## Objetivo

Retirar completamente el módulo `Story`/`StoryPage` administrado por base de datos, conforme a la amendment de FEAT-003 y a ADR-024, dejando el módulo `content` limpio antes de reconstruir el catálogo de cuentos sobre filesystem (SPRINT-083/SPRINT-084).

## Contexto

FEAT-003 había previsto `Story`/`StoryPage` como catálogo administrado internamente vía CRUD. ADR-024 (2026-08-26) confirma que los cuentos de Lectura Familiar son un recurso producido por una herramienta externa (Agente Cuenta cuentos) y recibido por backend como directorio con datos + imágenes + audio, no como entidades administradas campo a campo. La amendment de FEAT-003 marca `Story`/`StoryPage` como deprecated y exige su retirada.

**Decisión confirmada (2026-08-26):** no hay datos de `Story`/`StoryPage` en producción ni en desarrollo, por lo que la retirada es un borrado directo, sin migración de datos ni periodo de convivencia con el nuevo modelo.

## Diseño funcional-técnico

### 1. Alcance del borrado 
Módulo de dominio y persistencia:
- `content/model/Story.java`, `content/model/StoryPage.java`
- `content/validation/StoryValidator.java`, `content/validation/StoryPageValidator.java`
- `content/infrastructure/persistence/StoryJpaEntity.java`, `StoryPageJpaEntity.java`
- `content/infrastructure/persistence/StoryJpaRepository.java`, `StoryPageJpaRepository.java`
- `content/infrastructure/persistence/StoryPersistenceAdapter.java`, `StoryPagePersistenceAdapter.java`
- `content/ports/out/StoryRepository.java`, `StoryPageRepository.java`
- `content/ports/in/StoryUseCase.java`, `StoryPageUseCase.java`
- `content/service/StoryService.java`, `StoryPageService.java`

DTOs:
- `content/infrastructure/dto/CreateStoryRequest.java`, `UpdateStoryRequest.java`, `StoryResponse.java`, `StoryDetailResponse.java`
- `content/infrastructure/dto/CreateStoryPageRequest.java`, `UpdateStoryPageRequest.java`, `StoryPageResponse.java`

Controllers:
- `content/infrastructure/web/StoryController.java` (dev CRUD, `/api/v1/dev/content/stories`)
- `content/infrastructure/web/ProductiveStoryController.java` (lectura productiva actual, `/api/v1/content/stories`)

El catálogo productivo se reconstruye por completo en SPRINT-084 sobre el nuevo modelo de filesystem; no se conserva ningún fragmento del controller actual.

### 2. Migración Liquibase de baja

Nueva migración `032__drop_story_and_story_page.xml`:
- `dropAllForeignKeyConstraints` o `dropForeignKeyConstraint` de `fk_story_page_story` antes de eliminar tablas.
- `dropTable story_page`.
- `dropTable story`.

Incluir en `db.changelog-master.xml` después de `031__add_title_to_conversations.xml`. No modificar la migración `012__create_story_and_story_page.xml` existente (regla del proyecto: no editar migraciones ya aplicadas).

### 3. Referencias cruzadas

Revisar y limpiar:
- `SeedService` u otros seeds que referencien `Story`/`StoryPage`.
- Tests unitarios/integración existentes sobre estas clases.
- Cualquier import residual de `StoryUseCase`/`StoryPageUseCase` fuera del propio módulo.

### 4. Contrato OpenAPI

Eliminar de `docs/contracts/api/openapi/schemas/content/`:
- `create-story-request.yaml`, `update-story-request.yaml`
- `create-story-page-request.yaml`, `update-story-page-request.yaml`
- `story-response.yaml`, `api-story-response.yaml`, `api-list-story-response.yaml`
- `story-page-response.yaml`, `api-story-page-response.yaml`, `api-list-story-page-response.yaml`

`story-response`/`story-page-response` se recrean en SPRINT-084 con el nuevo contrato (id string, sin creación/edición); aquí se retiran junto con el resto para no dejar un contrato a medias.

## Contratos y dependencias externas

### Contratos

El catálogo de cuentos queda **sin API** entre este sprint y SPRINT-084. `/api/v1/content/stories*` y `/api/v1/dev/content/stories*` dejan de existir hasta que SPRINT-084 los reconstruya con el nuevo contrato.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | Ninguna — `LecturaFamiliarView.vue` es hoy un placeholder sin llamada real a `/api/v1/content/stories` | ✅ Sin bloqueo |
| Agents | Ninguna | ✅ Sin dependencia |
| TTS | Ninguna | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Un seed dev falla al arrancar si referenciaba `Story`/`StoryPage` | MEDIA | Revisar `SeedService` y eliminar cualquier seed de story antes de cerrar el sprint. |
| R2 | Referencias cruzadas olvidadas rompen la compilación | MEDIA | Build completo del backend tras el borrado; `grep` final de `Story`/`StoryPage` en el módulo `content`. |
| R3 | Drop de tabla falla en un entorno con datos residuales de pruebas manuales | BAJA | Confirmado que no hay datos en ningún entorno (decisión 2026-08-26); validar igualmente en un entorno limpio antes de aplicar. |

---

## Tareas del sprint

### Tarea 82.1: Eliminar modelo, validadores y persistencia de Story/StoryPage

**Descripción:** Borrar las clases de dominio, validación y persistencia JPA listadas en la sección 1.

**Criterios de aceptación:**
- No quedan las clases `Story`, `StoryPage`, `StoryValidator`, `StoryPageValidator`, `StoryJpaEntity`, `StoryPageJpaEntity`, `StoryJpaRepository`, `StoryPageJpaRepository`, `StoryPersistenceAdapter`, `StoryPagePersistenceAdapter`.
- Compilación sin errores tras el borrado de este grupo (puede requerir borrar también ports/services en la misma pasada si el compilador los enlaza).

---

### Tarea 82.2: Eliminar servicios, ports y DTOs de Story/StoryPage

**Descripción:** Borrar `StoryUseCase`, `StoryPageUseCase`, `StoryService`, `StoryPageService`, `StoryRepository`, `StoryPageRepository` y los DTOs de creación/actualización/respuesta.

**Criterios de aceptación:**
- No quedan referencias a estos tipos en el código fuente del backend.
- Compilación sin errores.

---

### Tarea 82.3: Eliminar controllers dev y productivo de Story/StoryPage

**Descripción:** Borrar `StoryController.java` (`/api/v1/dev/content/stories`) y `ProductiveStoryController.java` (`/api/v1/content/stories`).

**Criterios de aceptación:**
- Ambos endpoints dejan de existir (verificar con arranque en perfil `dev` y perfil productivo).

---

### Tarea 82.4: Migración Liquibase de baja

**Descripción:** Crear `032__drop_story_and_story_page.xml` que elimina `story_page` y `story` (en ese orden, por la FK), e incluirla en `db.changelog-master.xml`.

**Criterios de aceptación:**
- Migración aplica limpiamente sobre un esquema con `012__create_story_and_story_page.xml` ya aplicada.
- No se modifica ninguna migración existente.

---

### Tarea 82.5: Limpiar seeds, tests y contrato OpenAPI asociados

**Descripción:** Eliminar cualquier seed, test o schema OpenAPI que referencie `Story`/`StoryPage` (ver sección 4 del diseño).

**Criterios de aceptación:**
- `SeedService` no referencia stories.
- No quedan tests unitarios/integración sobre las clases retiradas.
- Los schemas OpenAPI listados en la sección 4 ya no existen; `docs/contracts/api/openapi.json` no referencia los endpoints retirados.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `content/model/Story.java`, `StoryPage.java` | Eliminar |
| `content/validation/StoryValidator.java`, `StoryPageValidator.java` | Eliminar |
| `content/infrastructure/persistence/Story*JpaEntity.java`, `Story*JpaRepository.java`, `Story*PersistenceAdapter.java` | Eliminar |
| `content/ports/out/StoryRepository.java`, `StoryPageRepository.java` | Eliminar |
| `content/ports/in/StoryUseCase.java`, `StoryPageUseCase.java` | Eliminar |
| `content/service/StoryService.java`, `StoryPageService.java` | Eliminar |
| `content/infrastructure/dto/*Story*.java` | Eliminar |
| `content/infrastructure/web/StoryController.java`, `ProductiveStoryController.java` | Eliminar |
| `db/changelog/migrations/032__drop_story_and_story_page.xml` | Nuevo |
| `db/changelog/db.changelog-master.xml` | Actualizar (incluir migración 032) |
| `docs/contracts/api/openapi/schemas/content/*story*.yaml` | Eliminar |
| `docs/contracts/api/openapi.json` | Actualizar |

## Estimación

- **Duración:** 0.5–1 día
- **Complejidad:** Baja
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. No quedan clases Java bajo `content/` relacionadas con `Story`/`StoryPage` en BD. *(Alcance)*
2. `/api/v1/dev/content/stories*` y `/api/v1/content/stories*` no existen tras este sprint. *(Contrato)*
3. La migración 032 elimina `story_page` y `story` correctamente en un entorno limpio. *(Persistencia)*
4. Compilación y suite de tests del backend en verde, sin referencias residuales. *(Calidad)*
5. `docs/contracts/api/openapi.json` no referencia los schemas retirados. *(Contrato)*

## Dependencias bloqueantes

- [x] ADR-024 aceptada.
- [x] Amendment de FEAT-003 confirmada.
- [x] Confirmado que no hay datos en producción ni desarrollo (decisión 2026-08-26).

## Handoffs a otras capas

### Frontend

- Ninguna acción requerida en este sprint. `LecturaFamiliarView.vue` sigue siendo un placeholder; no debe apuntarse a `/api/v1/content/stories` hasta que SPRINT-084 publique el nuevo contrato.

### Agents/TTS

- Sin dependencia.

## Notas adicionales

Este sprint es puramente de retirada: no introduce el nuevo lector de filesystem (SPRINT-083) ni la nueva API (SPRINT-084). El catálogo de Lectura Familiar queda intencionadamente sin backend funcional entre este sprint y SPRINT-084.
