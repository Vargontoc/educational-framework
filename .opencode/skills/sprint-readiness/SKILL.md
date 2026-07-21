# SKILL — sprint-readiness

## Objetivo
Comprobar que un sprint puede comenzar sin inventar requisitos ni trabajar sobre decisiones pendientes.

## Comprobaciones
1. Existe FEAT o ADR aprobado y referenciado.
2. El sprint identifica capa propietaria, objetivo y alcance.
3. Cada tarea tiene criterio de aceptacion y evidencia esperada.
4. Las dependencias y contratos estan disponibles o incluidos como tareas previas.
5. No hay contradicciones abiertas ni decisiones del usuario pendientes.
6. El estado del sprint permite implementacion.

## Resultado
Devuelve `READY`, `BLOCKED` o `USER_DECISION_REQUIRED`, con motivos y referencias. No implementes mientras el resultado no sea `READY`.
