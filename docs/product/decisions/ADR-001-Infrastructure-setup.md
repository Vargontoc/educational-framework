# ADR-001 - Infrastructure Initial Setup

## Status

status: proposed
date: 2026-04-21 12:00

## Context

El proyecto es una aplicación web monousuario accesible por URL pública. La aplicación incluye dos agentes de dominio que dependen de un modelo local (Ollama) y debe persistir eventos de jugador para tracking y logros. Se requiere un entorno reproducible para desarrollo y una configuración segura y escalable para producción.

## Decision 

Usar contenedores (Docker + docker-compose) para encapsular servicios en desarrollo y definir un plan de despliegue para producción que combine contenedores y servicios gestionados según necesidad. Servicios obligatorios:

- Ollama: obligatorio. En desarrollo se levantará como contenedor en la máquina del desarrollador (host local). En producción se evaluará Ollama en servidor dedicado o servicio gestionado, con acceso restringido.
- PostgreSQL: obligatorio para persistir eventos del jugador y tracking (volúmenes persistentes y política de backups).
- Cloudflare: sólo en producción para TLS, CDN y protección de capa web.

Servicios opcionales:
- Redis: opcional; añadir solo si se necesita caching, pub/sub o coordinación entre agentes.

## Rationale

Docker garantiza reproducibilidad del entorno de desarrollo y facilita la entrega.
PostgreSQL es necesario para auditoría y tracking de eventos; su persistencia y backups son críticos.
Ollama es obligatorio para la lógica de los agentes; correrlo localmente en desarrollo reduce latencia y permite testing, pero exige medidas de seguridad.

## Deployment & Network

- Desarrollo: docker-compose con servicios para la app, Ollama y Postgres; usar volúmenes para persistencia. Ollama debe quedar accesible solo desde la red de Docker o localhost (no exponer públicamente).
- Producción: exponer únicamente el servicio de la aplicación a través de un reverse-proxy (Nginx/Caddy) con TLS; Cloudflare delante (si se usa). No exponer Ollama directamente en Internet.
- Conexiones entre app y Ollama: preferir comunicación interna (Docker network o localhost socket). Documentar host/puerto que la app usará para comunicarse con Ollama.

## Availability & UX (tolerancia a fallos)

- Ollama puede estar no disponible; la app debe manejarlo con degradación elegible:
- Mostrar al usuario un estado claro de agente: "Agente dormido" / "Temporalmente no disponible".
- Registrar el intento de llamada y el motivo en PostgreSQL (evento de error) para tracking y diagnósticos.
- Implementar reintentos con backoff configurable y un máximo razonable.
- Ofrecer operaciones no dependientes del LLM mientras el agente está dormido (flujo degradado).
- Documentar el comportamiento esperado en `docs/contracts` y UI.


## Backup & Persistence

- PostgreSQL: volumen persistente + backups automáticos (diarios al menos) con política de retención (ej. 30 días) y procedimiento de restauración documentado.
- Export/archivos sensibles: no almacenar secretos en repositorio; usar vault/secret manager o variables de entorno en CI/CD.

## Security

- No exponer Ollama a la red pública.
- Todo acceso público debe usar TLS.
- API que interactúa con agentes debe tener autenticación mínima (token/API key).
- Gestionar secretos con un gestor de secretos; evitar .env en repositorio.
- Firewalls/ACLs que restrinjan acceso al host de Ollama en producción.

## Resources & Requierements

- Especificar requisitos mínimos para Ollama (CPU, RAM, VRAM/disco) en la documentación de despliegue.
- Documentar requisitos mínimos para la máquina del desarrollador si Ollama se ejecuta localmente.

## Options

- Opción A (recomendada): Docker + Ollama en contenedor local (dev) + PostgreSQL en contenedor (dev) + Postgres gestionado en producción; Cloudflare frente a la app; Redis solo si se necesita.
- Opción B (simple/prototipo): Reemplazar Postgres por SQLite temporalmente para pruebas rápidas (NO recomendado para producción por pérdida de concurrencia y backups).
- Opción C (hardened prod): Orquestador (k8s) para producción, Ollama en servidor dedicado o servicio gestionado, Postgres clonada/replicada con backups y HA.

## Impact

- Backend: conexión y configuración obligatoria contra Ollama; persistencia de eventos en Postgres; despliegue/secret management.
- Frontend: consumo de la API pública expuesta por backend; no debe comunicarse directamente con Ollama.
- docs/contracts: actualizar endpoints, autenticación y contratos si cambia la URL base o se añaden rutas para tracking.

## Risks

- Seguridad: Ollama expuesto por error puede filtrar datos o abrir vectores de ataque.
- Disponibilidad: depender de Ollama local puede producir bloqueo si el host del desarrollador está offline.
- Operacional: ejecutar Ollama en producción requiere recursos y mantenimiento.
- Datos: sin backups adecuados se puede perder tracking de jugadores.

## Consequences

positive:
  - Entorno reproducible en desarrollo.
  - Capacidad de tracking y análisis de eventos para funcionalidad de logros.
    negative:
  - Mayor responsabilidad operativa por administrar Ollama y backups.
  - Incremento de requisitos de seguridad y documentación.

## Checklist

- [ ] Incluir servicio `ollama` en `docker-compose` de desarrollo con red interna y sin exposición pública.
- [ ] Añadir `postgres` en `docker-compose` con volumen persistente.
- [ ] Documentar host/puerto de Ollama que la app usará (dev/prod).
- [ ] No exponer puertos de Ollama públicamente; documentar reglas de firewall y ACL.
- [ ] Definir plan de backups y restauración para PostgreSQL; programar pruebas de restauración.
- [ ] Actualizar `docs/contracts` con APIs de tracking, manejo de errores y estados del agente.
- [ ] Documentar requisitos de hardware para Ollama.
- [ ] Añadir healthchecks, métricas y runbook mínimo.
- [ ] Definir procedimiento de despliegue para producción (self-hosted o gestionado).



## Decision

Revisando las distintas partes de la infraestructura se va a tomar la opción A. No se incluirá el servicio Redis en docker-compose, ya que no es necesario en la versión actual, si más adelante se verifica que puede haber una mejora para la experiencia del usuario se propondrá analisis y verificación.

En cuanto a los riesgos:
- Revisión de seguridad: Se revisará el código fuente de Ollama para asegurar que no haya vulnerabilidades conocidas.
- Disponibilidad: Actualmente el host no se va a apagar por tanto estará siempre activo, pero si a futuro se ve que la aplicacón es usable y necesita más se evaluaran soluciones económicas para dejarla en un host.
- Backup: Se implementaran las políticas de backup necesarias para garantizar la seguridad de los datos.

En cuanto a seguridad:
- Todos los servicios de este proyecto operaran bajo red privada `educational-network-dev` y `educational-network` para producción
- En la carpeta `framework/infrastructure/envs` se guardarán las variables necesarias en fichero `{servicio}.env` y se creará un fichero `{servicio}.env.example` con valores placeholders para subir al repositorio.
- En la parte de develop se expondrán todos los puertos para una mejor experiencia de desarrollo.
