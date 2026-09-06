#!/usr/bin/env bash
# Construye (build incremental, cacheado por capas de Docker) y levanta db+api+app
# del stack E2E, esperando a que pasen sus healthchecks. Pensado para llamarse
# repetidas veces durante una sesión de revisión: solo reconstruye lo que haya
# cambiado en el código de api/app; si nada cambió, es prácticamente inmediato.
# No arranca Cypress (ver scripts/e2e-test.sh) ni destruye nada existente.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

docker compose -f docker-compose.e2e.yml up -d --build --wait db api app

echo "Stack E2E arriba: app en \${E2E_APP_HOST_PORT:-8880}, api en \${E2E_API_HOST_PORT:-18080}"
echo "Ejecuta scripts/e2e-test.sh para correr Cypress, o scripts/e2e-down.sh para limpiar."
