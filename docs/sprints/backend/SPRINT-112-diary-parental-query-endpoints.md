# SPRINT-112: Diario Parental - Endpoints de Consulta por Periodo

## Objetivo
Implementar los endpoints REST para el diario parental que permitan consultar datos por periodo (Hoy, Semana, Mes, Total), incluyendo tiempo jugado, actividades únicas completadas y nivel actual por actividad.

## Contexto
El diario parental requiere endpoints específicos que difieren del dashboard actual:
- Filtrado por periodo (fecha inicio/fin)
- Actividades únicas completadas (no duplicadas)
- Nivel actual por actividad (no histórico)
- Aislamiento por perfil y familia

## Requisitos

### Endpoints REST
- `GET /api/v1/diary/children/{childProfileId}/summary?period=TODAY|WEEK|MONTH|ALL`
  - Tiempo jugado en el periodo
  - Número de actividades únicas completadas
  - Estado neutro si no hubo actividad

- `GET /api/v1/diary/children/{childProfileId}/activities?period=TODAY|WEEK|MONTH|ALL`
  - Lista de actividades completadas en el periodo
  - Nivel actual de cada actividad (Fácil/Normal/Difícil)
  - Orden fijo: Reconocimiento, Comparación, Memoria
  - Subcategorías de Reconocimiento: Letras, Formas, Números, Colores, Animales

- `GET /api/v1/diary/children/{childProfileId}/abandonment-signal?activityId={id}`
  - Señal de abandono si hay 4+ abandonos en últimos 6 intentos iniciales
  - No devuelve datos si hay 3 o menos abandonos

### Lógica de Negocio
- Actividades únicas: una actividad cuenta una sola vez por periodo aunque se haya completado múltiples veces
- Nivel actual: siempre el nivel actual de la actividad, no un nivel histórico del periodo
- Aislamiento: verificar que el perfil pertenece a la familia actual
- Periodos:
  - TODAY: desde las 00:00:00 de hoy
  - WEEK: desde las 00:00:00 del lunes de la semana actual
  - MONTH: desde las 00:00:00 del primer día del mes actual
  - ALL: sin filtro de fecha

### Modelos de Datos
- `DiarySummaryResponse`: tiempo jugado, actividades únicas completadas
- `DiaryActivityResponse`: activityId, nombre, categoría, motor, nivel actual (EASY/MEDIUM/HARD)
- `AbandonmentSignalResponse`: activityId, abandonmentCount (solo si >= 4)

## Tareas

### Modelos y DTOs
- [ ] Crear `DiarySummaryResponse` con campos: playedTimeMinutes, uniqueActivitiesCompleted
- [ ] Crear `DiaryActivityResponse` con campos: activityId, name, category, engine, currentDifficulty
- [ ] Crear `AbandonmentSignalResponse` con campos: activityId, abandonmentCount
- [ ] Crear enum `DiaryPeriod` con valores: TODAY, WEEK, MONTH, ALL

### Servicios
- [ ] Crear `DiaryService` con métodos:
  - `getSummary(childProfileId, period)` → `DiarySummaryResponse`
  - `getActivities(childProfileId, period)` → `List<DiaryActivityResponse>`
  - `getAbandonmentSignal(childProfileId, activityId)` → `AbandonmentSignalResponse`
- [ ] Implementar lógica de filtrado por periodo (calcular fechas inicio/fin)
- [ ] Implementar lógica de actividades únicas (agrupar por activityId, contar una vez)
- [ ] Implementar lógica de tiempo jugado (sumar duración de sesiones en el periodo)
- [ ] Implementar lógica de nivel actual (obtener de `ActivitySummary.currentDifficultyLevelId`)
- [ ] Implementar lógica de señal de abandono (contar abandonos en últimos 6 intentos iniciales)
- [ ] Implementar orden fijo de actividades (Reconocimiento → Comparación → Memoria)
- [ ] Implementar subcategorías de Reconocimiento (Letras, Formas, Números, Colores, Animales)

### Repositorios
- [ ] Añadir método a `GameSessionSummaryRepository`: `findByChildProfileIdAndCompletedAtBetween(start, end)`
- [ ] Añadir método a `ActivityAttemptRepository`: `findRecentAbandonments(childProfileId, activityId, limit)`

### Controlador
- [ ] Crear `DiaryController` con endpoints:
  - `GET /api/v1/diary/children/{childProfileId}/summary`
  - `GET /api/v1/diary/children/{childProfileId}/activities`
  - `GET /api/v1/diary/children/{childProfileId}/abandonment-signal`
- [ ] Implementar verificación de aislamiento (perfil pertenece a familia)
- [ ] Implementar manejo de periodo por query param

### Tests
- [ ] Test unitario: filtrado por periodo TODAY
- [ ] Test unitario: filtrado por periodo WEEK
- [ ] Test unitario: filtrado por periodo MONTH
- [ ] Test unitario: filtrado por periodo ALL
- [ ] Test unitario: actividades únicas (no duplicadas)
- [ ] Test unitario: tiempo jugado calculado correctamente
- [ ] Test unitario: nivel actual por actividad
- [ ] Test unitario: señal de abandono con 4+ abandonos
- [ ] Test unitario: señal de abandono no devuelta con 3 o menos abandonos
- [ ] Test unitario: aislamiento por familia (perfil de otra familia → 403)
- [ ] Test de integración: flujo completo de consulta de diario

## Criterios de Aceptación

1. Los endpoints devuelven datos filtrados por periodo correctamente
2. Las actividades únicas no se duplican aunque se hayan completado múltiples veces
3. El nivel mostrado es el nivel actual de la actividad, no un nivel histórico
4. La señal de abandono solo se devuelve si hay 4+ abandonos en los últimos 6 intentos
5. El aislamiento por familia funciona correctamente (403 si el perfil no pertenece)
6. Los periodos se calculan correctamente (TODAY, WEEK, MONTH, ALL)
7. El orden de actividades es fijo: Reconocimiento → Comparación → Memoria
8. Los tests unitarios y de integración pasan

## Notas Técnicas

- **Periodos**: usar `LocalDateTime` con zona horaria del servidor
- **Actividades únicas**: agrupar por `activityId` y contar una vez por periodo
- **Tiempo jugado**: sumar `durationMs` de `GameSessionSummary` en el periodo
- **Nivel actual**: obtener de `ActivitySummary.currentDifficultyLevelId` y mapear a EASY/MEDIUM/HARD
- **Señal de abandono**: contar `GameSessionSummary` con `finalStatus = ABANDONED` y `abandonReason = CLIENT_REQUESTED` en los últimos 6 intentos iniciales
- **Orden fijo**: definir orden en código (no configurable)

## Dependencias

- SPRINT-095 completado (consolidación diferida y detección de repeticiones)
- SPRINT-096 completado (abandono explícito vs pérdida de conexión)
- FEAT-006 completado (Tracking Module)

## Estimación

- **Tamaño:** L (Large)
- **Complejidad:** Media-Alta (lógica de periodos, actividades únicas, señal de abandono)
- **Riesgo:** Medio (lógica de negocio compleja, aislamiento de datos)
