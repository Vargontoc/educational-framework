---
name: knowledge-api-tts
description: Conocimiento de la API del servicio TTS
---

# Proposito

Para lectura y escritura de los endpoints disponibles del servicio tts

## Cuando usar

- En el analisis e implementación de requisitos de la capa tts, en esta capa se puede leer, crear, editar, eliminar
- En el analisis y consumo de la api por la capa backend.

## Reglas

- Solo se puede editar los endpoints y sus esquemas cuando el alcance es tts
- Si hay que eliminar un endpoint o esquemas EXPLICAR al usuario el porque y esperar su confirmación

## Fichero

- `{workspace}/docs/contracts/api/openapi/openapi_tts.yaml` -> fichero donde se localizan los endpoints, schemas y paths