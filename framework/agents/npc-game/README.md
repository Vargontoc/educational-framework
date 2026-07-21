# npc-game

Base local de Ollama para Nubi, acompanante breve de situaciones de juego para ninos y ninas de 3-4 anos. Usa `qwen2.5:7b-instruct-q5_K_M` como modelo base.

Este directorio valida de forma aislada el contrato JSON v1 definido en `docs/contracts/schemas/`. No define un endpoint ni integra el agente con la aplicacion. En la futura integracion, backend debe validar la entrada antes de invocar Ollama y la salida despues de recibirla.

## Contrato JSON v1

- Entrada: `npc-game-event.v1.yaml`.
- Respuesta correcta: `npc-game-response.v1.yaml`.
- Error controlado: `npc-game-error.v1.yaml`.
- Eventos admitidos: `game_entered`, `world_discovery`, `minigame_started`, `minigame_attempt`, `minigame_hint` y `game_exited`.
- `event_id`, si llega en la envoltura de entrada, es metadato de transporte. La prueba lo elimina antes de invocar al modelo y este nunca lo recibe ni lo devuelve. La futura capa backend construira la envoltura de transporte y asociara el identificador tras validar la solicitud.
- `child_name` es opcional y solo puede ser proporcionado por la aplicacion para el evento actual. No se admiten ciudad, edad, perfil, historial ni otros datos personales.
- `curiosity_question_allowed: true` permite como maximo una pregunta breve relacionada con `subject`. No habilita preguntas de datos personales, conversacion libre, presion ni evaluacion.

## Requisitos

- Una instancia externa de Ollama accesible por HTTP. Por defecto se usa `http://127.0.0.1:11434`; puede cambiarse con `OLLAMA_HOST` o con el parametro `-OllamaHost`.
- PowerShell para ejecutar los scripts.
- Conectividad suficiente para descargar `qwen2.5:7b-instruct-q5_K_M` si todavia no esta disponible en la instancia.

No se requiere que el ejecutable `ollama` este instalado ni disponible en el `PATH` del host. Ollama es infraestructura externa a este repositorio; no se gestiona mediante su `docker-compose.yml`.

## Carga reproducible

Desde este directorio, cree o actualice el modelo local:

```powershell
.\load-ollama.ps1
```

Para una instancia de Ollama distinta:

```powershell
.\load-ollama.ps1 -OllamaHost http://servidor-ollama:11434
```

El script consulta `/api/tags`, descarga el modelo base mediante `/api/pull` cuando falta y crea o actualiza `npc-game` mediante `/api/create`. La descarga se muestra en consola y el script falla si Ollama no informa un resultado correcto.

## Prueba Manual HTTP

Tras cargar el modelo, ejecute:

```powershell
.\smoke-test.ps1
```

La prueba usa exclusivamente `POST /api/generate` con `format: "json"`. Comprueba un evento valido, JSON invalido, evento desconocido, campo personal no permitido, instrucciones embebidas, nombre permitido y curiosidad permitida. El validador local comprueba estrictamente JSON, campos obligatorios, tipos, `additionalProperties: false` y combinaciones permitidas de los contratos semanticos; tambien prueba que rechaza propiedades adicionales, campos incompatibles y tipos incorrectos.

## Limites De Contenido

- Nubi responde solo a situaciones concretas de juego; no es un chat libre.
- Las respuestas deben ser breves, tranquilas, juguetonas y sin presion.
- La unica informacion personal permitida es `child_name`, aportada por la aplicacion para el evento actual.
- Nubi no evalua, diagnostica ni interpreta al menor o sus intentos.
- Esta configuracion reduce riesgos de contenido, pero la validacion y el filtrado de la futura integracion siguen siendo necesarios.
