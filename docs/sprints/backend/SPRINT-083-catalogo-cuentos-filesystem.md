# SPRINT-083 — Lector de catálogo de cuentos basado en filesystem

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-26
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-082 (retirada de Story/StoryPage en BD), ADR-024 (aceptada), FEAT-008
- **Impacto estimado:** Nuevo servicio de solo lectura que escanea `resources/stories/*/`, valida completitud por convención de nombres y expone un modelo de dominio en memoria, sin persistencia en BD.

## Objetivo

Implementar un servicio de solo lectura que escanee `resources/stories/*/`, valide la completitud de cada cuento según la convención de nombres acordada y exponga un modelo de dominio de solo lectura listo para ser consumido por la API productiva (SPRINT-084).

## Contexto

ADR-024 confirma que cada cuento de Lectura Familiar es un recurso compuesto (datos + imagen + audio por página) recibido por backend, no una entidad administrada en BD. Las siguientes decisiones quedaron confirmadas el 2026-08-26:

1. El recurso es interno vía código fuente: `resources/stories/story_N/` sigue empaquetado en el jar (classpath), sin endpoint de subida ni ruta externa.
2. Se asume una **convención de nombres fija** por carpeta (no referencia explícita en `story.json`):
   - Portada: `cover.png`.
   - Por página `n` (según el campo `page` de `story.json`): `page_{n}.png` y `page_{n}.mp3`.
3. El **id público** del cuento es el nombre de la carpeta (`story_0`), sin id independiente.
4. Si faltan recursos o el número de páginas declaradas en `story.json` no coincide con los recursos de imagen/audio presentes, **se ignora la carpeta entera** (no se lista, sin fallback parcial).

## Diseño funcional-técnico

### 1. Convención de recurso (cerrada)

```
resources/stories/story_0/
  story.json        -> { "title": string, "pages": [ { "page": int, "text": string }, ... ] }
  cover.png
  page_1.png 
  page_1.mp3
  page_2.png
  page_2.mp3
  ...
```

### 2. Modelo de dominio (nuevo, sin JPA)

```java
package es.vargontoc.educational.framework.content.model;

public record StoryCatalogEntry(String id, String title) {}

public record StoryPageEntry(int page, String text) {}

public record StoryCatalog(String id, String title, List<StoryPageEntry> pages) {}
```

El modelo de dominio no incluye rutas de fichero: expone solo lo que viene de `story.json`. La resolución de rutas físicas (`cover.png`, `page_{n}.png/mp3`) es responsabilidad del adapter de infraestructura y, en SPRINT-084, del controller que sirve los binarios — así el dominio no acopla convención de nombres de fichero con el resto del backend.

### 3. Puerto y adapter

```java
package es.vargontoc.educational.framework.content.ports.out;

public interface StoryCatalogPort {
    List<StoryCatalog> loadCatalog();
}
```

`content/infrastructure/filesystem/StoryCatalogFilesystemAdapter.java`:
- Usa `PathMatchingResourcePatternResolver` para listar `classpath:/stories/*/story.json` (soporta tanto classpath explotado en desarrollo como jar empaquetado).
- Por cada `story.json` encontrado:
  1. Resuelve el directorio contenedor y su nombre (`storyId`).
  2. Parsea `story.json` con `ObjectMapper`. Si falla el parseo o `title`/`pages` están vacíos → excluir con `WARN`.
  3. Comprueba `cover.png` en el mismo directorio → si falta, excluir con `WARN`.
  4. Para cada `page` en `pages`, comprueba `page_{n}.png` y `page_{n}.mp3` → si falta cualquiera para cualquier página, excluir el cuento completo con `WARN` indicando qué fichero falta.
  5. Si todo lo anterior es correcto, añade el `StoryCatalog` al resultado.
- Log de exclusión explícito por motivo, por ejemplo:
  `WARN Cuento 'story_2' excluido del catálogo: falta page_3.mp3`

### 4. Servicio con caché en memoria

`content/service/StoryCatalogService.java`:
- Carga el catálogo una vez (`@PostConstruct` o carga perezosa en el primer acceso) y lo cachea en memoria.
- No hay invalidación en caliente: al no existir mecanismo de subida en runtime (decisión 1), el catálogo se recalcula solo al reiniciar la aplicación.
- Expone `List<StoryCatalog> listCatalog()` y `Optional<StoryCatalog> findById(String id)`, usados por SPRINT-084.

### 5. Manejo de catálogo vacío

Si ninguna carpeta pasa la validación, el servicio expone una lista vacía; no debe romper el arranque de la aplicación (solo logs de `WARN`/`INFO`).

## Contratos y dependencias externas

### Contratos

Ninguno todavía — este sprint es interno al backend. La API productiva que expone este catálogo se implementa en SPRINT-084.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | Ninguna | ✅ Sin dependencia |
| Agents/TTS | Ninguna | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Coste de escaneo en cada arranque si el catálogo crece mucho | BAJA | Caché en memoria calculada una única vez por arranque. |
| R2 | Un recurso real con nombre de fichero incorrecto (mayúsculas, extensión distinta) hace que el cuento entero desaparezca sin aviso visible salvo logs | MEDIA | Logging `WARN` explícito por cuento y fichero faltante, para que sea diagnosticable sin depurar código. |
| R3 | Acceso a classpath dentro de un jar empaquetado complica el listado de subdirectorios | MEDIA | Usar `PathMatchingResourcePatternResolver`, que soporta classpath explotado y empaquetado en jar. |
| R4 | `story.json` con páginas no consecutivas o duplicadas pasa la validación de completitud pero produce un catálogo inconsistente | BAJA | Añadir comprobación de integridad extra: los números de página deben ser únicos y consecutivos desde 1; si no, excluir con el mismo criterio que un recurso faltante. |

---

## Tareas del sprint

### Tarea 83.1: Definir modelo de dominio de solo lectura

**Descripción:** Crear `StoryCatalogEntry`, `StoryPageEntry` y `StoryCatalog` (o nombres equivalentes) en `content/model`, sin dependencias JPA.

**Criterios de aceptación:**
- Los records no tienen anotaciones de persistencia.
- Compilación sin errores.

---

### Tarea 83.2: Implementar el escáner/parser de `story.json`

**Descripción:** Implementar `StoryCatalogFilesystemAdapter` que localiza `classpath:/stories/*/story.json` y los parsea con Jackson.

**Criterios de aceptación:**
- Localiza correctamente todas las carpetas bajo `resources/stories/`.
- Un `story.json` malformado o con `title`/`pages` vacíos se excluye con log `WARN`, sin lanzar excepción que rompa el arranque.

---

### Tarea 83.3: Implementar validación de completitud

**Descripción:** Comprobar `cover.png` y, por cada página declarada, `page_{n}.png`/`page_{n}.mp3`; comprobar además que los números de página son únicos y consecutivos desde 1 (R4).

**Criterios de aceptación:**
- Cuento con todos los recursos presentes y paginación consistente → incluido en el catálogo.
- Cuento con cualquier recurso faltante o paginación inconsistente → excluido completo, con log `WARN` indicando el motivo concreto.

---

### Tarea 83.4: Implementar caché en memoria del catálogo

**Descripción:** `StoryCatalogService` carga el resultado del adapter una vez y lo sirve desde memoria en `listCatalog()`/`findById(id)`.

**Criterios de aceptación:**
- El escaneo del filesystem ocurre una sola vez por arranque (verificable por logs o test).
- `findById` devuelve `Optional.empty()` para un id no presente en el catálogo válido.

---

### Tarea 83.5: Tests unitarios con fixtures

**Descripción:** Tests sobre `StoryCatalogFilesystemAdapter`/`StoryCatalogService` usando fixtures de prueba bajo `src/test/resources/`.

**Criterios de aceptación:**
- Fixture de cuento completo → aparece en el catálogo con título y páginas correctas.
- Fixture sin `cover.png` → excluido.
- Fixture con una página sin `audio` → excluido (no hay estado "parcial").
- Fixture con `story.json` inválido (JSON malformado) → excluido, sin excepción propagada.
- Fixture con páginas no consecutivas (1, 3) → excluido.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `content/model/StoryCatalog.java`, `StoryPageEntry.java` | Nuevo |
| `content/ports/out/StoryCatalogPort.java` | Nuevo |
| `content/infrastructure/filesystem/StoryCatalogFilesystemAdapter.java` | Nuevo |
| `content/service/StoryCatalogService.java` | Nuevo |
| `src/test/java/.../content/StoryCatalogServiceTest.java` | Nuevo |
| `src/test/resources/stories-fixtures/**` | Nuevo |

## Estimación

- **Duración:** 1.5–2 días
- **Complejidad:** Media
- **Riesgo:** Bajo-medio

## Criterios de aceptación del sprint

1. Un cuento con `story.json`, `cover.png` y `page_{n}.png`/`page_{n}.mp3` para todas sus páginas aparece en el catálogo con todos los campos. *(Completitud)*
2. Un cuento al que le falta `cover.png`, cualquier `page_{n}.png`/`page_{n}.mp3`, o cuyo `story.json` es inválido, o cuya paginación es inconsistente, queda excluido del catálogo con log `WARN` describiendo el motivo. *(Validación)*
3. Un catálogo vacío no rompe el arranque de la aplicación. *(Robustez)*
4. `story_0` (recurso ya existente en el repositorio) queda excluido tras este sprint porque aún no tiene `cover.png` ni recursos por página — comportamiento esperado, documentado como pendiente de contenido, no como defecto. *(Validación)*
5. Tests unitarios cubren los casos de completitud e incompletitud descritos en la tarea 83.5. *(Calidad)*

## Dependencias bloqueantes

- [x] SPRINT-082 completado (módulo BD retirado, sin conflicto de nombres/paquetes).
- [x] Convención de nombres y regla de exclusión confirmadas (2026-08-26).

## Handoffs a otras capas

### Frontend

- Ninguna todavía. El consumo de este catálogo depende de la API que se publica en SPRINT-084.

### Agents/TTS

- Sin dependencia.

## Notas adicionales

El recurso de ejemplo `resources/stories/story_0/story.json` necesita completarse con `cover.png` y, por cada página declarada, `page_{n}.png`/`page_{n}.mp3` antes de que aparezca en el catálogo. Esto es una tarea de contenido (añadir los ficheros al directorio), no de código.
