# SPRINT-084 — API productiva de lectura de cuentos y binarios

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-26
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-083 (catálogo de cuentos en filesystem), FEAT-008, ADR-024 (aceptada)
- **Impacto estimado:** Publica la API REST de solo lectura que consumirá el catálogo/lectura de Lectura Familiar en frontend: listado, detalle y binarios (cover, imagen y audio por página), protegida por FamilySession y con protección anti path-traversal.

## Objetivo

Exponer el catálogo de cuentos leído en SPRINT-083 mediante una API REST de solo lectura (listado, detalle, binarios de cover/imagen/audio), protegida por FamilySession, con protección estricta contra path traversal, y actualizar el contrato OpenAPI en consecuencia.

## Contexto

SPRINT-082 retiró el modelo `Story`/`StoryPage` en BD y SPRINT-083 implementó el lector de catálogo sobre filesystem con validación de completitud. Este sprint cierra el lado backend de FEAT-008 publicando la API que el frontend necesita para el catálogo, la pantalla previa del cuento y la lectura página a página.

Decisión relevante ya cerrada: al ser la inclusión en catálogo "todo o nada" (SPRINT-083), cualquier cuento devuelto por esta API tiene garantizados portada, texto, imagen y audio en todas sus páginas — el frontend no necesita gestionar recursos ausentes dentro de un cuento activo.

## Diseño funcional-técnico

### 1. Endpoints JSON

`content/infrastructure/web/StoryCatalogController.java` (nuevo, sustituye al retirado en SPRINT-082):

- `GET /api/v1/content/stories`
  → `ApiResponse<List<StoryCatalogResponse>>` con `{ id, title, coverUrl }`.
- `GET /api/v1/content/stories/{id}`
  → `ApiResponse<StoryDetailResponse>` con `{ id, title, coverUrl, pages: [{ page, text, imageUrl, audioUrl }] }`.
  → 404 (`ResourceNotFoundException`) si `{id}` no está presente en el catálogo cacheado de `StoryCatalogService`.

`coverUrl`/`imageUrl`/`audioUrl` son rutas relativas a los endpoints de binario definidos abajo, no rutas de filesystem. El frontend debe tratarlas como referencias opacas (coherente con la regla ya vigente en FEAT-003 "Mitigations by layer / Frontend").

### 2. Endpoints de binario

- `GET /api/v1/content/stories/{id}/cover` → `cover.png`, `Content-Type: image/png`.
- `GET /api/v1/content/stories/{id}/pages/{page}/image` → `page_{page}.png`, `Content-Type: image/png`.
- `GET /api/v1/content/stories/{id}/pages/{page}/audio` → `page_{page}.mp3`, `Content-Type: audio/mpeg`.

Todos devuelven 404 si `{id}` no está en el catálogo o `{page}` no es una página válida de ese cuento.

**Streaming:** servir con `Resource`/`InputStreamResource` en la respuesta en vez de cargar el fichero completo en memoria, para no penalizar el rendimiento con audios/imágenes de tamaño mayor.

### 3. Seguridad — protección anti path-traversal

`{id}` y `{page}` llegan del cliente. Regla obligatoria: **nunca** se usan directamente para construir una `Path` de filesystem.

Flujo correcto:
1. `StoryCatalogService.findById(id)` resuelve contra el catálogo ya validado en SPRINT-083 (en memoria, sin tocar filesystem con el valor crudo).
2. Si no existe → 404 inmediato, sin acceso a disco.
3. Si existe, la ruta física del recurso (`cover.png`, `page_{n}.png/mp3`) se construye a partir del `id` **ya validado** por el catálogo (que solo contiene nombres de carpeta reales, descubiertos por el propio escaneo de SPRINT-083), no del string recibido en la request.
4. `{page}` se valida como página existente dentro de `pages` del cuento encontrado antes de resolver el fichero.

Esto hace estructuralmente imposible un `../../etc/passwd` como `id`: cualquier valor que no coincida exactamente con una carpeta ya descubierta y validada por el escaneo se rechaza en el paso 2, antes de tocar el filesystem.

### 4. Autenticación

Mantener el mismo criterio que el `ProductiveStoryController` original: todos los endpoints bajo `/api/v1/content/stories/**` requieren `FamilySession` (Bearer token del login por PIN), igual que el resto de `/api/v1/content/**` consumido por el panel parental.

### 5. Contrato OpenAPI

Recrear en `docs/contracts/api/openapi/schemas/content/`:
- `story-response.yaml` / `api-story-response.yaml`: `id` (string), `title`, `coverUrl`.
- `story-page-response.yaml`: `page` (integer), `text`, `imageUrl`, `audioUrl` — **sin nullable**, ya que la inclusión en catálogo garantiza completitud (SPRINT-083).
- `api-list-story-response.yaml`.

No se recrean schemas de creación/edición: la API es exclusivamente de lectura.

Actualizar `docs/contracts/api/openapi.json` con los nuevos paths y schemas.

## Contratos y dependencias externas

### Contratos

Publicación de:
- `GET /api/v1/content/stories`
- `GET /api/v1/content/stories/{id}`
- `GET /api/v1/content/stories/{id}/cover`
- `GET /api/v1/content/stories/{id}/pages/{page}/image`
- `GET /api/v1/content/stories/{id}/pages/{page}/audio`

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | Consumir `GET /api/v1/content/stories` y `GET /api/v1/content/stories/{id}` vía tipos derivados de OpenAPI, para el catálogo y la pantalla previa de FEAT-008 | ⏳ Pendiente |
| Frontend | Usar `coverUrl`/`imageUrl`/`audioUrl` como referencias opacas (`src` de `<img>`/`<audio>`) | ⏳ Pendiente |
| Frontend | Tratar 404 de cuento como "cuento no disponible" en la UI, sin romper la navegación | ⏳ Pendiente |
| Agents/TTS | Ninguna (ADR-024 excluye integración en tiempo real y TTS narrativo para este catálogo) | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Path traversal si `{id}`/`{page}` se usaran directamente para resolver ficheros | ALTA | Mitigado por diseño: toda resolución pasa primero por el catálogo cacheado y validado (sección 3); ningún valor crudo del cliente llega al filesystem. |
| R2 | Servir binarios grandes cargando el array de bytes completo en memoria por request | MEDIA | Streaming con `Resource`/`InputStreamResource` en vez de `byte[]`. |
| R3 | Cambio de contrato respecto al `StoryResponse` anterior (`id` `Long` → string) | BAJA | Sin impacto real: confirmado que no hay consumidores en producción del contrato anterior (SPRINT-082). |
| R4 | Cliente antiguo o test manual sigue apuntando a los endpoints retirados en SPRINT-082 | BAJA | Comunicar el nuevo contrato en la revisión del sprint; verificar que no queden referencias en frontend/documentación al contrato viejo. |

---

## Tareas del sprint

### Tarea 84.1: Implementar `StoryCatalogController` — listado y detalle (JSON)

**Descripción:** Endpoints `GET /api/v1/content/stories` y `GET /api/v1/content/stories/{id}` sobre `StoryCatalogService` (SPRINT-083).

**Criterios de aceptación:**
- Listado devuelve solo cuentos válidos, con `id` = nombre de carpeta.
- Detalle devuelve 404 (`ResourceNotFoundException`) si `{id}` no está en el catálogo.
- Respuestas envueltas en `ApiResponse<T>`.

---

### Tarea 84.2: Implementar endpoints de binario (cover, imagen y audio por página)

**Descripción:** Los tres endpoints descritos en la sección 2, con streaming y `Content-Type` correcto.

**Criterios de aceptación:**
- `Content-Type: image/png` para cover e imagen de página; `audio/mpeg` para audio.
- Uso de `Resource`/`InputStreamResource`, no `byte[]` completo en memoria.
- 404 si `{id}` no existe en catálogo o `{page}` no es válida para ese cuento.

---

### Tarea 84.3: Implementar validación anti path-traversal

**Descripción:** Garantizar que la resolución de rutas de fichero pasa siempre por el catálogo ya validado (sección 3), nunca por concatenación directa del valor recibido en la request.

**Criterios de aceptación:**
- Un `id` con secuencias `../` o rutas absolutas nunca resuelve un fichero fuera de `resources/stories/`.
- Test explícito con `id` malicioso (`../../etc/passwd`, `..%2F..%2F...`) devuelve 404, no 500 ni contenido inesperado.

---

### Tarea 84.4: Mantener protección `FamilySession`

**Descripción:** Confirmar que todos los endpoints bajo `/api/v1/content/stories/**` exigen `FamilySession` válida, igual que el resto de `/api/v1/content/**`.

**Criterios de aceptación:**
- Petición sin token válido → 401 en los 5 endpoints.
- Petición con `FamilySession` válida → funciona según el resto de criterios.

---

### Tarea 84.5: Actualizar contrato OpenAPI

**Descripción:** Recrear los schemas de story-response/story-page-response/api-list-story-response y publicar los nuevos paths en `docs/contracts/api/openapi.json` (sección 5 del diseño).

**Criterios de aceptación:**
- OpenAPI incluye los 5 endpoints con sus schemas de respuesta.
- No incluye schemas de creación/edición (fuera de alcance, es solo lectura).

---

### Tarea 84.6: Tests de integración

**Descripción:** Cobertura de integración de los 5 endpoints.

**Criterios de aceptación:**
- Listado devuelve solo cuentos válidos.
- Detalle de cuento existente devuelve todos los campos; de cuento inexistente devuelve 404.
- Binario de página inexistente devuelve 404.
- Intento de path traversal en `{id}` rechazado (404, sin acceso a fichero fuera de `resources/stories`).
- Petición sin autenticación devuelve 401 en los 5 endpoints.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `content/infrastructure/web/StoryCatalogController.java` | Nuevo |
| `content/infrastructure/dto/StoryCatalogResponse.java`, `StoryDetailResponse.java`, `StoryPageResponse.java` | Nuevos (reemplazan a los retirados en SPRINT-082) |
| `docs/contracts/api/openapi/schemas/content/story-response.yaml`, `api-story-response.yaml`, `story-page-response.yaml`, `api-list-story-response.yaml` | Recreados |
| `docs/contracts/api/openapi.json` | Actualizado |
| `src/test/java/.../content/StoryCatalogControllerIntegrationTest.java` | Nuevo |

## Estimación

- **Duración:** 1.5–2 días
- **Complejidad:** Media
- **Riesgo:** Medio (superficie de seguridad por path traversal)

## Criterios de aceptación del sprint

1. `GET /api/v1/content/stories` devuelve solo cuentos válidos según SPRINT-083, con `id` = nombre de carpeta. *(Listado)*
2. `GET /api/v1/content/stories/{id}` devuelve el detalle completo o 404 si no existe/no es válido. *(Detalle)*
3. Los tres endpoints de binario sirven el fichero correcto con `Content-Type` correcto. *(Binarios)*
4. Un `id` o `page` fuera del catálogo conocido devuelve 404 sin que el valor recibido llegue a resolverse contra el filesystem directamente. *(Seguridad)*
5. Un intento de path traversal en `{id}` es rechazado (404, no 500, sin acceso a fichero fuera de `resources/stories`). *(Seguridad)*
6. Todos los endpoints requieren `FamilySession` válida. *(Autenticación)*
7. `docs/contracts/api/openapi.json` publicado y coherente con las respuestas reales. *(Contrato)*
8. Tests de integración en verde. *(Calidad)*

## Dependencias bloqueantes

- [x] SPRINT-082 completado (modelo BD retirado).
- [x] SPRINT-083 completado (catálogo de solo lectura disponible en `StoryCatalogService`).

## Handoffs a otras capas

### Frontend

- Implementar catálogo (portada + título), pantalla previa del cuento y lectura página a página consumiendo estos 5 endpoints, conforme a FEAT-008.
- Tratar `coverUrl`/`imageUrl`/`audioUrl` como referencias opacas.
- Manejar 404 de cuento como "no disponible" sin romper la navegación del catálogo.
- Sin cambios adicionales de contrato pendientes con FEAT-005 (`narrativeVoiceEnabled` ya expuesto vía `useGlobalConfig`).

### Agents/TTS

- Sin dependencia (ADR-024).

## Notas adicionales

`resources/stories/story_0/` debe completarse con `cover.png`, `page_1.png` y `page_1.mp3` (tarea de contenido, no de código) para que aparezca en el catálogo una vez desplegado este sprint; hasta entonces, el listado puede estar vacío en el entorno de desarrollo si no se añade ningún cuento completo.
