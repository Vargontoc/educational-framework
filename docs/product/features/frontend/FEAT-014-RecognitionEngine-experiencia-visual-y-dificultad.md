# FEAT-014 — RecognitionEngine: experiencia visual y dificultad sin fricción

## Estado

- **Estado:** aceptada parcialmente; quedan tres decisiones de producto pendientes.
- **Responsable principal:** frontend.
- **Decisión de producto:** ADR-028.
- **Depende de:** FEAT-009 — Recognition Engine Module; FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción; ADR-023; ADR-027.

## 1. Objetivo y valor para la familia

Hacer que el minijuego de reconocimiento sea entendible y jugable de forma autónoma por un niño o niña de 3–4 años: ve el elemento que busca, toca una opción y puede probar de nuevo sin límite ni presión. La familia conserva una experiencia válida sin audio y con controles parentales separados.

## 2. Hechos observados, supuestos y decisiones confirmadas

### Hechos observados

- El producto es un acompañamiento familiar no evaluativo y funciona en móvil y tableta.
- El audio y Nubi pueden no estar disponibles o estar desactivados por la familia.
- ADR-023 ya exige claves no cromáticas para que los minijuegos de color no dependan únicamente del matiz.

### Decisiones confirmadas

1. El estímulo visual se mantiene visible en todos los niveles EASY, MEDIUM y HARD, en una zona central separada de Nubi.
2. El niño toca directamente una opción; una selección válida resuelve la ronda sin confirmación adicional.
3. Una selección no acertada causa solo feedback visual neutro y breve. La ronda continúa sin límite de reintentos, derrota, temporizador, contador infantil de fallos, rojo, sonido negativo ni mensaje evaluativo.
4. La progresión confirmada es: EASY con 2 opciones, distractores lejanos, cromo guía y espera corta; MEDIUM con 3 opciones, distractores de la misma categoría, sin cromo y espera media; HARD con 3–4 opciones, distractores de contorno o grafía similar, sin cromo y espera nula o mínima.
5. La espera de EASY y MEDIUM solo precede a la disponibilidad del toque y no puede generar un fallo, una pérdida ni una presión temporal.
6. El audio y la narración opcional de Nubi son aditivos; la misma interacción y comprensión están disponibles sin ellos.
7. Nubi no muestra el estímulo. Si está activo puede narrarlo y usar un icono neutro genérico; si está dormido no habla ni se anima y no modifica el juego.
8. Letras y números se trabajan como reconocimiento visual de grafía, no como fonética, lectura, cantidad o conteo.
9. Cuando el perfil tenga configurada una preferencia de visión de color, las rondas de colores añaden un diferenciador no cromático a los distractores.
10. Aciertos y selecciones no acertadas se registran solo al completar, para información parental autorizada y no evaluativa. El dashboard queda fuera de alcance.

### Supuestos explícitos

- La especificación vigente de cada actividad determina qué categorías y elementos están disponibles.
- La presencia de una clave no cromática no debe sustituir el significado del color que la actividad propone reconocer.

## 3. Actores y escenarios de uso

### Niño o niña jugando sin audio

1. Ve el estímulo en la tarjeta central y las opciones disponibles.
2. Espera, cuando aplique, a que se habilite el toque sin ver un cronómetro ni una consecuencia negativa.
3. Toca una opción. Si no es la buscada, percibe una señal neutra y puede probar de nuevo; si acierta, la ronda se resuelve de inmediato.

### Niño o niña con Nubi activo o dormido

1. El estímulo sigue visible en su propia zona.
2. Nubi puede acompañar opcionalmente sin convertirse en instrucción necesaria.
3. El estado dormido de Nubi no introduce voz, animación ni bloqueo de la ronda.

### Adulto que ha configurado una preferencia visual de color

1. Configura la preferencia únicamente desde el panel parental separado.
2. El niño juega una ronda de color cuyos distractores incorporan claves visuales adicionales al matiz.
3. La familia no recibe una interpretación clínica ni una evaluación de visión del menor.

## 4. Requisitos funcionales y no funcionales

1. El estímulo objetivo debe permanecer visualmente disponible durante toda la ronda, independientemente de dificultad, audio y estado de Nubi.
2. Las opciones deben ser táctiles, amplias, distinguibles y aptas para pantalla móvil y tableta; el rango de 88–96 px es una referencia no definitiva pendiente de validación.
3. El cromo guía solo aparece en EASY y debe señalar la zona de respuesta sin ocultar ni alterar el estímulo.
4. Los distractores deben respetar la similitud propia de cada nivel y no crear ambigüedad que impida una respuesta comprensible.
5. El feedback de una selección no acertada debe ser neutral, breve, no verbal o comprensible sin lectura, y no usar códigos exclusivos de color o sonido.
6. Ninguna ronda exige arrastre, audio, lectura, memoria del estímulo, conteo, valor numérico ni intervención adulta.
7. En letras y números, el modelo puede diferir de las opciones en estilo o tamaño, preservando su reconocimiento visual.
8. En color con preferencia visual configurada, los distractores deben distinguirse además por patrón o textura.
9. Los datos de aciertos y selecciones no acertadas solo son accesibles a adultos autorizados y solo con la finalidad de información familiar no evaluativa.

## 5. Criterios de aceptación verificables

1. Con audio y Nubi desactivados, el niño puede identificar el estímulo, elegir una opción, reintentar, recibir ayuda visual prevista por su actividad y completar la experiencia.
2. En EASY se ven 2 opciones, cromo guía y una espera corta antes del toque; en MEDIUM, 3 opciones y espera media sin cromo; en HARD, 3–4 opciones sin cromo y sin espera apreciable o con una mínima.
3. El estímulo se mantiene visible en la tarjeta propia antes, durante y después de una selección no acertada.
4. Una selección no acertada no cambia la ronda, no muestra texto de error, rojo, sonido negativo, fallo acumulado ni pantalla de pérdida.
5. El niño puede seleccionar la opción correcta tras cualquier número de selecciones no acertadas y la ronda se resuelve inmediatamente al tocarla.
6. Nubi no contiene el estímulo visual; cuando está dormido no emite voz ni animación y la ronda conserva todas sus señales necesarias.
7. Una ronda de letras o números puede presentar el modelo y las opciones con diferente tamaño o estilo sin convertir la actividad en fonética, lectura, cantidad o conteo.
8. Con una preferencia de visión de color configurada, una ronda de color presenta al menos una clave visual adicional al matiz para cada distractor relevante.
9. El niño no puede consultar datos de acierto, selección no acertada ni dificultad; la información queda reservada al adulto autorizado tras completar el minijuego.

## 6. Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **Frontend y accesibilidad:** separación perceptible entre estímulo, opciones y Nubi; dimensión y espaciado táctil; comprensión de cromo, espera y señal neutra en dispositivo real.
- **Contenido:** selección de distractores por nivel, estilos visuales equivalentes de grafías y patrones o texturas que no confundan el propósito de color.
- **Backend y datos:** que el registro se limite a actividades completadas, se aísle por perfil y familia, y no exponga señales al niño.
- **Agentes:** límites de narración e icono neutro de Nubi, siempre opcionales y adecuados al contexto de juego.
- **Privacidad y seguridad infantil:** control parental del progreso y ausencia de uso secundario, diagnóstico o perfilado.

## 7. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- No se recogen datos personales nuevos para esta experiencia ni se reutilizan datos infantiles fuera del propósito parental autorizado.
- La dificultad alcanzada es una señal orientativa, no una medida de capacidad, aprendizaje, conducta o diagnóstico.
- Nubi no solicita datos personales, no conversa libremente, no presiona ni toma decisiones sensibles.
- La experiencia evita presión temporal, competición, castigo y persuasión; funciona sin audio, lectura, color como único canal o presencia activa de Nubi.

## 8. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Diseño o implementación de dashboard, puntuaciones, recompensas, interpretación de progreso o ajustes adaptativos.
- Memoria, fonética, lectura, conteo o evaluación del valor numérico.
- Diagnóstico o recomendación visual y cambios a los controles parentales de ADR-023.
- Decisiones sobre contratos, persistencia, integración, algoritmos, temporización o renderizado técnico.

### Riesgos

- Distractores demasiado similares o demasiado lejanos pueden volver la progresión frustrante o irrelevante; requieren validación de contenido y experiencia con la edad objetivo.
- Una espera perceptible como bloqueo puede causar frustración; debe conservar un propósito visual claro y breve.
- Si patrones o texturas son poco claros, pueden desplazar el reconocimiento del color o reducir accesibilidad.

### Decisiones pendientes

1. Confirmar si los patrones o texturas de color se muestran siempre o solo con una preferencia visual configurada.
2. Confirmar el tamaño táctil estándar común después de observar su uso en dispositivos reales.
3. Confirmar el tratamiento de la categoría FORMAS, incluida en documentación anterior pero no concretada en esta mejora.

## Referencias

- README.md.
- ADR-028 — Reconocimiento visual sin fricción.
- ADR-023 — Selector visual de accesibilidad cromática.
- ADR-027 — Nubi dormido como salida de minijuegos.
- FEAT-009 — Recognition Engine Module.
- FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción.
