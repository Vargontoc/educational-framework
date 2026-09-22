# Memoria (MemoryEngine) — Decisiones de diseño

> Documento de scoping. Recoge las decisiones cerradas en la sesión de debate sobre el motor de memoria, dentro del contexto de reglas estrictas de minijuegos (cero fricción, jugable sin audio, ayudas, EASY/MEDIUM/HARD) y de las reglas transversales ya cerradas en motores anteriores. Motor de complejidad de diseño baja/estándar (juego de cartas clásico) — sin cambios sustanciales sobre la plantilla original.

## 1. Volteo automático sin penalización

- Cuando dos cartas no coinciden, el volteo automático tras `flipDelayMs` debe ser **neutro**: sin sonido de error, sin shake, las cartas simplemente vuelven a su estado boca abajo.
- Consistente con la regla transversal ya cerrada: ningún fallo se comunica con tono negativo.

## 2. Variables de dificultad

- La dificultad se construye únicamente sobre **tamaño de tablero** y **`flipDelayMs`**. No se añaden ejes de contenido ni categorías cruzadas — la propia mecánica de memoria ya es autoevaluativa.
- En HARD, el único añadido de contenido es usar **elementos de la misma categoría temática** entre las cartas, para aumentar la interferencia visual entre parejas parecidas, sin introducir mecánica nueva.

## 3. Regla UX de permanencia de objeto

- Las cartas emparejadas correctamente **permanecen fijas boca arriba** en el tablero durante toda la partida, para no desorientar la permanencia de objeto del niño (regla ya establecida en la plantilla original, confirmada sin cambios).

## 4. Ladder de dificultad (sin cambios respecto a la plantilla original)

| Eje | EASY | MEDIUM | HARD |
|---|---|---|---|
| Tablero | 2×2 (4 cartas / 2 parejas) | 2×3 (6 cartas / 3 parejas) | 2×4 (8 cartas / 4 parejas) |
| `flipDelayMs` | 2000 ms | 1500 ms | 1000 ms |
| Contraste / contenido | Alto contraste temático | Estándar | Elementos de la misma categoría temática (mayor interferencia) |
| Regla UX | Parejas acertadas quedan fijas boca arriba | Igual | Igual |
| Feedback de no-coincidencia | Neutro (sin sonido de error, sin shake) | Igual | Igual |

## 5. Preguntas abiertas / pendientes

- Ninguna — motor cerrado sin cuestiones pendientes.
