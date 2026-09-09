# FEAT-011 — WorldMap: paseo visual básico tras la carga

## Estado

- **Estado:** aceptada parcialmente — fase 1; reacción visual de Nubi pendiente de decisión.
- **Responsable principal:** frontend.
- **Decisiones confirmadas:** 2026-09-07.
- **Depende de:** FEAT-010 — Entrada a GameView y carga inicial; perfil infantil habilitado.

## 1. Objetivo y valor para la familia

Tras la carga inicial, el niño accede a un paseo visual breve, amable y comprensible. Puede explorar un paisaje acotado que se desplaza de forma gradual y observar cómo el entorno responde a sus toques, sin tareas obligatorias, lectura, puntuaciones ni minijuegos.

El valor para la familia es ofrecer una entrada serena y autónoma a la experiencia de juego, adecuada a 3–4 años, sin convertir el mapa en un selector de niveles ni en una medición del niño.

## 2. Hechos, supuestos y decisiones confirmadas

### Hechos observados

- FEAT-010 termina en un estado visual base sin interacción; WorldMap estaba explícitamente fuera de su alcance.
- El producto define WorldMap como un paseo narrativo y no como un selector de niveles.
- Los elementos de descubrimiento son opcionales; ignorarlos no representa un fallo ni debe producir presión.
- El juego debe seguir siendo comprensible y utilizable sin audio o NPC.

### Decisiones confirmadas

- WorldMap empieza al concluir correctamente la carga inicial de `GameView`.
- La interacción infantil sigue el modelo de paseo guiado suavemente: Nubi está presente en el paisaje, pero el niño no tiene que dirigirlo para obtener una respuesta.
- En esta fase, al tocar un elemento interactuable, la respuesta acordada es una reacción del entorno, no el desplazamiento de Nubi hacia ese elemento.
- La vista muestra una zona acotada del mundo; el paisaje se desplaza gradualmente mientras el niño explora.
- La exploración es libre y voluntaria: no hay una secuencia obligatoria, objetivo que completar ni orden correcto de interacción.

### Supuestos explícitos

- La forma concreta de iniciar el desplazamiento y sus límites perceptibles se definirá en la fase 4A, sin alterar el carácter tranquilo y acotado aquí aprobado.
- Los elementos de esta fase producen únicamente reacciones básicas del entorno. No abren minijuegos ni representan avance infantil.

### Cuestión pendiente: presencia reactiva de Nubi

La familia ha indicado interés en que Nubi tenga reacciones visuales en el mapa, pero reconoce que podría contradecir el control parental existente sobre el NPC. No se considera una decisión confirmada.

Alternativas aún abiertas:

1. **Respetar estrictamente la preferencia de NPC:** con NPC desactivado, Nubi puede estar presente como protagonista visual estático o de movimiento ambiental, pero no muestra reacciones expresivas asociadas a la interacción; el entorno mantiene sus reacciones.
2. **Excepción visual limitada:** Nubi conserva reacciones visuales no verbales en WorldMap aunque el NPC esté desactivado, igual que la excepción ya aprobada exclusivamente para la despedida por pérdida de conexión.
3. **Protagonista alternativo por bioma:** cada mundo cuenta con un personaje visual propio para reacciones; Nubi permanece como hilo conductor general y de minijuegos.

La alternativa 1 protege con mayor claridad el significado de la preferencia parental. La 2 mantiene una conexión emocional constante, pero reduce el alcance práctico de dicho control y requeriría decisión explícita. La 3 evita esa excepción, pero puede fragmentar la identidad de la experiencia y aumentar la carga de comprensión para el niño.

## 3. Actores y escenarios de uso

### Niño que entra al juego

1. Selecciona un perfil infantil habilitado.
2. Ve la carga infantil definida en FEAT-010.
3. Al finalizar, aparece WorldMap con Nubi y una porción comprensible del paisaje.
4. Puede observar el paisaje sin necesidad de actuar.

### Niño que descubre un elemento

1. Ve un elemento del entorno que se diferencia visualmente de la decoración sin depender solo del color.
2. Lo toca.
3. El entorno ofrece una reacción corta, amable y comprensible sin texto, audio ni valoración.
4. El niño puede seguir explorando, repetir la interacción o no volver a tocar ese elemento.

### Niño que explora el paisaje

1. Explora desde la zona visible del mapa.
2. El paisaje se desplaza gradualmente dentro de los límites que se definan en una fase posterior.
3. No recibe mensajes de prisa, dirección obligatoria, progreso ni finalización.

## 4. Requisitos funcionales y no funcionales

1. Tras la carga inicial satisfactoria, la experiencia debe mostrar WorldMap en lugar del estado base no interactivo definido como entrega transitoria en FEAT-010.
2. WorldMap debe presentar inicialmente una zona acotada y comprensible del paisaje.
3. Nubi debe estar visualmente presente en el mapa.
4. Los elementos interactuables deben producir reacciones ambientales básicas, breves y no evaluativas.
5. Las reacciones deben ser comprensibles sin requerir lectura, sonido ni identificación exclusiva por color.
6. El paisaje debe poder desplazarse durante la exploración, sin convertir el desplazamiento en una carrera, un recorrido obligado o una prueba de precisión.
7. No se deben mostrar niveles, misiones, flechas de obligación, candados, puntuaciones, barras de progreso, temporizadores, clasificaciones, recompensas acumulativas ni mensajes de acierto/error.
8. No tocar un elemento, permanecer observando o repetir una interacción no debe provocar feedback negativo, insistencia ni bloqueo.
9. La experiencia debe seguir siendo visualmente comprensible cuando el audio no está disponible.
10. La reacción expresiva de Nubi ante los toques queda excluida hasta que se resuelva expresamente su relación con la preferencia parental de NPC.

## 5. Criterios de aceptación verificables

1. Después de la carga de un perfil habilitado, el niño ve una vista de WorldMap con Nubi y un paisaje visible acotado.
2. Al tocar un elemento interactuable, se observa una reacción del entorno sin texto obligatorio ni indicación de éxito o fallo.
3. Un niño puede no tocar ningún elemento y la vista no muestra avisos de inactividad, presión ni consecuencias negativas.
4. La exploración permite un desplazamiento gradual del paisaje sin mostrar un destino obligatorio, un porcentaje ni una ruta de nivel.
5. La comprensión de qué ocurre al tocar no depende únicamente de audio, color o texto.
6. Con audio no disponible, las reacciones ambientales y la exploración conservan sentido visual.
7. En esta fase, tocar un elemento no abre un minijuego, no cambia el progreso ni muestra datos del niño.
8. Hasta una decisión posterior, Nubi no emite una reacción expresiva vinculada al toque que contradiga la preferencia vigente de NPC.

## 6. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Comprensión infantil del paisaje acotado, los elementos interactuables y el desplazamiento gradual en móvil y tableta.
- Objetivos táctiles amplios, apoyos visuales y ausencia de dependencia exclusiva de color, texto o sonido.
- Transición coherente desde la carga de FEAT-010.

### Backend / World

- En la fase 2, validar qué información funcional del estado actual del mundo puede ofrecerse para representar el paisaje, sus límites y los elementos disponibles, sin exponer lógica de progreso al niño.
- Confirmar que la interacción decorativa básica no se interpreta ni registra como señal pedagógica, avance o evaluación.

### Contenido

- Adecuación por edad, variedad sensorial prudente y significado visual de paisaje, biomas y reacciones ambientales.
- Decidir posteriormente si el papel reactivo de Nubi o de personajes alternativos conserva una identidad comprensible para el niño.

### Seguridad infantil, privacidad y preferencias familiares

- Validar la decisión pendiente sobre reacciones de Nubi con NPC desactivado, pues puede reducir el control parental ya acordado.
- Confirmar que no se recogen datos adicionales de interacción decorativa ni se muestran datos infantiles.

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA

- No se incorporan publicidad, perfilado, comparativas, persuasión dirigida ni compartición de datos de menores.
- La interacción básica del entorno no debe solicitar ni revelar datos personales o de progreso del niño.
- Las acciones son cortas, predecibles, repetibles y sin castigos ni presión temporal.
- Los elementos deben tener zonas táctiles amplias y apoyos visuales suficientes para móvil y tableta.
- Esta fase no habilita conversación, generación de contenido ni decisión autónoma de IA. Cualquier audio opcional futuro debe mantener el contexto de juego, ser apropiado por edad y no pedir datos personales.

## 8. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Minijuegos, apertura de actividades, avance del jugador, adaptación de dificultad, tracking y dashboard.
- Definición de los biomas concretos (fase 3).
- Inicio y final perceptible de cada pantalla o zona del mapa (fase 4A).
- Catálogo y comportamiento concreto de animaciones de elementos (fase 4B).
- Reacciones expresivas de Nubi vinculadas a los toques, hasta decisión de producto posterior.
- Diseño técnico, contratos, persistencia, integración, mecanismos de desplazamiento y detalles de implementación.

### Riesgos

- Un desplazamiento ambiguo o demasiado amplio puede desorientar a un niño de 3–4 años.
- Si los elementos parecen botones, misiones o premios, el paseo puede percibirse como una obligación.
- Hacer reactivas las expresiones de Nubi cuando el NPC está desactivado puede vaciar de significado la preferencia parental.

### Decisiones pendientes

- ~~Confirmar una de las tres alternativas sobre la presencia reactiva de Nubi cuando el NPC está desactivado.~~ **RESUELTO 2026-09-07:** Alternativa 1 confirmada — respeto estricto de la preferencia parental. Nubi presente visualmente pero sin reacciones expresivas cuando el NPC está desactivado.
- Definir en fase 4A cómo percibe el niño el comienzo, límites y cierre de una zona sin introducir sensación de nivel o meta.
- Definir en fase 4B el repertorio de reacciones de entorno y sus límites de repetición.

## 9. Decisiones técnicas confirmadas para fase 1

- **Envío de eventos de interacción al backend:** NO se envía `WorldDiscoveryElementInteractiveEvent` en fase 1. La interacción es puramente decorativa y no se registra como señal pedagógica, avance o evaluación.
- **Ancho de mundo virtual placeholder:** 2560 px lógicos (2x el viewport de 1280). Permite desplazamiento moderado dentro de una zona acotada sin sensación de nivel o meta.
- **Repertorio de reacciones ambientales:** Única reacción genérica en fase 1 (oscilación de escala + cambio de tint temporal). Extensible en fase 4B a múltiples variantes.

## 10. Sprints de implementación

Los sprints de implementación se encuentran en ficheros independientes bajo `docs/sprints/frontend/`:

- **SPRINT-063** — WorldMap esqueleto con paisaje placeholder y desplazamiento
  - Archivo: `docs/sprints/frontend/SPRINT-063-worldmap-esqueleto-paisaje-placeholder.md`
  - Estado: pending

- **SPRINT-064** — Elementos interactuables y reacciones ambientales
  - Archivo: `docs/sprints/frontend/SPRINT-064-elementos-interactuables-reacciones-ambientales.md`
  - Estado: pending

- **SPRINT-065** — Robustez, integración y verificación transversal
  - Archivo: `docs/sprints/frontend/SPRINT-065-robustez-integracion-verificacion-transversal.md`
  - Estado: pending
