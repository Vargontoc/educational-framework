# SPRINT-107: Animal Recognition - Adaptive Difficulty with Groups and Biomes

**Estado:** implemented

## Objetivo
Implementar la lógica de dificultad adaptativa para el minijuego de reconocimiento de animales, usando los campos `group` y `biome` del seed existente, y filtrando por el bioma actual del jugador.

## Contexto
El seed `20-recognition-elements-animals.json` ya existe con la estructura:
```json
{
  "code": "bull",
  "nubi": "¿Dónde está el TORO?",
  "biome": ["MEADOW", "FARM"],
  "group": ["horns"]
}
```

Cada animal puede pertenecer a múltiples biomas y múltiples grupos. Los grupos definidos son:
- `horns`: bull, cow, deer, goat
- `equine`: deer, donkey, horse
- `peck`: duck, goose, chicken
- `wool`: goat, sheep

## Requisitos

### Dificultad Adaptativa

**EASY (2 opciones):**
- 1 target + 1 distractor
- Distractor: aleatorio EXCLUYENDO cualquier animal que comparta al menos un `group` con el target
- Ejemplo: si target es "bull" (group: horns), excluir cow, deer, goat

**MEDIUM (3 opciones):**
- 1 target + 2 distractores
- Distractores: aleatorios EXCLUYENDO cualquier animal que comparta al menos un `group` con el target

**HARD (3 opciones):**
- 1 target + 2 distractores
- Distractores: priorizar animales del mismo `group` que el target
- Si el grupo no tiene suficientes miembros, completar con aleatorios del resto del pool
- Ejemplo: si target es "bull" (group: horns), priorizar cow, deer, goat

### Filtrado por Bioma

- Backend debe filtrar los candidatos por el bioma actual del jugador
- Un animal es válido para un bioma si su array `biome` contiene el bioma actual
- El bioma actual se obtiene del `LaunchContext` o del estado de sesión del jugador

### Integración con DistractorSelector

Modificar `DistractorSelector` para soportar categoría ANIMAL:
- Inyectar servicio que acceda a los metadatos de animales (groups, biomes)
- Para ANIMAL:
  - EASY/MEDIUM: filtrar candidatos excluyendo animales del mismo grupo
  - HARD: priorizar animales del mismo grupo, fallback a aleatorios
- Filtrar previamente por bioma actual del jugador

### Nubi Audio

- Reutilizar `RoundAudioService` de SPRINT-104
- Extraer texto de `resourceRefs["nubi-audio"]` (campo `nubi` en el seed)
- Generar audio y enviar vía `GAME_AVATAR_EVENT` con `eventType = "ROUND_PROMPT"`
- No requiere cambios adicionales (ya implementado)

## Tareas

### Modelo de Datos
- [x] Actualizar seed `20-recognition-elements-animals.json` para incluir `resourceRefs` con `nubi-audio`:
  ```json
  {
    "code": "bull",
    "nubi": "¿Dónde está el TORO?",
    "biome": ["MEADOW", "FARM"],
    "group": ["horns"],
    "resourceRefs": "{\"nubi-audio\": \"¿Dónde está el TORO?\"}"
  }
  ```
- [x] Verificar que el campo `nubi` del seed se mapea correctamente a `resourceRefs["nubi-audio"]`

### Servicio de Grupos de Animales
- [x] Crear `AnimalGroupService` en `game/service/`:
  - Método `getAnimalsInSameGroup(targetCode: String)` → lista de códigos de animales que comparten al menos un grupo con el target
  - Método `getAnimalsByBiome(biome: String)` → lista de códigos de animales válidos para el bioma
  - Método `filterByBiome(candidates: List<String>, biome: String)` → filtra candidatos por bioma
- [x] Cargar metadatos de animales desde el seed al iniciar la aplicación
- [x] Registrar como bean de Spring

### Integración con DistractorSelector
- [x] Modificar `DistractorSelector` para soportar categoría ANIMAL:
  - Inyectar `AnimalGroupService`
  - Para ANIMAL:
    - Filtrar previamente candidatos por bioma actual
    - EASY/MEDIUM: excluir animales del mismo grupo
    - HARD: priorizar animales del mismo grupo, fallback a aleatorios
- [x] Mantener fallback si no hay suficientes candidatos válidos

### Integración con GameOrchestratorService
- [x] Inyectar `AnimalGroupService` en `GameOrchestratorService`
- [x] Obtener bioma actual del jugador desde `LaunchContext` o sesión
- [x] Pasar bioma y servicio a `DistractorSelector` durante la construcción de opciones
- [x] Asegurar que la categoría ANIMAL se propaga correctamente al selector

### Nubi Audio para Animales
- [x] Reutilizar `RoundAudioService` de SPRINT-104:
  - Extraer texto de `resourceRefs["nubi-audio"]`
  - Generar audio y enviar vía `GAME_AVATAR_EVENT`
- [x] No requiere cambios adicionales (ya implementado)

### Tests
- [x] Test unitario: `getAnimalsInSameGroup("bull")` devuelve ["cow", "deer", "goat"]
- [x] Test unitario: `getAnimalsByBiome("FARM")` devuelve animales válidos para FARM
- [x] Test unitario: EASY excluye animales del mismo grupo
- [x] Test unitario: MEDIUM excluye animales del mismo grupo
- [x] Test unitario: HARD prioriza animales del mismo grupo
- [x] Test unitario: HARD hace fallback a aleatorios si el grupo no tiene suficientes miembros
- [x] Test unitario: filtrado por bioma funciona correctamente
- [x] Test de integración: flujo completo con RecognitionEngine para categoría ANIMAL

## Criterios de Aceptación

1. EASY selecciona 1 distractor aleatorio excluyendo animales del mismo grupo
2. MEDIUM selecciona 2 distractores aleatorios excluyendo animales del mismo grupo
3. HARD prioriza distractores del mismo grupo, con fallback a aleatorios
4. Los candidatos se filtran por el bioma actual del jugador
5. Nubi Audio se genera y envía correctamente para animales
6. Los tests unitarios y de integración pasan

## Notas Técnicas

- **Grupos múltiples**: Un animal puede pertenecer a múltiples grupos (ej: deer está en "horns" y "equine")
- **Biomas múltiples**: Un animal puede aparecer en múltiples biomas (ej: bull está en MEADOW y FARM)
- **Fallback**: Si el grupo no tiene suficientes miembros en HARD, completar con aleatorios del resto del pool
- **Performance**: Cachear metadatos de animales en memoria (son estáticos)
- **Audio**: Reutilizar infraestructura de SPRINT-104 sin cambios

## Dependencias

- SPRINT-104 completado (Nubi Audio)
- SPRINT-103 completado (infraestructura de similitud para letras)
- Seed `20-recognition-elements-animals.json` existente

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (lógica de grupos y biomas, pero reutiliza infraestructura existente)
- **Riesgo:** Bajo (lógica similar a letras/números)

## Frontend

No se requiere sprint específico de frontend para animales. Las tareas ya están cubiertas por:
- **SPRINT-078**: Audio de Nubi y botón de salida (genérico para todas las categorías)
- **Carga dinámica**: El bloque `recognition-animals` ya existe en `assets-manifest.json` y funciona con la carga dinámica genérica implementada en SPRINT-079/081
- **Key de textura**: El `code` de `RecognitionElement` hace referencia directamente a la key de la imagen (ej: "bee", "bull", "cat")

## Implementación completada (2026-09-20)

### Resumen técnico
- **`AnimalGroupService`** (nuevo, bean en `GameModuleConfiguration`): carga una sola vez `seeds/20-recognition-elements-animals.json` (classpath) y cachea `group`/`biome` por `code`. Métodos: `getAnimalsInSameGroup` (mismos grupos, sin el propio target; `bull` -> `cow, deer, goat`), `getAnimalsByBiome`, `filterByBiome` (conserva el orden, descarta biomas ajenos y códigos desconocidos) e `isKnown`. Si el seed no se puede leer, el servicio queda vacío y el selector degrada a aleatorio (no rompe la partida).
- **`DistractorSelector`** (ANIMAL, con `AnimalGroupService`): EASY/MEDIUM excluyen a los compañeros de grupo del target; HARD (`SIMILAR_OUTLINE`) prioriza compañeros de grupo y completa con aleatorios del resto. En ambos sentidos el otro lado del reparto sirve de fallback, así que la ronda siempre recibe `count` distractores si el pool lo permite. Target sin grupos o desconocido: HARD/EASY se comportan como aleatorio / lógica previa.
- **Filtrado por bioma** en `GameOrchestratorService.resolveCandidates`: el `habitatTag` del `LaunchContext` filtra los candidatos ANIMAL por el `biome` de cada elemento (afecta a target y distractores). Si el filtro deja menos de `MIN_OPTIONS_PER_ROUND` candidatos se mantiene la lista sin filtrar (mismo criterio que el filtro anti-repetición).
- **Seed**: cada animal lleva `resourceRefs` con `nubi-audio` normalizado (sin espacios múltiples). `SeedService` lo usa (con fallback al `nubi` bruto) y sigue guardando `biome` y `group` en `resourceRefs` de BD. `RecognitionAnimalElementSeed` gana el campo `resourceRefs`.
- **Nubi Audio**: sin cambios funcionales; `RoundAudioService` ya lee `resourceRefs["nubi-audio"]`, que ahora es texto normalizado.

### Decisiones de detalle y desvíos
1. **El sprint decía "pasar bioma y servicio a `DistractorSelector`"**: se pasa el servicio, pero el bioma se aplica **antes**, sobre la lista de candidatos del motor. Así el filtro cubre también la elección del target, y no hace falta guardar el bioma en `RecognitionState` (evita cambiar el payload/contratos). El selector recibe candidatos ya filtrados.
2. **Bug latente corregido**: el topic sembrado "Animales" tiene `habitatTag = null`, y la consulta por hábitat existente (`listTopicsByRecognitionTypeAndHabitat`) devolvía vacío para cualquier bioma, dejando el juego sin candidatos. Ahora, si esa consulta no devuelve topics, se usan todos los topics ANIMAL y filtra el `biome` del elemento. El comportamiento con topics etiquetados por hábitat no cambia.
3. La dificultad HARD se identifica por `DistractorStrategy.SIMILAR_OUTLINE` (como en letras/números). El nº de opciones sigue viniendo de la escalera compartida (EASY 2, MEDIUM 3, HARD 4 en `RecognitionProperties`; el sprint dice 3 para HARD, discrepancia ya documentada en SPRINT-106, no se toca).
4. Animales sin metadatos (códigos no presentes en el seed, p. ej. contenido creado a mano) se descartan cuando se filtra por bioma; con < 2 válidos se mantiene todo.
5. Constructor de `GameOrchestratorService` con un parámetro más (`AnimalGroupService`, admite `null`); igual en `RecognitionEngine`/`DistractorSelector` (sobrecargas antiguas conservadas).

### Archivos
- **Nuevos:** `game/service/AnimalGroupService.java`; tests `AnimalGroupServiceTest` (10), `DistractorSelectorAnimalTest` (11), `RecognitionEngineAnimalTest` (3), `AnimalRecognitionSeedTest` (2).
- **Modificados:** `DistractorSelector`, `RecognitionEngine`, `GameOrchestratorService`, `GameModuleConfiguration`, `SeedData`, `SeedService`, seed `20-recognition-elements-animals.json`; tests: constructor del orquestador en 6 clases existentes y 3 tests nuevos en `GameOrchestratorServiceCandidateFilteringTest` (filtro por MEADOW/FARM y fallback).

### Migraciones y contratos
Sin migraciones de esquema (la semilla añade `resourceRefs` al cargarse con el seeding normal; los animales ya cargados en BD no se recargan por la clave `alreadyLoaded`, conservan el `nubi-audio` antiguo con espacios múltiples). Sin cambios en `docs/contracts`: no cambia ningún payload ni evento.

### Pruebas
- `mvn -o test -Dtest=!EducationalFrameworkApplicationTests`: 1102 tests, 0 fallos en el ámbito del sprint. Verdes: `AnimalGroupServiceTest`, `DistractorSelectorAnimalTest`, `RecognitionEngineAnimalTest` (partidas completas con 20 semillas por dificultad), `AnimalRecognitionSeedTest`, `GameOrchestratorService*Test`, `RecognitionEngine*Test`, `DistractorSelector*Test`, `SeedServiceTest`.
- **Fallos preexistentes ajenos al sprint** (los mismos que en SPRINT-106): `ChildProfileServiceTest` (11), `WorldOrchestratorServiceTest` (1) y 101 tests que cargan el contexto de Spring (falta la tabla pgvector `content_generated`). Por eso no se ha podido comprobar el arranque de la app con el bean nuevo; el bean no tiene dependencias (solo lee el classpath).
- El "test de integración con `RecognitionEngine`" se cubre con `RecognitionEngineAnimalTest` (motor real + servicio real con el seed real, sin `@SpringBootTest`).

### Riesgos y deuda
- Los animales ya sembrados en BD conservan el `nubi-audio` antiguo (con espacios múltiples); habría que re-sembrar o migrar si se quiere unificar. El audio funciona igualmente.
- El bioma se toma de `LaunchContext.habitatTag`; si el valor no es un `Biome` válido, `Biome.valueOf` sigue lanzando `IllegalArgumentException` (comportamiento previo).
- Los grupos/biomas se leen del seed del classpath, no de la BD: contenido animal creado en admin no participa en grupos.

### Corrección posterior: el bioma no llegaba al juego
El filtrado por bioma no se aplicaba en el flujo real. `world_discovery_interacted` arranca la partida con el `LaunchContext` (bioma incluido), pero el cliente después envía `game_start` con la misma actividad, y `GameWebSocketHandler.handleGameStart` abandonaba esa partida y volvía a crearla con `startGame(childProfileId, activityId)` **sin** contexto, perdiéndose el bioma.
- `WorldGameStartUseCase.resolveLaunchContext(childSessionId, activityId)` (nuevo) reconstruye el contexto desde la propuesta del mundo (misma lógica que `startGameFromProposal`); devuelve `null` si no hay mundo o la propuesta no es de animales.
- `handleGameStart` lo usa: con contexto llama a `startGame(..., launchContext)`; sin él, mantiene el comportamiento anterior.
- Tests: 3 en `WorldGameStartServiceTest` y 1 en `GameWebSocketHandlerTest` (el `game_start` conserva el bioma).
