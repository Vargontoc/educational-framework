# SPRINT-048 — E2E Fase 0: arnés de pruebas Cypress

## Estado

- **Estado:** pending
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
