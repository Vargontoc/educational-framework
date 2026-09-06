# SKILL — e2e-cypress

## Objetivo
Ejecutar y mantener las pruebas E2E de `framework/frontend/app` con Cypress sobre el stack Docker de `docker-compose.e2e.yml` (app + api + db, aislado de dev/produccion).

## Prerrequisitos
- Docker y Docker Compose con soporte BuildKit.
- Ollama local corriendo con el modelo de embeddings ya descargado (el arranque de la API carga el RAG de forma no perezosa y no levanta sin el).

## Comandos
- Sesion con muchas ejecuciones (revision activa, iteracion sobre fallos): `scripts/e2e-up.sh` (build incremental + arranque de db/api/app; repetir tras cada cambio de codigo) -> `scripts/e2e-test.sh` (ejecuta Cypress contra el stack ya arriba; repetible sin rebuild) -> `scripts/e2e-down.sh` (limpieza final, una sola vez al terminar la sesion).
- Ejecucion puntual (una sola vez): `scripts/run-e2e.sh` (o `scripts/run-e2e.ps1` en PowerShell); build, test y limpieza en un solo paso, con el codigo de salida de Cypress.
- Local sin Docker, contra `npm run dev`/`preview`: desde `framework/frontend/app`, `npm run cy:open` (interactivo) o `npm run cy:run` (headless); fija `CYPRESS_BASE_URL` si el puerto no es el 80 por defecto.

## Evidencia
Resultados en `framework/frontend/app/cypress/results/{junit,videos,screenshots}` (se regeneran en cada ejecucion). Adjunta el XML junit y, si hay fallo, el video o captura correspondiente.

## Reglas para specs nuevas o modificadas
- Un spec por flujo o criterio de aceptacion critico, en `cypress/e2e/<feature>.cy.ts`.
- No cubras solo el camino feliz: incluye al menos un caso negativo por flujo (entrada invalida, error de API/red, acceso no autorizado, estado vacio) que compruebe el mensaje o comportamiento esperado, no solo la ausencia de crash.
- El test crea sus propias precondiciones; no asumas datos dejados por otro spec ni orden de ejecucion.
- Alcance actual del stack: app + api con Ollama real; TTS esta deshabilitado (`TTS_ENABLED=false`). Usa `cy.intercept` para lo que dependa de TTS o de servicios fuera de alcance en vez de fallar el test.
- No dupliques cobertura ya cubierta por pruebas unitarias/componentes: reserva E2E para flujos completos con navegacion y consumo real de la API.

## Resultado
Suite verde/roja con evidencia reproducible; clasifica cada fallo como en `test-execution` (`product`, `test`, `environment`, `contract`, `unknown`).
