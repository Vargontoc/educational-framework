# FEAT-002 — Animaciones decorativas interactivas por bioma

## Estado

- **Estado:** aceptada — fase 4B.
- **Responsable principal:** contenido.
- **Decisión confirmada:** 2026-09-09.
- **Depende de:** ADR-026; FEAT-001 — Sistema de biomas conectados de WorldMap; FEAT-011 — WorldMap: paseo visual básico.

## 1. Objetivo y valor para la familia

Ofrecer pequeños descubrimientos agradables dentro de cada bioma para que el niño pueda tocar, observar y repetir libremente una reacción del entorno. Estas animaciones enriquecen el paseo sin crear actividades, objetivos, evaluación ni presión.

## 2. Actores y escenarios de uso

### Niño que toca un elemento decorativo

1. Ve un elemento propio del bioma que se diferencia de la decoración estática mediante apoyos visuales, no solo color.
2. Lo toca.
3. Observa una única reacción principal, breve y predecible.
4. Si esa animación dispone de sonido ambiental asociado, lo oye solo cuando las preferencias familiares de audio lo permiten.
5. Puede volver a tocar el mismo elemento y recibe la misma reacción, sin límites ni consecuencias.

### Niño que juega sin audio o sin NPC

1. Toca un elemento decorativo.
2. La animación visual conserva por sí misma su significado y disfrute.
3. Nubi no interviene, habla ni reacciona al toque. Con NPC desactivado, tampoco está presente o animado, según FEAT-005.

## 3. Requisitos funcionales y no funcionales

1. Cada elemento decorativo interactuable debe ofrecer una única reacción principal breve, agradable y predecible al tocarlo.
2. La reacción de un mismo elemento debe ser siempre la misma; no se incorporan variaciones aleatorias ni resultados diferenciados al repetirla.
3. El niño puede repetir una reacción libremente, sin enfriamiento perceptible, límite de uso, penalización, premio ni cambio de estado acumulativo.
4. Un sonido ambiental puede acompañar una animación únicamente cuando esa animación lo requiere para su identidad; nunca es necesario para comprenderla.
5. La ausencia, desactivación o volumen nulo de audio no debe impedir ni alterar la reacción visual.
6. Nubi no debe realizar voz, gesto o animación reactiva al toque de estos elementos, incluso si NPC y voz están activados.
7. Los elementos no deben abrir minijuegos, preguntas, asociaciones, menús, recompensas ni secuencias de acciones.
8. Las reacciones no deben generar indicadores de acierto, error, progreso, colección, uso repetido ni datos visibles sobre el niño.
9. Las animaciones deben ser cortas, no invasivas y no impedir al niño continuar explorando o tocar otro elemento tras terminar.

## 4. Placeholders de contenido confirmados

| Bioma | Elementos y reacción principal |
|---|---|
| Pradera | Una flor se abre; una mariposa aletea; una nube se desplaza. |
| Granja | Una gallina mueve las alas; un molino gira suavemente; un patito camina. |
| Bosque encantado | Aparecen luciérnagas; una seta brilla suavemente; las hojas se mecen. |
| Playa | Una concha se abre; un cangrejo se asoma; una ola llega y se retira. |
| Espacio | Una estrella parpadea; un planeta gira; una pequeña nave pasa a lo lejos. |
| Prehistoria | Una huella aparece; un dinosaurio lejano mueve la cola; una planta prehistórica se balancea. |

Estos ejemplos son placeholders de contenido. Cada elemento mantiene una única reacción constante cuando se repite.

## 5. Criterios de aceptación verificables

1. Al tocar un elemento decorativo interactuable, el niño observa una reacción principal breve sin texto obligatorio, puntuación ni valoración.
2. Al repetir el toque sobre el mismo elemento, recibe la misma reacción visual sin límite, enfriamiento perceptible, premio ni penalización.
3. Un elemento con sonido ambiental asociado sigue siendo comprensible y agradable cuando el audio está desactivado o no disponible.
4. Un elemento sin sonido asociado no reproduce sonido solo para llamar la atención del niño.
5. Ninguna reacción abre una actividad, solicita una respuesta, muestra una pregunta ni inicia un minijuego.
6. Con NPC y voz activados, tocar un elemento no provoca una voz, gesto ni animación reactiva de Nubi.
7. Con NPC desactivado, el entorno conserva sus reacciones decorativas sin mostrar o animar a Nubi.
8. Los placeholders de los seis biomas pueden representarse sin depender exclusivamente de color, sonido o lectura para entender la reacción.

## 6. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Contenido

- Adecuación por edad, intensidad sensorial y seguridad de cada elemento, reacción y sonido ambiental asociado.
- Coherencia visual entre cada reacción y su bioma, sin convertirla en pregunta o aprendizaje explícito.

### Frontend

- Comprensión visual, objetivos táctiles amplios, repetición voluntaria y continuidad de exploración en móvil y tableta.
- Respeto de la configuración de audio y de ausencia de NPC sin añadir mensajes infantiles sobre esos ajustes.

### Backend / World y privacidad

- Confirmar que estas interacciones decorativas no se usan como avance, señal pedagógica, tracking ni fuente de dashboard.

### Agentes, voz y preferencias familiares

- Confirmar que no se solicita intervención del NPC para estas reacciones y que no se activa contenido generado o conversacional.

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA

- No se recogen, conservan ni exponen datos personales, progreso o patrones de interacción por tocar estos elementos.
- No hay publicidad, persuasión, competición, presión temporal, castigos ni mecánicas de retorno compulsivo.
- Las reacciones son breves, repetibles y comprensibles sin texto, audio o color exclusivamente.
- Los sonidos son ambientales y subordinados a las preferencias familiares; no incluyen voz de Nubi ni instrucciones infantiles.
- No interviene IA, diálogo ni generación de contenido durante las reacciones.

## 8. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Minijuegos, preguntas, asociación de conceptos, avance, dificultad, tracking, dashboard, recompensas y colecciones.
- Variaciones aleatorias, estados acumulativos, contenido generado y diálogos reactivos de Nubi.
- Especificación técnica de animaciones, sonidos, recursos, formatos, eventos, almacenamiento o integración.

### Riesgos

- Sonidos o movimientos excesivos pueden sobreestimular o distraer; contenido debe validar intensidad y duración por elemento.
- Un elemento que parezca una tarea puede introducir presión no deseada.
- Registrar estas interacciones como progreso o comportamiento incumpliría el alcance y la minimización de datos.

### Decisiones pendientes

- Ninguna de producto para esta fase. La selección definitiva de recursos y su validación de adecuación infantil corresponden a contenido.
