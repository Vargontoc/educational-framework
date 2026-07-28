# FEAT-002 — Pantalla principal y accesos iniciales

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-07-23; ampliada el 2026-07-28
- **Historia de usuario:** La familia confirma la pantalla principal como punto de entrada para registrar la única familia, continuar hacia la selección o alta de niños y acceder a las opciones parentales o a la documentación.

## 1. Objetivo y valor para la familia

Ofrecer una entrada simple, amable y predecible a My Friend Nubi en tabletas y móviles. La pantalla debe orientar a la familia según el estado inicial de la aplicación sin exponer datos infantiles ni mezclar los controles adultos con la acción principal destinada a iniciar la experiencia infantil acompañada.

## 2. Hechos, supuestos y decisión confirmada

### Hechos observados

- My Friend Nubi es una aplicación para una única familia.
- Home es el punto de entrada para el registro familiar, la selección de niños y el acceso al panel parental.
- El panel parental se protege mediante el PIN familiar.
- La documentación forma parte de la aplicación para que siga disponible si en el futuro se distribuye como APK.

### Decisión confirmada

- La Home muestra un fondo de estética infantil, el avatar de Nubi proporcionado por el producto en el centro y una acción principal superpuesta al avatar.
- La acción principal cambia según exista o no una familia registrada.
- La esquina superior derecha contiene el acceso a documentación y, solo cuando hay familia registrada, el acceso a configuración parental.
- Los nombres familiares de más de 50 caracteres se muestran truncados visualmente con puntos suspensivos.
- El modal de registro familiar se organiza en dos pasos consecutivos: nombre de familia y creación con repetición del PIN familiar.
- Tras registrar correctamente la familia, la persona vuelve a Home, que muestra la bienvenida correspondiente; el alta de niños no forma parte de este flujo.
- El nombre de familia es libre. No se incorpora ningún límite ni regla de contenido adicional de producto; se conserva únicamente el truncado visual de la bienvenida ya acordado.

### Supuesto explícito

- La consulta de existencia y nombre de la única familia se realiza antes de presentar la acción principal. Su definición técnica queda pendiente del ámbito responsable.

## 3. Actores y escenarios de uso

### Adulto sin familia registrada

1. Abre la aplicación y llega a Home.
2. Ve la acción principal con el texto **«Registrar familia»**.
3. La pulsa para abrir el modal de registro familiar.
4. En el primer paso introduce el nombre de la familia y continúa.
5. En el segundo paso crea el PIN familiar de cuatro dígitos numéricos y lo repite.
6. Al confirmar un PIN coincidente, la familia queda registrada y vuelve a Home, donde ve la bienvenida a su familia.

### Familia registrada que inicia la experiencia

1. Abre la aplicación y llega a Home.
2. Ve la acción principal con el texto **«Bienvenida familia &lt;nombre de familia&gt;»**.
3. La pulsa para abrir el modal desde el que se selecciona un niño o se inicia el alta de un niño.

### Adulto que accede a opciones parentales

1. Con una familia registrada, identifica el botón de configuración en la esquina superior derecha.
2. Lo pulsa y se dirige al acceso al panel parental.
3. El acceso posterior conserva el requisito vigente de verificación mediante PIN; esta funcionalidad no lo modifica.

### Persona que consulta la documentación

1. Desde cualquier estado de Home, identifica el botón de interrogación en la esquina superior derecha.
2. Lo pulsa y se dirige al apartado de documentación integrado en la aplicación.

## 4. Requisitos funcionales

1. Home debe presentar el avatar de Nubi proporcionado por el producto como elemento visual central.
2. Debe existir una única acción principal superpuesta al avatar.
3. Cuando no exista una familia registrada, la acción principal debe mostrar exactamente **«Registrar familia»** y abrir el modal de registro familiar al pulsarse.
4. Cuando exista una familia registrada, la acción principal debe mostrar exactamente el patrón **«Bienvenida familia &lt;nombre de familia&gt;»** y abrir el modal de selección o alta de niños al pulsarse.
5. La presentación visual del nombre de familia en la bienvenida debe limitarse a 50 caracteres; si se supera el límite, debe truncarse con puntos suspensivos.
6. Debe mostrarse un acceso a configuración en la esquina superior derecha únicamente cuando exista una familia registrada. Debe dirigir al acceso al panel parental, sin omitir el PIN.
7. Debe mostrarse un acceso de interrogación a la documentación en la esquina superior derecha, tanto si existe familia como si no.
8. El acceso de documentación debe dirigir a un apartado interno de la aplicación, no a una URL externa.
9. El fondo debe mantener una estética infantil acorde al producto, sin competir visualmente con el avatar ni con la acción principal.
10. El modal de registro familiar debe presentar dos pasos claramente diferenciados y permitir avanzar desde el nombre de familia a la configuración del PIN.
11. El primer paso debe solicitar un nombre de familia. El producto no define límites de longitud ni reglas de contenido adicionales para ese nombre.
12. El segundo paso debe solicitar la creación de un PIN familiar de exactamente cuatro dígitos numéricos y una segunda introducción del mismo PIN para confirmarlo.
13. La creación de la familia solo se confirma cuando ambos valores de PIN coinciden. Si no coinciden, debe informarse de forma clara y respetuosa, sin crear la familia.
14. La persona debe poder cancelar el modal antes de la confirmación; al hacerlo, vuelve a Home sin que se cree una familia.
15. Después de confirmar el registro, el modal se cierra y Home debe reflejar el estado de familia registrada mediante la bienvenida. No debe abrirse automáticamente el alta de niños.

## 5. Requisitos no funcionales de producto

- La experiencia debe ser viable en móvil y tableta y priorizar la interacción táctil.
- La acción principal y los dos accesos superiores deben ser identificables, alcanzables y distinguibles entre sí.
- El stepper debe comunicar de forma comprensible el paso actual y que existen dos pasos, sin requerir lectura por parte de un niño.
- Los campos del flujo de registro están destinados a la configuración inicial adulta; no deben solicitar datos de menores.
- El botón de interrogación y el de configuración deben disponer de una denominación comprensible para personas que no interpreten el icono; el icono no será su única forma de comunicación.
- La pantalla no debe mostrar publicidad, avisos persuasivos, progreso infantil, comparativas ni clasificaciones.
- La Home no debe requerir que el niño lea para iniciar su experiencia; el avatar y la ubicación consistente de la acción principal actúan como apoyo visual.

## 6. Criterios de aceptación verificables

1. Sin una familia registrada, Home muestra «Registrar familia» y al pulsarlo se abre el modal de registro.
2. Con una familia registrada cuyo nombre tenga 50 caracteres o menos, Home muestra «Bienvenida familia» seguido del nombre completo.
3. Con una familia registrada cuyo nombre tenga más de 50 caracteres, la bienvenida muestra solo los primeros 50 caracteres del nombre y puntos suspensivos.
4. Con una familia registrada, al pulsar la bienvenida se abre el modal previsto para seleccionar un niño o iniciar su alta.
5. Sin una familia registrada no se muestra el acceso a configuración.
6. Con una familia registrada se muestra el acceso a configuración y este dirige al acceso protegido por PIN del panel parental.
7. En ambos estados se muestra el acceso de interrogación y dirige al apartado interno de documentación.
8. El avatar de Nubi está visualmente centrado y la acción principal se presenta superpuesta a él sin impedir identificarla o pulsarla.
9. Los controles de Home se pueden identificar sin depender solo de color ni de la interpretación de iconos.
10. Home no muestra nombres de niños, información de progreso ni datos parentales distintos del nombre de familia indicado en la bienvenida.
11. Al pulsar «Registrar familia», el modal muestra inicialmente el paso para introducir el nombre de familia.
12. Tras introducir el nombre de familia, el modal presenta el paso de creación y repetición del PIN de cuatro dígitos numéricos.
13. Si los dos valores de PIN no coinciden, se muestra un aviso comprensible y la familia no queda registrada.
14. Si los dos valores de PIN coinciden y se confirma el registro, al volver a Home se muestra «Bienvenida familia <nombre de familia>» y no se abre el alta de niños.
15. Si se cancela el flujo antes de confirmarlo, Home continúa mostrando «Registrar familia» y no se conserva una familia registrada.

## 7. Ámbitos que deben validar los responsables y dependencias conocidas

### Frontend

- Validar la presentación táctil y adaptable de la Home en móvil y tableta.
- Validar la comunicación visual y accesible de los controles mediante icono y denominación.
- Validar la navegación hacia el acceso parental y el apartado interno de documentación.
- Validar los estados de carga, indisponibilidad o error al conocer si existe familia, con un mensaje comprensible y sin revelar información familiar.
- Validar que el stepper, las indicaciones de PIN no coincidente, la cancelación y el retorno a Home sean comprensibles y utilizables en móvil y tableta.

### Backend y datos

- Validar qué información mínima y autorizada está disponible para determinar si existe familia y mostrar su nombre.
- Validar que la condición monofamiliar se respeta y que Home no puede mostrar datos de otra familia.
- Validar el alta de una familia con el nombre indicado y un PIN confirmado, sin definir en esta especificación los mecanismos técnicos aplicables.

### Seguridad y privacidad

- Validar que el acceso desde Home al panel parental conserva el control por PIN.
- Validar que el nombre de familia mostrado no queda expuesto fuera de la instalación y finalidad familiar autorizada.

### Contenido

- Validar que el apartado interno de documentación es comprensible y adecuado para adultos, y que su contenido no solicita ni divulga datos de menores.

### Dependencias de producto conocidas

- Modal de registro familiar.
- Creación y confirmación de PIN familiar, conforme al requisito vigente de cuatro dígitos numéricos.
- Modal de selección o alta de niños.
- Acceso con PIN al panel parental.
- Apartado interno de documentación.

## 8. Privacidad, seguridad infantil, accesibilidad y límites de IA

### Privacidad y seguridad infantil

- Solo se muestra el nombre de la familia registrada y únicamente en el contexto local de esa familia; no se muestran nombres de niños ni progreso.
- Configuración es un control de adultos y solo aparece tras el registro familiar; el acceso al contenido parental sigue requiriendo PIN.
- El alta familiar recoge exclusivamente el nombre de familia y el PIN necesario para la función acordada; no incluye datos de menores, contacto, publicidad, perfilado ni reutilización de información.
- El PIN es un dato sensible de control parental: nunca debe mostrarse como texto legible durante su introducción ni volver a mostrarse después del registro. El tratamiento técnico de este dato requiere validación de backend y seguridad.
- La documentación debe estar integrada en la aplicación y no debe introducir comunicaciones públicas, publicidad ni solicitudes de datos familiares o infantiles.

### Accesibilidad y experiencia infantil

- La acción principal debe ser amplia, clara y fácil de pulsar con acompañamiento adulto o autonomía acorde a 3–4 años.
- La experiencia evita temporizadores, castigos, presión, comparativas y mensajes que evalúen al niño.
- El significado de los accesos superiores no depende exclusivamente de color o iconos.
- El stepper debe mantener controles táctiles claros y una secuencia predecible. No forma parte de la experiencia de juego infantil ni debe incorporar mensajes evaluativos o persuasivos.

### Límites de IA

- Esta funcionalidad no activa ni expone interacciones de IA.

## 9. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Selección, alta y edición de niños.
- Definición del panel parental, de su PIN o de sus secciones internas.
- Contenido detallado de la documentación.
- Personalización del avatar, mensajes de Nubi, audio, progreso y minijuegos.
- Cualquier enlace a documentación externa.

### Riesgos de producto

- Mostrar el nombre familiar en una pantalla visible puede revelar un dato identificativo a personas próximas al dispositivo. Se limita la exposición al nombre de familia y se excluyen datos infantiles; seguridad y privacidad deben validar el riesgo residual.
- Un fondo excesivamente llamativo o una acción superpuesta mal resuelta puede dificultar reconocer la acción principal. Frontend y contenido deben validarlo desde la experiencia de 3–4 años.
- Un PIN creado con un error impediría al adulto acceder posteriormente al panel parental. La repetición obligatoria del PIN reduce este riesgo, sin eliminar la necesidad de que backend y seguridad validen el comportamiento del control parental.

### Decisiones pendientes

- El producto no establece una validación adicional para el nombre familiar más allá de solicitarlo; frontend, backend y datos deben señalar si existe alguna limitación necesaria que afecte a la experiencia o a la protección de datos antes de aplicarla.
- Los responsables técnicos deben definir cómo se resuelve la disponibilidad de datos, el alta única, el tratamiento seguro del PIN, la navegación y los estados de error, sin alterar los comportamientos aquí acordados.
