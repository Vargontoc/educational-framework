# FEAT-003 — Chatbot parental conversacional de Nubi

## Estado

- **Estado:** aceptada
- **Responsable principal:** agents
- **Decisión confirmada:** 2026-08-03
- **Historia de usuario:** Como adulto autenticado de la familia, quiero consultar a Nubi sobre My Friend Nubi y sobre la actividad orientativa de perfiles autorizados, con respuestas seguras que no sustituyan a profesionales.
- **Depende de:** ADR-003, acceso al panel parental y disponibilidad autorizada de información orientativa.

## 1. Objetivo y valor para la familia

Ofrecer una conversación adulta, amable y fuertemente limitada al producto. El chatbot ayuda a entender My Friend Nubi y la información familiar disponible sin convertir el progreso infantil en una evaluación, ni sustituir consejo sanitario, psicológico, educativo profesional o de seguridad.

## 2. Actores y escenarios de uso

### Adulto que consulta sobre la aplicación

1. Accede al Chatbot desde el panel parental autenticado.
2. Pregunta por una función o límite de My Friend Nubi.
3. Recibe una respuesta basada solo en contenido oficial aprobado de la aplicación.

### Adulto que consulta información general

1. Solicita un resumen general de actividad o progreso orientativo.
2. Recibe hechos disponibles y, si procede, una síntesis claramente limitada y no evaluativa.

### Adulto que consulta un perfil concreto

1. Si existe más de un perfil infantil, el chatbot solicita una selección explícita antes de mostrar información individual.
2. Tras seleccionar un perfil autorizado, el adulto recibe solo su información orientativa disponible.
3. Si existe un único perfil, el chatbot puede responder sin requerir selección adicional.

### Adulto que solicita un consejo o plantea una cuestión sensible

1. El chatbot ofrece un consejo familiar general no profesional, si es pertinente.
2. Deriva siempre a un profesional adecuado y no presenta el consejo como diagnóstico, tratamiento o evaluación.

### Persona que realiza una petición excluida

1. Solicita contenido político, un juicio moral, programación, una acción no relacionada con la aplicación o algo que pueda comprometer sus sistemas.
2. Recibe un mensaje cálido que incluye exactamente: **«No puedo hacer lo que me solicitas»**.
3. Puede recibir una orientación limitada sobre las funciones permitidas, pero no una respuesta a la petición excluida.

## 3. Requisitos funcionales y no funcionales

1. El chatbot solo está disponible para adultos autenticados en el panel parental.
2. Nubi usa un tono cercano, claro y respetuoso dirigido a adultos; no se presenta como profesional ni como interlocutor del niño.
3. Solo puede informar sobre My Friend Nubi a partir de contenido oficial aprobado de la aplicación.
4. Puede comunicar actividad y progreso de forma orientativa, descriptiva y no evaluativa.
5. Debe distinguir los hechos disponibles de cualquier síntesis, consejo o inferencia limitada.
6. Si hay más de un perfil infantil, debe requerir selección explícita del perfil antes de responder información individual.
7. Debe limitar cada respuesta individual al perfil autorizado seleccionado y no mezclar datos entre perfiles.
8. Puede dar consejos familiares generales no profesionales, acompañados siempre de una derivación a un profesional adecuado.
9. Ante asuntos sanitarios, psicológicos, educativos profesionales o de seguridad, debe derivar a un adulto responsable, emergencia o profesional adecuado y no emitir diagnóstico, terapia, tratamiento ni indicación especializada.
10. Ante peticiones fuera de alcance, debe incluir el mensaje exacto «No puedo hacer lo que me solicitas» y no proporcionar contenido que satisfaga la petición excluida.
11. Se consideran fuera de alcance, entre otras, política, juicios morales, creación de código o software, peticiones ajenas a My Friend Nubi y acciones que puedan comprometer la infraestructura de la aplicación.
12. No debe solicitar datos personales de menores, PIN, imágenes, datos de contacto, información de salud ni conversaciones para poder responder.
13. No debe usar documentos externos, datos familiares, conversaciones o mensajes de Contacto como fuente de conocimiento sobre la aplicación.
14. No debe mostrar porcentajes de capacidad, niveles, rankings, comparaciones entre niños, diagnósticos ni clasificaciones.

## 4. Criterios de aceptación verificables

1. Una persona sin acceso parental autenticado no puede acceder al chatbot ni a sus respuestas.
2. Una consulta sobre una función aprobada de My Friend Nubi recibe información sobre la aplicación y no una respuesta de conocimiento general ajeno al producto.
3. Un resumen de actividad identifica qué parte son hechos disponibles y no incluye una clasificación o diagnóstico infantil.
4. Con dos o más perfiles, una consulta individual sin perfil previamente seleccionado solicita una selección explícita antes de mostrar información de un niño.
5. Con un solo perfil, una consulta individual no solicita una selección adicional.
6. Tras seleccionar un perfil, la respuesta no incluye información de otro perfil infantil.
7. Una solicitud de consejo recibe una recomendación general no profesional y una derivación visible a un profesional adecuado.
8. Una petición de diagnóstico, terapia, orientación educativa profesional o seguridad especializada no recibe esa respuesta y muestra una derivación adecuada.
9. Una pregunta política, moral, de programación o que pretenda comprometer sistemas incluye exactamente «No puedo hacer lo que me solicitas» y no contiene instrucciones que satisfagan la solicitud excluida.
10. El chatbot no pide nombres completos, PIN, imágenes, datos sanitarios ni datos de contacto de menores.
11. Ninguna respuesta presenta comparación, ranking, nota, nivel de capacidad o diagnóstico de un niño.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Agents e IA

- Cumplimiento consistente del alcance permitido, rechazos, derivaciones, tono adulto y diferenciación entre hechos, síntesis y consejos.
- Ausencia de reutilización de conversaciones o fuentes no autorizadas.

### Frontend

- Acceso exclusivamente parental, selección explícita de perfil cuando corresponda y presentación clara de las derivaciones.
- Lectura accesible de respuestas y límites en móvil y tableta.

### Backend, datos y seguridad

- Autorización, aislamiento entre perfiles y disponibilidad exclusivamente de información familiar autorizada.
- Tratamiento, minimización, acceso y conservación de conversaciones conforme a privacidad infantil.

### Contenido

- Contenido oficial sobre My Friend Nubi, tono de Nubi, texto de rechazo y lenguaje de derivación.

### Infraestructura

- Viabilidad operativa, coste, disponibilidad y protección del servicio conversacional.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA

### Privacidad y seguridad infantil

- La funcionalidad es exclusiva de adultos autenticados y no permite acceso entre familias ni perfiles no autorizados.
- El progreso solo es orientativo para la familia y nunca se usa para medir o diagnosticar al niño.
- Los mensajes de Contacto no forman parte de las fuentes del chatbot.

### Accesibilidad y experiencia

- Lenguaje simple para adultos, estructura clara y separación visible entre hechos, consejo y derivación.
- No emplea presión, urgencia injustificada, culpabilización, comparativas ni mensajes persuasivos dirigidos a niños.

### Límites de IA

- El chatbot no es de propósito general, no responde a peticiones excluidas ni ejecuta acciones fuera de sus funciones.
- No solicita ni induce la revelación de datos personales o sensibles.
- Los responsables de IA deben validar que el comportamiento no exceda el contenido y contexto autorizados.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Diagnóstico, terapia, tratamiento, evaluación profesional, asesoramiento legal, político, moral o especializado de seguridad.
- Conversación infantil, comunicación pública, anuncios, perfilado comercial, uso de IA para Contacto y acciones sobre sistemas de la aplicación.
- Definición detallada de resúmenes, visualizaciones o métricas de progreso.

### Riesgos

- Un consejo general puede interpretarse como consejo profesional; la derivación obligatoria y el lenguaje prudente reducen, pero no eliminan, ese riesgo.
- Una pregunta ambigua puede provocar una selección incorrecta de perfil; la selección explícita cuando hay varios perfiles es obligatoria.
- Un rechazo demasiado escueto puede frustrar a la familia; la orientación limitada sobre funciones permitidas debe conservarse sin contestar la petición excluida.

### Supuestos

- El perfil único disponible está autorizado para el adulto que ha accedido al panel.
- La información que se muestre ha sido aprobada para el fin orientativo definido y no contiene categorías de progreso no acordadas.

### Decisiones pendientes

- Contenido concreto de las respuestas documentales y formato funcional de los resúmenes orientativos.
- La definición técnica de fuentes, controles, conservación, errores y operación corresponde a los responsables de capa, sin alterar este alcance.
