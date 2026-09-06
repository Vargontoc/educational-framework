# Limpieza final del stack E2E: para y elimina contenedores, red y volumenes
# (db es tmpfs efimero). Llamar una vez, al terminar la sesion de revision.
$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

docker compose -f docker-compose.e2e.yml down -v --remove-orphans

# Auto-purga de la cache de BuildKit (id=educational-framework-*): solo elimina
# cache mounts de Maven/npm sin uso en 14 dias, para que altas/bajas de
# dependencias con el tiempo no hagan crecer la cache sin limite. No toca la
# cache de capas normal, y respeta la de builds recientes/activos.
# Nota: el builder de BuildKit se comparte con otros proyectos de esta maquina;
# el filtro type=exec.cachemount limita el alcance a este tipo de cache.
try {
    docker buildx prune --force --filter type=exec.cachemount --filter unused-for=336h *> $null
} catch {}
