# Construye (build incremental, cacheado por capas de Docker) y levanta db+api+app
# del stack E2E, esperando a que pasen sus healthchecks. Pensado para llamarse
# repetidas veces durante una sesion de revision: solo reconstruye lo que haya
# cambiado en el codigo de api/app; si nada cambio, es practicamente inmediato.
# No arranca Cypress (ver scripts/e2e-test.ps1) ni destruye nada existente.
$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

docker compose -f docker-compose.e2e.yml up -d --build --wait db api app

Write-Host 'Stack E2E arriba: app y api ya healthy.'
Write-Host 'Ejecuta scripts/e2e-test.ps1 para correr Cypress, o scripts/e2e-down.ps1 para limpiar.'
