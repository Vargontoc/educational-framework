# Orquesta el stack de pruebas E2E (Cypress) definido en docker-compose.e2e.yml:
# construye las imagenes, levanta app+api+db, ejecuta Cypress, y al terminar
# (pase o falle) elimina los contenedores. Los resultados (junit, videos,
# screenshots) quedan en framework/frontend/app/cypress/results via bind mount.
$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$composeFile = 'docker-compose.e2e.yml'

try {
    docker compose -f $composeFile --profile test up --build --abort-on-container-exit --exit-code-from cypress
    $exitCode = $LASTEXITCODE
}
finally {
    docker compose -f $composeFile down -v --remove-orphans
    # Auto-purga de cache mounts de BuildKit (Maven/npm) sin uso en 14 dias, para
    # que altas/bajas de dependencias no hagan crecer la cache sin limite.
    try {
        docker buildx prune --force --filter type=exec.cachemount --filter unused-for=336h *> $null
    } catch {}
}

Write-Host "Resultados E2E en framework/frontend/app/cypress/results"

exit $exitCode
