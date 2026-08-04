# SPRINT-001 — Textos estáticos de documentación y contacto

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-03
- **Responsable principal:** contenido
- **Prioridad:** ALTA
- **Dependencias:** FEAT-007 (aceptada), ADR-017 (documentación estática)
- **Impacto estimado:** Archivos markdown con el contenido de las 4 secciones informativas (Quién soy, Primeros pasos, Agentes AI, Minijuegos) y textos del formulario de contacto (aviso de privacidad, información de finalidad, confirmación adulta). Estos textos serán consumidos por frontend en SPRINT-031 y SPRINT-033.

## Objetivo

Producir los textos definitivos en formato markdown para:
1. Las 4 secciones informativas de la documentación pública.
2. El aviso de privacidad del formulario de contacto.
3. La información de finalidad del mensaje.
4. La confirmación de persona adulta responsable.
5. Los mensajes de estado (éxito, error genérico, error de validación, error de rate limiting).

Los textos deben ser:
- Claros y dirigidos a personas adultas.
- Consistentes con la protección infantil y la privacidad.
- Sin tecnicismos innecesarios.
- Sin publicidad, perfilado comercial ni mecanismos persuasivos.

## Contexto

**FEAT-007** requiere una sección pública de documentación con 5 secciones:
- Quién soy
- Primeros pasos
- Agentes AI
- Minijuegos
- Contacto

Las 4 primeras son informativas y su detalle editorial se definirá en especificaciones posteriores. Este sprint proporciona los textos placeholder iniciales que serán revisados y aprobados antes de la publicación.

**ADR-017** establece que la documentación es estática y sin búsqueda. Su actualización requiere una nueva publicación de la aplicación.

**Decisión confirmada:** Los textos se entregan como archivos markdown (`.md`) que frontend importará en build time.

## Textos a producir

### 1. Sección "Quién soy"

**Archivo:** `framework/frontend/app/src/content/docs/quien-soy.md`

**Objetivo:** Presentar My Friend Nubi a las familias, explicar qué es y qué no es.

**Requisitos de contenido:**
- Explicar que es una aplicación de acompañamiento de refuerzo para niños de 3-4 años.
- Aclarar que NO es una herramienta educativa profesional, diagnóstica ni evaluativa.
- Aclarar que el progreso es orientativo para la familia.
- Mencionar los personajes principales (Nubi y los agentes AI).
- Tono cercano pero claro, dirigido a adultos.
- Longitud: 200-400 palabras.

### 2. Sección "Primeros pasos"

**Archivo:** `framework/frontend/app/src/content/docs/primeros-pasos.md`

**Objetivo:** Orientar a las familias sobre cómo comenzar a usar la aplicación.

**Requisitos de contenido:**
- Explicar el registro familiar (adulto responsable).
- Explicar la creación de perfiles infantiles.
- Explicar el acceso al panel parental con PIN.
- Mencionar brevemente las funcionalidades principales (minijuegos, seguimiento orientativo).
- Tono práctico y claro.
- Longitud: 200-400 palabras.

### 3. Sección "Agentes AI"

**Archivo:** `framework/frontend/app/src/content/docs/agentes-ai.md`

**Objetivo:** Informar sobre los agentes AI de la aplicación, sus capacidades y límites.

**Requisitos de contenido:**
- Explicar qué son los agentes AI en el contexto de la aplicación.
- Describir las capacidades aprobadas (acompañamiento en juegos, curiosidades, historias).
- Describir los límites: no sustituyen orientación profesional, no diagnostican, no evalúan.
- Mencionar que el chatbot es exclusivo para adultos.
- Tono informativo y transparente.
- Longitud: 200-400 palabras.

### 4. Sección "Minijuegos"

**Archivo:** `framework/frontend/app/src/content/docs/minijuegos.md`

**Objetivo:** Describir los minijuegos disponibles y su propósito orientativo.

**Requisitos de contenido:**
- Explicar que los minijuegos son actividades de refuerzo adaptadas por edad.
- Mencionar las categorías disponibles (números, letras, formas, etc.).
- Aclarar que el progreso es orientativo y no constituye evaluación.
- Mencionar la dificultad adaptativa.
- Tono cercano y claro.
- Longitud: 200-400 palabras.

### 5. Aviso de privacidad (Contacto)

**Texto para i18n:** `views.docs.contact.privacyNotice`

**Objetivo:** Advertir al usuario que no incluya datos sensibles en el mensaje.

**Requisitos de contenido:**
- Prohibir explícitamente: datos de menores, nombres, PIN, información privada.
- Ser claro y visible.
- Tono directo pero no alarmista.
- Longitud: 1-2 frases.

**Propuesta:**
> No incluyas datos de menores, nombres, PIN ni otra información privada en tu mensaje.

### 6. Información de finalidad (Contacto)

**Texto para i18n:** `views.docs.contact.purposeInfo`

**Objetivo:** Informar al usuario sobre el uso que se dará a su mensaje.

**Requisitos de contenido:**
- Explicar que el mensaje es recibido por el equipo de My Friend Nubi.
- Aclarar que no se usa para publicidad, perfilado ni entrenamiento de IA.
- Aclarar que no se comparte con otras familias.
- Tono transparente y tranquilizador.
- Longitud: 2-3 frases.

**Propuesta:**
> Tu mensaje será recibido por el equipo de My Friend Nubi para atender tu consulta. No se utiliza para publicidad, perfilado ni entrenamiento de sistemas de inteligencia artificial. Tu mensaje no se comparte con otras familias.

### 7. Confirmación de persona adulta (Contacto)

**Texto para i18n:** `views.docs.contact.adultConfirmation`

**Objetivo:** Requerir la confirmación de que el remitente es persona adulta responsable.

**Requisitos de contenido:**
- Declaración de mayoría de edad o responsabilidad adulta.
- Aceptación de la información de finalidad.
- Tono formal pero accesible.
- Longitud: 1 frase.

**Propuesta:**
> Soy persona adulta responsable y acepto la información sobre el uso de mi mensaje.

### 8. Mensajes de estado (Contacto)

**Textos para i18n:**

| Clave | Texto propuesto |
|---|---|
| `successMessage` | Tu mensaje ha sido recibido. Gracias por contactar con nosotros. |
| `errorGeneric` | No se ha podido enviar el mensaje. Inténtalo más tarde. |
| `errorValidation` | El mensaje no es válido. Revisa el contenido e inténtalo de nuevo. |
| `errorRateLimit` | Has realizado demasiados intentos. Inténtalo más tarde. |

## Contratos y dependencias externas

### Contratos

- **Sin cambios.** Este sprint produce textos, no contratos técnicos.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | SPRINT-031 consume los archivos markdown. SPRINT-033 consume los textos de contacto. | ⏳ Pendiente |
| Backend | Ninguna. | ✅ Sin dependencia |
| Agents | Ninguna. | ✅ Sin dependencia |
| TTS | Ninguna. | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Los textos no están aprobados al inicio de los sprints de frontend. | MEDIA | Los sprints de frontend pueden usar placeholders. Los textos definitivos se integran antes de la publicación. |
| R2 | Los textos pueden no ser suficientemente claros o completos. | BAJA | Revisión por el equipo de producto antes de la aprobación final. |
| R3 | Los textos pueden quedar desactualizados entre publicaciones. | BAJA | ADR-017 establece que la documentación se revisa antes de cada publicación. |

---

## Tareas del sprint

### Tarea 1.1: Redactar "Quién soy"

**Descripción:** Redactar el contenido de la sección "Quién soy" en formato markdown.

**Archivo:** `framework/frontend/app/src/content/docs/quien-soy.md` (nuevo)

**Requisitos:** Ver sección 1 de "Textos a producir".

**Criterios de aceptación:**
- Explica qué es My Friend Nubi (acompañamiento de refuerzo).
- Aclara que NO es herramienta profesional, diagnóstica ni evaluativa.
- Aclara que el progreso es orientativo.
- Menciona a Nubi y los agentes AI.
- Tono cercano y claro para adultos.
- Longitud entre 200 y 400 palabras.
- Formato markdown válido.

---

### Tarea 1.2: Redactar "Primeros pasos"

**Descripción:** Redactar el contenido de la sección "Primeros pasos" en formato markdown.

**Archivo:** `framework/frontend/app/src/content/docs/primeros-pasos.md` (nuevo)

**Requisitos:** Ver sección 2 de "Textos a producir".

**Criterios de aceptación:**
- Explica el registro familiar.
- Explica la creación de perfiles infantiles.
- Explica el acceso al panel parental con PIN.
- Menciona funcionalidades principales.
- Tono práctico y claro.
- Longitud entre 200 y 400 palabras.
- Formato markdown válido.

---

### Tarea 1.3: Redactar "Agentes AI"

**Descripción:** Redactar el contenido de la sección "Agentes AI" en formato markdown.

**Archivo:** `framework/frontend/app/src/content/docs/agentes-ai.md` (nuevo)

**Requisitos:** Ver sección 3 de "Textos a producir".

**Criterios de aceptación:**
- Explica qué son los agentes AI en la aplicación.
- Describe capacidades aprobadas.
- Describe límites (no sustituyen profesional, no diagnostican).
- Menciona que el chatbot es exclusivo para adultos.
- Tono informativo y transparente.
- Longitud entre 200 y 400 palabras.
- Formato markdown válido.

---

### Tarea 1.4: Redactar "Minijuegos"

**Descripción:** Redactar el contenido de la sección "Minijuegos" en formato markdown.

**Archivo:** `framework/frontend/app/src/content/docs/minijuegos.md` (nuevo)

**Requisitos:** Ver sección 4 de "Textos a producir".

**Criterios de aceptación:**
- Explica que son actividades de refuerzo adaptadas por edad.
- Menciona categorías disponibles.
- Aclara que el progreso es orientativo.
- Menciona la dificultad adaptativa.
- Tono cercano y claro.
- Longitud entre 200 y 400 palabras.
- Formato markdown válido.

---

### Tarea 1.5: Aprobar textos de contacto

**Descripción:** Revisar y aprobar los textos del formulario de contacto: aviso de privacidad, información de finalidad, confirmación adulta y mensajes de estado.

**Textos a aprobar:** Ver secciones 5-8 de "Textos a producir".

**Criterios de aceptación:**
- Aviso de privacidad: claro, prohíbe datos de menores, nombres, PIN.
- Información de finalidad: transparente, aclara uso no comercial ni de IA.
- Confirmación adulta: declaración de responsabilidad adulta.
- Mensajes de estado: comprensibles, no exponen datos.
- Todos los textos son coherentes con la protección infantil.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/frontend/app/src/content/docs/quien-soy.md` | Nuevo archivo |
| `framework/frontend/app/src/content/docs/primeros-pasos.md` | Nuevo archivo |
| `framework/frontend/app/src/content/docs/agentes-ai.md` | Nuevo archivo |
| `framework/frontend/app/src/content/docs/minijuegos.md` | Nuevo archivo |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Baja (redacción de contenidos)
- **Riesgo:** Bajo

## Criterios de aceptación del sprint

1. Los 4 archivos markdown existen en `src/content/docs/`.
2. Cada archivo tiene contenido aprobado por el equipo de producto.
3. Los textos son claros, dirigidos a adultos y consistentes con la protección infantil.
4. Los textos de contacto (aviso, finalidad, confirmación, estados) están aprobados.
5. Los textos no contienen publicidad, perfilado comercial ni mecanismos persuasivos.
6. Los textos no prometen capacidades diagnósticas, evaluativas ni profesionales.
7. Formato markdown válido en todos los archivos.

## Evidencias esperadas

- Revisión de los 4 archivos markdown por el equipo de producto.
- Aprobación formal de los textos de contacto.
- Verificación de que los textos no contienen lenguaje inapropiado.
- Verificación de que los textos son coherentes con FEAT-007 y ADR-017.

## Dependencias bloqueantes

- [x] FEAT-007 aceptada.
- [x] ADR-017 vigente.

## Handoffs a otras capas

### Frontend:
- SPRINT-031 consume los archivos markdown de las 4 secciones.
- SPRINT-033 consume los textos de contacto (aviso, finalidad, confirmación, estados).

### Backend/Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Privacidad infantil

- Los textos no solicitan ni prometen recogida de datos personales.
- El aviso de privacidad es la primera línea de defensa contra envío accidental de datos sensibles.
- La información de finalidad es transparente sobre el uso de los mensajes.

### Relación con FEAT-007

Este sprint satisface los requisitos de contenido del FEAT-007:
- Sección 2: "El detalle editorial de las secciones informativas se definirá en especificaciones posteriores."
- Sección 6: "Redacción, revisión y vigencia de las cinco secciones estáticas."
- Sección 6: "Claridad del aviso de Contacto y de la explicación de finalidad para adultos."

### Actualización futura

Los textos pueden quedar desactualizados entre publicaciones. ADR-017 establece que la documentación se revisa antes de cada nueva publicación de la aplicación. Este sprint proporciona los textos iniciales; futuras actualizaciones requerirán nuevos sprints de contenido.
