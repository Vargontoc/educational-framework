# Reglas Git para agentes de desarrollo y revision

## Principios

- Trabaja sobre la rama proporcionada; no cambies de rama sin instruccion.
- No uses `git reset --hard`, `git clean -fd`, rebase, force push ni comandos destructivos sin autorizacion explicita.
- No reviertas cambios ajenos no relacionados con el sprint.
- Mantén los cambios limitados a la capa y tareas aprobadas.
- Revisa `git status` y `git diff` antes y despues de implementar.
- No incluyas secretos, credenciales, tokens, binarios generados ni datos personales.

## Developer

- Puede modificar archivos y ejecutar comandos necesarios.
- Debe agrupar cambios coherentes y documentar archivos afectados.
- No crea commits ni publica ramas salvo peticion explicita.
- No marca una tarea como implementada con cambios sin probar.

## Reviewer

- Usa Git en modo lectura para inspeccionar diff, historial y alcance.
- No modifica codigo de produccion ni reescribe commits.
- Puede crear o actualizar exclusivamente informes de revision y estados del sprint.
