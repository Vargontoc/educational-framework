# SKILL — sprint-task-tracking

## Objetivo
Mantener el sprint como registro auditable de implementacion y revision.

## Estados
- `pending`
- `in_progress`
- `implemented`
- `verified`
- `blocked`
- `rejected`
- `waived`

## Reglas
- El developer puede usar hasta `implemented`.
- Solo el reviewer puede usar `verified` o `rejected`.
- Cada cambio de estado incluye fecha, evidencia y motivo.
- Una casilla marcada no sustituye la evidencia.
- No cierres un sprint con tareas `pending`, `in_progress`, `blocked` o `rejected`.
