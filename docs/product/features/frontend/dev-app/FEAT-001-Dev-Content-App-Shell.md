# FEAT-001 - Dev Content App Shell

## Status

state: proposal
user_history: Mini-app interna para gestionar contenido en perfil de desarrollo
depends_on: ADR-011-Dev-Content-Manager, FEAT-001-Base-Styles
owned_by: frontend
scope: frontend view shell + routing + environment activation. No backend implementation is included in this feature.
test: Validar ruta habilitada/deshabilitada por flag, layout base, navegación entre secciones, estados de carga, error y vacío.

## Description

El objetivo de esta feature es crear la base de la mini-app de desarrollo para gestión de contenido.
La mini-app vive dentro de `framework/frontend/app` y se accede mediante una ruta dedicada, por
ejemplo `/dev/content`.

Esta feature no implementa todavía CRUD completo de entidades. Su responsabilidad es preparar la
estructura visual, la navegación interna y el punto de integración con las siguientes features.

La mini-app está deshabilitada por defecto y solo se activa cuando la configuración frontend define:

```text
VITE_ENABLE_DEV_CONTENT=true
```

## Scope

In scope:

- Crear ruta dedicada para la mini-app de contenido de desarrollo.
- Crear vista principal `DevContentView` o equivalente.
- Crear layout base con sidebar y área de trabajo principal.
- Añadir navegación interna entre secciones previstas: categories, topics, activities, difficulty levels, resources, locales, curiosities, avatar events.
- Añadir estado vacío inicial por sección.
- Añadir estados visuales de carga y error.
- Añadir i18n para textos visibles.
- Respetar el diseño base existente de la aplicación.
- Permitir uso en horizontal y vertical; esta mini-app de desarrollo no debe forzar landscape.

Out of scope:

- Implementar formularios CRUD completos.
- Implementar lógica de backend.
- Añadir seguridad de usuario o PIN parental.
- Gestionar datos mock persistentes.
- Gestionar delete de entidades.

## Acceptance Criteria

- La ruta `/dev/content` solo está disponible cuando `VITE_ENABLE_DEV_CONTENT === 'true'`.
- Si la feature está deshabilitada, el acceso directo redirige a `/`
- La mini-app muestra un layout con sidebar y panel principal.
- El sidebar permite seleccionar las secciones previstas sin recargar la página.
- Cada sección muestra un estado vacío si todavía no tiene integración de datos.
- Todos los textos visibles pasan por Vue i18n.
- No se requiere PIN parental ni sesión de usuario para usar la mini-app cuando está habilitada.
- No se realizan llamadas directas con Axios desde componentes; cualquier integración futura debe pasar por servicios.
- La mini-app no muestra el overlay de rotación ni bloquea la interfaz en orientación vertical.

## Technical Notes

- El flag `VITE_ENABLE_DEV_CONTENT` debe tratarse como opt-in estricto: cualquier valor distinto de `true` significa deshabilitado.
- El código puede existir en assets compartidos, pero la disponibilidad funcional depende de configuración frontend y perfil backend.
- La navegación debe mantenerse simple; no crear un router secundario si no es necesario.
- La restricción landscape del aplicativo base no aplica a esta herramienta interna; el layout debe ser responsive para desktop, tablet y móvil en ambas orientaciones.
- Preferir componentes pequeños solo cuando eviten duplicación real.

## Risks and Mitigations

- Risk: La ruta aparece accidentalmente en producción.
  Mitigation: El flag está deshabilitado por defecto y producción no debe definir `VITE_ENABLE_DEV_CONTENT=true`.

- Risk: La shell crece demasiado antes de tener CRUD real.
  Mitigation: Mantener esta feature limitada a layout, navegación y estados base.

- Risk: Textos hardcodeados por tratarse de tooling interno.
  Mitigation: Mantener la regla frontend: todos los textos visibles pasan por i18n.

## References

- ADR-011: Development Content Manager
- ADR-010: Frontend Layer Architecture
- `framework/frontend/agent.md`
- `docs/contracts/api/openapi.json`
