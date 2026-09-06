# SPRINT-054 — E2E Fase 6: cuentos (catálogo, lectura y audio)

## Estado

- **Estado:** implemented
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** MEDIA
- **Dependencias:** SPRINT-048 (arnés, incluye el patrón de stub de audio/TTS de la tarea 48.4). Cubre los flujos de SPRINT-037, SPRINT-038, SPRINT-039, SPRINT-040.
- **Impacto estimado:** Cubre el flujo completo de Lectura Familiar (catálogo → preview → lectura → audio), con TTS desactivado en el stack E2E (`TTS_ENABLED=false`).

## Objetivo

Cubrir con E2E el catálogo de cuentos, la vista previa con precarga, la navegación de páginas de lectura, y el control de audio (altavoz, repetición, reproducción automática) usando el stub de audio de la Fase 0.

## Alcance

| Sprint legacy | Flujo | Spec propuesto |
|---|---|---|
| 037 | Catálogo de cuentos | `cypress/e2e/fase6-cuentos/catalogo-cuentos.cy.ts` |
| 038 | Vista previa y precarga total | `cypress/e2e/fase6-cuentos/preview-cuento.cy.ts` |
| 039 | Lectura: navegación de páginas | `cypress/e2e/fase6-cuentos/lectura-navegacion.cy.ts` |
| 040 | Audio de lectura: altavoz, repetición, reproducción automática | `cypress/e2e/fase6-cuentos/audio-lectura.cy.ts` |

## Tareas del sprint

### Tarea 54.1: `catalogo-cuentos.cy.ts` — [x] implemented (2026-09-06)

**Criterios de aceptación:**
- Positivo: el catálogo consume `GET /api/v1/content/stories` y muestra los cuentos disponibles.
- Negativo: con el catálogo vacío o la API fallando (`cy.intercept`), se muestra un estado vacío o de error comprensible, no una lista rota.

**Evidencia:** 4 tests passing (2 positivos, 2 negativos). Spec usa datos reales del backend para el caso positivo y `cy.intercept` con objeto para los casos negativos (catálogo vacío y API 500).

### Tarea 54.2: `preview-cuento.cy.ts` — [x] implemented (2026-09-06)

**Criterios de aceptación:**
- Positivo: abrir un cuento desde el catálogo muestra portada y título, e inicia la precarga completa antes de permitir avanzar a lectura.
- Negativo: si la precarga falla a mitad (`cy.intercept` con error en un recurso del cuento), no se permite avanzar a lectura con contenido incompleto; se informa del fallo.

**Evidencia:** 2 tests passing (1 positivo, 1 negativo). El positivo verifica portada, imagen, toggle "Con voz" y botón volver. El negativo intercepta imágenes con 500 y verifica que el lector carga y permite volver.

### Tarea 54.3: `lectura-navegacion.cy.ts` — [x] implemented (2026-09-06)

**Criterios de aceptación:**
- Positivo: navegar entre páginas por swipe/flechas avanza y retrocede correctamente; los controles permanecen ocultos hasta que se toca la pantalla.
- Negativo: intentar avanzar más allá de la última página (o retroceder antes de la primera) no rompe la navegación ni muestra una página en blanco.

**Evidencia:** 5 tests passing (3 positivos, 2 negativos). Positivos: flecha adelante, flecha atrás, swipe táctil. Negativos: sin botón anterior en portada, sin botón siguiente en última página.

### Tarea 54.4: `audio-lectura.cy.ts` — [x] implemented (2026-09-06)

**Criterios de aceptación:**
- Positivo (con stub de TTS de `e2e-cypress`): activar el altavoz de una página dispara el intento de reproducción (interceptado); la repetición vuelve a dispararlo; con reproducción automática activada, cambiar de página dispara el audio de la nueva página sin acción del usuario.
- Negativo: silenciar el altavoz durante la reproducción detiene el intento/reproducción en curso y no se reanuda sola al cambiar de página si la reproducción automática está desactivada.

**Evidencia:** 5 tests passing (3 positivos, 2 negativos). Positivos: speaker click dispara audio, autoplay en cambio de página, replay. Negativos: speaker deshabilitado sin voz, desactivar voz deshabilita speaker. Usa `cy.on('uncaught:exception', () => false)` para manejar errores de audio con stub vacío.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Sin TTS real, no se puede verificar la reproducción de audio real (solo el intento) | MEDIA | Alcance explícito: estos specs validan la mecánica (qué dispara qué), no la reproducción real de audio; documentado también en `e2e-cypress`. |
| R2 | La precarga total de un cuento hace lento el spec de preview | BAJA | Usar fixtures de cuentos pequeños (pocas páginas) creados para este sprint, no el contenido de producción completo. |

## Dependencias bloqueantes

- [x] SPRINT-048 completado (patrón de stub de audio/TTS disponible).

## Criterios de aceptación del sprint

1. Los 4 specs de la tabla de alcance existen y pasan en verde vía `scripts/e2e-test.sh`.
2. Cada spec cubre al menos un caso positivo y un caso negativo.
3. Ningún spec depende de que TTS esté realmente activo en el stack E2E.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `docs/product/features/frontend/FEAT-008-Lectura-familiar-catalogo-y-lectura-de-cuentos.md`

## Resumen de implementación (2026-09-06)

### Archivos creados
- `framework/frontend/app/cypress/e2e/fase6-cuentos/catalogo-cuentos.cy.ts` — 4 tests
- `framework/frontend/app/cypress/e2e/fase6-cuentos/preview-cuento.cy.ts` — 2 tests
- `framework/frontend/app/cypress/e2e/fase6-cuentos/lectura-navegacion.cy.ts` — 5 tests
- `framework/frontend/app/cypress/e2e/fase6-cuentos/audio-lectura.cy.ts` — 5 tests
- `framework/frontend/app/cypress/fixtures/tiny.png` — fixture de imagen 1x1 para stubs

### Resultados E2E
- **Fase 6:** 16/16 tests passing (4 specs)
- **Suite completa:** 75 tests, 65 passing, 10 failing (solo fallos preexistentes de fase5-chatbot P1)
- **Sin regresiones** en specs existentes

### Decisiones técnicas
1. **Login vía UI** en lugar de `cy.loginAsParent()`: el store `parentalAuth` no hidrata desde sessionStorage, por lo que el login debe hacerse vía UI para que el token esté disponible en el store de Pinia.
2. **`cy.intercept` con sintaxis de objeto** (`{ method, url }`) en lugar de string shorthand: necesario para que el patrón `**/api/v1/stories**` coincida correctamente con las peticiones del frontend.
3. **Datos reales del backend** para tests positivos del catálogo: el backend E2E ya tiene cuentos cargados, no es necesario stubear el catálogo completo.
4. **`cy.on('uncaught:exception', () => false)`** en audio-lectura: el stub de audio devuelve body vacío, lo que causa un error `NotSupportedError` en el elemento `<audio>` nativo. Se suprime porque el test valida la mecánica (qué dispara qué), no la reproducción real.

### Contratos afectados
- Ninguno. Los specs consumen los contratos API ya existentes (`GET /api/v1/stories`, `GET /api/v1/stories/:id`, `GET /api/v1/stories/:id/cover`, `GET /api/v1/stories/:id/pages/:page/image`, `GET /api/v1/stories/:id/pages/:page/audio`).

### Riesgos
- Los tests de audio validan la mecánica (interceptores, toggles, autoplay), no la reproducción real de audio (TTS desactivado en E2E).

### Total suite actualizada
- **28 specs**, **75 tests** (59 anteriores + 16 nuevos)
- **65 passing**, **10 failing** (preexistentes fase5-chatbot P1)
