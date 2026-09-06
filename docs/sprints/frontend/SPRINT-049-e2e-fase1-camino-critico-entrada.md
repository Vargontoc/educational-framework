# SPRINT-049 — E2E Fase 1: camino crítico de entrada

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-048 (arnés Cypress). Cubre los flujos de SPRINT-001, SPRINT-002, SPRINT-011, SPRINT-012, SPRINT-013, SPRINT-014.
- **Impacto estimado:** Si estos flujos fallan, ninguna otra fase del roadmap E2E (050-055) es alcanzable: son el punto de entrada de toda la aplicación.

## Objetivo

Cubrir con E2E el camino sin el cual no se puede llegar a ninguna otra pantalla: arranque de la app, portada neutral, registro familiar, verificación parental y alta de perfil, y listado de perfiles.

## Alcance

| Sprint legacy | Flujo | Spec propuesto |
|---|---|---|
| 001, 002 | Arranque de la app, shell, renderizado horizontal permanente | `cypress/e2e/fase1-entrada/app-shell.cy.ts` |
| 014 | Portada neutral y autenticación | `cypress/e2e/fase1-entrada/portada-autenticacion.cy.ts` |
| 011 | Registro familiar completo (nombre de familia + PIN) | `cypress/e2e/fase1-entrada/registro-familiar.cy.ts` |
| 013 | Verificación parental y alta de perfil infantil | `cypress/e2e/fase1-entrada/alta-perfil-infantil.cy.ts` |
| 012 | Listado de perfiles tras alta | `cypress/e2e/fase1-entrada/listado-perfiles.cy.ts` |

## Tareas del sprint

### Tarea 49.1: `app-shell.cy.ts`

**Criterios de aceptación:**
- Positivo: la app carga, el shell se renderiza y la navegación básica (rutas `/`, `/panel`, `/docs`) resuelve sin error de consola.
- Negativo: navegar directamente a una ruta protegida (`/panel`, `/game/:childId`) sin sesión activa redirige a la portada neutral en vez de mostrar contenido protegido.

### Tarea 49.2: `portada-autenticacion.cy.ts`

**Criterios de aceptación:**
- Positivo: introducir el PIN familiar correcto navega al panel parental.
- Negativo: introducir un PIN incorrecto muestra un error comprensible y no navega; tras varios intentos fallidos no se filtra información sobre el PIN real.

### Tarea 49.3: `registro-familiar.cy.ts`

**Criterios de aceptación:**
- Positivo: completar los dos pasos (nombre de familia y PIN) crea la familia y deja al usuario autenticado.
- Negativo: un PIN que no cumple el formato requerido, o una confirmación de PIN que no coincide, bloquea el envío y muestra el error de validación correspondiente sin llamar a la API.

### Tarea 49.4: `alta-perfil-infantil.cy.ts`

**Criterios de aceptación:**
- Positivo: con verificación parental superada, dar de alta un perfil infantil válido lo deja disponible en el listado.
- Negativo: intentar el alta sin verificación parental (o con verificación fallida) no crea el perfil ni concede acceso al formulario de alta.

### Tarea 49.5: `listado-perfiles.cy.ts`

**Criterios de aceptación:**
- Positivo: tras dar de alta uno o más perfiles, el listado los muestra con su información básica (nombre, avatar).
- Negativo: con cero perfiles dados de alta, el listado muestra un estado vacío comprensible en vez de una pantalla en blanco o un error.

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El PIN de familia queda expuesto en logs de red capturados por Cypress | MEDIA | No incluir capturas de red con el PIN en la evidencia adjunta al informe de revisión; usar valores de PIN de prueba, nunca reales. |
| R2 | El flujo de registro cambia de dos pasos a uno en una futura iteración y rompe el spec | BAJA | El spec navega por el DOM (roles/labels), no por índices de paso fijos. |

## Dependencias bloqueantes

- [ ] SPRINT-048 completado (comandos de sesión y estrategia de datos disponibles).

## Criterios de aceptación del sprint

1. Los 5 specs de la tabla de alcance existen y pasan en verde vía `scripts/e2e-test.sh`.
2. Cada spec cubre al menos un caso positivo y un caso negativo, según `dev-agents/skills/e2e-cypress/SKILL.md`.
3. Ningún spec depende del orden de ejecución ni de datos dejados por otro spec.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `docs/sprints/frontend/SPRINT-048-e2e-fase0-arnes-cypress.md`
- `docs/product/features/frontend/FEAT-003-Seleccion-y-alta-de-perfiles-infantiles.md`
