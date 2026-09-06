# SPRINT-052 — E2E Fase 4: contenido estático y contacto

## Estado

- **Estado:** implemented
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

---

## Informe de implementación

**Fecha:** 2026-09-06
**Estado:** implemented

### Resumen

Se han implementado los 2 specs E2E de la Fase 4, cubriendo el layout de documentación con navegación lateral y el formulario de contacto público. Todos los tests pasan en verde (49/49, 21 specs, 100%).

### Archivos creados

| Archivo | Descripción |
|---------|-------------|
| `cypress/e2e/fase4-documentacion/documentacion-layout.cy.ts` | Spec E2E: layout documentación, navegación lateral entre secciones, estado "no encontrado" |
| `cypress/e2e/fase4-documentacion/formulario-contacto.cy.ts` | Spec E2E: formulario contacto público, envío válido, validaciones negativas |

### Tests creados

#### `documentacion-layout.cy.ts` (3 tests)

| Test | Tipo | Descripción |
|------|------|-------------|
| positivo: navegar entre secciones desde el sidebar carga el contenido sin recargar la SPA | Positivo | Verifica que el sidebar tiene 5 secciones, que la navegación entre ellas cambia el contenido (HTML distinto) y actualiza `aria-current="page"` |
| positivo: la seccion Contacto se muestra dentro del layout de documentacion | Positivo | Verifica que clicking "Contacto" en el sidebar navega a `/docs/contacto` y renderiza `ContactView` |
| negativo: acceder a una seccion inexistente muestra estado no encontrado dentro del layout | Negativo | Verifica que `/docs/seccion-inexistente` muestra "Sección no encontrada" dentro del layout (no pantalla en blanco) |

#### `formulario-contacto.cy.ts` (3 tests)

| Test | Tipo | Descripción |
|------|------|-------------|
| positivo: completar formulario con datos válidos y enviar confirma el envío | Positivo | Intercepta `POST /api/v1/contact`, verifica payload (message + type), status 200/201/202 y mensaje de éxito |
| negativo: textarea vacío bloquea el envío sin llamar a la API | Negativo | Verifica que el botón está disabled con textarea vacío + checkbox marcado, y que no se llama a la API |
| negativo: checkbox de confirmación adulta sin marcar bloquea el envío sin llamar a la API | Negativo | Verifica que el botón está disabled con textarea lleno + checkbox sin marcar, y que no se llama a la API |

### Comandos ejecutados y resultados

| Comando | Resultado |
|---------|-----------|
| `scripts/e2e-up.ps1` | ✅ Stack E2E arriba (db + api + app healthy) |
| `scripts/e2e-test.ps1` | ✅ **All specs passed!** 49 tests, 49 passing, 0 failing, 21 specs, ~21s |

### Criterios de aceptación — Estado

| # | Criterio | Estado |
|---|----------|--------|
| 1 | Los 2 specs existen y pasan en verde vía `scripts/e2e-test.sh` | ✅ `documentacion-layout.cy.ts` (3/3) + `formulario-contacto.cy.ts` (3/3) |
| 2 | Cada spec cubre al menos un caso positivo y un caso negativo | ✅ `documentacion-layout`: 2 positivos + 1 negativo; `formulario-contacto`: 1 positivo + 2 negativos |

### Contratos afectados

Ninguno. Los specs E2E no modifican contratos; consumen los existentes (`POST /api/v1/contact`).

### Riesgos y deuda técnica

Sin riesgos ni deuda técnica新增. La base del stack E2E es efímera (tmpfs), los datos de contacto de prueba se destruyen en `e2e-down.sh`.

### Tareas bloqueadas

Ninguna.
