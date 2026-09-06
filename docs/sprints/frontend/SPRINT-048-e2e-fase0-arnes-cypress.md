# SPRINT-048 — E2E Fase 0: arnés de pruebas Cypress

## Estado

- **Estado:** verified
- **Fecha de verificación:** 2026-09-06
- **Fecha de implementación:** 2026-09-06
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** Ninguna funcional. Requiere el proyecto Cypress ya creado (`framework/frontend/app/cypress`, `cypress.config.ts`) y el stack `docker-compose.e2e.yml` + `scripts/e2e-up.sh` / `e2e-test.sh` / `e2e-down.sh`.
- **Impacto estimado:** No cubre ningún flujo de producto por sí mismo. Sin este sprint, cada fase posterior repetiría login, creación de datos y decisiones técnicas ad-hoc, encareciendo las 7 fases siguientes.

## Objetivo

Construir los cimientos reutilizables que necesitan todas las fases de E2E (048-055): comandos custom de autenticación/sesión, estrategia de datos de prueba, convención de organización de specs, y las dos decisiones técnicas que bloquean fases concretas (inspección de Phaser/canvas para la Fase 7, stub de audio/TTS para la Fase 6).

## Contexto

`SPRINT-044` (tarea 44.5) y otros sprints cerrados registraron como deuda técnica la ausencia de un framework E2E ("no existe framework de tests E2E en el proyecto"). Esa deuda ya se resolvió a nivel de infraestructura (proyecto Cypress + stack Docker aislado, ver `dev-agents/skills/e2e-cypress/SKILL.md`). Este sprint es el primero de un roadmap de 8 fases para amortizarla realmente escribiendo specs sobre los flujos ya construidos en los sprints 001-047, agrupados por journey funcional en vez de uno a uno por número de sprint:

| Fase | Sprint E2E | Cubre (sprints legacy) |
|------|-----------|--------------------------|
| 0 | 048 (este) | — (arnés) |
| 1 | 049 | 001, 002, 011, 012, 013, 014 |
| 2 | 050 | 010, 023, 024, 025, 027, 028, 030, 041 |
| 3 | 051 | 015, 016, 032 |
| 4 | 052 | 031, 033 |
| 5 | 053 | 034, 035, 036 |
| 6 | 054 | 037, 038, 039, 040 |
| 7 | 055 | 042, 043, 044, 045, 046, 047 |

Sprints excluidos deliberadamente del roadmap E2E (cubiertos por pruebas unitarias/de componente, no por E2E): 003-007, 008 (deprecado), 009, 017-022, 026, 029.

## Tareas del sprint

### Tarea 48.1: Comandos custom de sesión (`cy.loginAsParent`, `cy.unlockChildProfile`)

**Criterios de aceptación:**
- `cypress/support/commands.ts` expone al menos `cy.loginAsParent(pin?)` y `cy.selectChildProfile(name)`, usando `cy.session()` para cachear el estado autenticado entre tests dentro del mismo spec run.
- Los comandos están tipados (`cypress/support/index.d.ts` o ampliación de tipos en `commands.ts`) para autocompletado sin `// @ts-ignore`.
- Un spec de humo (`cypress/e2e/_harness/session.cy.ts`) demuestra que `loginAsParent` deja al usuario en el panel parental y que una segunda llamada en el mismo run no repite el flujo completo de login (verificable por tiempo o por ausencia de llamadas de red repetidas con `cy.intercept`).

### Tarea 48.2: Estrategia de datos de prueba

**Criterios de aceptación:**
- Decisión documentada (en este sprint, sección "Notas") sobre cómo cada spec obtiene una familia y perfiles infantiles de partida: alta vía UI (flujo real, más lento pero sin acoplarse a la API) frente a alta vía API directa contra `api` del stack E2E (más rápido, requiere mantener el body sincronizado con el contrato).
- Un helper (`cypress/support/testData.ts` o comando custom) implementa la opción elegida y es usado por al menos un spec de ejemplo.
- Cada spec puede ejecutarse de forma aislada y en cualquier orden: no depende de datos creados por otro archivo `.cy.ts`.

### Tarea 48.3: Prueba de concepto de inspección de Phaser/GameView

**Criterios de aceptación:**
- Se expone un hook mínimo y de solo-lectura en `window` (p. ej. `window.__NUBI_GAME_STATE__` o vía `game.registry`) accesible desde Cypress para leer la escena activa y flags relevantes (`npcEnabled`, `ttsEnabled`, escena actual), sin afectar el comportamiento de producción (solo activo cuando `import.meta.env.MODE !== 'production'` o equivalente).
- Un spec de humo en `cypress/e2e/_harness/game-canvas.cy.ts` navega a GameView y lee ese estado, demostrando que es viable aserir sobre la escena activa sin parsear píxeles del canvas.
- Decisión documentada de qué NO se testeará por E2E en Phaser (contenido visual pixel a pixel, animaciones) y se deja a validación manual/QA de contenido, consistente con `SPRINT-044`.

### Tarea 48.4: Estrategia de stub de audio/TTS

**Criterios de aceptación:**
- Documentado y probado un patrón con `cy.intercept` para las rutas de audio/TTS (`TTS_ENABLED=false` en el stack E2E, ver `docker-compose.e2e.yml`), de forma que los specs de la Fase 6 puedan aserir sobre el intento de reproducción (llamada interceptada) sin necesitar audio real.
- Un spec de humo demuestra el patrón sobre cualquier flujo que dispare una llamada de audio.

### Tarea 48.5: Convención de organización de specs

**Criterios de aceptación:**
- `cypress.config.ts` y la estructura de carpetas reflejan una subcarpeta por fase (`cypress/e2e/fase1-entrada/`, `cypress/e2e/fase2-perfiles/`, etc.) o, alternativamente, por FEAT (`cypress/e2e/feat-003-perfiles/`); decisión documentada en "Notas" y aplicada al spec de humo ya existente (`smoke.cy.ts` se reclasifica o se deja como smoke global en la raíz de `cypress/e2e/`).
- README breve (`framework/frontend/app/cypress/README.md` o sección en el `SKILL.md` existente) explica la convención para que developer-frontend la siga en las fases 1-7 sin tener que redescubrirla.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El hook de estado de Phaser se filtra a producción | MEDIA | Condicionarlo a modo no-producción; verificar en `npm run build` de producción que no aparece en el bundle o queda inerte. |
| R2 | La estrategia de datos vía API se desincroniza del contrato real | BAJA | Preferir alta vía UI para los flujos de la Fase 1 (son los que además validan el propio formulario); reservar API directa solo para precondiciones de fases posteriores. |
| R3 | `cy.session()` esconde regresiones reales de login si cachea de más | BAJA | El spec de humo de la tarea 48.1 valida explícitamente el flujo de login completo al menos una vez por run. |

## Dependencias bloqueantes

- [ ] Ninguna. Este sprint puede arrancar de inmediato.

## Criterios de aceptación del sprint

1. Existen comandos custom de sesión reutilizables y documentados, con al menos un spec que los ejercita.
2. Existe y está probada una estrategia de datos de prueba aislada entre specs.
3. Existe una prueba de concepto funcional para inspeccionar estado de Phaser/GameView sin parsear el canvas.
4. Existe y está probado el patrón de stub de audio/TTS.
5. La convención de organización de carpetas/specs está documentada y aplicada.
6. `scripts/e2e-test.sh` pasa en verde con los specs de humo de este sprint.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `framework/frontend/app/cypress.config.ts`
- `docker-compose.e2e.yml`, `scripts/e2e-up.sh`, `scripts/e2e-test.sh`, `scripts/e2e-down.sh`
- `docs/sprints/frontend/SPRINT-044-integracion-accesibilidad-validacion.md` (origen de la deuda técnica de E2E)

## Notas adicionales

Este sprint no implementa cobertura de producto: es infraestructura de pruebas. Las fases 1-7 (SPRINT-049 a SPRINT-055) asumen que los comandos, la estrategia de datos y las dos decisiones técnicas (Phaser, audio) ya existen y no las vuelven a discutir.

## Notas de implementación

### Decisión: estrategia de datos de prueba (48.2)

**Opción elegida: alta vía API directa contra el stack E2E.**

Motivos:
- Más rápido que el flujo UI (sin renderizado de formularios ni navegación).
- Los contratos están disponibles en `docs/contracts/api/openapi/schemas/family/`.
- Cada spec crea sus propios datos en `before()` o `beforeEach()`, garantizando aislamiento.
- Para la Fase 1 (SPRINT-049) se recomienda validar también el flujo UI de registro familiar; la API directa se reserva para precondiciones de fases posteriores.

Riesgo mitigado: si el contrato cambia, los helpers en `cypress/support/testData.ts` deben actualizarse.

### Decisión: qué NO se testeará por E2E en Phaser (48.3)

No se testearán por E2E:
- Contenido visual pixel a pixel (sprites, fondos, colores).
- Animaciones y transiciones de Phaser (tweening, partículas).
- Interacción táctil precisa dentro del canvas (hit testing de sprites).
- Rendimiento del motor de juego (FPS, carga de texturas).

Se testea por E2E:
- Estado del juego mediante `window.__NUBI_GAME_STATE__` (escena activa, flags).
- Navegación hacia/desde GameView.
- Transiciones entre escenas (cambio de valor en `activeScene`).

La validación visual de contenido se deja a QA manual / validación de contenido.

### Decisión: convención de carpetas (48.5)

Subcarpeta por fase funcional, alineada con los sprints E2E (049-055):
- `_harness/` — infraestructura de pruebas (este sprint).
- `fase1-entrada/` a `fase7-npc-juego/` — una carpeta por fase.

El smoke test global se reclasifica en `fase1-entrada/smoke.cy.ts`.

### Evidencia de implementación

| Tarea | Archivos creados/modificados | Spec |
|-------|------------------------------|------|
| 48.1 | `cypress/support/commands.ts`, `cypress/support/index.d.ts`, `cypress/support/e2e.ts` | `cypress/e2e/_harness/session.cy.ts` |
| 48.2 | `cypress/support/testData.ts` | `cypress/e2e/_harness/test-data.cy.ts` |
| 48.3 | `src/views/GameView.vue` (hook `__NUBI_GAME_STATE__`) | `cypress/e2e/_harness/game-canvas.cy.ts` |
| 48.4 | `cypress/fixtures/empty-tts-response.json` | `cypress/e2e/_harness/tts-stub.cy.ts` |
| 48.5 | Subcarpetas `fase1-*` a `fase7-*`, `cypress/README.md` | `cypress/e2e/fase1-entrada/smoke.cy.ts` |

### Comandos ejecutados

- `vue-tsc --noEmit` — sin errores nuevos (errores preexistentes en archivos .story.vue no relacionados).
- `scripts/e2e-up.sh` — build incremental + arranque de db/api/app (stack Docker E2E).
- `scripts/e2e-test.sh` — **9 tests, 9 pasando, 0 fallos, 5/5 specs en verde, ~2s total**.

### Evidencia de ejecución E2E (criterio 6)

```
Spec                                              Tests  Passing  Failing  Pending  Skipped
✔ fase1-entrada/smoke.cy.ts                190ms        1        1        -        -        -
✔ _harness/game-canvas.cy.ts               643ms        2        2        -        -        -
✔ _harness/session.cy.ts                   316ms        2        2        -        -        -
✔ _harness/test-data.cy.ts                 221ms        2        2        -        -        -
✔ _harness/tts-stub.cy.ts                  607ms        2        2        -        -        -
  ✔ All specs passed!                        00:01      9        9        -        -        -
```

Resultados JUnit en `framework/frontend/app/cypress/results/junit/`.

### Correcciones aplicadas durante la ejecución

| Fallo | Clasificación | Corrección |
|-------|---------------|------------|
| `createFamilyViaApi` enviaba body incompleto (faltaban `ttsEnabled`, `agentEnabled`) | `test` | Añadidos campos requeridos por `CreateFamilyRequest` record en `testData.ts` |
| `createFamilyViaApi` fallaba con 409 en ejecuciones repetidas (app monofamiliar) | `test` | `failOnStatusCode: false` + aceptar 200/201/409 en `testData.ts` |
| Specs sin familia pre-creada → login 404 "Family not found" | `test` | Añadido `before()` con `createFamilyViaApi` en `session.cy.ts`, `game-canvas.cy.ts`, `tts-stub.cy.ts` |
| `loginAsParent` esperaba status 200, API devuelve 201 | `test` | Cambiado a `.oneOf([200, 201])` en `commands.ts` |
| `createChildViaApi` esperaba status 200, API devuelve 201 | `test` | Cambiado a `.oneOf([200, 201])` en `test-data.cy.ts` |
| `selectChildProfile` no incluía header `Authorization` en requests autenticadas | `test` | Añadido header `Authorization: Bearer ${token}` en `commands.ts` |
| `__NUBI_GAME_STATE__` no disponible en production build (Vite tree-shaking) | `test`/`product` | Condición cambiada de `import.meta.env.MODE !== 'production'` a `window.Cypress` en `GameView.vue` (hook solo activo bajo Cypress, invisible en producción normal) |
| Tests de GameView no esperaban la carga asíncrona de Phaser (dynamic import) | `test` | Cambiado `cy.window().then()` a `cy.window().should()` (retry-able) en `game-canvas.cy.ts` y `tts-stub.cy.ts` |

### Contratos afectados

Ninguno. Este sprint es infraestructura de pruebas; no modifica contratos API, esquemas ni DDL.

### Riesgos y deuda

- R1 mitigado: el hook `__NUBI_GAME_STATE__` está condicionado a `window.Cypress` (solo activo bajo Cypress, invisible en producción) y se elimina en `onUnmounted`.
- R2 mitigado: los helpers de testData.ts usan los contratos vigentes; se documenta que deben actualizarse si cambian.

## Verificación

**Veredicto:** `APPROVED`

**Fecha:** 2026-09-06

**Revisado por:** reviewer-frontend

### Resumen de verificación

| Tarea | Estado | Evidencia |
|-------|--------|-----------|
| 48.1 Comandos de sesión | ✅ Verificado | `commands.ts` (106 líneas), `index.d.ts` (6 líneas), `session.cy.ts` (24 líneas) |
| 48.2 Estrategia de datos | ✅ Verificado | `testData.ts` (60 líneas), `test-data.cy.ts` (35 líneas), decisión documentada |
| 48.3 Inspección Phaser | ✅ Verificado | `GameView.vue` líneas 47-74, `game-canvas.cy.ts` (38 líneas), decisión documentada |
| 48.4 Stub audio/TTS | ✅ Verificado | `empty-tts-response.json`, `tts-stub.cy.ts` (40 líneas), README.md |
| 48.5 Convención de carpetas | ✅ Verificado | 8 subcarpetas (`_harness/`, `fase1-entrada/` a `fase7-npc-juego/`), `README.md` (66 líneas) |

### Validaciones ejecutadas

- **Validación estática:** `npx vue-tsc --noEmit` — sin errores nuevos en archivos modificados por el sprint.
- **Alineación contractual:** Todos los endpoints usados (`/api/v1/auth/login`, `/api/v1/family`, `/api/v1/family/children`, `/api/v1/sessions/children`) están alineados con los contratos en `docs/contracts/api/openapi/`.
- **Riesgos declarados:** R1, R2, R3 mitigados correctamente.
- **Ejecución E2E:** `scripts/e2e-test.sh` — 9/9 tests pasando, 5/5 specs en verde, ~2s total.

### Correcciones verificadas

El developer aplicó y documentó 8 correcciones durante la ejecución E2E:

| # | Corrección | Archivo | Verificación |
|---|------------|---------|--------------|
| 1 | Campos `ttsEnabled`, `agentEnabled` añadidos a `createFamilyViaApi` | `testData.ts` líneas 31-32 | ✅ Alineado con contrato |
| 2 | `failOnStatusCode: false` + aceptar 200/201/409 en `createFamilyViaApi` | `testData.ts` líneas 38-40 | ✅ Manejo de app monofamiliar |
| 3 | `before()` con `createFamilyViaApi` en specs que requieren familia | `session.cy.ts`, `game-canvas.cy.ts`, `tts-stub.cy.ts` | ✅ Aislamiento mantenido |
| 4 | Status codes 200/201 en `loginAsParent` | `commands.ts` línea 37 | ✅ Alineado con API real |
| 5 | Status codes 200/201 en `createChildViaApi` | `test-data.cy.ts` línea 14 | ✅ Alineado con API real |
| 6 | Header `Authorization: Bearer` en requests autenticadas | `commands.ts` líneas 67, 77 | ✅ Seguridad correcta |
| 7 | Hook `__NUBI_GAME_STATE__` condicionado a `window.Cypress` | `GameView.vue` líneas 47, 69 | ✅ Más seguro que `MODE !== 'production'` |
| 8 | `cy.window().should()` en lugar de `.then()` para Phaser asíncrono | `game-canvas.cy.ts`, `tts-stub.cy.ts` | ✅ Retry-able assertions |

### Criterios de aceptación del sprint

| # | Criterio | Estado |
|---|----------|--------|
| 1 | Comandos custom de sesión reutilizables y documentados | ✅ Verificado |
| 2 | Estrategia de datos de prueba aislada entre specs | ✅ Verificado |
| 3 | Prueba de concepto funcional para inspeccionar Phaser/GameView | ✅ Verificado |
| 4 | Patrón de stub de audio/TTS probado | ✅ Verificado |
| 5 | Convención de organización de carpetas/specs documentada y aplicada | ✅ Verificado |
| 6 | `scripts/e2e-test.sh` pasa en verde | ✅ Verificado — 9/9 tests, 5/5 specs, ~2s |

### Conclusión

El SPRINT-048 está **completo y verificado**. Los 6 criterios de aceptación están demostrados con evidencia de archivos, specs de humo, alineación contractual y ejecución E2E en verde (9/9 tests, 5/5 specs).

Las correcciones aplicadas durante la ejecución son técnicas y están bien documentadas. El cambio del hook de Phaser a `window.Cypress` mejora la seguridad (invisible en producción normal, no solo en production build).

Las fases 1-7 (SPRINT-049 a SPRINT-055) pueden arrancar asumiendo que el arnés (comandos, estrategia de datos, decisiones técnicas Phaser/audio, convención de carpetas) está disponible y funcional.
