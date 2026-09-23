# SPRINT-085: Diario Parental - Vista y Filtros

## Objetivo
Implementar la vista del diario parental en el frontend con filtros de periodo (Hoy, Semana, Mes, Total), resumen de actividad, lista de actividades por categoría y nivel actual por actividad.

## Contexto
El diario parental es una vista de solo lectura para adultos que muestra:
- Tiempo jugado y actividades únicas completadas en el periodo seleccionado
- Lista de actividades organizadas por categoría (Reconocimiento, Comparación, Memoria)
- Nivel actual de cada actividad (Fácil/Normal/Difícil)
- Señal de abandono contextual (si aplica)

## Requisitos

### Vista del Diario
- **Ruta**: `/parental/children/{childProfileId}/diary`
- **Acceso**: solo adultos autorizados de la misma familia
- **Periodo por defecto**: Semana

### Filtros de Periodo
- Botones de selección: Hoy, Semana, Mes, Total
- Al cambiar el periodo, se actualizan:
  - Resumen (tiempo jugado, actividades únicas)
  - Lista de actividades completadas
  - Nivel actual (siempre el actual, no histórico)

### Resumen
- Tiempo jugado en el periodo (en minutos)
- Número de actividades únicas completadas
- Mensaje neutro si no hubo actividad: "Hoy todavía no ha jugado"

### Lista de Actividades
- **Orden fijo**: Reconocimiento → Comparación → Memoria
- **Subcategorías de Reconocimiento**: Letras, Formas, Números, Colores, Animales
- Cada actividad muestra:
  - Nombre de la actividad
  - Categoría y subcategoría
  - Nivel actual: "Nivel actual de esta actividad: [Fácil | Normal | Difícil]. El juego lo ajusta automáticamente para que pueda jugar a gusto."
  - Indicador visual de nivel (tres paradas discretas, no dependiente de color)

### Señal de Abandono
- Solo se muestra si hay 4+ abandonos en los últimos 6 intentos iniciales de esa actividad
- Mensaje neutro: "Esta actividad ha sido abandonada varias veces recientemente"
- No se muestra si hay 3 o menos abandonos

### Estados Vacíos
- Si no hay actividad en el periodo: "Hoy todavía no ha jugado"
- Si no hay actividades completadas: lista vacía con mensaje neutro

### Accesibilidad
- Nivel, filtro, estado vacío y abandonos se entienden sin depender solo de color
- Contenido legible y utilizable en móvil y tableta
- Textos cálidos y no evaluativos

## Tareas

### Componentes Vue
- [ ] Crear `DiaryView.vue` con estructura principal del diario
- [ ] Crear `DiaryPeriodFilter.vue` con botones de selección de periodo
- [ ] Crear `DiarySummary.vue` con resumen de actividad
- [ ] Crear `DiaryActivityList.vue` con lista de actividades por categoría
- [ ] Crear `DiaryActivityCard.vue` con tarjeta de actividad individual
- [ ] Crear `DiaryDifficultyIndicator.vue` con indicador de nivel (tres paradas)
- [ ] Crear `DiaryAbandonmentSignal.vue` con señal de abandono contextual
- [ ] Crear `DiaryEmptyState.vue` con estado vacío

### Servicios API
- [ ] Crear `diaryService.ts` con métodos:
  - `getSummary(childProfileId, period)` → `DiarySummaryResponse`
  - `getActivities(childProfileId, period)` → `DiaryActivityResponse[]`
  - `getAbandonmentSignal(childProfileId, activityId)` → `AbandonmentSignalResponse`
- [ ] Implementar manejo de errores y estados de carga

### Tipos TypeScript
- [ ] Crear `DiarySummaryResponse` con campos: playedTimeMinutes, uniqueActivitiesCompleted
- [ ] Crear `DiaryActivityResponse` con campos: activityId, name, category, engine, currentDifficulty
- [ ] Crear `AbandonmentSignalResponse` con campos: activityId, abandonmentCount
- [ ] Crear enum `DiaryPeriod` con valores: TODAY, WEEK, MONTH, ALL
- [ ] Crear enum `DifficultyLevel` con valores: EASY, MEDIUM, HARD

### Integración
- [ ] Añadir ruta `/parental/children/:childProfileId/diary` en `router/index.ts`
- [ ] Implementar verificación de acceso parental válido
- [ ] Implementar verificación de aislamiento (perfil pertenece a familia)
- [ ] Cargar datos al cambiar el periodo seleccionado
- [ ] Manejar estados de carga y error

### Estilos y Accesibilidad
- [ ] Implementar diseño responsive (móvil y tableta)
- [ ] Implementar indicador de nivel con texto visible (no solo color)
- [ ] Implementar mensajes neutros y no evaluativos
- [ ] Verificar accesibilidad sin dependencia de color
- [ ] Implementar textos cálidos y comprensibles para adultos

### Tests
- [ ] Test de componente: `DiaryPeriodFilter` cambia periodo correctamente
- [ ] Test de componente: `DiarySummary` muestra datos correctos
- [ ] Test de componente: `DiaryActivityList` ordena actividades correctamente
- [ ] Test de componente: `DiaryActivityCard` muestra nivel actual
- [ ] Test de componente: `DiaryAbandonmentSignal` solo se muestra con 4+ abandonos
- [ ] Test de componente: `DiaryEmptyState` se muestra sin actividad
- [ ] Test de integración: flujo completo de consulta de diario
- [ ] Test de accesibilidad: indicador de nivel sin dependencia de color

## Criterios de Aceptación

1. La vista del diario solo es accesible para adultos autorizados
2. El periodo por defecto es Semana
3. Al cambiar el periodo, se actualizan resumen y lista de actividades
4. El resumen muestra tiempo jugado y actividades únicas completadas
5. La lista de actividades está ordenada: Reconocimiento → Comparación → Memoria
6. Cada actividad muestra su nivel actual (Fácil/Normal/Difícil)
7. El indicador de nivel tiene tres paradas discretas con texto visible
8. La señal de abandono solo se muestra con 4+ abandonos en los últimos 6 intentos
9. Los estados vacíos muestran mensajes neutros
10. El contenido es legible y utilizable en móvil y tableta
11. No se depende exclusivamente del color para transmitir información
12. Los textos son cálidos y no evaluativos
13. Los tests de componente y de integración pasan

## Notas Técnicas

- **Periodos**: calcular fechas en frontend o backend (recomendado: backend)
- **Actividades únicas**: el backend ya devuelve actividades únicas, el frontend solo las muestra
- **Nivel actual**: el backend devuelve el nivel actual, el frontend lo muestra
- **Señal de abandono**: el backend solo devuelve la señal si hay 4+ abandonos
- **Orden fijo**: definir orden en frontend (no configurable)
- **Accesibilidad**: usar texto además de color para indicador de nivel

## Dependencias

- SPRINT-112 completado (endpoints de consulta por periodo)
- SPRINT-078 completado (audio de Nubi y botón de salida)
- FEAT-015 (frontend) aceptado

## Estimación

- **Tamaño:** L (Large)
- **Complejidad:** Media (múltiples componentes, integración con API, accesibilidad)
- **Riesgo:** Medio (accesibilidad, textos no evaluativos, orden fijo)
