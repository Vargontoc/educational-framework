# SKILL — sprint-review

## Objetivo
Emitir el veredicto independiente de un sprint y dejarlo listo para cierre o correccion.

## Activacion

- El developer ha entregado tareas en estado `implemented`.
- Existen evidencias y pruebas asociadas.
- El usuario solicita revision o el flujo entra en fase de validacion.

## Procedimiento

1. Ejecuta `sprint-completeness`.
2. Ejecuta las pruebas aplicables mediante `test-execution`.
3. Valida contratos con `contract-validation`.
4. Revisa cambios mediante `code-review` y la skill especifica de capa.
5. Registra cada defecto usando `defect-reporting`.
6. Marca tareas demostradas como `verified` y las fallidas como `rejected`.
7. Emite un unico veredicto:
   - `APPROVED`
   - `APPROVED_WITH_OBSERVATIONS`
   - `CHANGES_REQUIRED`
   - `BLOCKED`
   - `USER_DECISION_REQUIRED`

## Reglas de cierre

- Solo `APPROVED` y `APPROVED_WITH_OBSERVATIONS` permiten cerrar el sprint.
- `CHANGES_REQUIRED` vuelve al developer con incidencias concretas.
- `BLOCKED` mantiene el sprint abierto y explica la dependencia.
- `USER_DECISION_REQUIRED` se reserva para decisiones funcionales, contractuales o arquitectonicas.
- No corrijas codigo de produccion durante la revision.

## Seccion de revision

Incluye:

- tareas verificadas y rechazadas;
- comandos y resultados;
- contratos revisados;
- defectos abiertos;
- limitaciones de la validacion;
- observaciones no bloqueantes;
- veredicto y siguiente transicion.
