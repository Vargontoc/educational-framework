# agent-educational-parent

Base local de Ollama para un asistente conversacional destinado exclusivamente a personas adultas responsables. Usa `qwen3:14b` como modelo base y mantiene un tono calmado, sin diagnosticos ni evaluaciones del menor.

Este directorio no define una API, contrato de entrada o salida, endpoint, historial, persistencia, herramientas ni acceso a datos de actividad. Los mensajes de prueba son sinteticos y no incluyen datos personales ni datos reales de menores.

## Requisitos

- Una instancia externa de Ollama accesible por HTTP. Por defecto se usa `http://127.0.0.1:11434`; puede cambiarse con `OLLAMA_HOST` o `-OllamaHost`.
- PowerShell para ejecutar los scripts.
- Conectividad suficiente para descargar `qwen3:14b` si todavia no esta disponible en la instancia.

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

La prueba usa `/api/generate` con una consulta general sintetica y una consulta sintetica de salud, desactivando el razonamiento visible de Qwen3 solo para la comprobacion. Comprueba que hay respuesta y que la segunda recomienda apoyo profesional. No valida una integracion futura ni establece un contrato de mensajes.

## Limites De Contenido

- El agente atiende solo a personas adultas responsables y usa lenguaje calmado.
- Distingue hechos aportados de explicaciones generales y no inventa informacion sobre un menor.
- No solicita ni repite datos personales, familiares o sensibles.
- No diagnostica, evalua, clasifica ni interpreta capacidades, conducta, emociones, aprendizaje, salud o progreso.
- Ante salud, psicologia, educacion profesional, legal o seguridad, declara el limite y recomienda apoyo profesional o servicios locales de emergencia si existe peligro inmediato.
- Estas instrucciones no sustituyen los futuros controles de autorizacion, sanitizacion y validacion de backend.
