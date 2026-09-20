# SPRINT-107: Animal Recognition - Adaptive Difficulty with Groups and Biomes

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
- [ ] Actualizar seed `20-recognition-elements-animals.json` para incluir `resourceRefs` con `nubi-audio`:
  ```json
  {
    "code": "bull",
    "nubi": "¿Dónde está el TORO?",
    "biome": ["MEADOW", "FARM"],
    "group": ["horns"],
    "resourceRefs": "{\"nubi-audio\": \"¿Dónde está el TORO?\"}"
  }
  ```
- [ ] Verificar que el campo `nubi` del seed se mapea correctamente a `resourceRefs["nubi-audio"]`

### Servicio de Grupos de Animales
- [ ] Crear `AnimalGroupService` en `game/service/`:
  - Método `getAnimalsInSameGroup(targetCode: String)` → lista de códigos de animales que comparten al menos un grupo con el target
  - Método `getAnimalsByBiome(biome: String)` → lista de códigos de animales válidos para el bioma
  - Método `filterByBiome(candidates: List<String>, biome: String)` → filtra candidatos por bioma
- [ ] Cargar metadatos de animales desde el seed al iniciar la aplicación
- [ ] Registrar como bean de Spring

### Integración con DistractorSelector
- [ ] Modificar `DistractorSelector` para soportar categoría ANIMAL:
  - Inyectar `AnimalGroupService`
  - Para ANIMAL:
    - Filtrar previamente candidatos por bioma actual
    - EASY/MEDIUM: excluir animales del mismo grupo
    - HARD: priorizar animales del mismo grupo, fallback a aleatorios
- [ ] Mantener fallback si no hay suficientes candidatos válidos

### Integración con GameOrchestratorService
- [ ] Inyectar `AnimalGroupService` en `GameOrchestratorService`
- [ ] Obtener bioma actual del jugador desde `LaunchContext` o sesión
- [ ] Pasar bioma y servicio a `DistractorSelector` durante la construcción de opciones
- [ ] Asegurar que la categoría ANIMAL se propaga correctamente al selector

### Nubi Audio para Animales
- [ ] Reutilizar `RoundAudioService` de SPRINT-104:
  - Extraer texto de `resourceRefs["nubi-audio"]`
  - Generar audio y enviar vía `GAME_AVATAR_EVENT`
- [ ] No requiere cambios adicionales (ya implementado)

### Tests
- [ ] Test unitario: `getAnimalsInSameGroup("bull")` devuelve ["cow", "deer", "goat"]
- [ ] Test unitario: `getAnimalsByBiome("FARM")` devuelve animales válidos para FARM
- [ ] Test unitario: EASY excluye animales del mismo grupo
- [ ] Test unitario: MEDIUM excluye animales del mismo grupo
- [ ] Test unitario: HARD prioriza animales del mismo grupo
- [ ] Test unitario: HARD hace fallback a aleatorios si el grupo no tiene suficientes miembros
- [ ] Test unitario: filtrado por bioma funciona correctamente
- [ ] Test de integración: flujo completo con RecognitionEngine para categoría ANIMAL

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
