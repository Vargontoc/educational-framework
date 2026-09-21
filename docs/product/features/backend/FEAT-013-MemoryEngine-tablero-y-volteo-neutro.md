# FEAT-013 — MemoryEngine: tablero y volteo neutro

## Estado

- **Estado:** aceptada.
- **Responsable principal:** backend.
- **Decisión de producto:** ADR-030 — MemoryEngine: tablero y volteo neutro.
- **Depende de:** ADR-028; ADR-030; FEAT-013 frontend — Minijuegos: interacción visual básica y cierre sin fricción.

## 1. Objetivo y valor para la familia

Ofrecer un minijuego de parejas de memoria visual que permita al niño o niña explorar, recordar y encontrar coincidencias sin penalización. La progresión aumenta solo el tamaño del tablero, reduce el tiempo de observación de una pareja no coincidente y, en HARD, acerca temáticamente los elementos.

## 2. Actores y escenarios de uso

### Niño o niña que encuentra una pareja

1. Toca dos cartas boca abajo.
2. Si forman pareja, ambas quedan boca arriba durante el resto de la partida.
3. Continúa explorando las cartas restantes.

### Niño o niña que no encuentra una pareja

1. Toca dos cartas boca abajo que no coinciden.
2. Las ve durante el tiempo correspondiente a la dificultad.
3. Ambas vuelven boca abajo sin señal de error, vaivén, lenguaje negativo ni pérdida.
4. Puede continuar inmediatamente con la misma partida.

### Niño o niña que progresa de dificultad

1. En EASY juega con 4 cartas y 2 parejas de alto contraste temático.
2. En MEDIUM juega con 6 cartas y 3 parejas de contraste estándar.
3. En HARD juega con 8 cartas y 4 parejas de una misma categoría temática.

## 3. Requisitos funcionales y no funcionales

1. La actividad usa exclusivamente la mecánica de encontrar parejas entre cartas.
2. EASY presenta un tablero de 2 × 2, con 4 cartas y 2 parejas; una no coincidencia permanece visible 2 segundos antes de volver boca abajo.
3. MEDIUM presenta un tablero de 2 × 3, con 6 cartas y 3 parejas; una no coincidencia permanece visible 1,5 segundos.
4. HARD presenta un tablero de 2 × 4, con 8 cartas y 4 parejas; una no coincidencia permanece visible 1 segundo.
5. El contenido de EASY debe tener alto contraste temático; MEDIUM usa contraste estándar; HARD usa elementos de la misma categoría temática.
6. Las parejas acertadas permanecen boca arriba hasta el cierre de la partida en todas las dificultades.
7. Una no coincidencia causa exclusivamente el volteo neutro de las dos cartas tras el tiempo aplicable; no se acompaña de sonido negativo, vaivén, contador de fallos ni mensaje evaluativo.
8. La ausencia de audio o de Nubi no impide comprender, jugar ni completar la actividad.
9. No se añaden ejes de contenido, categorías cruzadas, mecánicas de secuencia, conteo, lectura o presión temporal.
10. Si se conservan resultados de juego, se aplican las reglas transversales de consolidación solo al completar y de acceso exclusivamente parental.

## 4. Criterios de aceptación verificables

1. EASY muestra exactamente 4 cartas organizadas en 2 × 2 y contiene 2 parejas.
2. MEDIUM muestra exactamente 6 cartas organizadas en 2 × 3 y contiene 3 parejas.
3. HARD muestra exactamente 8 cartas organizadas en 2 × 4 y contiene 4 parejas.
4. Una pareja no coincidente permanece visible 2 segundos en EASY, 1,5 segundos en MEDIUM y 1 segundo en HARD, tras lo cual ambas cartas vuelven boca abajo.
5. El volteo posterior a una no coincidencia no produce sonido negativo, vaivén, texto de error, pérdida ni contador infantil de fallos.
6. Una pareja acertada continúa boca arriba hasta finalizar la partida, en cualquier dificultad.
7. El contenido de HARD usa elementos de la misma categoría temática y no incorpora una mecánica distinta de parejas.
8. Con audio o Nubi desactivados, el niño puede abrir cartas, encontrar parejas y completar la partida.
9. El niño no ve métricas, tiempos, fallos acumulados ni interpretaciones de capacidad.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **Backend y datos:** coherencia de tablero, parejas, duración por dificultad y aplicación de la regla funcional de consolidación únicamente al completar.
- **Frontend y accesibilidad:** tamaño y separación de cartas, entendimiento del estado boca arriba/boca abajo y carácter neutro del volteo, en móvil y tableta.
- **Contenido:** adecuación por edad y contraste temático del catálogo por dificultad.
- **Agentes:** acompañamiento opcional de Nubi que no juzgue ni etiquete una no coincidencia.
- **Privacidad y seguridad infantil:** acceso parental exclusivo y tratamiento no evaluativo de cualquier dato de actividad autorizado.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- La actividad no solicita datos personales, no ofrece publicidad ni perfilado comercial.
- Los intentos no coincidentes no se comunican al niño como fallos ni se traducen en una valoración.
- El juego es viable sin audio, lectura, color como canal único o intervención activa de Nubi.
- Nubi se limita al juego estructurado, no solicita datos personales ni formula juicios sobre capacidad, aprendizaje o conducta.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Categorías cruzadas, mecánicas distintas de parejas, secuencias, conteo, cantidad, lectura, fonética, competición y recompensas.
- Dashboard, puntuación, clasificación, diagnóstico, adaptación posterior de dificultad y detalles de implementación.

### Riesgos

- Un tablero o cartas demasiado pequeños pueden generar errores de toque o cansancio visual.
- Elementos de HARD demasiado parecidos pueden resultar ambiguos; contenido y accesibilidad deben validar que la interferencia no impida jugar.

### Supuestos

- Se aplican las reglas transversales vigentes de minijuegos, incluido reintentar sin pérdida y registro orientativo solo tras completar.

### Decisiones pendientes

- Ninguna dentro del alcance de este motor.

## Referencias

- README.md.
- memoria-decisiones.md.
- ADR-028 — Reconocimiento visual sin fricción.
- ADR-030 — MemoryEngine: tablero y volteo neutro.
- FEAT-013 frontend — Minijuegos: interacción visual básica y cierre sin fricción.
