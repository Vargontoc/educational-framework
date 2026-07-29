# ADR-020 — Estructura adaptable del panel parental

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-07-28
- **Supersede:** —

## 1. Contexto y problema

El panel parental se accede desde Home mediante PIN y debe resultar usable para adultos tanto en tabletas en horizontal como en móviles en vertical. La decisión vigente confirma una navegación lateral, pero el comportamiento visual por orientación y el punto de entrada del panel aún no estaban definidos. Esta decisión se limita a la estructura de navegación y no define el contenido interno de sus secciones.

## 2. Necesidad de la familia y usuarios afectados

Los adultos de la única familia necesitan localizar de forma predecible las configuraciones, perfiles infantiles, chatbot y experiencias familiares, sin confundir esos controles con la experiencia infantil. También necesitan una salida clara que cierre su acceso parental antes de volver a Home.

Usuarios afectados:

- Adultos autenticados mediante PIN, como usuarios exclusivos del panel parental.
- Niños, de forma indirecta, al mantener sus datos y las opciones de adulto separados de su experiencia.

## 3. Alternativas de producto consideradas y compromisos

### A. Lateral visible en landscape y lateral bajo demanda en portrait

**Valor:** utiliza el espacio horizontal de tabletas y evita que la navegación reste espacio al contenido en móviles verticales.

**Compromiso:** en portrait el adulto debe abrir el lateral para cambiar de sección.

### B. Lateral visible y colapsable en ambas orientaciones

**Valor:** navegación siempre visible y consistente.

**Inconveniente:** reduce de forma innecesaria el espacio disponible en móviles verticales.

### C. Portada basada solo en tarjetas y sin lateral persistente

**Valor:** accesos iniciales directos.

**Inconveniente:** contradice la navegación lateral ya confirmada y añade un paso al cambiar entre secciones.

## 4. Decisión confirmada y justificación

Se confirma la alternativa A:

- En **landscape**, la navegación lateral se muestra visible.
- En **portrait**, la navegación lateral se abre bajo demanda.
- Tras validar el PIN, el panel abre una **portada neutral** con una breve descripción, sin mostrar datos infantiles, progreso, avisos evaluativos ni destacar una sección como recomendación.
- La navegación se agrupa y ordena así:
  - **Panel:** Configuración, Niños, Chatbot y Documentación.
  - **Experiencias:** Lectura familiar y Relajación familiar.
- Documentación aparece también dentro del panel, sin sustituir su acceso público e interno existente desde Home.
- La navegación incluye como última acción **«Salir»**. Esta acción es visualmente secundaria, cierra la sesión parental y redirige a Home en ambas orientaciones.

La decisión conserva la coherencia con la navegación lateral existente, protege el espacio útil en móvil y hace explícita la separación entre el área adulta autenticada y Home.

## 5. Impacto

### Experiencia infantil

- Los controles, configuraciones y datos parentales permanecen fuera de la experiencia infantil.
- No se introducen comparativas, progreso ni mensajes sobre capacidad infantil en la portada o navegación del panel.

### Experiencia parental

- La estructura facilita cambiar entre las seis secciones sin exigir orientación horizontal.
- La portada neutral ofrece una entrada clara sin suponer qué acción desea realizar el adulto.
- «Salir» permite terminar de forma explícita el acceso parental antes de volver a Home.

### Accesibilidad

- Las etiquetas de las secciones deben ser explícitas; los iconos, si existen, no son su único identificador.
- «Salir» puede ser visualmente discreto, pero debe conservar un objetivo táctil accesible.
- La estructura debe ser usable en móvil portrait y tableta landscape, sin depender solo del color.

### Seguridad infantil y privacidad

- El panel continúa requiriendo PIN para acceder a controles y datos parentales.
- La salida cierra la sesión parental antes de regresar a la vista común Home.
- La portada y la navegación no muestran información de niños ni conversaciones del chatbot.
- Documentación accesible desde Home debe seguir sin solicitar ni divulgar datos familiares o infantiles.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- Contenido, acciones y datos internos de Configuración, Niños, Chatbot, Documentación, Lectura familiar y Relajación familiar.
- Definición de métricas, progreso, dashboard o evaluación infantil.
- Cambios al PIN, recuperación de PIN o duración de sesión ya acordada.
- Diseño técnico de rutas, navegación, sesión o adaptación por orientación.

### Preguntas abiertas para los responsables técnicos

- **Frontend:** validar el reacomodo de la navegación, portada y acción «Salir» en móviles y tabletas reales, manteniendo controles accesibles.
- **Backend y seguridad:** validar que «Salir» termina el acceso parental y que el retorno a Home no expone contenido o datos protegidos.
- **Contenido:** definir la breve descripción de la portada neutral y validar que no sea persuasiva, evaluativa ni solicite datos.

## Referencias

- README.md
- ADR-017 — Componentes globales del panel parental, modo oscuro y catálogo de desarrollo.
- ADR-019 — Rediseño portrait real para evitar CSS problemático.
- FEAT-004 — Estructura visual y navegación del panel parental.
