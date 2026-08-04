# FEAT-007 — Sección pública de documentación y contacto

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-08-03
- **Historia de usuario:** Como persona adulta de la familia o visitante, quiero consultar la documentación pública de My Friend Nubi y poder enviar un comentario sin acceder al panel parental ni exponer datos de menores.
- **Depende de:** FEAT-002, FEAT-004 y ADR-017.

## 1. Objetivo y valor para la familia

Ofrecer una referencia pública, clara y separada del panel parental para que las familias conozcan My Friend Nubi y sus límites. La sección facilita la orientación inicial y un canal de comentarios, sin transformar la documentación en una experiencia infantil, un espacio social ni un canal de datos familiares.

La documentación mantiene la decisión vigente de ADR-017: es **estática y sin búsqueda**. Su actualización requiere una nueva publicación de la aplicación; no se incorpora una gestión dinámica de contenido en este alcance.

## 2. Hechos, decisiones y exclusiones de contenido

### Hechos observados

- Home ya ofrece acceso público e interno a Documentación.
- Documentación también figura dentro de la navegación del panel parental.
- El panel parental requiere PIN, mientras que la documentación pública no solicita autenticación.

### Decisiones confirmadas

- Documentación es una vista independiente del panel parental, con navegación lateral propia.
- Se puede abrir desde Home, desde el panel parental y mediante su URL pública directa.
- La navegación lateral muestra, en este orden: **Quién soy**, **Primeros pasos**, **Agentes AI**, **Minijuegos** y **Contacto**.
- Solo se muestra la acción **«Volver»** cuando la persona llega desde el panel parental; esta devuelve a la sección parental exacta desde la que se abrió la documentación.
- Contacto incluye un campo de texto para comentarios, sugerencias o errores y no permite adjuntar imágenes ni archivos en esta versión.
- El detalle editorial de las secciones informativas se definirá en especificaciones posteriores.

## 3. Actores y escenarios de uso

### Persona que entra desde Home o una URL pública

1. Abre la documentación desde Home o directamente en el navegador.
2. Consulta una de las cinco secciones mediante la navegación propia.
3. No se le pide PIN, ni se muestran controles, datos o estado del panel parental.
4. No ve la acción «Volver».

### Adulto que entra desde el panel parental

1. Desde una sección del panel parental abre Documentación.
2. Consulta la documentación mediante su navegación propia.
3. Ve «Volver» y, al elegirlo, retorna a la sección parental desde la que accedió.

### Persona adulta que envía un comentario

1. Abre Contacto y encuentra un textarea y la finalidad del canal.
2. Lee el aviso de no incluir datos de menores, nombres, PIN ni otra información privada.
3. Confirma que es una persona adulta responsable y acepta la información sobre el uso del mensaje.
4. Escribe y envía un comentario, sugerencia o error sin adjuntar archivos.
5. Recibe una confirmación o un aviso de que el envío no se ha podido completar, sin revelar datos de otras personas ni contenido protegido.

## 4. Requisitos funcionales y no funcionales

1. La documentación debe ser accesible públicamente sin autenticación desde sus tres vías de entrada acordadas.
2. Debe presentarse fuera de la estructura y opciones del panel parental y tener una navegación lateral propia.
3. La navegación debe incluir exactamente las cinco secciones y en el orden confirmado.
4. El contenido de una sección debe poder consultarse sin depender solo de iconos, color o ubicación de la navegación.
5. «Volver» solo debe estar disponible si el acceso procedió del panel parental y debe devolver a su sección de origen.
6. La entrada directa por URL y el acceso desde Home no deben presentar «Volver» ni inferir una sesión parental.
7. Contacto debe ofrecer un textarea para el envío de comentarios, sugerencias o errores.
8. Contacto no debe solicitar nombres, perfiles infantiles, progreso, PIN, conversaciones, datos de contacto ni otros datos personales como condición de uso.
9. Antes del envío, Contacto debe mostrar un aviso claro que prohíba incluir datos de menores, nombres, PIN o información privada; debe requerir la confirmación de persona adulta responsable y la aceptación informada de la finalidad del mensaje.
10. Contacto no debe admitir adjuntos de imágenes, audio, vídeo ni archivos.
11. La documentación y el formulario deben ser usables en móvil y tableta, con controles táctiles amplios, etiquetas visibles y texto dirigido a adultos.
12. La sección no debe contener publicidad, perfilado comercial, contenido público aportado por usuarios, comparativas infantiles ni mecanismos persuasivos dirigidos a menores.
13. El contenido sigue siendo estático y no incluye búsqueda, edición desde la aplicación ni actualización sin nueva publicación de la aplicación.

## 5. Criterios de aceptación verificables

1. Desde Home se abre la documentación sin solicitar PIN.
2. Desde una URL pública directa se abre la documentación sin solicitar PIN.
3. Desde el panel parental se abre la misma sección pública de documentación sin exponer el resto de las opciones parentales.
4. La navegación lateral propia contiene las cinco etiquetas acordadas en el orden especificado.
5. Cada etiqueta puede identificarse sin depender exclusivamente de un icono o color.
6. Al acceder desde Home o una URL directa, «Volver» no se muestra.
7. Al acceder desde una sección del panel parental, «Volver» se muestra y lleva a esa misma sección parental.
8. Contacto permite introducir texto y no ofrece controles para adjuntar archivos.
9. Antes de poder enviar un mensaje, se muestran el aviso de privacidad, la confirmación de persona adulta y la información de finalidad acordados.
10. La pantalla de Contacto no solicita datos personales o infantiles como campos obligatorios.
11. Ante éxito o imposibilidad de envío, se comunica un estado comprensible sin exponer datos de terceros ni contenido protegido.
12. La documentación no ofrece buscador, herramientas de edición ni actualización de contenido desde la aplicación.

## 6. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Navegación pública, sidebar propio, retorno contextual y estados comprensibles en móvil y tableta.
- Accesibilidad de etiquetas, controles táctiles, textarea, avisos y confirmaciones.

### Contenido

- Redacción, revisión y vigencia de las cinco secciones estáticas.
- Claridad del aviso de Contacto y de la explicación de finalidad para adultos.

### Backend, datos, seguridad y privacidad

- Recepción, acceso autorizado, conservación y eliminación de los mensajes de Contacto conforme a minimización de datos.
- Protección frente a contenido indebido, datos personales enviados por error y exposición de mensajes entre familias.
- Información de privacidad y base de tratamiento aplicable al canal público antes de su puesta a disposición.

### Dependencias de producto conocidas

- Acceso público a documentación desde Home (FEAT-002).
- Entrada desde la navegación del panel parental (FEAT-004).
- Decisión de documentación estática y sin búsqueda (ADR-017).

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA

### Privacidad y seguridad infantil

- La documentación no muestra ni solicita datos familiares o infantiles.
- El textarea es un riesgo de introducción accidental de información sensible; el aviso previo, la confirmación adulta y la información de finalidad son requisitos bloqueantes del canal.
- Los mensajes de Contacto no son públicos, no se comparten entre familias ni se utilizan para publicidad, perfilado o entrenamiento de sistemas de IA.
- La recepción y tratamiento de mensajes requiere validación específica antes de activarse.

### Accesibilidad y experiencia

- La sección está dirigida a adultos, pero debe mantener lectura clara, contraste suficiente, etiquetas visibles y objetivos táctiles amplios.
- No debe requerir orientación concreta del dispositivo ni depender de iconos, color o memoria para navegar.
- No introduce presión temporal, recompensas, competición ni evaluación del niño.

### Límites de IA

- La sección Agentes AI solo informará de capacidades y límites que se aprueben posteriormente.
- Contacto no es un chatbot ni debe generar respuestas automáticas de IA.
- La funcionalidad no debe reutilizar los mensajes para entrenar, evaluar o personalizar agentes.

## 8. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Redacción y aprobación editorial detallada de Quién soy, Primeros pasos, Agentes AI y Minijuegos.
- Búsqueda, edición dentro de la aplicación, publicación dinámica y actualización de contenido sin una nueva publicación de la aplicación.
- Adjuntos y cualquier canal de contacto distinto del textarea.
- Respuesta individual a mensajes, soporte conversacional, chatbot, comunidad o publicación de comentarios.

### Riesgos

- Una persona puede ignorar el aviso e incluir información sensible en texto libre. Los responsables de privacidad y seguridad deben validar si el riesgo residual permite activar el envío.
- Un retorno incorrecto puede llevar al adulto a una sección parental distinta de la que abandonó, generando confusión o exposición innecesaria.
- La documentación estática puede quedar desactualizada entre publicaciones; contenido debe revisar su vigencia antes de cada publicación.

### Supuestos

- La respuesta «Sí» a la pregunta sobre el retorno se interpreta como retorno a la sección parental exacta de origen.
- Mantener ADR-017 significa que el contenido no se actualizará de forma dinámica ni sin una nueva publicación de la aplicación.

### Decisiones pendientes

- El producto debe aprobar el contenido concreto de cada sección en especificaciones posteriores.
- Los responsables de contenido, privacidad, seguridad, backend y datos deben validar el tratamiento completo de mensajes antes de habilitar Contacto, sin ampliar los datos solicitados ni alterar sus límites funcionales.
