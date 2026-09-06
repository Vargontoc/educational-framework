#!/usr/bin/env bash
# Limpieza final del stack E2E: para y elimina contenedores, red y volúmenes
# (db es tmpfs efímero). Llamar una vez, al terminar la sesión de revisión.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

docker compose -f docker-compose.e2e.yml down -v --remove-orphans

# Auto-purga de la caché de BuildKit (id=educational-framework-*): solo elimina
# cache mounts de Maven/npm sin uso en 14 días, para que altas/bajas de
# dependencias con el tiempo no hagan crecer la caché sin límite. No toca la
# caché de capas normal, y respeta la de builds recientes/activos.
# Nota: el builder de BuildKit se comparte con otros proyectos de esta máquina;
# el filtro type=exec.cachemount limita el alcance a este tipo de caché.
docker buildx prune --force --filter type=exec.cachemount --filter unused-for=336h >/dev/null 2>&1 || true
