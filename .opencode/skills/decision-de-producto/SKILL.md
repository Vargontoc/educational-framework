---
name: decision-de-producto
description: "Usar para registrar una decisión de producto de My Friend Nubi confirmada por el usuario, con alternativas, límites e impactos familiares."
---

# Decisión de producto

## Propósito

Registrar de forma trazable una decisión confirmada que afecte a la experiencia de niños, adultos o al alcance del producto. Aunque se usa el prefijo ADR, el documento no es una decisión arquitectónica.

## Requisito previo

Debe existir una confirmación explícita del usuario. No convertir recomendaciones, hipótesis o alternativas en una decisión.

## Ubicación

Crear o actualizar `docs/decisions/ADR-<numero>-<titulo>.md`.

## Plantilla obligatoria

1. Contexto y problema.
2. Necesidad de la familia y usuarios afectados.
3. Alternativas de producto consideradas y compromisos.
4. Decisión confirmada y justificación.
5. Impacto en experiencia infantil, parental, accesibilidad, seguridad infantil y privacidad.
6. Límites, exclusiones y preguntas abiertas para los responsables técnicos.

## Reglas de decisión

- Explicar por qué se descartan las alternativas no elegidas.
- Señalar los efectos sobre el control parental, la información de menores y el contenido sensible.
- Identificar riesgos bloqueantes antes de registrar la decisión.
- Mantener la decisión proporcional al contexto monofamiliar y reversible cuando sea posible.

## Límites

No decidir arquitectura, proveedores, integraciones, formatos de datos, autenticación, herramientas ni despliegue. Las implicaciones técnicas se formulan como preguntas abiertas para los ámbitos responsables.
