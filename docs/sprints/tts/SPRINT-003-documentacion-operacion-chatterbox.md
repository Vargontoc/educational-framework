# Sprint 003 - TTS

## Goal
Crear la documentación técnica y preparar los handoffs operativos para el servicio TTS nuevo con Chatterbox como único proveedor.

## Status
status: blocked
started_at:
closed_at:
blocked_by: docs/sprints/tts/SPRINT-002-pruebas-contrato-errores.md
waiting_for: Validación de pruebas y contrato con Chatterbox único.

## Tasks
- [ ] Crear la documentación de `framework/tts` para describir Chatterbox, sus perfiles de voz y el formato de salida.
- [ ] Actualizar README raíz y documentación relacionada para declarar Chatterbox como único proveedor y ausencia de fallback automático.
- [ ] Documentar las variables de entorno requeridas para TTS y Chatterbox único.
- [ ] Documentar la diferencia entre ejecución local de TTS en host (`127.0.0.1:4123`) y ejecución de TTS en Docker con red independiente.
- [ ] Registrar el handoff a infraestructura: debe proporcionar una URL/ruta alcanzable desde la red TTS hacia Chatterbox; TTS no define redes Docker externas.
- [ ] Documentar que backend valida texto/locales y que frontend/backend mantienen continuidad cuando no hay audio.
- [ ] Verificar que la documentación no presenta selección de proveedor como opción para familias o menores.

## Risks
- Documentar `127.0.0.1` sin contexto puede inducir configuraciones fallidas en Docker.
- Puede quedar una dependencia operativa de Coqui fuera del repositorio que requiera confirmación de infraestructura.

## Dependencies
- Sprint 001 y Sprint 002 completados.
- `docs/product/features/tts/FEAT-001-Chatterbox-unico-proveedor-TTS.md`.
- `docs/product/decisions/ADR-013-Chatterbox-unico-proveedor-TTS.md`.
- Infraestructura debe confirmar el mecanismo de acceso desde la red TTS al contenedor Chatterbox.

## Agent Instruction
- Actualizar únicamente documentación relacionada con el alcance de TTS y sus handoffs.
- No tomar decisiones de red, Docker Compose o infraestructura; registrar dependencias y responsables.
- Mantener la trazabilidad de ADR-013 y FEAT-001 al documentar Chatterbox como proveedor único.
- No prometer fallback automático de proveedor ni ocultar que el audio puede no estar disponible.
- Expresar que el juego y lectura deben continuar sin audio como dependencia de backend/frontend, no como responsabilidad del servicio TTS.
- Actualizar Review con enlaces, documentos revisados y bloqueos que sigan abiertos.

## Notes
- Chatterbox único reduce complejidad para una aplicación monofamiliar, pero no elimina la necesidad de conectividad entre redes Docker.
- La configuración de proveedor debe ser externa al código y apta para host o contenedor según el entorno.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
