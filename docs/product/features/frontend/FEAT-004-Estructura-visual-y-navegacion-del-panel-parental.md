# FEAT-004 — Estructura visual y navegación del panel parental

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-07-28
- **Historia de usuario:** Como adulto de la familia, quiero acceder mediante PIN a un panel organizado y adaptable para localizar las opciones parentales y experiencias familiares, y poder salir explícitamente al terminar.
- **Depende de:** ADR-017; ADR-019; ADR-020; acceso parental mediante PIN.

## 1. Objetivo y valor para la familia

Ofrecer a los adultos una estructura de panel parental clara, predecible y usable en móvil y tableta, sin mezclar sus controles con la experiencia infantil. La funcionalidad proporciona el marco de navegación y una portada neutral, pero no desarrolla el contenido de las secciones.

## 2. Actores y escenarios de uso

### Adulto que entra al panel

1. Desde Home accede al panel parental mediante el icono de configuración y valida el PIN familiar.
2. Llega a una portada neutral con una breve descripción del panel.
3. Consulta la navegación disponible según la orientación de su dispositivo.

### Adulto que cambia de sección

1. En landscape ve la navegación lateral.
2. En portrait abre la navegación lateral bajo demanda.
3. Elige una de las secciones agrupadas en «Panel» o «Experiencias».

### Adulto que termina su acceso

1. Localiza «Salir» como última acción de la navegación.
2. La pulsa.
3. Su sesión parental se cierra y vuelve a Home.

## 3. Requisitos funcionales y no funcionales de producto

1. El panel parental solo se muestra después de una validación correcta mediante PIN familiar.
2. Tras entrar, debe mostrarse una portada neutral con una breve descripción; no debe mostrar datos infantiles, progreso, avisos evaluativos ni priorizar una sección.
3. En landscape, la navegación lateral debe estar visible.
4. En portrait, la navegación lateral debe estar disponible bajo demanda sin ocultar de forma permanente el contenido principal.
5. La navegación debe agrupar y ordenar las secciones exactamente así:
   - **Panel:** Configuración, Niños, Chatbot, Documentación.
   - **Experiencias:** Lectura familiar, Relajación familiar.
6. Documentación debe estar disponible en el panel sin eliminar el acceso público e interno desde Home.
7. La última acción de navegación debe denominarse exactamente **«Salir»**.
8. «Salir» debe cerrar la sesión parental y dirigir a Home.
9. La acción «Salir» puede tener menor prominencia visual que las secciones, pero debe mantener un objetivo táctil amplio y una denominación visible.
10. Los nombres de las secciones y «Salir» deben ser comprensibles sin depender exclusivamente de iconos, color o posición.
11. La estructura debe ser usable en móvil portrait y tableta landscape, con lectura clara y controles táctiles amplios.

## 4. Criterios de aceptación verificables

1. Sin una validación correcta de PIN no se presenta el contenido del panel parental.
2. Tras validar el PIN, se presenta una portada neutral con una breve descripción y sin datos infantiles ni progreso.
3. En landscape se puede identificar la navegación lateral sin abrir un control adicional.
4. En portrait se puede abrir la navegación lateral y acceder a todas las secciones.
5. La navegación muestra los grupos «Panel» y «Experiencias» con las seis secciones en el orden acordado.
6. Documentación puede abrirse desde la navegación parental y continúa accesible desde Home sin PIN.
7. «Salir» es la última acción de la navegación, cierra la sesión parental y dirige a Home.
8. Las etiquetas de navegación y «Salir» se identifican sin depender solo de iconos o color.
9. La estructura no expone nombres infantiles, fechas de nacimiento, progreso, configuraciones ni conversaciones en la portada o navegación.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Usabilidad de navegación y portada en landscape y portrait en móvil y tableta.
- Identificación accesible de secciones y acción «Salir».
- Estados comprensibles cuando el acceso parental no esté disponible o la sesión haya finalizado.

### Backend y seguridad

- Conservación del requisito de PIN para acceder al panel.
- Cierre de sesión parental al elegir «Salir» y retorno sin contenido protegido a Home.

### Contenido

- Texto breve de la portada neutral, adecuado exclusivamente para adultos y sin lenguaje persuasivo o evaluativo.

### Dependencias de producto conocidas

- Acceso parental mediante PIN.
- ADR-017, ADR-019 y ADR-020.
- Definición posterior de cada una de las seis secciones.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- El panel es un espacio exclusivo de adultos autenticados.
- La estructura no expone datos de menores ni contenido sensible antes de entrar en una sección autorizada.
- «Salir» reduce la exposición accidental al finalizar la sesión parental.
- Documentación pública desde Home no debe solicitar ni divulgar datos familiares o infantiles.

### Accesibilidad y experiencia

- Controles amplios, etiquetas visibles y uso viable en móvil y tableta.
- La navegación se adapta a la orientación sin exigir girar el dispositivo.
- No se emplean mecanismos de presión, competición, comparativas ni evaluación infantil.

### Límites de IA

- La estructura no muestra conversaciones ni activa el chatbot.
- El chatbot, cuando se defina, continuará limitado a adultos autenticados.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Contenido y comportamiento interno de las seis secciones.
- Dashboard, métricas, progreso o clasificación infantil.
- Edición de perfiles, configuración, chatbot, catálogo de cuentos y ejercicios de relajación.
- Cambios en la política vigente de PIN, recuperación de PIN, modo oscuro o inactividad.

### Riesgos

- Una navegación poco clara en portrait puede dificultar que el adulto encuentre las secciones. Debe validarse su reconocimiento y acceso sin depender de iconos.
- Una acción «Salir» demasiado discreta podría no resultar localizable; su jerarquía visual no puede perjudicar su accesibilidad táctil ni su etiqueta.
- La portada no debe transformarse en un dashboard ni introducir información infantil sin una decisión de producto específica.

### Supuestos

- El adulto accede al panel desde Home tras validar el PIN familiar.
- La documentación mantiene simultáneamente su acceso interno público desde Home y su entrada dentro del panel.

### Decisiones pendientes

- El contenido concreto y las acciones disponibles en cada sección requieren especificaciones independientes.
- Los responsables técnicos deben definir la resolución de orientación, navegación, cierre de sesión y estados de error sin modificar los comportamientos confirmados.
