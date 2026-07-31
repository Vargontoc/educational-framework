---
name: especificacion-funcional
description: "Usar para redactar o actualizar una especificación funcional FEAT confirmada para My Friend Nubi, sin incluir decisiones de implementación."
---

# Especificación funcional

## Propósito

Documentar una funcionalidad ya confirmada de forma suficiente para que los responsables de cada ámbito entiendan el valor, el comportamiento esperado y los límites de producto.

## Requisito previo

La necesidad debe estar confirmada explícitamente por el usuario. Si todavía es una idea o una alternativa, usar `descubrimiento-de-necesidades-infantiles` en su lugar.

## Ubicación

Crear o actualizar `docs/specs/<capa>/FEAT-<numero>-<titulo>.md`. La capa identifica el ámbito responsable principal, pero no supone una solución técnica.

## Plantilla obligatoria

1. Objetivo y valor para la familia.
2. Actores y escenarios de uso.
3. Requisitos funcionales y no funcionales de producto.
4. Criterios de aceptación verificables.
5. Ámbitos que deben validar los responsables y dependencias de producto conocidas.
6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables.
7. Exclusiones, riesgos, supuestos y decisiones pendientes.

## Reglas de redacción

- Usar español y diferenciar hechos, supuestos, decisión confirmada y pendientes.
- Describir resultados observables por niños y adultos, no mecanismos internos.
- Especificar qué debe ocurrir si una experiencia opcional no está disponible desde el punto de vista del usuario, sin indicar cómo lograrlo.
- Mantener el progreso como señal orientativa, nunca como diagnóstico, capacidad o clasificación del niño.

## Límites

No incluir arquitectura, endpoints, datos, esquemas, librerías, comandos, configuraciones, pruebas técnicas ni planes de ejecución. Documentar las dudas técnicas como preguntas para el ámbito correspondiente.
