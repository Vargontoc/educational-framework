# SPRINT-113: Diario Parental - Reinicio de Historial y Adaptación

## Objetivo
Implementar el endpoint REST para que el adulto pueda reiniciar el historial de tracking y la adaptación de dificultad de un perfil infantil, conservando el perfil y sus preferencias.

## Contexto
El diario parental incluye una acción de "limpiar datos" que:
- Elimina todos los datos de tracking del perfil (intentos, resúmenes, logros, progreso de aprendizaje)
- Reinicia la adaptación de dificultad de todas las actividades a EASY
- Conserva el perfil infantil (nombre, avatar, mes/año de nacimiento, preferencias)
- Requiere confirmación adulta (doble verificación)

## Requisitos

### Endpoint REST
- `POST /api/v1/diary/children/{childProfileId}/reset`
  - Elimina todos los datos de tracking del perfil
  - Reinicia dificultad de todas las actividades a EASY
  - Devuelve estado de éxito con datos eliminados

### Lógica de Negocio
- **Eliminar datos de tracking:**
  - `ActivityAttempt` (todos los intentos del perfil)
  - `ActivitySummary` (todos los resúmenes de actividad)
  - `TopicSummary` (todos los resúmenes de tema)
  - `GameSessionSummary` (todos los resúmenes de sesión)
  - `ChildAchievement` (todos los logros)
  - `ChildLearningProgress` (todo el progreso de aprendizaje)
  - `ChildLearningCompletedStep` (todos los pasos completados)
  - `CuriosityViewed` (todas las curiosidades vistas)
  - `DifficultyEvolution` (toda la evolución de dificultad)

- **Reiniciar adaptación:**
  - No se eliminan las `DifficultyLevel` del catálogo (son globales)
  - Se eliminan las referencias a dificultad actual en `ActivitySummary` (ya eliminadas)
  - La próxima vez que el niño juegue, comenzará en EASY (comportamiento por defecto)

- **Conservar:**
  - `ChildProfile` (nombre, avatar, mes/año de nacimiento, preferencias)
  - `Family` (datos familiares)
  - `AdultProfile` (datos del adulto)

- **Aislamiento:**
  - Verificar que el perfil pertenece a la familia actual
  - Verificar que el usuario es adulto autorizado

### Modelos de Datos
- `DiaryResetResponse`: childProfileId, deletedAttempts, deletedSummaries, deletedAchievements, resetAt

## Tareas

### Modelos y DTOs
- [ ] Crear `DiaryResetResponse` con campos: childProfileId, deletedAttempts, deletedSummaries, deletedAchievements, resetAt

### Servicios
- [ ] Crear método en `DiaryService`: `resetDiary(childProfileId)` → `DiaryResetResponse`
- [ ] Implementar eliminación de `ActivityAttempt` por childProfileId
- [ ] Implementar eliminación de `ActivitySummary` por childProfileId
- [ ] Implementar eliminación de `TopicSummary` por childProfileId
- [ ] Implementar eliminación de `GameSessionSummary` por childProfileId
- [ ] Implementar eliminación de `ChildAchievement` por childProfileId
- [ ] Implementar eliminación de `ChildLearningProgress` por childProfileId
- [ ] Implementar eliminación de `ChildLearningCompletedStep` por childProfileId
- [ ] Implementar eliminación de `CuriosityViewed` por childProfileId
- [ ] Implementar eliminación de `DifficultyEvolution` por childProfileId
- [ ] Implementar conteo de registros eliminados para el response
- [ ] Implementar transacción atómica (todo o nada)

### Repositorios
- [ ] Añadir método a `ActivityAttemptRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `ActivitySummaryRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `TopicSummaryRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `GameSessionSummaryRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `ChildAchievementRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `ChildLearningProgressRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `ChildLearningCompletedStepRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `CuriosityViewedRepository`: `deleteByChildProfileId(childProfileId)`
- [ ] Añadir método a `DifficultyEvolutionRepository`: `deleteByChildProfileId(childProfileId)`

### Controlador
- [ ] Añadir endpoint `POST /api/v1/diary/children/{childProfileId}/reset` en `DiaryController`
- [ ] Implementar verificación de aislamiento (perfil pertenece a familia)
- [ ] Implementar verificación de usuario adulto autorizado
- [ ] Implementar manejo de response con datos eliminados

### Tests
- [ ] Test unitario: eliminación de todos los datos de tracking
- [ ] Test unitario: conservación del perfil infantil
- [ ] Test unitario: conteo correcto de registros eliminados
- [ ] Test unitario: transacción atómica (rollback si falla alguna eliminación)
- [ ] Test unitario: aislamiento por familia (perfil de otra familia → 403)
- [ ] Test unitario: verificación de usuario adulto autorizado
- [ ] Test de integración: flujo completo de reinicio de diario

## Criterios de Aceptación

1. El endpoint elimina todos los datos de tracking del perfil correctamente
2. El perfil infantil y sus preferencias se conservan
3. La adaptación de dificultad se reinicia (próxima actividad comienza en EASY)
4. El response incluye el conteo de registros eliminados
5. La operación es atómica (todo o nada)
6. El aislamiento por familia funciona correctamente (403 si el perfil no pertenece)
7. Solo usuarios adultos autorizados pueden ejecutar el reinicio
8. Los tests unitarios y de integración pasan

## Notas Técnicas

- **Transacción atómica**: usar `@Transactional` para garantizar consistencia
- **Eliminación en cascada**: verificar que no haya referencias huérfanas
- **Performance**: la eliminación puede ser costosa si hay muchos datos; considerar batch deletion
- **Auditoría**: registrar en log la operación de reinicio (quién, cuándo, qué perfil)
- **Confirmación**: el frontend debe implementar doble confirmación antes de llamar al endpoint

## Dependencias

- SPRINT-112 completado (endpoints de consulta por periodo)
- FEAT-006 completado (Tracking Module)

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (eliminación masiva de datos, transacción atómica)
- **Riesgo:** Medio (eliminación irreversible de datos, aislamiento de datos)
