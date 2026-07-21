# Sprint 003 - Frontend

## Goal
Definir la integración del mundo y minijuegos Phaser con Vue y backend, junto con la estrategia de pruebas de experiencia Android para niños de 3-4 años.

## Status
status: blocked
started_at:
closed_at:
blocked_by: docs/product/sprints/frontend/SPRINT-002-familia-perfiles-privacidad.md
waiting_for: Cierre de rutas, sesión, perfiles y contratos base de familia del Sprint 002.

## Tasks
- [ ] Definir la frontera Vue-Phaser para mapa/mundo interactivo y minijuegos.
- [ ] Especificar el contrato de eventos semánticos de interacción hacia el cliente Vue y backend, sin acoplar Phaser a endpoints ni sesión.
- [ ] Definir el ciclo de actividad infantil: entrada desde perfil, mapa, acceso a minijuego, pausa, salida y retorno.
- [ ] Definir preservación y reanudación amable ante segundo plano, pérdida/recuperación de red y cambio de orientación física.
- [ ] Especificar el escalado del mundo horizontal en viewport vertical, con relación de aspecto estable y objetivos táctiles accesibles.
- [ ] Definir la degradación de interfaz cuando backend, audio, TTS o NPC no estén disponibles; el juego debe permanecer utilizable sin audio/NPC.
- [ ] Trazar consumo frontend exclusivo de backend frente a `docs/contracts/api/openapi.json`, `docs/contracts/api/websocket.json`, `docs/contracts/schemas/motivation_action.schema.json` y `docs/contracts/agents/education-framework-agent-child.json`.
- [ ] Elaborar estrategia de pruebas: unidad, componentes, E2E y validación manual en Chrome Android/PWA opcional.
- [ ] Preparar matriz de pruebas en Samsung Galaxy A15 físico y emuladores Samsung Galaxy Tab S4, Pixel 8 y Samsung Galaxy S20, en horizontal y vertical física.

## Risks
- El mundo interactivo puede exigir demasiada precisión, lectura o memoria para niños de 3-4 años.
- La rotación física a vertical puede hacer los controles demasiado pequeños pese a mantener renderizado horizontal.
- Fallos de red, agentes o TTS pueden interrumpir indebidamente una actividad si no se diseñan estados degradados.
- Los contratos actuales pueden no cubrir mapa, minijuegos, tracking o eventos de motivación con el detalle que requiere la interfaz.
- La emulación no reproduce rendimiento, barras de navegador, gestos ni comportamiento táctil real del Galaxy A15.

## Dependencies
- Resultado de Sprint 001 y Sprint 002.
- `docs/contracts/api/openapi.json` para contenido, sesión, juego, tracking y configuración.
- `docs/contracts/api/websocket.json` solo si se confirma valor funcional de tiempo real.
- `docs/contracts/schemas/motivation_action.schema.json` y `docs/contracts/agents/education-framework-agent-child.json` para eventos seguros de motivación recibidos desde backend.
- Backend debe intermediar cualquier resultado de agents/TTS y exponer fallbacks normalizados; frontend no consume esos servicios directamente.
- TTS debe poder fallar sin impedir la actividad; el detalle de generación y fallback es responsabilidad de backend/TTS.

## Agent Instruction
- No implementar código; especificar arquitectura de integración y plan de pruebas frontend.
- Phaser se limita a renderizado/escenas e interacción infantil; Vue controla rutas, estado global, sesión, datos remotos y accesos parentales.
- Mantener renderizado horizontal sin mensajes de giro; no deformar escenas y no bloquear sensores de movimiento/orientación.
- Validar requisitos para 3-4 años: objetivos táctiles amplios, interacciones cortas y repetibles, sin lectura obligatoria, prisa, castigos ni comparaciones.
- Ante error, desconexión, ausencia de audio o NPC, usar continuidad amable y no mostrar detalle técnico al menor.
- Documentar endpoints/contratos consumidos, dependencias de backend/agents/tts y handoffs de integración.

## Notes
- El mundo incluye mapa interactuable y minijuegos, por lo que Phaser ha sido aceptado como motor encapsulado sujeto a la especificación de este sprint.
- El frontend renderiza audio/animaciones que entregue backend; no llama a TTS ni agents.
- La PWA es opcional y el uso por URL debe seguir siendo completo.
- El evento de cumpleaños queda fuera del alcance hasta que producto lo priorice y exista un contrato aprobado.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
