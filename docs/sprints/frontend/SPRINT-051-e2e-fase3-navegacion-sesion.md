# SPRINT-051 — E2E Fase 3: navegación transversal y sesión

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** MEDIA
- **Dependencias:** SPRINT-048 (arnés), SPRINT-049/050 (pantallas reales que recorrer). Cubre los flujos de SPRINT-015, SPRINT-016, SPRINT-032.
- **Impacto estimado:** Cubre comportamiento transversal (navegación adaptable, cierre de sesión, "volver" contextual) que afecta a todas las pantallas del panel parental, no a una vista concreta.

## Objetivo

Cubrir con E2E la navegación adaptable y salida del panel parental, el logout automático por inactividad, y el retorno contextual ("Volver") con detección de origen.

## Alcance

| Sprint legacy | Flujo | Spec propuesto |
|---|---|---|
| 015 | Navegación adaptable y acción «Salir» | `cypress/e2e/fase3-navegacion/salir-panel.cy.ts` |
| 016 | Logout automático por inactividad | `cypress/e2e/fase3-navegacion/logout-inactividad.cy.ts` |
| 032 | Retorno contextual "Volver" y detección de origen | `cypress/e2e/fase3-navegacion/volver-contextual.cy.ts` |

## Tareas del sprint

### Tarea 51.1: `salir-panel.cy.ts`

**Criterios de aceptación:**
- Positivo: la acción «Salir» desde el panel parental cierra la sesión y devuelve a la portada neutral.
- Negativo: navegar hacia atrás con el botón del navegador tras salir no restaura el panel parental (la ruta protegida vuelve a exigir autenticación).

### Tarea 51.2: `logout-inactividad.cy.ts`

**Criterios de aceptación:**
- Positivo: simulando el tiempo de inactividad configurado (vía manipulación del reloj de Cypress, no esperando en tiempo real) se cierra la sesión automáticamente.
- Negativo: actividad del usuario (click, tecleo) antes de cumplirse el umbral reinicia el contador y no cierra la sesión.

### Tarea 51.3: `volver-contextual.cy.ts`

**Criterios de aceptación:**
- Positivo: llegar a documentación desde el panel parental (`query.from`) y pulsar «Volver» regresa al panel parental.
- Negativo: llegar a documentación desde un acceso público (sin `query.from`, p. ej. portada neutral) no muestra la opción de "volver al panel" o la deshabilita, evitando exponer una ruta protegida a quien no tenía sesión.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El test de inactividad depende de temporizadores reales y hace la suite lenta o flaky | ALTA | Usar `cy.clock()`/`cy.tick()` para avanzar el tiempo simulado en vez de `cy.wait()` real. |

## Dependencias bloqueantes

- [ ] SPRINT-048, SPRINT-049 y SPRINT-050 completados.

## Criterios de aceptación del sprint

1. Los 3 specs de la tabla de alcance existen y pasan en verde vía `scripts/e2e-test.sh`.
2. Cada spec cubre al menos un caso positivo y un caso negativo.
3. El spec de inactividad usa tiempo simulado (`cy.clock`), no `cy.wait` con duraciones reales de minutos.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `docs/product/features/frontend/FEAT-004-Estructura-visual-y-navegacion-del-panel-parental.md`
