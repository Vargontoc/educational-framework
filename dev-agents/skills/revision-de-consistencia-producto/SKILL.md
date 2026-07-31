---
name: revision-de-consistencia-producto
description: "Usar para contrastar README, decisiones de producto y especificaciones de My Friend Nubi, detectando contradicciones o requisitos sin confirmar."
---

# Revisión de consistencia de producto

## Propósito

Mantener coherente la documentación de producto de My Friend Nubi y hacer visibles las decisiones que aún requieren debate o confirmación.

## Alcance de la revisión

Contrastar `README.md`, `docs/decisions/` y `docs/specs/` cuando existan. Revisar especialmente:

- Alcance monofamiliar, edades y uso prioritario en tabletas y móviles.
- Carácter de acompañamiento no profesional ni evaluativo.
- Separación entre experiencia infantil y controles parentales.
- Progreso orientativo sin clasificaciones ni diagnósticos.
- Límites de `npc-game`, `dashboard-bot`, lectura y relajación.
- Privacidad, control parental, minimización de datos y contenido adecuado por edad.
- Exclusiones y decisiones que se presentan como confirmadas sin evidencia.

## Resultado

Entregar una lista priorizada de:

1. Contradicciones documentales.
2. Requisitos implícitos que deben confirmarse.
3. Información que falta para cerrar una especificación.
4. Riesgos de producto, seguridad infantil, privacidad o accesibilidad.
5. Documentos que conviene actualizar una vez el usuario confirme una decisión.

Cada hallazgo debe mencionar el documento implicado y explicar el impacto para la familia en lenguaje no técnico.

## Límites

No revisar código, configuraciones, arquitectura, rendimiento ni seguridad técnica. No modificar documentación para resolver contradicciones sin confirmación explícita del usuario; proponer las preguntas necesarias para resolverlas.
