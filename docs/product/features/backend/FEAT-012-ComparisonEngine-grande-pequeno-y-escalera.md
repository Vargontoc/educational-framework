# FEAT-012 — ComparisonEngine: grande/pequeño y escalera de dificultad

## Estado

- **Estado:** aceptada.
- **Responsable principal:** backend.
- **Decisión de producto:** ADR-029 — Comparación grande/pequeño y escalera de dificultad.
- **Depende de:** ADR-028; ADR-029; FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción.

## 1. Objetivo y valor para la familia

Ofrecer un primer minijuego de comparación visual centrado exclusivamente en **grande/pequeño**. El niño identifica siempre el objeto más grande entre opciones del mismo objeto, con una progresión predecible y sin que el juego se convierta en conteo, lectura o evaluación.

## 2. Actores y escenarios de uso

### Niño o niña en EASY

1. Ve dos versiones del mismo objeto.
2. Una resulta visualmente mucho mayor que la otra, con la referencia relativa 100 % frente a 40 %.
3. Identifica y toca el objeto más grande.

### Niño o niña en MEDIUM

1. Ve dos versiones idénticas del mismo objeto.
2. Compara una diferencia moderada: 100 % frente a 65 %.
3. Identifica y toca el objeto más grande.

### Niño o niña en HARD

1. Ve tres versiones idénticas del mismo objeto.
2. Compara tres tamaños relativos: 100 %, 75 % y 50 %.
3. Identifica y toca el objeto más grande.

## 3. Requisitos funcionales y no funcionales

1. El único eje disponible en esta funcionalidad es grande/pequeño.
2. La consigna solicita siempre el extremo «más grande»; no alterna con «más pequeño».
3. Las opciones de una ronda muestran el mismo sprite u objeto, sin variaciones que distraigan de la comparación de tamaño.
4. EASY presenta 2 opciones en proporción de referencia 100 % y 40 %.
5. MEDIUM presenta 2 opciones en proporción de referencia 100 % y 65 %.
6. HARD presenta 3 opciones en proporciones de referencia 100 %, 75 % y 50 %.
7. Las proporciones son una regla de resultado visual y no prescriben cómo se realizan técnicamente.
8. La consigna y la acción deben comprenderse sin audio, texto o color como canal único.
9. La experiencia aplica las reglas transversales vigentes: toque directo, feedback no evaluativo, reintentos sin límite, ausencia de derrota y sin presión temporal.
10. Este minijuego no exige ni registra como parte de su propósito conteo exacto, lectura, memoria, fonética, cantidad ni valoración de desarrollo.

## 4. Criterios de aceptación verificables

1. Solo se ofrecen rondas del eje grande/pequeño; no aparecen otros ejes de comparación.
2. En todas las rondas, la consigna pide seleccionar el objeto más grande.
3. En EASY hay exactamente dos opciones del mismo objeto, con diferencia visual extrema correspondiente a 100 % frente a 40 %.
4. En MEDIUM hay exactamente dos opciones del mismo objeto, con diferencia visual moderada correspondiente a 100 % frente a 65 %.
5. En HARD hay exactamente tres opciones del mismo objeto, con tamaños relativos 100 %, 75 % y 50 %.
6. Ninguna opción se diferencia de las demás por un cambio de sprite, objeto o atributo ajeno al tamaño.
7. Con audio desactivado, el niño puede comprender la comparación y resolver una ronda.
8. Tras una selección no acertada, la misma ronda sigue disponible sin pérdida, contador infantil de fallos, penalización ni presión temporal.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **Backend y datos:** coherencia de las rondas con la escalera aprobada y aplicación de las reglas transversales de registro solo cuando se completa la actividad.
- **Frontend y accesibilidad:** claridad visual de la diferencia de tamaño, comprensión sin audio, separación y tamaño táctil de las opciones en móvil y tableta.
- **Contenido:** disponibilidad de sprites adecuados por edad y su identidad consistente entre las opciones de una ronda.
- **Agentes:** Nubi, cuando exista, solo puede reforzar opcionalmente la actividad sin reemplazar la consigna visual.
- **Privacidad y seguridad infantil:** tratamiento parental y no evaluativo de cualquier señal de actividad que se acuerde conservar.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- El niño no ve métricas, fallos acumulados ni interpretaciones sobre su capacidad.
- El juego es viable sin audio y no requiere lectura, color como señal única o intervención adulta.
- Nubi no solicita datos personales, no conversa libremente ni valora el desempeño infantil.
- No se habilita diagnóstico, clasificación, perfilado comercial ni reutilización de datos infantiles.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Alto/bajo, largo/corto, lleno/vacío y muchos/pocos.
- Conteo, cantidad exacta, aritmética temprana, lectura, fonética y memoria.
- Alternar la consigna a «más pequeño».
- Diseño de dashboard, puntuaciones, evaluación, contratos, persistencia, algoritmos, integraciones y detalles de implementación.

### Riesgos

- Si el objeto no conserva una identidad visual clara entre opciones, el niño puede responder a una diferencia ajena al tamaño.
- Si las proporciones no resultan perceptibles en pantalla pequeña, la progresión puede perder claridad; requiere validación de experiencia y accesibilidad.

### Supuestos

- Las reglas transversales de ADR-028 y FEAT-013 son aplicables a este minijuego.

### Decisiones pendientes

- Ninguna para este eje y ladder. Los demás ejes de comparación exigen nuevas decisiones de producto antes de incluirse.

## Referencias

- README.md.
- comparacion-decisiones.md (fuente de la sesión de debate; alcance parcial confirmado).
- ADR-028 — Reconocimiento visual sin fricción.
- ADR-029 — Comparación grande/pequeño y escalera de dificultad.
- FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción.
