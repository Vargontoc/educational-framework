---
name: knowledge-websocket
description: Conocimiento de la comunicación websocket de la aplicación
---

# Proposito

Para lectura y escritura de las rutas y eventos websocket de la app

## Cuando usar

- En el analisis e implementación de requisitos de la capa backend, en esta capa se puede leer, crear, editar, eliminar
- En el analisis y consumo del websocket en la capa frontend.

## Reglas

- Solo se puede editar los endpoints y sus esquemas cuando el alcance es backend
- Si hay que eliminar un endpoint o esquemas EXPLICAR al usuario el porque y esperar su confirmación

## Directorio

- `{workspace}/docs/contracts/api/asyncapi/websocket.yaml` -> fichero donde se define el contrato websocket
- `{workspace}/docs/contracts/api/asyncapi/channels/*` -> se define los distintos canales
- `{workspace}/docs/contracts/api/asyncapi/messages/*` -> se define los mensajes
- `{workspace}/docs/contracts/api/asyncapi/schemas/*` -> se define la estructura de los eventos