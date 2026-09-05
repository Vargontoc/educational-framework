# FEAT-010 — Entrada a GameView y carga inicial

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-09-05
- **Historia de usuario:** Como niño que selecciona un perfil habilitado, quiero entrar en una experiencia de juego tranquila y comprensible mientras se prepara, sin tener que interpretar errores ni esperas técnicas.
- **Depende de:** FEAT-003 — Selección y alta de perfiles infantiles; estado de acceso del perfil infantil.

## 1. Objetivo y valor para la familia

Establecer la entrada mínima y segura a `GameView` antes de implementar el paseo narrativo o los minijuegos. La familia obtiene una transición predecible desde la selección de perfil y el niño recibe una espera visual amable, adecuada a su edad, antes de ver un estado base de la experiencia.

Esta entrega deja preparado el punto de carga común que podrán usar futuras escenas infantiles, sin definir todavía dichas escenas ni su comportamiento jugable.

## 2. Hechos, supuestos y decisiones confirmadas

### Hechos observados

- La selección de un perfil existente continúa hacia su experiencia de juego (FEAT-003).
- Un perfil bloqueado no puede acceder al juego (ADR-022).
- El juego debe seguir siendo utilizable cuando el audio o el NPC no estén disponibles (README).
- El World Map y los minijuegos son experiencias posteriores y no están definidos como parte de esta entrega.

### Decisiones confirmadas

- Al seleccionar un perfil habilitado, la aplicación entra en `GameView` y muestra una carga inicial infantil.
- La carga termina en un estado visual base de `GameView`, no interactivo y sin loop jugable.
- Si el perfil está bloqueado, la aplicación avisa de forma neutral y permanece en el selector de perfiles; no entra en `GameView`.
- La carga inicial muestra un placeholder visual preparado para incorporar posteriormente una animación de Nubi cuando ese recurso esté disponible.
- La carga se concibe como punto común para preparar los recursos necesarios de futuras escenas —imágenes, audio y animaciones, incluidos los recursos de personaje que el contenido acuerde—, sin determinar en esta especificación su tratamiento técnico.

### Supuestos explícitos

- El aviso de perfil bloqueado se muestra en el contexto del selector de perfiles y no revela la causa del bloqueo ni opciones parentales.
- El estado visual base posterior a la carga no presenta destinos, elementos de descubrimiento, métricas ni acciones de juego hasta que una funcionalidad posterior los defina.

## 3. Actores y escenarios de uso

### Niño o adulto que selecciona un perfil habilitado

1. Desde el selector de perfiles, pulsa un perfil habilitado.
2. Entra en `GameView` y ve la carga inicial con su placeholder visual.
3. Cuando finaliza la preparación inicial, ve el estado visual base no interactivo del juego.

### Niño o adulto que selecciona un perfil bloqueado

1. Desde el selector de perfiles, pulsa un perfil bloqueado.
2. Recibe un aviso neutral de que ese perfil no puede entrar al juego en ese momento.
3. Permanece en el selector de perfiles y puede escoger otro perfil habilitado.

### Recurso opcional no disponible

1. Un perfil habilitado entra en `GameView`.
2. Algún recurso opcional de audio o del NPC no está disponible.
3. La experiencia llega al estado visual base sin requerir audio ni interacción del NPC.

## 4. Requisitos funcionales y no funcionales

1. La selección de un perfil habilitado debe dirigir a `GameView`.
2. Antes de mostrar el estado visual base, `GameView` debe presentar un estado de carga inicial.
3. El estado de carga debe incluir un placeholder visual infantil que pueda sustituirse o complementarse en el futuro con una animación de Nubi, sin modificar el flujo infantil acordado.
4. Al finalizar la carga, debe mostrarse un estado visual base de `GameView` sin controles ni acciones jugables.
5. La selección de un perfil bloqueado no debe abrir `GameView`.
6. Ante un perfil bloqueado, debe mostrarse un aviso neutral y la persona debe permanecer en el selector de perfiles.
7. El aviso no debe explicar el motivo del bloqueo, sugerir culpa o castigo, ni mostrar controles parentales.
8. La experiencia inicial debe poder alcanzar el estado visual base aunque audio o NPC no estén disponibles.
9. La carga y el estado base no deben mostrar porcentajes, puntuaciones, progreso, comparativas, temporizadores ni mensajes que valoren al niño.
10. Los estados visibles deben ser comprensibles en móvil y tableta, con apoyos visuales y sin depender exclusivamente de texto, color o sonido.

## 5. Criterios de aceptación verificables

1. Al seleccionar un perfil habilitado desde el selector, se muestra `GameView` con una carga inicial antes del estado visual base.
2. La carga inicial contiene un placeholder visual identificable y no requiere que el niño lea para comprender que la experiencia se está preparando.
3. Cuando concluye la carga, `GameView` muestra un estado visual base sin elementos interactivos de mapa ni controles de minijuego.
4. El estado base no contiene puntuaciones, indicadores de progreso, temporizadores, clasificaciones ni mensajes evaluativos.
5. Al seleccionar un perfil bloqueado, `GameView` no se abre.
6. Tras seleccionar un perfil bloqueado, se muestra un aviso neutral y la vista continúa siendo el selector de perfiles.
7. El aviso de bloqueo no muestra causa, datos parentales, configuración ni lenguaje de castigo.
8. La ausencia de audio o del NPC no impide llegar al estado visual base.
9. Los estados de carga y bloqueo se pueden diferenciar sin depender únicamente de color, texto o sonido y son utilizables en móvil y tableta.

## 6. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Comprensión infantil del placeholder de carga, del estado visual base y del aviso neutral.
- Tamaño y claridad táctil del selector y de los mensajes en móvil y tableta.
- Continuidad visual entre selector, carga y estado base, sin incorporar interacción jugable no acordada.

### Backend, sesión y seguridad/privacidad

- Disponibilidad del estado autorizado y vigente del perfil para distinguir acceso habilitado de bloqueado.
- Garantía de que un perfil bloqueado no acceda a la experiencia por rutas alternativas.
- Tratamiento de indisponibilidades o cambios de estado sin exponer información infantil o parental no necesaria.

### Contenido

- Adecuación por edad del placeholder visual y de la futura animación de Nubi.
- Disponibilidad y derechos de uso de los recursos visuales, sonoros y de animación que finalmente se incorporen.

### Audio, NPC e IA

- Confirmar que la ausencia de audio o NPC mantiene una experiencia visual comprensible.
- Cualquier contenido futuro del NPC deberá conservar los límites ya acordados: contexto de juego, mensajes breves apropiados por edad y ninguna solicitud de datos personales.

### Dependencias de producto conocidas

- FEAT-003 — Selección y alta de perfiles infantiles.
- ADR-022 — Gestión parental de perfiles infantiles.
- Definición posterior del World Map y de los minijuegos.

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- Solo un perfil habilitado puede entrar en la experiencia infantil.
- El aviso de bloqueo minimiza la información expuesta: no muestra causas, ajustes adultos, progreso ni otros datos del perfil.
- No se recogen ni muestran datos adicionales de menores durante la carga.
- No hay publicidad, perfilado, compartición de datos ni persuasión dirigida al niño.

### Experiencia infantil y accesibilidad

- La transición debe ser corta, predecible y amable, sin hacer que el niño espere una acción obligatoria.
- El placeholder ofrece apoyo visual; la comprensión no depende de lectura, color o audio.
- No hay competición, castigos, presión temporal ni evaluación.
- El estado base no interactivo evita que el niño interprete como disponible una actividad que aún no forma parte de la entrega.

### Límites de IA

- Esta entrega no activa IA, conversaciones ni generación de contenido.
- La futura animación de Nubi no implica diálogo ni intervención del NPC en esta funcionalidad.

## 8. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- World Map, biomas, destinos narrativos y elementos de descubrimiento.
- Minijuegos, loop jugable, adaptación de dificultad, tracking, puntuaciones, logros y dashboard.
- Contenido oral, diálogos, peticiones al NPC o generación de respuestas.
- Definición de recursos concretos, formatos, carga, almacenamiento, tratamiento de Spine, caché, reintentos, contratos, sesiones o mecanismos técnicos.
- Controles parentales para bloquear, desbloquear o explicar el bloqueo.

### Riesgos

- Una carga larga, visualmente ambigua o sin alternativa comprensible puede frustrar al niño. Frontend y contenido deben validarla con interacción infantil de 3–4 años.
- Si la comprobación de bloqueo no es consistente, un menor podría entrar en una experiencia que un adulto ha restringido. Seguridad, sesión y backend deben considerarlo un riesgo de acceso.
- Tratar audio o NPC como requisito de entrada contradice el requisito existente de continuidad del juego sin ellos.

### Decisiones pendientes

- El producto no define todavía el texto exacto, icono ni duración perceptible del aviso de bloqueo; deben proponerse para validación de producto antes de fijarlos si afectan a la comprensión infantil o parental.
- El producto no define el aspecto ni el contenido de la futura animación de Nubi; requiere una decisión de contenido antes de incorporarse.
- Los responsables técnicos deben determinar cómo se prepara y comunica la disponibilidad de recursos y del estado de acceso, sin alterar los comportamientos funcionales acordados.
