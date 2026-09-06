#!/usr/bin/env bash
# Ejecuta Cypress contra el stack E2E ya arrancado (scripts/e2e-up.sh). No
# construye ni recrea contenedores: pensado para repetirse muchas veces en una
# sesión de revisión sin pagar el coste de un rebuild/restart cada vez.
# Resultados (junit, vídeos, capturas) en framework/frontend/app/cypress/results.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

docker compose -f docker-compose.e2e.yml --profile test run --rm cypress
exit_code=$?

echo "Resultados E2E en framework/frontend/app/cypress/results"

exit $exit_code
