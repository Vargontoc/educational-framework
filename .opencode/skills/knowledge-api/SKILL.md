---
name: knowledge-api
description: Conocimiento de la API de la aplicación
---

# Proposito

Para lectura y escritura de los endpoints disponibles de la app

## Cuando usar

- En el analisis e implementación de requisitos de la capa backend, en esta capa se puede leer, crear, editar, eliminar
- En el analisis y consumo de la api en la capa frontend.

## Reglas

- Solo se puede editar los endpoints y sus esquemas cuando el alcance es backend
- Si hay que eliminar un endpoint o esquemas EXPLICAR al usuario el porque y esperar su confirmación

## Directorio

- `{workspace}/docs/contracts/api/openapi/openapi.yaml` -> fichero donde se localizan los endpoints
- `{workspace}/docs/contracts/api/openapi/paths/<module>/*` -> las distintas rutas
- `{workspace}/docs/contracts/api/openapi/schemas/<module>/*`