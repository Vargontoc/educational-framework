# SPRINT-050 — E2E Fase 2: gestión de perfiles y configuración

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-09-06
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-048 (arnés), SPRINT-049 (Fase 1, provee sesión autenticada y perfiles de partida). Cubre los flujos de SPRINT-010, SPRINT-023/024/025, SPRINT-027, SPRINT-028, SPRINT-030, SPRINT-041.
- **Impacto estimado:** Cubre la gestión parental cotidiana (perfiles, sesiones activas, configuración global de audio/NPC/PIN) y el punto de entrada visual (HomeView), área con alto volumen de interacción diaria de un adulto.

## Objetivo

Cubrir con E2E la gestión de perfiles infantiles (cuadrícula, edición, accesibilidad cromática, bloqueo/validación) y la configuración global de familia (audio, NPC, voces, PIN), y la HomeView como punto de entrada.

## Alcance

| Sprint legacy | Flujo | Spec propuesto |
|---|---|---|
| 010 | HomeView portrait: accesos iniciales | `cypress/e2e/fase2-perfiles/homeview.cy.ts` |
| 027 | Cuadrícula de perfiles, sesiones activas, expulsar/bloquear/desbloquear | `cypress/e2e/fase2-perfiles/cuadricula-perfiles.cy.ts` |
| 028 | Edición de perfil individual | `cypress/e2e/fase2-perfiles/edicion-perfil.cy.ts` |
| 030 | Selector de accesibilidad cromática | `cypress/e2e/fase2-perfiles/accesibilidad-cromatica.cy.ts` |
| 041 | Validación de estado de perfil y aviso de bloqueo desde HomeView | `cypress/e2e/fase2-perfiles/perfil-bloqueado.cy.ts` |
| 023, 024, 025 | Configuración global (audio, NPC, voces, PIN familiar) | `cypress/e2e/fase2-perfiles/configuracion-global.cy.ts` |

## Tareas del sprint

### Tarea 50.1: `homeview.cy.ts`

**Criterios de aceptación:**
- Positivo: HomeView muestra los accesos iniciales esperados (selección de perfil, acceso parental) tras login.
- Negativo: con la API de perfiles fallando (`cy.intercept` con error 500), HomeView muestra un estado de error, no una pantalla en blanco ni una excepción no controlada.

### Tarea 50.2: `cuadricula-perfiles.cy.ts`

**Criterios de aceptación:**
- Positivo: la cuadrícula muestra perfiles con sesión activa y su duración; expulsar/bloquear/desbloquear desde la acción correspondiente cambia el estado visible del perfil.
- Negativo: bloquear un perfil sin confirmar la acción (si existe paso de confirmación) no cambia su estado.

### Tarea 50.3: `edicion-perfil.cy.ts`

**Criterios de aceptación:**
- Positivo: editar nombre, fecha de nacimiento y avatar de un perfil persiste los cambios y se reflejan en la cuadrícula.
- Negativo: guardar con un campo obligatorio vacío o una fecha de nacimiento inválida bloquea el envío y muestra el error de validación.

### Tarea 50.4: `accesibilidad-cromatica.cy.ts`

**Criterios de aceptación:**
- Positivo: seleccionar una tarjeta de accesibilidad cromática distinta de la actual la persiste y la marca como seleccionada tras recargar.
- Negativo: sin seleccionar ninguna tarjeta nueva, el valor previamente guardado se mantiene inalterado (no se pierde la preferencia por un guardado accidental).

### Tarea 50.5: `perfil-bloqueado.cy.ts`

**Criterios de aceptación:**
- Positivo: seleccionar un perfil habilitado desde HomeView navega a GameView.
- Negativo: seleccionar un perfil bloqueado muestra el aviso neutral (sin revelar la causa del bloqueo) y permanece en HomeView, tal como exige `SPRINT-041`.

### Tarea 50.6: `configuracion-global.cy.ts`

**Criterios de aceptación:**
- Positivo: cambiar audio general, NPC, voz del NPC, voz narrativa y guardar con «Guardar cambios» persiste los 5 valores.
- Negativo: cambiar el PIN familiar con una confirmación que no coincide bloquea el guardado; cerrar el formulario sin guardar no aplica ningún cambio (los controles vuelven al valor persistido, no al último editado).

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | El polling de sesiones activas (cada 5s en SPRINT-027) hace flaky el spec de cuadrícula | MEDIA | Usar `cy.intercept` para controlar la respuesta del polling en vez de depender del temporizador real. |
| R2 | Cambios de PIN familiar en este sprint interfieren con el PIN usado por specs de la Fase 1 si comparten family | BAJA | Cada spec de esta fase crea su propia familia (estrategia de datos de SPRINT-048), no reutiliza la de Fase 1. |

## Dependencias bloqueantes

- [ ] SPRINT-048 y SPRINT-049 completados.

## Criterios de aceptación del sprint

1. Los 6 specs de la tabla de alcance existen y pasan en verde vía `scripts/e2e-test.sh`.
2. Cada spec cubre al menos un caso positivo y un caso negativo.
3. El polling y demás temporizadores están controlados vía `cy.intercept`/mocks de tiempo, no vía `cy.wait(ms)` fijo.

## Referencias

- `dev-agents/skills/e2e-cypress/SKILL.md`
- `docs/product/features/frontend/FEAT-005-Configuracion-global-de-audio-NPC-y-PIN.md`
- `docs/product/features/frontend/FEAT-006-Gestion-parental-de-perfiles-infantiles.md`
