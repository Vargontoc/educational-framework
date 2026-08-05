# agent-educational-parent

Base local de Ollama para un asistente conversacional destinado exclusivamente a personas adultas responsables. Usa `qwen2.5:7b-instruct-q5_K_M` como modelo base y mantiene un tono calmado, sin diagnosticos ni evaluaciones del menor.

Este directorio no define una API, contrato de entrada o salida, endpoint, historial, persistencia, herramientas ni acceso a datos de actividad. Los mensajes de prueba son sinteticos y no incluyen datos personales ni datos reales de menores.

## Modelo y carga bajo demanda

El modelo base es `qwen2.5:7b-instruct-q5_K_M`, seleccionado para permitir concurrencia con npc-game (`qwen2.5:7b-instruct-q5_K_M`) en GPU de 12 GB VRAM. El modelo se carga bajo demanda cuando un padre realiza una consulta, con una latencia aceptable de aproximadamente 15-20 segundos. Esta latencia es asumible dado que el chatbot parental no requiere respuesta en tiempo real.

## Requisitos

- Una instancia externa de Ollama accesible por HTTP. Por defecto se usa `http://127.0.0.1:11434`; puede cambiarse con `OLLAMA_HOST` o `-OllamaHost`.
- PowerShell para ejecutar los scripts.
- Conectividad suficiente para descargar `qwen2.5:7b-instruct-q5_K_M ` si todavia no esta disponible en la instancia.

No se requiere el ejecutable `ollama` en el `PATH` del host. Los scripts usan exclusivamente la API HTTP de Ollama. Ollama es infraestructura externa a este repositorio y no se gestiona mediante el `docker-compose.yml` de la aplicacion.

## Carga Reproducible

Desde este directorio, cree o actualice el modelo:

```powershell
.\load-ollama.ps1
```

Para otra instancia compatible:

```powershell
.\load-ollama.ps1 -OllamaHost http://servidor-ollama:11434
```

El script consulta `/api/tags`, descarga el modelo base mediante `/api/pull` si no esta disponible y crea o actualiza `agent-educational-parent` mediante `/api/create`.

## Smoke Test Manual

Tras cargar el modelo, ejecute:

```powershell
.\smoke-test.ps1
```

La prueba usa `/api/generate` con seis consultas sinteticas que verifican: respuesta general sobre la app, derivacion profesional ante consulta de salud, frase exacta de rechazo ante peticion excluida, y no solicitud de PII. El timeout se ajusta a 30 segundos para reflejar la latencia de carga bajo demanda. No valida una integracion futura ni establece un contrato de mensajes.

## Limites De Contenido

Segun FEAT-003, el agente aplica los siguientes guardrails:

- El agente atiende solo a personas adultas responsables y usa lenguaje claro, cercano y respetuoso.
- Solo responde sobre My Friend Nubi: funcionamiento, panel parental, progreso orientativo, perfiles infantiles, actividades y contenidos aprobados.
- Basa sus respuestas en el corpus oficial inyectado via RAG (`corpus_context`), no en conocimiento general. Si el corpus no contiene la informacion, lo indica claramente.
- Distingue hechos aportados de sintesis orientativas. Nunca presenta inferencias como hechos.
- Longitud maxima de respuesta: 1500 caracteres, compatible con `num_predict 512` y con TTS.
- Ante consultas de salud, desarrollo, conducta preocupante, diagnosticos o terapia, deriva siempre a un profesional cualificado (pediatra, psicologo infantil u orientador educativo).
- Ante peticiones excluidas (politica, moral, programacion, codigo, tareas ajenas a Nubi, etc.), incluye EXACTAMENTE la frase: "No puedo hacer lo que me solicitas".
- No solicita ni repite datos personales, familiares ni sensibles (nombres completos, PIN, imagenes, datos sanitarios, datos de contacto, ni ningun PII).
- No diagnostica, evalua, clasifica ni presenta comparaciones, rankings, notas, niveles de capacidad ni diagnosticos del menor.
- No inventa informacion que no este en el corpus oficial.
- Estas instrucciones no sustituyen los futuros controles de autorizacion, sanitizacion y validacion de backend.

## Dependencia con RAG para corpus oficial

El agente recibe fragmentos del corpus oficial mediante RAG (embeddings + vector store), inyectados como `corpus_context` por el backend. El corpus oficial se almacenara en `docs/official-corpus/` y su alimentacion es responsabilidad de backend/infraestructura (sprint posterior). Mientras el corpus no este disponible, el agente indica que no dispone de la informacion solicitada.
