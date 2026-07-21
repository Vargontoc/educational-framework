# Sprint 003 - agents

## Goal

Preparar `npc-game` para aceptar exclusivamente eventos JSON de juego y devolver exclusivamente respuestas JSON semanticas o errores JSON semanticos, con validacion aislada contra Ollama externo y sin integracion con backend.

## Status

status: implemented
started_at: 2026-07-21
closed_at:
blocked_by:
waiting_for:

## Tasks

- [x] Crear los esquemas versionados de entrada, respuesta y error para `npc-game` en `docs/contracts/schemas/`.
- [x] Documentar los eventos iniciales permitidos y los campos admitidos, incluyendo el uso acotado del nombre proporcionado por la aplicacion para la situacion actual.
- [x] Actualizar el `Modelfile` de `npc-game` para aceptar solo JSON y devolver un unico objeto JSON sin texto exterior.
- [x] Definir respuestas de error JSON para entrada no JSON, evento no admitido, contexto invalido o instrucciones embebidas.
- [x] Permitir preguntas breves de curiosidad en mensajes de juego, sin permitir peticiones de datos personales, conversacion libre, presion ni evaluacion.
- [x] Actualizar el script de carga para recrear el modelo con las nuevas instrucciones.
- [x] Crear o actualizar una prueba manual por API con casos validos, JSON invalido, evento no admitido, campos no permitidos e instrucciones embebidas.
- [x] Separar la respuesta semantica del modelo de la futura envoltura de transporte de backend. El modelo no recibe ni devuelve `event_id`.
- [x] Validar que las respuestas semanticas de Ollama son JSON y cumplen estrictamente el esquema esperado, incluidos campos obligatorios, tipos, propiedades permitidas y combinaciones validas.
- [x] Documentar el contrato y la validacion local, dejando claro que backend validara entrada y salida de nuevo al integrarlo.
- [x] Registrar las tareas realizadas, comprobaciones ejecutadas y bloqueos de entorno en este sprint.

## Risks

- Los modelos generativos pueden devolver texto ajeno al JSON o ignorar instrucciones; la validacion de la prueba no sustituye la validacion obligatoria del futuro backend.
- Un LLM no garantiza conservar identificadores de correlacion; `event_id` no debe depender de la salida del modelo.
- El esquema inicial puede requerir evolucion cuando backend concrete los eventos de mundo y minijuegos.
- El uso permitido del nombre puede ampliarse accidentalmente a otros datos personales si el contrato no limita expresamente los campos.
- Una regla que prohiba cualquier pregunta bloquearia mensajes de curiosidad adecuados para el juego.

## Dependencies

- Ollama de infraestructura externa, contenedor `ollama`, accesible mediante `http://127.0.0.1:11434`.
- Modelo `npc-game` existente y su base `qwen2.5:7b-instruct-q5_K_M` disponibles en dicha instancia.
- La integracion real queda pendiente del flujo y esquemas definitivos de backend para mundo y minijuegos.

## Agent Instruction

- Implementar exclusivamente la validacion aislada de contrato JSON para `npc-game`.
- Modificar solo `framework/agents/npc-game/`, `docs/contracts/schemas/` y este sprint cuando sea necesario para el alcance.
- No modificar backend, frontend, TTS, Docker, WebSocket, persistencia, tracking, comandos, herramientas MCP ni controles parentales.
- No crear endpoint ni integrar el agente con una capa de aplicacion.
- Los contratos deben ser versionados y distinguir entrada valida, respuesta correcta y error controlado.
- La salida del modelo es exclusivamente semantica y no contiene `event_id`. La futura capa backend construira la envoltura de transporte con el identificador de una solicitud ya validada.
- Para JSON invalido, el modelo no recibe ni inventa un identificador. La futura capa backend podra asociar el fallo con su propio contexto de transporte.
- La entrada solo admite eventos de juego JSON. Debe rechazar texto libre, tipos de evento desconocidos, campos no permitidos e instrucciones embebidas.
- El nombre solo es admisible como campo opcional proporcionado por la aplicacion para el evento actual. No admitir ciudad, edad, perfil, historial ni otros datos personales.
- La respuesta correcta debe ser JSON semantico exclusivo y breve. Puede contener una pregunta breve de curiosidad dentro del juego, pero no preguntas de datos personales, conversacion libre, presion o evaluacion.
- La respuesta de error debe ser JSON exclusivo y no contener texto conversacional dirigido al menor.
- Usar por defecto `http://127.0.0.1:11434` y la API HTTP de Ollama, sin requerir el ejecutable `ollama` en PATH.
- Ejecutar la carga y las pruebas reales contra Ollama. Si falla el entorno, registrar el bloqueo y toda validacion estatica posible en Developer Evidence.
- Actualizar este sprint solo como implementado; no marcarlo como verificado.

## Notes

- Fuente funcional: `docs/product/features/agents/FEAT-002-NPC-Game-Domain-Agent.md`.
- El nombre del menor puede utilizarse solo si la aplicacion lo proporciona expresamente para la situacion de juego actual, por ejemplo en un saludo.
- Las preguntas de curiosidad son validas para Nubi, por ejemplo: "Mira ese cofre, que tendra dentro?".
- Backend sera responsable de validar el contrato antes y despues de invocar al agente cuando exista integracion; este sprint no implementa esa capa.
- La futura envoltura de backend conservara y emitira `event_id`; no forma parte de la salida semantica generada por el modelo en este sprint.
- Los eventos iniciales pueden ser provisionales para la validacion aislada, pero sus nombres y esquemas deben quedar documentados como contrato versionado.

## Developer Evidence

completed_tasks:
- Contratos YAML v1 creados: `npc-game-event.v1.yaml`, `npc-game-response.v1.yaml` y `npc-game-error.v1.yaml`.
- Documentados seis eventos iniciales, campos cerrados, uso puntual de `child_name` y curiosidad acotada a `subject`.
- Modelfile, carga HTTP y prueba manual actualizados para JSON exclusivo mediante Ollama HTTP.
- Modelo `npc-game` recreado contra `http://127.0.0.1:11434`.
- Separado `event_id` como metadato opcional de la envoltura de entrada; el contrato de respuesta y error es exclusivamente semantico.
- Prueba local endurecida para rechazar JSON no objeto, propiedades adicionales, campos incompatibles y tipos incorrectos.

executed_checks:
- `powershell -ExecutionPolicy Bypass -File .\load-ollama.ps1`: correcto; modelo creado o actualizado.
- `powershell -ExecutionPolicy Bypass -File .\load-ollama.ps1`: correcto; modelo `npc-game` recreado contra `http://127.0.0.1:11434`.
- `powershell -ExecutionPolicy Bypass -File .\smoke-test.ps1`: correcto; superados evento valido, JSON invalido, evento desconocido, campo PII no permitido, instruccion embebida, nombre permitido y curiosidad permitida. El validador tambien rechazo correctamente propiedad adicional, campo incompatible y tipo incorrecto.
- `git diff --check`: sin errores de espacios en los cambios del sprint; Git emitio avisos CRLF sobre archivos ajenos.

known_limitations:
- La validacion aislada no sustituye la validacion obligatoria de entrada y salida que hara backend al integrar el agente.

## Review

review_status: reopened

reviewed_at: 2026-07-21

findings:
- severity: high
  location: docs/contracts/schemas/npc-game-error.v1.yaml:16-18, framework/agents/npc-game/smoke-test.ps1:33-44
  issue: La respuesta real de Ollama para un evento desconocido no conserva `event_id`. El contrato de error lo declara opcional, pero el sprint exige correlacion cuando la entrada contiene un identificador valido.
  impact: La capa consumidora no puede asociar de forma segura el error a la solicitud de juego que lo origino.
- severity: medium
  location: framework/agents/npc-game/smoke-test.ps1:27-38
  issue: La prueba no valida exhaustivamente la forma de salida frente a los esquemas. Permite propiedades adicionales y combinaciones de campos que los contratos excluyen.
  impact: Una respuesta aparentemente valida puede incumplir el contrato antes de que exista el validador de backend.

analyst_request:
- Proponer la solucion tecnica para garantizar la correlacion de `event_id` tanto en respuestas correctas como de error, teniendo en cuenta que un LLM no garantiza conservar campos bajo instrucciones de prompt.
- Determinar si `event_id` debe ser obligatorio en el esquema de error cuando la entrada estructurada incluye uno y como representar errores de JSON invalido, donde no puede extraerse un identificador fiable.
- Proponer una validacion local estricta y reutilizable de los esquemas v1 que rechace propiedades adicionales, campos inesperados, tipos incorrectos y combinaciones no admitidas.
- Mantener el alcance sin backend ni integracion de aplicacion; backend seguira siendo responsable de la validacion obligatoria antes y despues de invocar al agente cuando exista integracion.

resolution_approved:
- La salida de `npc-game` se separa en respuesta semantica del modelo y envoltura de transporte futura.
- El modelo no recibe ni devuelve `event_id`.
- Backend agregara y validara `event_id` al construir la envoltura de transporte desde una solicitud ya validada.
- La validacion aislada de este sprint comprueba solo el contrato semantico del modelo.

retest_required:
- Evento valido devuelve una respuesta semantica que cumple el esquema de respuesta.
- Evento desconocido devuelve un error semantico que cumple el esquema de error.
- JSON invalido devuelve un error semantico sin atribuir un `event_id` no verificable.
- Las respuestas con propiedades adicionales, campos incompatibles o tipos incorrectos son rechazadas por la prueba local.
