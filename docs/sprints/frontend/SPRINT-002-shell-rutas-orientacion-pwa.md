# Sprint 002 - Frontend

## Goal
Implementar shell de aplicación con navegación, renderizado horizontal permanente y estrategia PWA.

## Status
status: blocked
started_at:
closed_at:
blocked_by: docs/sprints/frontend/SPRINT-001-scaffold-configuracion-inicial.md
waiting_for: Cierre de scaffold y configuración base del Sprint 001.

## Tasks
- [ ] Implementar cliente API base (fetch/axios) con configuración de base URL desde variables de entorno
- [ ] Implementar cliente WebSocket base con reconexión exponencial
- [ ] Implementar guards de navegación con `router.replace()` y validación de sesión/PIN
- [ ] Implementar renderizado horizontal permanente (escalado proporcional en orientación vertical física)
- [ ] Implementar preservación de estado ante giro, segundo plano y retorno
- [ ] Configurar PWA opcional (instalación solo para adultos, no promocionada en flujo infantil)
- [ ] Definir matriz de compatibilidad: Android 16, Chrome 143.0.7499.193, Galaxy A15 físico, Tab S4/Pixel 8/S20 emulados
- [ ] Documentar estrategia de pruebas de orientación y PWA

## Acceptance Criteria
- Cliente API funcional con variables de entorno
- Cliente WebSocket funcional con reconexión automática
- Guards de navegación operativos (redirección a `/` si no hay sesión/PIN)
- Renderizado horizontal en orientación física vertical sin mensaje de giro
- Estado preservado ante giro y segundo plano
- PWA instalable opcionalmente desde menú del navegador
- Matriz de compatibilidad documentada y verificada

## Risks
- La aplicación web no puede garantizar el bloqueo de orientación o de controles del sistema en todos los dispositivos Android.
- El escalado del lienzo horizontal en un móvil sostenido en vertical puede reducir los objetivos táctiles por debajo de un umbral aceptable para 3-4 años.
- Android 16 y Chrome 143 son una base reciente de piloto y no acreditan compatibilidad con dispositivos más antiguos.
- La URL HTTPS final de infraestructura es necesaria para validar PWA y comportamiento de caché en entorno de entrega.

## Dependencies
- Resultado de SPRINT-001
- `docs/contracts/api/openapi.json` y `docs/contracts/api/websocket.json` para mapeo de endpoints
- URL HTTPS final de infraestructura para validar PWA y caché

## Agent Instruction
- No implementar código de negocio ni flujos de familia/perfiles; solo shell técnico.
- Mantener Android/Chrome como único alcance; excluir explícitamente iOS/iPadOS y APK de esta versión.
- La experiencia debe renderizarse en composición horizontal incluso con orientación física vertical; no mostrar indicaciones para girar el dispositivo.
- No bloquear ni requerir sensores de movimiento u orientación, para conservar posibles minijuegos futuros.
- La PWA es opcional, no debe interferir con el acceso por URL ni promocionarse dentro del flujo infantil.
- Documentar endpoints/contratos consumidos, dependencias de backend/agents/tts y handoffs de integración.

## Notes
- Primera entrega para una única familia por URL; la aplicación será funcional, no una demo.
- Se probará obligatoriamente en Samsung Galaxy A15 físico. Los perfiles emulados complementan, no sustituyen, las pruebas reales.
- La documentación es una ruta pública del mismo frontend.
- El frontend consumirá exclusivamente el backend; nunca TTS ni agents de forma directa.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
