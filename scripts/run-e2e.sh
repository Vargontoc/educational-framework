#!/usr/bin/env bash
# Orquesta el stack de pruebas E2E (Cypress) definido en docker-compose.e2e.yml:
# construye las imágenes, levanta app+api+db, ejecuta Cypress, y al terminar
# (pase o falle) elimina los contenedores. Los resultados (junit, videos,
# screenshots) quedan en framework/frontend/app/cypress/results vía bind mount.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

COMPOSE_FILE="docker-compose.e2e.yml"

cleanup() {
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans
  # Auto-purga de cache mounts de BuildKit (Maven/npm) sin uso en 14 días, para
  # que altas/bajas de dependencias no hagan crecer la caché sin límite.
  docker buildx prune --force --filter type=exec.cachemount --filter unused-for=336h >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker compose -f "$COMPOSE_FILE" --profile test up \
  --build \
  --abort-on-container-exit \
  --exit-code-from cypress

exit_code=$?

echo "Resultados E2E en framework/frontend/app/cypress/results"

exit $exit_code
