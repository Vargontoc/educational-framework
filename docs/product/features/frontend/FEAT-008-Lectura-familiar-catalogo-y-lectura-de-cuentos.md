# FEAT-008 — Lectura familiar: catálogo y lectura de cuentos

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-08-26
- **Historia de usuario:** Como adulto de la familia, quiero abrir el catálogo de cuentos desde el panel parental y leérselos al niño en pantalla, con narración opcional, para compartir un momento de lectura en familia sin refranes ni moralejas.
- **Depende de:** ADR-024 — Origen de los cuentos de Lectura Familiar (recurso externo subido a backend); FEAT-004 — Estructura visual y navegación del panel parental; FEAT-005 — Configuración global de audio, NPC y PIN (ajuste «Voz narrativa»); ADR-020; ADR-021.

## 1. Objetivo y valor para la familia

Ofrecer a la familia un momento de lectura compartida, tranquilo y sin presión, a partir de un catálogo de cuentos asépticos (sin refranes ni finales con valores morales, tal y como establece el README del producto). El adulto elige el cuento y controla el avance de las páginas junto al niño; la narración es un apoyo opcional y reversible, nunca un requisito para disfrutar del cuento.

Esta funcionalidad cubre la sección «Lectura familiar» ya prevista como acceso en FEAT-004, hasta ahora sin desarrollar. Consume un catálogo de cuentos ya recibido por el sistema: cada cuento es un recurso externo (título, textos e imagen/audio por página) subido a backend, según ADR-024. Esta funcionalidad no genera, produce ni integra ese origen; solo lee y presenta el catálogo ya disponible.

## 2. Actores y escenarios de uso

### Adulto que abre el catálogo

1. Desde «Experiencias» en el panel parental entra a Lectura familiar.
2. Ve un fondo visual propio de la sección, ajustado correctamente tanto si el dispositivo está en vertical como en horizontal.
3. Pulsa el acceso para ver el catálogo de cuentos disponibles.
4. Consulta el catálogo mostrado como portadas con su título.

### Adulto que abre un cuento

1. Elige un cuento del catálogo.
2. Ve su portada con el título antes de empezar a leer.
3. Si la voz narrativa está activada globalmente en Configuración (FEAT-005), ve un interruptor para decidir si este cuento empieza narrado o en silencio.
4. Si la voz narrativa está desactivada globalmente, no ve ningún control de voz en esta pantalla.
5. Inicia la lectura.

### Adulto que lee el cuento con el niño

1. Pasa las páginas una a una; el cambio de página tiene una transición fluida que acompaña la lectura sin distraer del contenido.
2. Si activó la narración en la portada, cada página nueva reproduce automáticamente su narración al mostrarse.
3. Ve un icono de altavoz para silenciar o reactivar la voz mientras lee, de forma independiente a lo elegido en la portada.
4. Puede pulsar un control de reproducción para volver a escuchar la narración de la página actual.
5. Termina el cuento y puede volver al catálogo para elegir otro o salir de la sección.

## 3. Requisitos funcionales y no funcionales de producto

1. La sección debe mostrar un fondo visual propio, correctamente ajustado tanto en orientación vertical como horizontal, en móvil y tableta.
2. Debe existir una acción visible para acceder al catálogo de cuentos.
3. El catálogo debe presentar cada cuento como una portada con su título; no debe mostrar edad recomendada, duración ni otros datos adicionales en esta vista.
4. Al elegir un cuento, debe mostrarse una pantalla previa con su portada y título antes de empezar a leer.
5. Si la «Voz narrativa» está activa en la Configuración global (FEAT-005), la pantalla previa del cuento debe mostrar un interruptor para decidir si ese cuento se narra o no.
6. Si la «Voz narrativa» está desactivada en la Configuración global, ni la pantalla previa ni la lectura deben mostrar ningún control relacionado con la voz (interruptor, altavoz o reproducir).
7. Durante la lectura, el avance entre páginas debe tener un efecto de transición fluido y agradable, sin resultar llamativo ni competir con el contenido del cuento.
8. Durante la lectura, debe mostrarse un icono de altavoz para silenciar o reactivar la voz. Este control es independiente del interruptor de la portada: puede diferir temporalmente de la preferencia elegida al empezar el cuento sin modificarla de forma permanente.
9. Con la voz activa, al llegar a una página nueva debe reproducirse automáticamente el audio ya incluido en esa página del cuento; esta funcionalidad no sintetiza narración por TTS.
10. Debe existir un control de reproducción visible para repetir el audio de la página actual en cualquier momento.
11. El avance de páginas lo controla exclusivamente el adulto; esta funcionalidad no incorpora controles para que el niño navegue el cuento de forma autónoma.
12. El estilo visual del catálogo y de la lectura debe ser agradable, sobrio y coherente con el resto del producto, evitando elementos competitivos, de presión temporal o evaluativos.
13. La sección debe ser usable en móvil portrait y tableta landscape, con objetivos táctiles amplios para pasar página, el altavoz y el control de reproducción.

## 4. Criterios de aceptación verificables

1. El fondo de la sección se muestra correctamente ajustado tanto en vertical como en horizontal, sin recortes ni deformaciones que impidan reconocerlo.
2. Existe una acción para abrir el catálogo de cuentos desde la sección.
3. El catálogo muestra cada cuento como portada con título únicamente.
4. Al seleccionar un cuento se muestra su portada y título antes de iniciar la lectura.
5. Con «Voz narrativa» activa globalmente, la pantalla previa del cuento muestra un interruptor de narración para ese cuento.
6. Con «Voz narrativa» desactivada globalmente, no aparece interruptor en la portada del cuento ni altavoz ni control de reproducción durante la lectura.
7. El adulto puede pasar páginas hacia adelante y hacia atrás, con una transición fluida perceptible entre una página y la siguiente.
8. Con la narración activada al iniciar el cuento, cada página nueva reproduce automáticamente el audio ya incluido en esa página al mostrarse.
9. El icono de altavoz permite silenciar el audio en curso y reactivarlo, sin alterar el interruptor original de la portada ni la preferencia de otros cuentos.
10. El control de reproducción repite el audio de la página actual cuando se pulsa, incluso si ya se escuchó antes.
11. Ningún control de la vista de lectura requiere que el niño interactúe directamente; el adulto puede completar el cuento entero sin necesitar que el niño toque la pantalla.
12. La sección no muestra datos infantiles, progreso, comparativas ni elementos evaluativos.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Ajuste correcto del fondo personalizado en los distintos tamaños y orientaciones de pantalla.
- Diseño del catálogo (portada + título) y de la pantalla previa del cuento.
- Implementación del efecto de transición entre páginas, sobrio y no distractor.
- Estados visuales del interruptor de portada, el altavoz y el control de reproducción, incluyendo su ausencia total cuando la voz narrativa esté desactivada globalmente.
- Comportamiento ante un cuento sin páginas, sin portada o con recursos de imagen no disponibles.

### Backend y contenido

- El alcance CRUD de `Story`/`StoryPage` previsto en FEAT-003 queda eliminado; el catálogo de cuentos se recibe como recurso subido a backend desde una herramienta externa (ADR-024).
- Definir cómo se valida y se expone al frontend la estructura de cada recurso (título, texto por página, imagen por página, audio por página) una vez recibido.
- Los cuentos deben mantenerse asépticos, sin refranes ni finales con valores morales, según el README; esta garantía es responsabilidad de quien produce y sube el recurso, ajena a esta aplicación (ADR-024).

### Dependencias de producto conocidas

- Acceso a la sección mediante el panel parental autenticado por PIN (FEAT-004).
- Ajuste global «Voz narrativa» de Configuración (FEAT-005), que determina si esta sección puede ofrecer algún control de audio.
- Origen del catálogo de cuentos como recurso externo subido a backend, según ADR-024.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- La sección se accede únicamente tras validar el PIN parental, dentro de «Experiencias».
- No se recogen datos del niño en esta funcionalidad: no hay registro de qué cuentos se leen, cuántas veces ni durante cuánto tiempo.
- El adulto controla en todo momento el avance de páginas y la voz; el niño no dispone de controles propios en esta vista.
- Los cuentos del catálogo son asépticos: sin refranes, moralejas ni contenido que pueda contradecir los valores propios de la familia. Esta garantía corresponde a quien produce y sube el recurso, ajeno a esta aplicación (ADR-024), no a un filtro dentro de esta funcionalidad.
- No se establece ninguna conexión en tiempo real ni integración con el Agente Cuenta cuentos; la única relación con esa herramienta externa es recibir el recurso que sube al backend (ADR-024).

### Accesibilidad y experiencia

- Fondo, portadas y controles deben ser legibles y usables en móvil portrait y tableta landscape.
- El icono de altavoz y el control de reproducción deben ser reconocibles sin depender únicamente del color.
- La lectura debe seguir siendo utilizable sin audio: el texto y la imagen de cada página son autosuficientes cuando la voz narrativa esté desactivada o silenciada.
- Objetivos táctiles amplios para pasar página, silenciar y reproducir, adecuados para uso compartido adulto-niño.

### Límites de IA

- Esta funcionalidad no genera cuentos nuevos ni invoca IA en tiempo real; se limita a leer y reproducir el catálogo ya recibido por el sistema.
- La generación de cuentos mediante el Agente Cuenta cuentos ocurre por completo fuera de esta aplicación, en una herramienta externa ajena a sus capas (ADR-024).
- Esta funcionalidad no invoca TTS: el audio narrado ya viene incluido en cada cuento.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Navegación autónoma del niño por el catálogo o por las páginas del cuento.
- Registro de progreso, historial de lectura, cuentos favoritos o recomendaciones.
- Generación dinámica de cuentos nuevos por IA.
- Edición, alta o gestión del catálogo desde esta vista; incorporar o retirar cuentos es una tarea técnica/administrativa fuera de la aplicación (ADR-024).
- Búsqueda, filtros o categorías dentro del catálogo en este alcance.
- Descarga o lectura sin conexión.
- Cambios en el ajuste global «Voz narrativa» de Configuración; esta funcionalidad solo lee su estado.
- Síntesis de narración mediante TTS para cuentos de este catálogo (ADR-024).
- Conexión en tiempo real con el Agente Cuenta cuentos u otra herramienta externa mientras la familia usa la sección.

### Riesgos

- Un efecto de pasar página demasiado vistoso podría distraer del cuento en lugar de acompañar la lectura; debe validarse que resulte sutil.
- La independencia entre el interruptor de portada y el altavoz de lectura podría confundir al adulto si no queda claro que el altavoz es un silencio temporal y no cambia la preferencia del cuento; debe comunicarse con claridad visual.
- La reproducción automática de audio al cambiar de página podría sonar de forma inesperada si el adulto esperaba silencio; el altavoz debe ser fácil de localizar y accionar en cualquier momento.

### Supuestos

- Cada cuento del catálogo ya incluye, al llegar a esta funcionalidad, título, portada y, por cada página, texto, imagen y audio (recurso externo subido a backend según ADR-024).
- El ajuste global «Voz narrativa» (FEAT-005) es la única condición que determina si esta sección puede ofrecer algún control de audio.
- La revisión de que cada cuento es aséptico ocurre antes de que el cuento llegue a estar disponible en el catálogo, no dentro de esta funcionalidad.

### Decisiones pendientes

- Los responsables técnicos deben definir el mecanismo de subida del recurso al backend y cómo se valida su estructura al recibirlo.
- Los responsables técnicos deben definir el comportamiento exacto cuando el audio de una página no esté disponible o el recurso esté incompleto (silencio, reintento, aviso no intrusivo).
- Diseño visual definitivo del fondo adaptado, el efecto de paso de página y los iconos de voz, dentro de las guías del sistema de diseño existente.
