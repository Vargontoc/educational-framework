# FEAT-006 — Gestión parental de perfiles infantiles

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-07-31
- **Historia de usuario:** Como adulto autenticado, quiero consultar y gestionar los perfiles infantiles de mi familia, adaptar sus configuraciones individuales y controlar su acceso al juego para mantener una experiencia adecuada y bajo control parental.
- **Depende de:** ADR-022; FEAT-003 — Selección y alta de perfiles infantiles; FEAT-004 — Estructura visual y navegación del panel parental; FEAT-005 — Configuración global de audio, NPC y PIN; acceso parental mediante PIN vigente.

## 1. Objetivo y valor para la familia

Permitir que el adulto de la familia gestione perfiles infantiles desde una sección parental claramente separada del juego: consultar los perfiles, editar datos y preferencias individuales, terminar una sesión activa, bloquear temporalmente el acceso al juego, eliminar por completo un perfil y acceder a su dashboard como destino placeholder.

El ajuste visual individual pretende facilitar la participación en minijuegos relacionados con colores, sin evaluar ni diagnosticar al niño.

## 2. Actores y escenarios de uso

### Adulto que consulta perfiles infantiles

1. Accede a **Niños** dentro del panel parental mediante una sesión parental válida.
2. Ve los perfiles infantiles en una cuadrícula.
3. En cada tarjeta identifica avatar, nombre y una etiqueta con la duración transcurrida de la sesión actual mientras el niño está jugando.
4. Si el perfil tiene una sesión activa, puede elegir **Expulsar** y confirma la acción tras revisar su aviso.
5. Puede elegir **Bloquear** para impedir el acceso posterior al juego, conservando la información y progreso del perfil.
6. Pulsa la tarjeta para abrir la edición del perfil.
7. Puede pulsar **«Registrar niño»** bajo la cuadrícula para iniciar el mismo stepper de creación de perfiles ya disponible desde Home.

### Adulto que edita un perfil

1. Desde la tarjeta abre la edición y ve el breadcrumb **«Niños > [Nombre]»**.
2. Modifica nombre, fecha de nacimiento, avatar y los ajustes individuales disponibles de audio/NPC.
3. Consulta si algún ajuste individual está deshabilitado por una configuración global familiar y no puede modificarlo mientras permanezca deshabilitado.
4. Revisa o activa el ajuste visual para minijuegos de color, si corresponde.
5. Guarda los cambios, elimina el perfil previa confirmación o abre **Dashboard**.

### Adulto que ajusta la visualización de colores

1. En la edición del perfil encuentra la sección de accesibilidad visual.
2. Ve que el ajuste está inactivo cuando el perfil no tiene una configuración visual y activo cuando sí posee un valor.
3. Puede dejar el estado predeterminado sin ajuste o seleccionar manualmente un perfil de visualización de colores.
4. Consulta ejemplos compuestos solo por elementos simples y un aviso de que la sección no realiza diagnósticos y que debe consultar a un especialista ante dudas.

### Adulto que consulta el dashboard individual

1. Desde la edición pulsa **Dashboard**.
2. Llega al destino placeholder con breadcrumb **«Niños > [Nombre] > Dashboard»**.

## 3. Requisitos funcionales y no funcionales de producto

1. La sección **Niños**, sus perfiles, sus configuraciones y su dashboard solo están disponibles para un adulto con acceso parental válido.
2. La entrada de la sección muestra los perfiles infantiles de la familia en una cuadrícula adecuada para móvil y tableta.
3. Debe existir una acción **«Registrar niño»** bajo la cuadrícula. Debe iniciar el mismo stepper de creación de perfiles definido en FEAT-003, sin introducir una variante de alta distinta.
4. Cada tarjeta de perfil debe mostrar avatar, nombre y una etiqueta con la duración transcurrida de la sesión actual mientras el niño está jugando; la tarjeta completa permite abrir la edición de ese perfil. Esta duración no representa progreso, rendimiento ni capacidad.
5. Cada tarjeta debe incluir las acciones **«Expulsar»** y **«Bloquear»**.
6. **«Expulsar»** solo debe aparecer si el perfil tiene una sesión activa. Al seleccionarlo, debe aparecer una confirmación para el adulto que advierta que terminará esa sesión. La expulsión solo se aplica tras confirmación explícita.
7. **«Bloquear»** debe impedir que el perfil entre al juego y conservar su información y progreso para permitir un desbloqueo posterior. El efecto sobre un niño que ya está jugando no forma parte de esta funcionalidad.
8. Al seleccionar la tarjeta, la edición debe presentar el breadcrumb **«Niños > [Nombre]»**.
9. La edición debe permitir modificar nombre, fecha de nacimiento y avatar predefinido del perfil.
10. La fecha de nacimiento debe destinarse únicamente a adecuar la experiencia jugable a la edad del niño.
11. La edición debe presentar los ajustes individuales de audio del NPC y su valor porcentual previstos por el producto.
12. Cuando los ajustes globales familiares correspondientes estén deshabilitados, los ajustes individuales de audio/NPC deben seguir siendo visibles, no deben poder modificarse y deben mostrar una etiqueta o texto que indique que están deshabilitados a nivel familiar.
13. La edición debe incluir las acciones **«Guardar cambios»**, **«Eliminar»** y **«Dashboard»**.
14. **«Eliminar»** debe requerir confirmación explícita del adulto y, tras confirmarse, debe eliminar el perfil y toda la información relacionada.
15. **«Dashboard»** debe abrir por ahora un placeholder y mostrar el breadcrumb **«Niños > [Nombre] > Dashboard»**.
16. La sección de accesibilidad visual debe permitir mantener el estado predeterminado sin ajuste o seleccionar manualmente: DEUTERENOPIA, DEUTERANOMALY, PROTANOPIA, PROTANOMALY, TRITANOPIA, TRITANOMALY, ACHROMATOMALY o ACHROMATOPSIA.
17. El interruptor del ajuste visual debe presentarse inactivo cuando no haya una configuración visual individual y activo cuando el perfil tenga un valor de configuración visual. Al desactivarlo y guardar cambios, debe eliminarse la configuración visual individual y restablecerse el estado predeterminado sin ajuste.
18. La sección de accesibilidad visual debe incluir ejemplos simples —por ejemplo, círculos o cuadrados— y un aviso visible de que no es una sección médica ni diagnóstica y de que, ante dudas, se consulte a un especialista.
19. La sección no debe ofrecer pruebas, resultados, diagnósticos, recomendaciones clínicas ni inferencias sobre la visión del niño.
20. Los controles y estados deben ser comprensibles para adultos, no depender exclusivamente del color y ser utilizables con objetivos táctiles amplios en móvil y tableta.
21. La experiencia infantil no debe mostrar ni permitir acceder a estos controles parentales, a los datos de perfil ni a la información de sesión.

## 4. Criterios de aceptación verificables

1. Una persona sin acceso parental válido no puede consultar la cuadrícula, editar perfiles ni abrir el dashboard individual.
2. La vista de entrada muestra los perfiles de la familia en cuadrícula y la acción **«Registrar niño»** bajo ella. Esta acción abre el mismo stepper de creación confirmado en FEAT-003.
3. Cada tarjeta muestra avatar, nombre, la duración de la sesión actual mientras está activa y permite abrir la edición. Esa duración no se presenta como progreso, rendimiento ni capacidad.
4. Una tarjeta con sesión activa muestra **«Expulsar»**; una tarjeta sin sesión activa no muestra esa acción.
5. Al elegir **«Expulsar»**, se muestra una confirmación que comunica el cierre de sesión y la sesión no se termina si el adulto cancela.
6. Al confirmar **«Expulsar»**, la sesión activa del perfil termina.
7. Al bloquear un perfil, este no puede entrar al juego y su información y progreso permanecen disponibles para un desbloqueo posterior.
8. Al abrir la edición desde una tarjeta, se muestra **«Niños > [Nombre]»**.
9. La edición permite guardar modificaciones de nombre, fecha de nacimiento y avatar predefinido.
10. Los controles individuales de audio/NPC se ven pero no son modificables cuando el control familiar relacionado está deshabilitado, y el motivo se comunica explícitamente.
11. **«Eliminar»** solicita confirmación; al cancelarla el perfil se conserva y al confirmarla se eliminan el perfil y toda la información relacionada.
12. Al pulsar **«Dashboard»**, se muestra el placeholder con breadcrumb **«Niños > [Nombre] > Dashboard»**.
13. Sin configuración visual individual, el interruptor de accesibilidad visual se muestra inactivo; con una configuración visual individual, se muestra activo. Si el adulto lo desactiva y guarda, la configuración visual individual se elimina y el perfil vuelve al estado predeterminado sin ajuste.
14. El adulto puede seleccionar el estado sin ajuste o cualquiera de los ocho perfiles visuales confirmados.
15. Los ejemplos visuales contienen únicamente elementos simples y no incluyen resultados, evaluaciones ni indicaciones diagnósticas.
16. El aviso de consulta a especialista comunica que la configuración es orientativa y no médica.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Comprensión de cuadrícula, tarjetas seleccionables, breadcrumbs, confirmaciones y estados de bloqueo en móvil y tableta.
- Diferenciación clara entre bloquear, expulsar y eliminar, sin depender solo del color.
- Claridad del estado deshabilitado por configuración familiar y del ajuste visual activo/inactivo.
- Comprensión adulta de los ejemplos y del aviso no médico.

### Backend, datos y sesiones

- Disponibilidad exclusiva de perfiles de la familia autorizada.
- Coherencia observable de sesión activa, expulsión, bloqueo, desbloqueo, edición, eliminación completa y adecuación jugable por fecha de nacimiento.
- Conservación de información y progreso durante el bloqueo y eliminación de toda la información asociada tras la confirmación de eliminar.

### Contenido y accesibilidad

- Redacción no clínica de los perfiles visuales, ejemplos simples y aviso a especialista.
- Apoyos visuales adicionales al color en los minijuegos correspondientes, sin transformar el ajuste en una evaluación.

### Seguridad y privacidad infantil

- Control parental exclusivo de consulta, edición, expulsión, bloqueo y eliminación.
- Uso limitado de fecha de nacimiento y ausencia de exposición, reutilización o compartición de datos infantiles fuera de la finalidad confirmada.

### Dependencias de producto conocidas

- FEAT-003 define el alta y los avatares predefinidos.
- FEAT-005 y ADR-021 definen las configuraciones globales familiares de audio y NPC que condicionan los ajustes individuales.
- El dashboard individual permanece fuera de alcance funcional como placeholder.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- Nombre y fecha de nacimiento son datos personales infantiles; solo se usan bajo control parental y la fecha de nacimiento se limita a adecuar la experiencia jugable.
- Bloquear no elimina ni reutiliza datos; eliminar, una vez confirmado, elimina el perfil y toda su información relacionada.
- No se muestran ni comparten datos entre perfiles, familias o terceros.
- No existe publicidad, perfilado comercial ni persuasión dirigida al niño.

### Accesibilidad y experiencia infantil

- Los controles se dirigen al adulto y permanecen separados del juego infantil.
- El ajuste visual es opcional, manual y no clínico; los minijuegos de color no deben apoyarse únicamente en color.
- No se incluyen temporizadores, castigos, comparativas, clasificaciones ni mensajes sobre capacidad infantil.

### Límites de IA

- Esta funcionalidad no activa conversaciones con el niño ni solicita información personal.
- Los ajustes de audio/NPC solo afectan a la experiencia de juego ya delimitada; no amplían las capacidades ni el ámbito de intervención del NPC.
- El dashboard placeholder no presenta análisis, inferencias ni recomendaciones generadas sobre el niño.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Entrega de una expulsión al niño cuando está jugando como consecuencia del bloqueo.
- Implementación funcional del dashboard, seguimiento, métricas, diagnósticos o clasificaciones.
- Diagnóstico, cribado, recomendación clínica, recopilación médica o interpretación de condiciones visuales.
- Avatares personalizados, fotografías, carga de imágenes o cambios al catálogo de avatares.
- Decisiones técnicas sobre navegación, persistencia, sesiones, controles, eventos, eliminación o tratamiento de datos.

### Riesgos

- Confundir bloquear con eliminar puede provocar expectativas equivocadas sobre los datos del niño; las etiquetas y confirmaciones deben explicar las consecuencias.
- Los nombres de perfiles visuales pueden interpretarse como diagnóstico; deben estar acompañados de ejemplos no evaluativos y el aviso a especialista.
- Mostrar un tiempo de sesión sin contexto puede sugerir control o valoración de uso; debe limitarse a la sesión actual activa y no mostrarse como señal de capacidad ni rendimiento.

### Supuestos

- El acceso parental mediante PIN ya está implementado, según confirmación del solicitante.
- El dashboard individual se limita actualmente a un placeholder.
- «Ajuste visual activo» equivale a que el perfil posee una configuración visual individual; «inactivo» equivale a que no la posee.

### Decisiones pendientes

- Los responsables técnicos deben determinar cómo materializar los comportamientos acordados sin modificar el alcance ni las garantías de privacidad.
