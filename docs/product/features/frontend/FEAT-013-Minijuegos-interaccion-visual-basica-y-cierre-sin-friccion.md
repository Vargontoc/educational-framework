# FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción

## Estado

- **Estado:** aceptada.
- **Responsable principal:** frontend.
- **Decisiones confirmadas:** 2026-09-14; actualización 2026-09-14.
- **Depende de:** ADR-021; ADR-027; FEAT-002 agentes — Acompañante de Juego Nubi; contenido de actividades disponible.

## 1. Objetivo y valor para la familia

Ofrecer una presentación visual básica, breve y amable de los minijuegos abiertos desde WorldMap. El niño puede probar, equivocarse, recibir una ayuda visual y salir en cualquier momento sin presión. La familia conserva una experiencia utilizable sin audio y sin NPC activo. La presentación parental de abandonos pertenece a una futura funcionalidad de dashboard.

## 2. Hechos observados, supuestos y decisiones confirmadas

### Hechos observados

- Los minijuegos forman parte de Game y se abren desde determinados elementos interactivos de WorldMap.
- El producto no es una herramienta diagnóstica, evaluativa ni profesional; el progreso es únicamente orientativo para la familia.
- La familia puede desactivar el audio y el NPC globalmente.

### Decisiones confirmadas

1. Al tocar un elemento de WorldMap que contiene un minijuego, se muestra el recuadro de juego.
2. Si el NPC está activo, Nubi se sitúa en la esquina inferior derecha y puede mostrar una nube de diálogo breve y contextual.
3. Si el NPC está desactivado, se aplica ADR-027: Nubi aparece solo dormido, con nube de diálogo en estado de sueño, como referencia visual de salida; no habla, se mueve ni acompaña el juego.
4. Dos toques consecutivos sobre Nubi abandonan inmediatamente el minijuego, sin confirmación infantil, penalización ni feedback negativo.
5. Un acierto provoca una animación visual de éxito y, cuando el audio está disponible y permitido, un sonido amable.
6. Un fallo provoca un vaivén visual leve y, cuando el audio está disponible y permitido, un sonido de error suave. No se usan mensajes que califiquen al niño ni el intento como malo.
7. Tras varios fallos de contenido, cada minijuego puede ofrecer una pista visual breve. El umbral se define por actividad y la pista no debe revelar necesariamente la solución ni presentar los intentos previos como fracaso. El tiempo de respuesta no equivale a un fallo de contenido ni activa por sí mismo esta pista.
8. El juego debe conservar sentido y ser jugable sin audio.
9. Al completar todas las rondas, se muestra una celebración visual breve, sin premios gamificados, y se vuelve a WorldMap.
10. El niño puede repetir libremente un minijuego completado durante la sesión actual, pero ni sus resultados ni un eventual abandono de esa repetición se registran para tracking.
11. Los aciertos, fallos y tiempos de respuesta de un intento inicial solo se consolidan para tracking cuando el minijuego se completa. Si se abandona antes, los resultados de rondas previas no se consolidan; el abandono sí queda disponible como señal informativa no evaluativa.
12. **Tiempo de respuesta.** El tiempo de respuesta se conserva solo como señal contextual y no evaluativa. Una respuesta correcta sigue consolidándose como acierto y una incorrecta como fallo, con independencia de cuánto tarde el niño. No existe temporizador visible, bloqueo, pausa, cambio de feedback infantil ni clasificación de rendimiento basada en el tiempo. Cualquier uso futuro de esta señal para la dificultad adaptativa exige una decisión de producto independiente.
13. **Cierre de conexión / app en segundo plano.** Si la app pasa a segundo plano o se cierra la conexión websocket con un minijuego activo, no cuenta ni como resultado de tracking ni como abandono; es una señal de plataforma distinta de la inactividad dentro del juego y no comparte contador con la decisión 12.
14. **Recurrencia de abandonos para la familia.** Se fija la regla de disponibilidad del dato para una futura funcionalidad de dashboard; esta especificación no define su visualización ni su texto:
    - Agregación **por actividad**, no global — para preservar el contexto de si el patrón es específico de una actividad o generalizado.
    - Se muestra únicamente cuando existen **cuatro o más abandonos** entre los **seis intentos iniciales más recientes** de esa actividad. Un intento inicial es una apertura de la actividad antes de haberla completado en la sesión; las repeticiones ya excluidas de tracking no forman parte de esta ventana.

### Supuestos explícitos

- La actividad define sus rondas, los aciertos y fallos posibles, y el umbral de pista aplicable.
- La celebración es un cierre de actividad, no una recompensa acumulable ni una invitación insistente a repetir.

## 3. Actores y escenarios de uso

### Niño que inicia un minijuego

1. Toca un elemento de WorldMap asociado a una actividad.
2. Ve el recuadro de juego y comprende visualmente qué puede hacer, sin necesitar audio ni lectura.
3. Si el NPC está activo, Nubi acompaña desde la esquina inferior derecha; si no, ve a Nubi dormido exclusivamente como salida.

### Niño que prueba una respuesta

1. Hace una elección o acción propia de la ronda.
2. Si acierta, percibe una respuesta visual breve de éxito.
3. Si falla, percibe un vaivén amable, sin reproche, castigo ni presión temporal.
4. Si se repiten fallos según la regla de esa actividad, recibe una pista visual breve.

### Niño que abandona

1. Toca dos veces consecutivas a Nubi.
2. Vuelve inmediatamente a WorldMap, incluso si la salida fue accidental.
3. No ve confirmaciones, avisos de pérdida, castigos, puntuaciones ni obligación de volver.

### Niño que completa o repite

1. Completa las rondas de un minijuego y observa una celebración corta de cierre.
2. Vuelve a WorldMap sin desbloqueos, rachas, puntos ni indicación de superioridad.
3. Puede abrir de nuevo esa actividad en la misma sesión; la repetición sigue siendo una experiencia de juego, no una nueva señal para tracking.

## 4. Requisitos funcionales y no funcionales

1. El recuadro de minijuego debe diferenciarse visualmente de WorldMap y conservar una acción de salida siempre disponible.
2. Los objetivos infantiles deben ser táctiles, amplios y adecuados a móvil y tableta.
3. Las respuestas de éxito, fallo, pista, salida y cierre deben comprenderse sin depender solo de color, texto, audio o voz de Nubi.
4. El audio es opcional: su ausencia, indisponibilidad o desactivación parental no bloquea acciones, pistas, salida ni comprensión del resultado visual.
5. Las intervenciones de Nubi, cuando estén activas, son breves, contextuales y no conversacionales; no solicitan datos personales ni juzgan capacidad, conducta o progreso.
6. La salida por doble toque no aplica penalización visible ni modifica por sí misma la dificultad adaptativa.
7. En un intento inicial, solo se consolidan para tracking los aciertos, fallos y tiempos de respuesta cuando se completa el minijuego. El abandono descarta los resultados parciales de sus rondas.
8. Una repetición posterior a una finalización en la misma sesión no consolida aciertos, fallos, tiempos de respuesta ni abandonos para tracking.
9. El abandono de un intento inicial debe diferenciarse de resultados de juego y no debe exponerse al niño como una medida de rendimiento.
10. No se muestran puntuaciones, cronómetros visibles, clasificaciones, vidas, premios acumulables, castigos, comparativas, rachas, desbloqueos ni llamadas insistentes a continuar.
11. El tiempo de respuesta no modifica la respuesta visual, el resultado de acierto o fallo ni la pista que percibe el niño.

## 5. Criterios de aceptación verificables

1. Al tocar un elemento de WorldMap asociado a una actividad, el niño ve un recuadro de minijuego comprensible visualmente.
2. Con audio desactivado o no disponible, el niño puede iniciar, jugar, recibir una pista, salir y completar el minijuego.
3. Con NPC activo, Nubi aparece en la esquina inferior derecha con una nube de diálogo contextual breve; con NPC desactivado, solo aparece dormido conforme a ADR-027.
4. Dos toques consecutivos sobre Nubi devuelven al niño a WorldMap sin pantalla de confirmación ni mensaje negativo.
5. Un acierto produce una respuesta visual de éxito; un fallo produce únicamente un vaivén visual leve. Los sonidos asociados solo ocurren cuando las preferencias parentales lo permiten.
6. Una actividad con fallos repetidos según su regla muestra una pista visual sin requerir texto, voz o audio y sin etiquetar el comportamiento infantil.
7. Completar todas las rondas produce una celebración breve y retorno a WorldMap, sin puntos, premios acumulables ni desbloqueos.
8. Si el niño abandona un primer intento después de responder rondas previas, no se consolidan aciertos, fallos ni tiempos de respuesta de ese intento; el abandono se conserva separadamente como señal informativa.
9. Tras completar una actividad, una nueva apertura de esa actividad en la misma sesión no genera datos de resultados ni abandono para tracking.
10. El niño no ve métricas, historial de abandono ni interpretaciones sobre su desempeño.
11. Una respuesta correcta se consolida como acierto y una incorrecta como fallo, independientemente de su tiempo de respuesta; el niño no percibe ninguna clasificación temporal.
12. Un cierre de app o de conexión websocket con un minijuego activo no genera ni resultado de tracking ni registro de abandono.

## 6. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Comprensión infantil del recuadro, la salida por doble toque, las respuestas visuales y la celebración breve.
- Tamaño táctil, prevención de activaciones ambiguas y equivalencia de uso con audio o NPC desactivados.
- Separación perceptible entre WorldMap y minijuego, sin convertir el cierre en una pantalla de nivel superado.
- Garantizar que la interfaz no convierte el tiempo de respuesta en un temporizador, una clasificación, un cambio de feedback ni una presión para el niño.

### Backend y datos

- Aplicación de la regla funcional de consolidación solo al completar, descarte de resultados parciales y exclusión total de repeticiones en la sesión actual.
- Separación del abandono informativo respecto de aciertos, fallos, tiempos de respuesta y dificultad adaptativa.
- Minimización, acceso exclusivamente parental y presentación no evaluativa de cualquier información de abandono.
- Conservación del tiempo de respuesta solo como señal contextual, separada de los resultados de acierto y fallo y sin uso adaptativo mientras no exista una decisión posterior.
- Diferenciación de la señal de cierre de app/websocket respecto a la inactividad dentro de una sesión de juego activa.
- Agregación de abandonos por actividad (no global) y aplicación de la regla confirmada de cuatro o más abandonos en los seis intentos iniciales más recientes antes de que el dato esté disponible para el panel parental.

### Contenido

- Adecuación por edad de los diálogos breves, la nube de sueño y la celebración.
- Las reglas concretas de rondas, señales, umbral de fallos repetidos y pistas visuales corresponden a las especificaciones de cada minijuego.

### Agentes

- Respeto de las preferencias de NPC y voz, y límites de Nubi al contexto de juego estructurado.

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- Solo se conservan los datos estrictamente necesarios para la finalidad confirmada; los resultados parciales de una actividad abandonada no se reutilizan como progreso.
- El abandono es una señal contextual para la familia, no una medida de capacidad, motivación, conducta, diagnóstico ni recomendación.
- Los datos de un perfil infantil no se exponen entre perfiles ni fuera del panel parental autorizado.
- Nubi no solicita datos personales, no conversa libremente, no infiere estados emocionales y no propone decisiones sensibles.
- Las interacciones son breves, predecibles, repetibles y viables sin sonido, lectura o identificación exclusiva por color.
- El tiempo de respuesta no se presenta al niño ni se traduce en una evaluación. Cualquier presentación futura a la familia requiere lenguaje descriptivo y no evaluativo, y una decisión de producto específica.

## 8. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Definir nuevos minijuegos concretos, su contenido pedagógico o la implementación de sus rondas.
- Puntuaciones, logros, vidas, niveles, recompensas acumulativas, desbloqueos, competición, temporizadores visibles o presión de repetición.
- Diseño de contratos, persistencia, mecanismos de animación, integración de audio, cálculo de dificultad o detalles técnicos.
- Interpretar abandonos como comportamiento, capacidad o necesidad educativa del niño.
- Cualquier uso del tiempo de respuesta o de abandonos para modificar la dificultad adaptativa.
- Diseño visual e implementación del panel de progreso de dificultad alcanzada por actividad (estilo barras).
- Diseño, ubicación, texto e implementación del dashboard, incluida la presentación parental de abandonos.

### Riesgos

- El doble toque puede causar abandonos accidentales; se acepta para minimizar fricción, sin atribuir el abandono al niño.
- Una pista demasiado evidente puede reducir exploración; una demasiado sutil puede no ayudar.
- Informar abandonos sin contexto o con etiquetas negativas puede inducir interpretaciones familiares inapropiadas.
- Una futura barra de progreso de dificultad, si se coloca cerca del dato de abandono sin cuidado, puede inducir al padre a leer el abandono como causa de una barra "baja" en términos de rendimiento del niño en lugar de exploración — a vigilar cuando se diseñe esa pieza.

### Decisiones pendientes

- Ninguna dentro del alcance de esta funcionalidad. El diseño y texto del dashboard, así como las reglas específicas de pistas de cada minijuego, se decidirán en sus funcionalidades correspondientes.

## Referencias

- README.md.
- ADR-021 — Configuración global de audio, NPC y PIN.
- ADR-027 — Nubi dormido como salida de minijuegos.
- FEAT-002 agentes — Acompañante de Juego Nubi.
- FEAT-006 backend — Tracking Module.
