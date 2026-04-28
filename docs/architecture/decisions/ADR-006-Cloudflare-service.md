# ADR-005 — Cloudflare Deployment for Production
# ─────────────────────────────────────────────

## Status
status:        accepted
date:          2026-04-28
superseded_by: —

## Context
La aplicación está actualmente desplegada en un entorno local con Docker Compose, lo cual es suficiente para el desarrollo y pruebas. Sin embargo, para el despliegue en producción, se requiere una infraestructura escalable, segura y con alta disponibilidad.

Cloudflare se propone como una solución para manejar el tráfico web, mejorar la seguridad, optimizar el rendimiento y ofrecer una capa de protección contra amenazas externas. Además, Cloudflare permite el uso de DNS, SSL/TLS, y otras funcionalidades esenciales para un despliegue en producción.

## Decision
Se implementará Cloudflare como proveedor de servicios para el despliegue en producción. Cloudflare se configurará para manejar el tráfico web, gestionar el DNS, proporcionar SSL/TLS, y ofrecer protección contra amenazas. No se considerará el uso de Cloudflare en entornos de desarrollo o pruebas.

## Consequences
positive:
  - Mejora en la seguridad del servicio gracias a las funciones de protección de Cloudflare.
  - Mejora en el rendimiento del servicio mediante la optimización de la red.
  - Escalabilidad del servicio para soportar un mayor número de usuarios.
  - Facilidad de gestión del DNS y certificados SSL/TLS.

negative:
  - Coste adicional asociado al uso de Cloudflare en producción.
  - Requiere configuración adicional y gestión de recursos en la infraestructura de Cloudflare.
  - Dependencia de un servicio externo, lo que puede afectar la disponibilidad si no se gestiona adecuadamente.

neutral:
  - La implementación de Cloudflare no afectará directamente la lógica de negocio de la aplicación, sino solo la capa de infraestructura y red.

## Alternatives considered
alternative: Despliegue sin Cloudflare
reason_rejected: No proporciona protección, optimización de red ni gestión de DNS/SSL, lo cual es crítico para un despliegue en producción.

alternative: Usar otro proveedor de CDN
reason_rejected: Cloudflare ofrece un conjunto completo de herramientas y es más integrado con la infraestructura actual.

alternative: Usar Cloudflare en desarrollo
reason_rejected: No es necesario en entornos de desarrollo, y podría introducir dependencias innecesarias.

## References
- ADR-004: TTS Service: Coqui TTS on-premise
- FEAT-001: Límite de 300 chars en `content_text`
- FEAT-002: `agent_name` sanitizado
- FEAT-003: Presets de prosodia por edad
- FEAT-004, FEAT-005, FEAT-006: Contenido de catálogo finito y curado