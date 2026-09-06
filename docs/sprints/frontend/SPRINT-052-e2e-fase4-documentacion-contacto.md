# SPRINT-052 — E2E Fase 4: contenido estático y contacto

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** BAJA
- **Dependencias:** SPRINT-048 (arnés). Cubre los flujos de SPRINT-031, SPRINT-033.
- **Impacto estimado:** Riesgo bajo (contenido mayormente estático), pero coste bajo de cubrir; incluye el único formulario público con envío real a la API en este bloque.

## Objetivo

Cubrir con E2E la sección pública de documentación (layout, navegación lateral, secciones estáticas) y el formulario de contacto.

## Alcance

| Sprint legacy | Flujo | Spec propuesto |
|---|---|---|
| 031 | Layout de documentación y navegación lateral | `cypress/e2e/fase4-documentacion/documentacion-layout.cy.ts` |
| 033 | Formulario de contacto público | `cypress/e2e/fase4-documentacion/formulario-contacto.cy.ts` |

## Tareas del sprint

### Tarea 52.1: `documentacion-layout.cy.ts`

**Criterios de aceptación:**
- Positivo: navegar entre las secciones estáticas desde la navegación lateral carga el contenido correspondiente sin recargar toda la SPA.
- Negativo: acceder directamente por URL a una sección de documentación inexistente muestra un estado "no encontrado" dentro del layout, no una pantalla en blanco.

### Tarea 52.2: `formulario-contacto.cy.ts`

**Criterios de aceptación:**
- Positivo: completar el formulario con datos válidos y enviarlo confirma el envío (`POST` interceptado o contra la API real del stack E2E).
- Negativo: enviar el formulario con un campo obligatorio vacío o un email con formato inválido bloquea el envío y muestra el error de validación sin llamar a la API.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El envío real de contacto genera datos de prueba persistentes en la base del stack E2E | BAJA | La base del stack E2E es efímera (tmpfs, se destruye en cada `e2e-down.sh`); no hay limpieza adicional que hacer. |

## Dependencias bloqueantes

- [ ] SPRINT-048 completado.

## Criterios de aceptación del sprint

1. Los 2 specs de la tabla de alcance existen y pasan en verde vía `scripts/e2e-test.sh`.
2. Cada spec cubre al menos un caso positivo y un caso negativo.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `docs/product/features/frontend/FEAT-007-Seccion-publica-de-documentacion-y-contacto.md`
