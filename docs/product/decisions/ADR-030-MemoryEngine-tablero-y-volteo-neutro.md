# ADR-030 — MemoryEngine: tablero y volteo neutro

## Estado

- **Estado:** aceptada.
- **Fecha:** 2026-09-20.
- **Alcance:** decisiones de producto para el motor de memoria; no define su realización técnica.
- **Modifica de forma acotada:** FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción, decisión confirmada 6, solo para una pareja no coincidente de `MemoryEngine`.

## 1. Contexto y problema

La familia incorpora un minijuego clásico de parejas para niños y niñas de 3–4 años. Debe mantener las reglas de juego sin fricción: los intentos no acertados no se comunican como fallos, no hay presión ni castigo y el juego sigue siendo comprensible sin audio.

La memoria visual ya es el reto propio de esta actividad. Añadir múltiples ejes de contenido o cambios de mecánica elevaría la carga cognitiva sin aportar valor proporcional.

## 2. Necesidad de la familia y usuarios afectados

- **Niño o niña:** necesita descubrir parejas mediante un tablero pequeño, una respuesta visual predecible y permanencia de las parejas encontradas.
- **Familia:** necesita una actividad breve, autónoma y no evaluativa, sin requerir audio ni ayuda adulta para continuar.
- **Adulto autenticado:** puede recibir en el futuro señales orientativas de actividad conforme a las reglas transversales, sin convertirlas en diagnóstico o clasificación.

## 3. Alternativas de producto consideradas y compromisos

### A. Añadir categorías cruzadas o mecánicas nuevas para aumentar la dificultad

- **Valor aparente:** incrementaría la variedad de contenido.
- **Inconveniente:** añade carga ajena a la memoria visual y dificulta una experiencia predecible; no se acepta.

### B. Graduar por tablero, tiempo de visualización y similitud temática

- **Valor:** mantiene la mecánica clásica y permite una progresión comprensible.
- **Compromiso:** requiere seleccionar contenido apropiado y visualmente distinguible para cada nivel.

### C. Ocultar de nuevo las parejas acertadas

- **Valor aparente:** reduciría elementos visibles en el tablero.
- **Inconveniente:** puede desorientar la permanencia de objeto del niño; no se acepta.

## 4. Decisión confirmada y justificación

Se confirma una mecánica de parejas clásica. La dificultad varía exclusivamente mediante el tamaño del tablero y el tiempo que una pareja no coincidente permanece visible antes de volver boca abajo. En HARD se añade únicamente que los elementos pertenecen a la misma categoría temática, para aumentar la interferencia visual sin alterar la mecánica.

| Eje | EASY | MEDIUM | HARD |
|---|---|---|---|
| Tablero | 2 × 2: 4 cartas / 2 parejas | 2 × 3: 6 cartas / 3 parejas | 2 × 4: 8 cartas / 4 parejas |
| Tiempo visible tras no coincidencia | 2 segundos | 1,5 segundos | 1 segundo |
| Contraste o contenido | Alto contraste temático | Estándar | Elementos de una misma categoría temática |
| Parejas acertadas | Permanecen boca arriba | Igual | Igual |
| No coincidencia | Volteo neutro, sin sonido negativo ni vaivén | Igual | Igual |

Cuando el niño elige dos cartas que no forman pareja, ambas vuelven boca abajo de manera neutra tras el tiempo aplicable. No hay sonido de error, vaivén, lenguaje negativo, contador infantil de fallos ni pérdida. Las parejas acertadas permanecen boca arriba hasta finalizar la partida.

## 5. Impacto

### Experiencia infantil

- El niño conserva la referencia visual de las parejas acertadas y no debe recordar objetos que ya ha encontrado.
- Un intento no coincidente se entiende como parte normal de explorar, sin reproche ni interrupción.
- El tablero crece gradualmente sin introducir gestos ni reglas nuevas.

### Experiencia parental

- La actividad sigue siendo breve y autónoma, sin necesidad de audio ni acompañamiento adulto.
- No se incorporan puntuaciones, temporizadores visibles, diagnósticos ni comparaciones de desempeño.

### Accesibilidad

- El estado de carta, coincidencia y volteo deben comprenderse visualmente sin sonido, texto o color como único canal.
- Las cartas deben respetar los criterios transversales de objetivos táctiles amplios y uso viable en móvil y tableta.

### Seguridad infantil y privacidad

- No se recogen datos personales nuevos ni se amplía el ámbito de Nubi.
- Los datos de juego que se acuerden conservar solo pueden servir a la finalidad parental autorizada, con lenguaje no evaluativo y acceso aislado por perfil/familia.

## 6. Límites, exclusiones y cuestiones para los responsables técnicos

### Límites y exclusiones

- No se incluyen categorías cruzadas, secuencias, conteo, lectura, fonética, competición ni mecánicas distintas de encontrar parejas.
- No se definen contratos, persistencia, modelos de datos, algoritmos, renderizado, animaciones, integración de audio ni otros detalles técnicos.
- No se diseña dashboard, puntuación, interpretación de progreso ni adaptación de dificultad posterior.

### Ámbitos que deben validar los responsables

- **Frontend y accesibilidad:** comprensión del volteo neutro, permanencia de parejas, tamaño de las cartas y separación táctil en móvil y tableta.
- **Contenido:** selección de elementos de alto contraste, estándar y de una misma categoría temática que sean adecuados a la edad y no ambiguos.
- **Backend y datos:** aplicación consistente de la ladder y de las reglas transversales de registro solo al completar, si se registra actividad.
- **Agentes y seguridad infantil:** Nubi, si acompaña, es opcional y no puede transformar el intento no coincidente en una valoración.

### Preguntas abiertas

- Ninguna dentro del alcance confirmado.

## Referencias

- README.md.
- memoria-decisiones.md.
- ADR-028 — Reconocimiento visual sin fricción.
- FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción.
