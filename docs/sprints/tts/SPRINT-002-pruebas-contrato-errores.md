# Sprint 002 - TTS

## Goal
Verificar que el servicio con Chatterbox único cumple el contrato de síntesis, errores y estado sin ejecutar fallback a proveedores alternativos.

## Status
status: blocked
started_at:
closed_at:
blocked_by: docs/sprints/tts/SPRINT-001-chatterbox-unico-docker.md
waiting_for: Simplificación a Chatterbox único y disponibilidad de una imagen TTS construible.

## Tasks
- [ ] Crear las pruebas de adaptador, configuración, tonos, conversión y API para el único proveedor Chatterbox.
- [ ] Verificar síntesis correcta para perfiles `npc` y `storyteller`.
- [ ] Verificar tonos aceptados y error contractual para tono no admitido.
- [ ] Verificar indisponibilidad de proveedor, timeout, respuesta inválida, respuesta vacía y error de conversión.
- [ ] Verificar que el endpoint de estado informa Chatterbox como único proveedor.
- [ ] Verificar que las respuestas de éxito continúan siendo `audio/mpeg` y los errores cumplen `docs/contracts/api/openapi_tts.json`.
- [ ] Verificar que una URL de Chatterbox no alcanzable produce error contractual de proveedor no disponible y no activa un segundo proveedor.
- [ ] Ejecutar pruebas automatizadas y construir la imagen Docker TTS.

## Risks
- El contrato puede contener enumeraciones históricas de proveedor que no reflejen la decisión aceptada.
- La prueba de síntesis real desde Docker seguirá bloqueada si infraestructura no entrega una ruta alcanzable a Chatterbox.

## Dependencies
- Sprint 001 completado.
- `docs/contracts/api/openapi_tts.json`.
- Infraestructura debe proporcionar conectividad o URL alcanzable desde la red TTS para una validación end-to-end en contenedor.

## Agent Instruction
- Implementar y ejecutar pruebas exclusivamente para la capa TTS.
- Usar dobles/mocks para pruebas unitarias; no depender de una GPU o proveedor real salvo en validación de integración explícita.
- No introducir XTTS, Coqui ni fallback al crear las pruebas.
- Si el contrato declara información incompatible con Chatterbox único, documentar el hallazgo y escalarlo; no modificar el contrato unilateralmente.
- Registrar comandos, resultados, incidencias y evidencia de build en Review.

## Notes
- La ausencia de audio se comunica mediante errores contractuales; backend y frontend son responsables de la continuidad de experiencia.
- El valor `127.0.0.1:4123` no valida conectividad desde el contenedor TTS.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
