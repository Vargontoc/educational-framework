# Ejecuta Cypress contra el stack E2E ya arrancado (scripts/e2e-up.ps1). No
# construye ni recrea contenedores: pensado para repetirse muchas veces en una
# sesion de revision sin pagar el coste de un rebuild/restart cada vez.
# Resultados (junit, videos, capturas) en framework/frontend/app/cypress/results.
$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

docker compose -f docker-compose.e2e.yml --profile test run --rm cypress
$exitCode = $LASTEXITCODE

Write-Host 'Resultados E2E en framework/frontend/app/cypress/results'

exit $exitCode
