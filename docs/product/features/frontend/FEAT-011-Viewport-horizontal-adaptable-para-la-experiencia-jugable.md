# FEAT-011 — Viewport horizontal adaptable para la experiencia jugable

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-09-05
- **Historia de usuario:** Como niño que usa My Friend Nubi en móvil o tableta, quiero que la experiencia jugable se vea completa y estable en horizontal, para reconocer lo importante y jugar sin recortes ni cambios confusos.
- **Depende de:** FEAT-010 — Entrada a GameView y carga inicial; ADR-019 — Rediseño portrait real.

## 1. Objetivo y valor para la familia

Definir un viewport común para toda la experiencia jugable infantil: carga inicial, estado base de `GameView`, World Map y futuros minijuegos. La experiencia se ofrece exclusivamente en horizontal y preserva siempre la escena esencial, sus apoyos visuales y los futuros objetivos táctiles.

La familia obtiene un comportamiento predecible en dispositivos móviles y tabletas, sin forzar la orientación del dispositivo ni ocultar contenido relevante al niño.

## 2. Hechos, supuestos y decisiones confirmadas

### Hechos observados

- El producto se diseña primero para móviles y tabletas.
- ADR-019 descartó los mecanismos frágiles de rotación o escalado para simular una orientación y dejó `GameView` pendiente de una decisión propia.
- La experiencia infantil debe ser estable, comprensible y accesible para niños de 3–4 años.

### Decisiones confirmadas

- Toda la experiencia jugable infantil se presenta exclusivamente en orientación horizontal: carga, `GameView`, World Map y minijuegos futuros.
- Si el dispositivo está en vertical, se muestra un estado de orientación requerida, no se fuerza la orientación del dispositivo.
- El estado de orientación requerida incluye una indicación visual amable de Nubi para girar el dispositivo y no depende solo de texto, color o sonido.
- Mientras se muestra dicho estado, no comienza una nueva escena jugable. Si el niño ya estaba en una escena, su estado se conserva y vuelve a mostrarse al recuperar la orientación horizontal.
- Las escenas se ajustan al viewport horizontal disponible y se prioriza que se vea toda la escena esencial. Se aceptan márgenes decorativos cuando sean necesarios; no se recortan Nubi, indicaciones infantiles, elementos interactivos ni futuros controles de juego.
- Las escenas usan un lienzo visual de referencia común para que los recursos de imagen, audio y animación mantengan proporciones coherentes entre tamaños de pantalla. El tamaño numérico, formatos y método de escalado no son decisiones de producto y deben ser validados por frontend y contenido.
- Un cambio de tamaño u orientación dentro de una sesión no reinicia la carga, la escena ni la sesión infantil, ni produce penalización o pérdida de progreso.

### Supuestos explícitos

- El estado de orientación requerida no se presenta como un error ni una instrucción evaluativa; es una pausa visual de continuidad.
- Las escenas futuras deberán declarar qué elementos forman parte de su contenido esencial antes de que frontend y contenido definan sus composiciones visuales.

## 3. Actores y escenarios de uso

### Entrada a juego con el dispositivo en horizontal

1. El niño o adulto selecciona un perfil habilitado.
2. El dispositivo está en horizontal.
3. La carga y la experiencia jugable se muestran adaptadas al espacio horizontal disponible.

### Entrada a juego con el dispositivo en vertical

1. El niño o adulto selecciona un perfil habilitado.
2. El dispositivo está en vertical.
3. Se muestra el estado visual de Nubi que indica amablemente girar el dispositivo.
4. Al pasar a horizontal, se inicia la carga o se muestra la escena que correspondía, sin exigir una acción adicional al niño.

### Cambio de orientación durante una escena

1. El niño está viendo una escena jugable horizontal.
2. El dispositivo cambia a vertical.
3. La escena deja de mostrarse y aparece el estado de orientación requerida, sin reiniciar ni penalizar al niño.
4. Al volver a horizontal, se recupera la misma escena en el estado en que se encontraba.

### Ajuste a distintos tamaños horizontales

1. La familia usa un móvil o tableta en horizontal con un tamaño o proporción diferente.
2. La escena se ajusta al viewport disponible.
3. Todo el contenido esencial permanece visible y los márgenes, si aparecen, son solo decorativos.

## 4. Requisitos funcionales y no funcionales

1. La experiencia jugable infantil debe requerir orientación horizontal en carga, `GameView`, World Map y minijuegos futuros.
2. La aplicación no debe forzar la orientación del dispositivo para iniciar o continuar la experiencia jugable.
3. En orientación vertical debe mostrarse un estado de orientación requerida con Nubi y una indicación visual comprensible para girar el dispositivo.
4. La indicación debe ser amable, breve y no evaluativa; no debe mostrar errores técnicos, temporizadores, culpa ni presión.
5. En vertical no debe iniciarse una escena jugable nueva.
6. Si el cambio a vertical sucede dentro de una escena, al recuperar la orientación horizontal debe restaurarse la misma escena y estado visible, sin reiniciar la sesión ni la carga.
7. En cualquier tamaño horizontal debe mostrarse todo el contenido esencial de la escena.
8. Los márgenes permitidos para conservar la escena completa solo pueden contener decoración; no pueden ocultar ni sustituir información, acciones o elementos infantiles esenciales.
9. Los recursos visuales y de animación deben conservar una proporción visual coherente respecto al lienzo común de referencia.
10. Los futuros objetivos de interacción deben seguir siendo claros, amplios y alcanzables en los viewports horizontales admitidos.
11. La adaptación de viewport debe respetar las preferencias de accesibilidad aplicables, incluidas las que reduzcan movimiento, sin impedir comprender el estado de orientación requerida.

## 5. Criterios de aceptación verificables

1. Con el dispositivo en horizontal, la carga inicial y el estado base de `GameView` se muestran sin indicación de giro.
2. Con el dispositivo en vertical, no se muestra una escena jugable activa y se muestra la indicación visual de Nubi para girar el dispositivo.
3. La indicación de giro se comprende sin depender exclusivamente de texto, color o sonido y no presenta lenguaje de error, castigo o evaluación.
4. La aplicación no exige ni intenta forzar la orientación del dispositivo para mostrar el juego.
5. Si el dispositivo pasa de horizontal a vertical durante una escena, se muestra el estado de orientación requerida sin reiniciar ni mostrar una pérdida de sesión al niño.
6. Si después vuelve a horizontal, se recupera la misma escena y estado visible que existían antes del cambio.
7. En los tamaños horizontales validados, Nubi, las indicaciones, los elementos interactivos y los controles de la escena permanecen íntegramente visibles.
8. Cuando existan márgenes por diferencia de proporción, contienen exclusivamente decoración y no alteran el significado ni la interacción de la escena.
9. Los recursos de una misma escena no aparecen deformados por la adaptación a distintos viewports horizontales.
10. Los futuros controles de juego se mantienen utilizables mediante objetivos táctiles amplios en los tamaños horizontales admitidos.

## 6. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Presentación estable del estado de orientación requerida y restauración de escena al volver a horizontal.
- Conservación de toda la información y acciones esenciales, sin recortes ni deformación visibles.
- Usabilidad táctil en móviles y tabletas en horizontal, incluidos cambios de tamaño durante una sesión.
- Aplicación de preferencias de accesibilidad relativas al movimiento.

### Contenido y diseño

- Definición de contenido esencial y decoración prescindible para cada escena.
- Adecuación por edad y comprensión no lectora de la indicación visual de giro.
- Propuesta del lienzo de referencia y de los recursos fuente que permita mantener proporciones, sin que producto fije tamaños o formatos técnicos.

### Backend, sesión y seguridad/privacidad

- Confirmar que conservar una escena durante un cambio de orientación no altera indebidamente la sesión, el control parental ni la continuidad autorizada.
- Validar que no se exponen datos infantiles o parentales nuevos en el estado de orientación requerida.

### Dependencias de producto conocidas

- FEAT-010 — Entrada a GameView y carga inicial.
- ADR-019 — Rediseño portrait real.
- Definiciones futuras de World Map y minijuegos.

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- El estado de orientación requerida no muestra nombres, progreso, controles parentales, causas de interrupción ni datos técnicos.
- No recoge datos adicionales del menor ni realiza perfilado a partir de la orientación o tamaño del dispositivo.

### Experiencia infantil y accesibilidad

- La orientación requerida se comunica como una ayuda visual amable, no como un fallo atribuible al niño.
- La escena completa prevalece sobre llenar el dispositivo: ningún elemento esencial queda fuera de pantalla.
- La recuperación tras volver a horizontal conserva el contexto del niño y evita repeticiones involuntarias.
- La indicación y los futuros controles son comprensibles sin lectura, audio o dependencia exclusiva de color.

### Límites de IA

- Esta funcionalidad no activa IA, conversaciones ni generación de contenido.
- La presencia visual de Nubi en la indicación de giro no implica una interacción conversacional ni solicita datos personales.

## 8. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Tamaño numérico, formatos, empaquetado, carga, almacenamiento o mecanismo de escalado de assets.
- Forzar programáticamente la orientación del dispositivo.
- Rediseño de la experiencia jugable para funcionar en vertical.
- Diseño detallado, contenido verbal o animación concreta de Nubi para la indicación de giro.
- Mecánicas, reglas, contenido y controles específicos de World Map o minijuegos.

### Riesgos

- Algunos niños pueden no poder o no querer girar el dispositivo sin ayuda. La indicación debe ser clara para el acompañamiento adulto y no debe culpabilizar al niño.
- Una definición imprecisa de elementos esenciales podría provocar recortes de contenido relevante en escenas futuras. Contenido y diseño deben declararlos antes de cerrar cada escena.
- Un cambio de orientación mal resuelto puede hacer que el niño crea que ha perdido su actividad. Frontend y sesión deben validar la continuidad percibida.

### Decisiones pendientes

- El producto no fija el diseño exacto, texto ni duración de la indicación de giro; contenido y frontend deben proponerlos para validación de producto.
- Los responsables técnicos deben determinar los tamaños de referencia, los viewports horizontales que validar, la preservación de estado y la resolución de cambios de tamaño, sin modificar los comportamientos acordados.
