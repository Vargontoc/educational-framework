# Sprint 001 - Frontend

## Goal
Definir la arquitectura de reconstrucción web/PWA Android-first, el soporte de orientación horizontal renderizada y la matriz de compatibilidad para la primera entrega por URL.

## Status
status: activve
started_at: 2026-07-21 00:00:00
closed_at:
blocked_by:
waiting_for:

## Tasks
- [ ] Auditar `framework/frontend/app` y documentar el punto de partida, dependencias y elementos reutilizables.
- [ ] Formalizar el stack objetivo: Vue 3, TypeScript, Vite, Vue Router, Pinia, Phaser encapsulado y PWA opcional.
- [ ] Definir la separación de responsabilidades entre shell Vue, escenas Phaser, cliente API y reproducción de audio entregado por backend.
- [ ] Definir el mapa de rutas: inicio, registro familiar, documentación pública, experiencia infantil y panel parental.
- [ ] Especificar la renderización horizontal permanente, incluido escalado proporcional en orientación física vertical, sin mensaje de giro y sin deformar escenas.
- [ ] Definir la estrategia de preservación de estado ante giro, segundo plano y retorno a la aplicación.
- [ ] Documentar la entrega inicial por URL HTTPS y la instalación PWA como opción exclusiva para adultos.
- [ ] Formalizar la matriz de soporte y pruebas: Android 16, Chrome 143.0.7499.193, Samsung Galaxy A15 físico, Samsung Galaxy Tab S4, Pixel 8 y Samsung Galaxy S20 emulados.

## Risks
- La aplicación web no puede garantizar el bloqueo de orientación o de controles del sistema en todos los dispositivos Android.
- El escalado del lienzo horizontal en un móvil sostenido en vertical puede reducir los objetivos táctiles por debajo de un umbral aceptable para 3-4 años.
- Android 16 y Chrome 143 son una base reciente de piloto y no acreditan compatibilidad con dispositivos más antiguos.
- Phaser puede adquirir responsabilidades de sesión o navegación que corresponden al shell Vue si no se establece una frontera explícita.

## Dependencies
- `README.md` y `AGENTS.md` como restricciones de producto y separación entre experiencia infantil y controles parentales.
- Acceso de lectura a `framework/frontend/app` para la auditoría inicial.
- `docs/contracts/api/openapi.json` y `docs/contracts/api/websocket.json` para delimitar el cliente API, sin diseñar endpoints nuevos en este sprint.
- Infrastructure debe proporcionar URL HTTPS final antes de validar PWA y comportamiento de caché en entorno de entrega.

## Agent Instruction
- No implementar código durante este sprint; producir únicamente especificaciones y decisiones técnicas frontend pendientes de confirmación.
- Mantener Android/Chrome como único alcance; excluir explícitamente iOS/iPadOS y APK de esta versión.
- La experiencia debe renderizarse en composición horizontal incluso con orientación física vertical; no mostrar indicaciones para girar el dispositivo.
- No bloquear ni requerir sensores de movimiento u orientación, para conservar posibles minijuegos futuros.
- Diseñar para niños de 3-4 años: interacciones grandes, simples, sin presión temporal ni penalizaciones perceptibles.
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
