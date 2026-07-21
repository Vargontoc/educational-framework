[CmdletBinding()]
param(
    [string]$OllamaHost = $(if ($env:OLLAMA_HOST) { $env:OLLAMA_HOST } else { "http://127.0.0.1:11434" }),
    [string]$BaseModel = "qwen3:14b",
    [string]$ModelName = "agent-educational-parent"
)

$ErrorActionPreference = "Stop"
$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$modelFile = Join-Path $scriptDirectory "Modelfile"
$apiRoot = $OllamaHost.TrimEnd('/')

if (-not (Test-Path -LiteralPath $modelFile -PathType Leaf)) {
    throw "No se encontro Modelfile en $modelFile"
}

function Get-OllamaModels {
    try {
        return Invoke-RestMethod -Method Get -Uri "${apiRoot}/api/tags"
    }
    catch {
        throw "No se pudo consultar Ollama en ${apiRoot}: $($_.Exception.Message)"
    }
}

$models = Get-OllamaModels
$baseModelAvailable = $models.models | Where-Object { $_.name -eq $BaseModel }
if ($null -eq $baseModelAvailable) {
    "Descargando el modelo base '$BaseModel' en ${apiRoot}..."
    try {
        $pullResponse = Invoke-RestMethod -Method Post -Uri "${apiRoot}/api/pull" -ContentType "application/json" -Body (@{
            name = $BaseModel
            stream = $false
        } | ConvertTo-Json)
    }
    catch {
        throw "No se pudo descargar el modelo base '$BaseModel' en ${apiRoot}: $($_.Exception.Message)"
    }

    if ($pullResponse.status -ne "success") {
        throw "La descarga del modelo base '$BaseModel' no termino correctamente. Estado recibido: $($pullResponse.status)"
    }
}

$modelfileContent = Get-Content -LiteralPath $modelFile -Raw
if ($modelfileContent -notmatch "(?m)^FROM\s+$([regex]::Escape($BaseModel))$") {
    throw "El Modelfile no usa el modelo base esperado '$BaseModel'. Actualice ambos antes de continuar."
}

$systemPrompt = (($modelfileContent -split '(?s)SYSTEM """', 2)[1] -replace '"""\s*$', '').Trim()
if ([string]::IsNullOrWhiteSpace($systemPrompt)) {
    throw "El Modelfile no contiene un prompt de sistema valido."
}

try {
    $createBody = (@{
        name = $ModelName
        from = $BaseModel
        system = $systemPrompt
        parameters = @{
            temperature = 0.3
            top_p = 0.8
            top_k = 20
            repeat_penalty = 1.1
            num_predict = 320
        }
        stream = $false
    } | ConvertTo-Json -Compress)
    $createResponse = Invoke-RestMethod -Method Post -Uri "${apiRoot}/api/create" -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($createBody))
}
catch {
    $details = ""
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $details = $reader.ReadToEnd()
    }
    throw "Ollama no pudo crear o actualizar '$ModelName' en ${apiRoot}: $($_.Exception.Message) $details"
}

if ($createResponse.status -ne "success") {
    throw "Ollama no pudo crear o actualizar '$ModelName'. Estado recibido: $($createResponse.status)"
}

"Modelo '$ModelName' creado o actualizado en ${apiRoot}."
