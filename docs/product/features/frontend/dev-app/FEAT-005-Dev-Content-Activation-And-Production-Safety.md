# FEAT-005 - Dev Content Activation And Production Safety

## Status

state: proposal
user_history: Activar la mini-app de contenido solo por configuración explícita de desarrollo
depends_on: ADR-011-Dev-Content-Manager
owned_by: frontend
scope: frontend environment configuration and production safety rules for the development content mini-app. No backend implementation is included in this feature.
test: Validar valor por defecto false, activación explícita, ocultación de ruta/navegación y comportamiento con backend sin perfil dev.

## Description

El objetivo de esta feature es registrar y validar la estrategia de activación segura de la mini-app
de contenido.

La decisión principal es que el Content Manager siempre está deshabilitado por defecto. Para usarlo,
un entorno de desarrollo debe configurarlo explícitamente con:

```text
VITE_ENABLE_DEV_CONTENT=true
```

Esto fuerza una decisión consciente al preparar el entorno y evita que la ruta aparezca por accidente
cuando frontend y producción comparten assets.

## Scope

In scope:

- Definir `VITE_ENABLE_DEV_CONTENT` como flag opt-in estricto.
- Documentar que el valor por defecto es deshabilitado.
- Añadir ejemplo de configuración para entornos de desarrollo si aplica.
- Ocultar navegación hacia `/dev/content` cuando el flag no sea `true`.
- Bloquear o redirigir acceso directo a `/dev/content` cuando el flag no sea `true`.
- Mostrar estado de no disponible si el backend no expone `/api/v1/dev/content/**`.

Out of scope:

- Seguridad por roles, usuarios o PIN dentro de la mini-app.
- Cambios de perfil Spring en backend.
- Separar físicamente los bundles de producción y desarrollo.
- Garantizar ausencia del código en assets compartidos.

## Acceptance Criteria

- Si `VITE_ENABLE_DEV_CONTENT` no existe, la mini-app está deshabilitada.
- Si `VITE_ENABLE_DEV_CONTENT=false`, la mini-app está deshabilitada.
- Si `VITE_ENABLE_DEV_CONTENT` tiene cualquier valor distinto de `true`, la mini-app está deshabilitada.
- Solo `VITE_ENABLE_DEV_CONTENT=true` habilita la ruta y navegación.
- Producción no debe definir `VITE_ENABLE_DEV_CONTENT=true`.
- Si el backend no expone `/api/v1/dev/content/**`, la mini-app muestra no disponible o redirige sin romper la aplicación.
- La feature no añade PIN parental ni sesión obligatoria para operar cuando está habilitada.

## Technical Notes

- Implementar helper simple, por ejemplo `isDevContentEnabled`, si evita duplicar la comparación del flag.
- No usar `import.meta.env.DEV` como única condición, porque el entorno develop desplegado puede no ser equivalente al modo local de Vite.
- El backend productivo sigue siendo la frontera final: `/api/v1/dev/content/**` no debe existir fuera del perfil dev.
- No incluir seeds, secretos ni datos privilegiados en assets frontend.

## Risks and Mitigations

- Risk: Un entorno productivo configura accidentalmente `VITE_ENABLE_DEV_CONTENT=true`.
  Mitigation: El valor por defecto es false y los archivos de entorno productivo no deben definir el flag como true.

- Risk: El código de la mini-app está presente en assets compartidos.
  Mitigation: Se acepta la presencia de código; la disponibilidad funcional depende del flag frontend y del perfil backend.

- Risk: El equipo asume que `import.meta.env.DEV` equivale a perfil backend dev.
  Mitigation: Documentar que son conceptos distintos y que el flag explícito es obligatorio.

- Risk: La mini-app se usa contra datos productivos.
  Mitigation: Backend producción no expone endpoints dev-content y frontend producción mantiene el flag deshabilitado.

## References

- ADR-011: Development Content Manager
- ADR-010: Frontend Layer Architecture
- `docs/contracts/api/openapi.json`
- `framework/frontend/app/envs/app.env.example`
