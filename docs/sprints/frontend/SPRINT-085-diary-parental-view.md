# SPRINT-085: Diario Parental - Vista y Filtros

**Estado:** implemented

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
- [x] Crear `DiaryView.vue` con estructura principal del diario
- [x] Crear `DiaryPeriodFilter.vue` con botones de selección de periodo
- [x] Crear `DiarySummary.vue` con resumen de actividad
- [x] Crear `DiaryActivityList.vue` con lista de actividades por categoría
- [x] Crear `DiaryActivityCard.vue` con tarjeta de actividad individual
- [x] Crear `DiaryDifficultyIndicator.vue` con indicador de nivel (tres paradas)
- [x] Crear `DiaryAbandonmentSignal.vue` con señal de abandono contextual
- [x] Crear `DiaryEmptyState.vue` con estado vacío

### Servicios API
- [x] Crear `diaryService.ts` con métodos:
  - `getSummary(childProfileId, period)` → `DiarySummaryResponse`
  - `getActivities(childProfileId, period)` → `DiaryActivityResponse[]`
  - `getAbandonmentSignal(childProfileId, activityId)` → `AbandonmentSignalResponse`
- [x] Implementar manejo de errores y estados de carga (composable `useDiary.ts`: `loading`/`error`)

### Tipos TypeScript
- [x] Crear `DiarySummaryResponse` con campos: playedTimeMinutes, uniqueActivitiesCompleted
- [x] Crear `DiaryActivityResponse` con campos: activityId, name, category, engine, currentDifficulty (+ `subcategory`, ver desvíos)
- [x] Crear `AbandonmentSignalResponse` con campos: activityId, abandonmentCount
- [x] Crear `DiaryPeriod` con valores: TODAY, WEEK, MONTH, ALL (como union type, ver desvíos)
- [x] Crear `DifficultyLevel` con valores: EASY, MEDIUM, HARD (como union type, ver desvíos)

### Integración
- [x] Añadir ruta del diario en `router/index.ts` (`/panel/ninos/:id/diary`, ver desvíos)
- [x] Implementar verificación de acceso parental válido (`meta.requiresParentalAuth`)
- [x] Verificación de aislamiento (perfil pertenece a familia) — ya la hace el backend, ver desvíos
- [x] Cargar datos al cambiar el periodo seleccionado
- [x] Manejar estados de carga y error

### Estilos y Accesibilidad
- [x] Implementar diseño responsive (móvil y tableta)
- [x] Implementar indicador de nivel con texto visible (no solo color)
- [x] Implementar mensajes neutros y no evaluativos
- [x] Verificar accesibilidad sin dependencia de color
- [x] Implementar textos cálidos y comprensibles para adultos

### Tests
No hay tooling de test de componente (Vitest/Vue Test Utils) en este proyecto; decidido con el usuario sustituir por un spec e2e de Cypress que cubre los mismos escenarios (ver "Pruebas" en la implementación completada).
- [ ] ~~Test de componente: `DiaryPeriodFilter` cambia periodo correctamente~~ → cubierto por e2e (cambio de periodo recarga resumen y actividades)
- [ ] ~~Test de componente: `DiarySummary` muestra datos correctos~~ → cubierto por e2e
- [ ] ~~Test de componente: `DiaryActivityList` ordena actividades correctamente~~ → cubierto por e2e
- [ ] ~~Test de componente: `DiaryActivityCard` muestra nivel actual~~ → cubierto por e2e
- [ ] ~~Test de componente: `DiaryAbandonmentSignal` solo se muestra con 4+ abandonos~~ → cubierto por e2e
- [ ] ~~Test de componente: `DiaryEmptyState` se muestra sin actividad~~ → cubierto por e2e
- [ ] Test de integración: flujo completo de consulta de diario — spec e2e escrito (`diario-parental.cy.ts`), **no ejecutado** (ver "Pruebas")
- [ ] Test de accesibilidad: indicador de nivel sin dependencia de color — cumplido por diseño (icono + texto, no solo color), sin test automatizado dedicado

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

## Implementación completada (2026-09-23)

### Resumen técnico
- **Vista** `DiaryView.vue` (`src/views/parental/`), ruta `/panel/ninos/:id/diary`, accesible desde el botón "Diario" en `ChildProfileEditView.vue` (junto a "Dashboard"). Usa el composable `useDiary.ts` para cargar resumen + actividades + señal de abandono por actividad, y renderiza `DiaryPeriodFilter` → `DiarySummary` / `DiaryActivityList` con estados de carga (`NubiSpinner`) y error (`NubiErrorState`, con reintento).
- **`DiaryPeriodFilter.vue`**: 4 `NubiButton` (Hoy/Semana/Mes/Total) en `role="group"`, `aria-pressed` en el periodo activo. Periodo por defecto: `WEEK`.
- **`DiarySummary.vue`**: minutos jugados + actividades únicas; si ambos son 0 muestra `DiaryEmptyState` con "Hoy todavía no ha jugado" (texto literal del sprint, reutilizado para cualquier periodo).
- **`DiaryActivityList.vue`**: agrupa las actividades recibidas en el orden fijo Reconocimiento → Comparación → Memoria (redundante con el orden que ya entrega el backend, pero definido también en frontend por la nota técnica del sprint); si no hay actividades, `DiaryEmptyState` con mensaje neutro propio.
- **`DiaryActivityCard.vue`**: nombre, "Categoría · Subcategoría" (p.ej. "Reconocimiento · Letras"), la frase de nivel actual pedida literalmente por el sprint, `DiaryDifficultyIndicator`, y `DiaryAbandonmentSignal` solo si el backend confirmó la señal.
- **`DiaryDifficultyIndicator.vue`**: tres paradas discretas (Fácil/Normal/Difícil) con texto siempre visible bajo cada una; la actual se marca con un icono de check además de color, para no depender solo del color.
- **`DiaryAbandonmentSignal.vue`**: mensaje neutro fijo, sin variantes de severidad ni opción de descartar (es una señal contextual persistente, no una notificación transitoria).
- **Backend**: `DiaryActivityResponse`/`ActivityInformationPort.ActivityDetail` ganan el campo `subcategory` (LETTER/NUMBER/SHAPE/COLOR/ANIMAL), resuelto en `ActivityInformationPortImpl.resolveCategory` a partir del `RecognitionType` real de los topics de la actividad en vez de colapsarlo todo en `"RECOGNITION"`.

### Decisiones de detalle y desvíos
1. **Ruta distinta a la del sprint**: el documento pedía `/parental/children/{childProfileId}/diary`, pero ese prefijo `/parental` no existe en ningún otro sitio de la app — todo el panel de adultos vive bajo `/panel` (`ParentPanelLayout.vue`), con el patrón ya establecido `/panel/ninos/:id/dashboard`. Decidido con el usuario usar `/panel/ninos/:id/diary` para mantener la convención existente.
2. **`subcategory` añadido a `DiaryActivityResponse`**: el sprint pide mostrar subcategoría (Letras/Formas/Números/Colores/Animales) pero su propia lista de tareas de tipos no incluye ese campo, y el backend (SPRINT-112) no lo exponía — `resolveCategory` colapsaba todo a `"RECOGNITION"`. Decidido con el usuario exponerlo en vez de omitir la subcategoría.
3. **`DiaryPeriod`/`DifficultyLevel` como union types, no `enum`**: en todo el frontend (`GameEvent.ts`, etc.) los tipos de valores fijos se modelan como `type X = 'A' | 'B'`, nunca con la palabra clave `enum` de TypeScript. Se sigue esa convención en vez de introducir el primer `enum` del proyecto.
4. **Verificación de aislamiento**: ya la hace el backend (`DiaryController.verifyChildBelongsToFamily`, responde 403 si el perfil no es de la familia autenticada). El frontend no la duplica; solo debería manejar con gracia un 403 (hoy cae en el estado de error genérico con reintento, no hay mensaje específico para "perfil de otra familia").
5. **Autenticación**: se sigue el patrón de `storyService.ts` (`Authorization: Bearer <token>` desde `useParentalAuthStore().token` en cada llamada), no el de `familyService.ts` (que no adjunta token).
6. **Señal de abandono por actividad**: se pide una vez por cada actividad visible (`Promise.all` en `useDiary.load()`), cada una aislada con `try/catch` para que un fallo puntual no tire todo el diario.
7. **Arreglo aparte, no relacionado con el diario**: el build de producción (Docker/PWA) estaba roto por un asset de 2.3MB (`daisy-tap.png`, del trabajo de animaciones del bioma meadow) que superaba el límite de precache de workbox (2MB). Se excluyó ese asset del precache en `vite.config.ts` (carga bajo demanda) para poder construir la imagen de producción.

### Archivos
- **Nuevos (frontend):** `src/types/diary.ts`, `src/services/diaryService.ts`, `src/composables/useDiary.ts`, `src/views/parental/DiaryView.vue`, `src/components/diary/{DiaryPeriodFilter,DiarySummary,DiaryActivityList,DiaryActivityCard,DiaryDifficultyIndicator,DiaryAbandonmentSignal,DiaryEmptyState}.vue`, `cypress/e2e/fase8-diario-parental/diario-parental.cy.ts`.
- **Modificados (frontend):** `router/index.ts` (ruta `PanelNinoDiary`), `views/parental/ChildProfileEditView.vue` (botón "Diario"), `i18n/locales/es.ts` (`views.ninos.diary.*`, `views.ninos.edit.diaryButton`), `vite.config.ts` (precache PWA).
- **Modificados (backend):** `tracking/ports/out/ActivityInformationPort.java`, `content/infrastructure/ActivityInformationPortImpl.java`, `tracking/infrastructure/dto/DiaryActivityResponse.java`, `tracking/service/DiaryService.java`, `test/.../DiaryServiceTest.java` (campo `subcategory`).

### Pruebas
- `npx tsc --noEmit` (frontend) y `mvn -q -o compile` (backend): limpios.
- Revisión manual línea a línea de `diario-parental.cy.ts` contra las plantillas reales de cada componente (selectores, textos i18n exactos, orden de categorías, `aria-pressed`) y contra el patrón ya usado por `edicion-perfil.cy.ts`.
- **No se ha podido ejecutar el spec e2e.** Se intentó dos veces levantando el stack Docker (`scripts/e2e-up.sh`): la primera vez falló por el asset de 2.3MB (arreglado, ver desvío 7); la segunda vez falló porque `mvn package` compila también los tests del backend, y hay ~32 errores de compilación preexistentes en tests del backend (trabajo del propio usuario sobre reconocimiento de formas, ya reportado antes en esta misma sesión, ninguno relacionado con `Diary*`/`ActivityInformation*`) — bloqueante, no arreglado por quedar fuera de alcance de este sprint. El diario queda implementado y verificado estáticamente, pero **sin verificación end-to-end real**.

### Riesgos, deuda y pendientes
- El spec `diario-parental.cy.ts` debe ejecutarse (y corregirse si hace falta) en cuanto el backend compile limpio, antes de dar el sprint por completamente verificado.
- Un 403 por aislamiento de familia (perfil de otra familia) cae hoy en el estado de error genérico de `DiaryView`, sin mensaje específico.
- Sin test de accesibilidad automatizado (solo cumplimiento por diseño: icono + texto en el indicador de nivel, nunca solo color).
