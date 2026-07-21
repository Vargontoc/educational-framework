# Sprint 002 - Frontend

## Goal
Especificar los flujos adultos de familia, PIN y perfiles infantiles, alineando privacidad frontend, catálogo local de avatares y contratos requeridos con backend.

## Status
status: blocked
started_at:
closed_at:
blocked_by: docs/product/sprints/frontend/SPRINT-001-arquitectura-web-pwa-android.md
waiting_for: Cierre de arquitectura de rutas, estado y cliente API del Sprint 001.

## Tasks
- [ ] Definir el comportamiento de inicio sin familia registrada: registro de familia y documentación pública.
- [ ] Definir el comportamiento de inicio con familia registrada: acceso a experiencia, panel parental protegido por PIN y documentación.
- [ ] Especificar el flujo adulto de alta de familia con nombre de familia y configuración de PIN.
- [ ] Especificar la creación opcional de perfiles infantiles desde inicio y desde panel parental.
- [ ] Inventariar `framework/frontend/app/src/assets/images/child-avatars.svg` y documentar un catálogo de `avatarId` estables, semánticos y versionados.
- [ ] Definir el fallback visual seguro cuando backend entregue un `avatarId` desconocido.
- [ ] Definir límites de almacenamiento frontend: no incluir PIN, datos de menores o fecha de nacimiento en URL, logs cliente, caché PWA o persistencia no aprobada.
- [ ] Trazar los contratos backend necesarios para familia, sesión multidispositivo, PIN, perfiles y `avatarId`.
- [ ] Registrar como requisito futuro el evento de cumpleaños: backend decide elegibilidad y frontend recibe solo el evento, animación y audio autorizados, nunca fecha de nacimiento.

## Risks
- La URL inicial puede compartirse y no debe tratarse como autenticación o autorización.
- El PIN y datos de perfiles requieren controles de sesión backend que no puede resolver el frontend por sí solo.
- Un `avatarId` no compatible entre versiones de frontend y backend puede degradar la experiencia visual.
- La fecha de nacimiento es un dato personal del menor; usarla directamente en frontend incumpliría minimización de datos.

## Dependencies
- Resultado del Sprint 001 para rutas, estado y política PWA.
- `docs/contracts/api/openapi.json` debe cubrir o requerirá ampliación para alta de familia, sesión, PIN, perfiles y avatar.
- Backend debe definir manejo de sesión multidispositivo y verificación de PIN.
- Backend debe persistir únicamente el identificador de avatar, no una ruta de recurso frontend.
- El futuro contrato de cumpleaños debe ser independiente y no exponer fecha de nacimiento.

## Agent Instruction
- No implementar código; redactar especificación frontend y propuestas de cambios de contrato para validación de los responsables.
- Mantener separación total: registro, perfiles, PIN y configuraciones son flujos adultos; nunca deben aparecer dentro del mundo infantil.
- Los avatares son únicamente animales infantiles precargados del SVG indicado; prohibir carga de fotos o archivos.
- Tratar nombre y fecha de nacimiento como datos personales: no exponerlos en URL, cache o logs del cliente.
- No diseñar todavía la experiencia visual de cumpleaños ni implementar contrato de cumpleaños; solo registrar su frontera de integración futura.
- Documentar endpoints/contratos consumidos, dependencias de backend/agents/tts y handoffs de integración.

## Notes
- Los perfiles infantiles son opcionales durante el alta de familia y también pueden crearse desde el panel parental.
- El nombre visible lo introduce el padre/madre; puede usarse para mensajes de ánimo según decisiones futuras de producto.
- El panel parental solo se ofrece cuando hay una familia registrada y está protegido por PIN.
- La aplicación podrá usarse inicialmente desde varios dispositivos familiares sin política de revocación/caducidad definida.

## Review

completed_tasks:

incomplete_tasks:

contract_changes:

learnings:

next_sprint_suggestions:
