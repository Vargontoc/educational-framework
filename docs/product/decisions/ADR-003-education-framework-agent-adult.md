# ADR-003 — Chatbot parental conversacional de Nubi

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-08-03
- **Sustituida por:** —

## 1. Contexto y problema

Los adultos de la única familia necesitan consultar el uso de My Friend Nubi y recibir resúmenes orientativos de la actividad de sus perfiles infantiles. También pueden solicitar ayuda general relacionada con la aplicación. Sin límites claros, un chat conversacional puede inducir diagnósticos, evaluaciones, comparaciones entre niños, peticiones ajenas al producto o la exposición de datos de menores.

## 2. Necesidad de la familia y usuarios afectados

- **Adultos autenticados:** necesitan información clara, prudente y contextual sobre la aplicación y la actividad disponible de los perfiles familiares autorizados.
- **Niños:** se benefician de que sus datos no se expongan, clasifiquen, comparen ni usen para conclusiones sobre sus capacidades.
- **Familia:** necesita un canal cálido pero acotado, que no sustituya a profesionales ni se convierta en un asistente de propósito general.

## 3. Alternativas de producto consideradas y compromisos

### Alternativa A — Chatbot parental acotado a My Friend Nubi y al progreso orientativo

- **Valor:** ayuda útil en el contexto de la familia, con límites comprensibles y menor riesgo para menores.
- **Compromiso:** no responde a solicitudes generales, profesionales o ajenas a la aplicación.

### Alternativa B — Asistente conversacional generalista para adultos

- **Valor:** aparentemente más flexible.
- **Riesgo:** puede dar respuestas no fiables, tratar asuntos clínicos o sensibles, recibir datos innecesarios y desviar el producto de su finalidad familiar.
- **Decisión:** descartada.

### Alternativa C — Mostrar solo datos sin conversación

- **Valor:** reduce la variabilidad de las respuestas.
- **Riesgo:** es menos comprensible para la familia y no permite explicar los límites ni orientar la lectura de la información.
- **Decisión:** descartada.

## 4. Decisión confirmada y justificación

Se confirma un **Chatbot parental conversacional de Nubi**, disponible exclusivamente para adultos autenticados dentro del panel parental.

El chatbot puede:

1. Dar información sobre funciones, límites y uso de My Friend Nubi, exclusivamente a partir de contenido oficial aprobado de la aplicación.
2. Responder sobre actividad y progreso orientativo general de la familia, distinguiendo hechos disponibles de cualquier síntesis limitada.
3. Responder sobre un perfil infantil concreto. Si existe más de un perfil, el adulto debe seleccionar explícitamente el perfil antes de que se muestre información individual. Si solo existe uno, no se exige esa selección adicional.
4. Ofrecer consejos familiares generales no profesionales y derivar siempre a un profesional adecuado.

El chatbot no es un profesional, no diagnostica, no evalúa y no clasifica a ningún niño. No debe usar documentación externa, datos familiares, conversaciones ni mensajes de Contacto como fuente de información documental.

Las solicitudes fuera de alcance —incluidas política, juicios morales, creación de software, peticiones no relacionadas con la aplicación o peticiones que puedan comprometer la infraestructura— reciben un mensaje cálido con el texto: **«No puedo hacer lo que me solicitas»**. Puede añadir una orientación segura y limitada sobre aquello con lo que sí puede ayudar, sin responder a la petición excluida.

Las situaciones sanitarias, psicológicas, educativas profesionales o de seguridad se derivan a un adulto responsable, servicio de emergencia o profesional adecuado; no se resuelven con un consejo del chatbot.

## 5. Impacto

### Experiencia parental

- El tono de Nubi puede ser cercano y respetuoso, sin infantilizar al adulto ni presentar la respuesta como autoridad profesional.
- La familia recibe ayuda contextual y límites explícitos cuando una pregunta queda fuera de alcance.

### Experiencia infantil

- El chatbot no forma parte de la experiencia del niño ni le pide información.
- El progreso se presenta como señal orientativa para la familia, nunca como medida de capacidad.

### Accesibilidad

- Las respuestas deben usar lenguaje claro, separar hechos de consejos y hacer visible cualquier derivación profesional.
- La selección de perfil, cuando sea necesaria, debe ser comprensible para adultos en móvil y tableta.

### Seguridad infantil y privacidad

- Solo adultos autenticados acceden al chatbot.
- Nunca se exponen datos de otros perfiles ni se mezclan perfiles infantiles en una respuesta.
- No se solicitan ni reutilizan datos personales de menores, conversaciones o mensajes de Contacto para fines ajenos a la consulta autorizada.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- Diagnósticos, tratamientos, terapia, evaluación educativa profesional, asesoramiento legal, político, moral o de seguridad especializada.
- Comparativas entre niños, rankings, porcentajes de capacidad, etiquetas de competencia o conclusiones sobre desarrollo.
- Solicitudes de programación, administración, acceso, modificación o exposición de sistemas de la aplicación.
- Conversación pública, contacto con menores, publicidad, perfilado o uso de mensajes para entrenar sistemas de IA.

### Validaciones requeridas

- **Frontend:** experiencia exclusiva para adultos, selección de perfil cuando exista más de uno, legibilidad de límites y derivaciones.
- **Backend, datos y seguridad:** acceso exclusivo de la familia autorizada, aislamiento entre perfiles, minimización de información y tratamiento autorizado de conversaciones.
- **Agentes e IA:** cumplimiento consistente de los límites, diferenciación entre hechos y síntesis, rechazo seguro y ausencia de solicitudes de datos personales.
- **Contenido:** redacción aprobada sobre la aplicación, tono de Nubi, mensajes de rechazo y derivaciones apropiadas.
- **Infraestructura:** cualquier efecto de disponibilidad, coste, protección y operación del chatbot, sin ampliar el alcance funcional acordado.

### Preguntas abiertas

- El producto debe aprobar en una especificación posterior los contenidos documentales concretos que el chatbot podrá explicar y la presentación concreta de los resúmenes orientativos.

## Referencias

- README.md
- FEAT-003 — Chatbot parental conversacional de Nubi
- FEAT-004 — Estructura visual y navegación del panel parental
