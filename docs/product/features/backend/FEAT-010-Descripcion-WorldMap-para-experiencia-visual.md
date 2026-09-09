# FEAT-010 — Descripción de WorldMap para la experiencia visual

## Estado

- **Estado:** aceptada parcialmente — fase 2; selección futura de mapa entre sesiones pendiente.
- **Responsable principal:** backend / World.
- **Decisiones confirmadas:** 2026-09-07.
- **Depende de:** FEAT-008 — World Module (propuesta vigente que requiere validación); FEAT-011 frontend — WorldMap: paseo visual básico.

## 1. Objetivo y valor para la familia

Permitir que la experiencia infantil represente un mapa completo como un paisaje coherente, mostrando únicamente la parte que el niño está explorando en cada momento. La familia obtiene un paseo de descubrimiento tranquilo y autónomo, no una secuencia de niveles ni una exposición de progreso infantil.

## 2. Actores y escenarios de uso

### Niño que entra por primera vez a WorldMap

1. Tras la carga, recibe un paisaje completo correspondiente al mapa disponible actualmente.
2. Inicialmente, el único mapa disponible es **MEADOW**.
3. Ve una parte acotada del paisaje y puede explorarlo libremente.

### Niño que explora una parte del mapa

1. El niño desplaza su exploración por el paisaje.
2. La experiencia visual sitúa de manera estable los elementos del entorno respecto al mapa completo.
3. Como máximo, hay pocos elementos interactuables claramente visibles y separados entre sí; el valor inicial confirmado es entre dos y tres.
4. Los elementos de esta fase son decorativos y solo producen reacciones ambientales.

### Niño que alcanza una zona aún no visible

1. Se aproxima al borde de la porción que está observando.
2. Recibe una señal visual suave de que existe más paisaje por descubrir.
3. Decide libremente si continúa explorando o permanece donde está, sin obligación ni mensaje de avance.

### Mundo no disponible

1. La experiencia no dispone de una descripción válida y segura del mapa.
2. No se muestra un mapa vacío ambiguo, un error técnico ni información interna.
3. Se aplica el cierre amable y retorno ya definido en FEAT-010.

## 3. Requisitos funcionales y no funcionales

1. World debe proporcionar una descripción funcional del mapa completo disponible para la sesión infantil, no solo de la porción visible inicialmente.
2. La descripción debe permitir situar de manera coherente el paisaje, sus límites y cada elemento interactuable dentro del mapa completo.
3. Cada elemento interactuable debe tener una identidad estable para que la experiencia pueda reconocerlo y representarlo siempre en el mismo lugar durante la exploración.
4. La descripción debe indicar qué elementos están disponibles para interacción básica y qué reacción ambiental les corresponde, sin abrir actividades en esta fase.
5. La experiencia visual debe poder determinar qué elementos son visibles desde la porción del paisaje que explora el niño, a partir de la descripción completa recibida.
6. El mapa debe limitar a dos o tres los elementos interactuables claramente visibles de forma simultánea y mantener una separación perceptible entre ellos.
7. World debe indicar los límites funcionales de exploración necesarios para que la experiencia pueda ofrecer una señal visual suave de continuidad de paisaje, no un bloqueo o una meta.
8. El único mapa inicial confirmado es MEADOW. La incorporación de otros biomas se tratará en la fase 3.
9. Los datos dirigidos a la experiencia infantil no deben incluir dificultad, recomendaciones pedagógicas, progreso, resultados, estados de engagement, motivos de disponibilidad ni otros datos internos del niño.
10. Si no se puede ofrecer una descripción válida del mapa, la experiencia debe poder aplicar el cierre amable definido en FEAT-010.

## 4. Criterios de aceptación verificables

1. Al iniciar una sesión con mapa disponible, la experiencia recibe información suficiente para representar un único mapa completo MEADOW y una porción inicial de ese paisaje.
2. Al cambiar la porción visible durante la exploración, los elementos se mantienen en una ubicación coherente respecto al mapa completo.
3. La vista nunca presenta más de tres elementos claramente interactuables a la vez y estos no quedan amontonados.
4. Cada elemento visible en esta fase se comporta como decoración interactiva: su toque no inicia minijuego, no desbloquea contenido y no muestra progreso.
5. Al aproximarse a una zona aún no visible, el mapa puede mostrar una señal visual suave de continuidad sin flechas obligatorias, candados, texto de nivel ni presión.
6. Los datos consumidos por la experiencia infantil no exponen resultados, dificultad, progreso, etiquetas de comportamiento ni datos personales del menor.
7. Cuando no existe una descripción de mapa válida, no se muestra contenido técnico ni un mapa vacío; se sigue el cierre amable de FEAT-010.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Backend / World

- Validar que World puede expresar el mapa completo, los límites de exploración, la ubicación estable de elementos y su estado decorativo, sin que esta especificación determine contratos ni mecanismos de comunicación.
- Confirmar el comportamiento cuando el mapa no esté disponible o su descripción no sea válida.
- Mantener separados los datos visuales necesarios de la lógica de progreso, selección pedagógica y tracking.

### Frontend

- Validar la representación de una porción acotada de un mapa completo y la comprensión infantil de la señal de continuidad.
- Garantizar que solo se destaquen pocos elementos separados y que los datos internos nunca aparezcan en la interfaz infantil.

### Contenido

- Validar los límites visuales de MEADOW, los elementos decorativos y sus reacciones ambientales apropiadas por edad.

### Seguridad infantil y privacidad

- Confirmar que la descripción destinada al niño se limita a la finalidad visual y no contiene información de progreso, perfilado o datos personales.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA

- No se incorpora recogida adicional de datos por explorar, observar o ignorar decoración.
- La exploración es opcional y no produce penalizaciones, comparativas ni mensajes evaluativos.
- La señal de continuidad debe ser visualmente comprensible y no depender solo de texto, color o sonido.
- Esta funcionalidad no activa IA, diálogo ni generación de contenido.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Minijuegos, actividades, avance infantil, dificultad adaptativa, tracking y dashboard.
- Persistencia de posición, de mapa elegido o de exploración entre sesiones.
- Aleatoriedad de selección de bioma o de mapa entre sesiones.
- Reacciones expresivas de Nubi sujetas a la preferencia parental de NPC.
- Diseño técnico de la comunicación, estructura de contratos, almacenamiento, representación gráfica o movimiento de la vista.

### Riesgos

- Una descripción incompleta o con posiciones inconsistentes puede hacer que el paisaje parezca cambiar de forma desconcertante.
- Demasiados elementos visibles pueden saturar al niño o hacer que el mapa parezca una lista de tareas.
- Incluir datos pedagógicos o de interacción en la descripción visual vulneraría la minimización de datos y el carácter no evaluativo del producto.

### Decisiones pendientes

- Cuando existan varios mapas o biomas, confirmar si se elige uno de manera aleatoria, si se conserva un mapa previo para continuar en la siguiente sesión, o si se aplica otra regla familiar explícita.
- La posible continuidad verbal de Nubi en la carga (por ejemplo, al retomar un lugar) no está confirmada y requiere resolver antes la preferencia parental de NPC.

## 8. Decisiones técnicas confirmadas para fase 2 (2026-09-07)

Confirmadas tras el análisis técnico de `analyser-backend` sobre el estado actual del dominio World (ningún elemento de posición, límite de mundo ni tope de elementos visibles existía en el código antes de esta fase):

1. **Posición por elemento:** continua y normalizada (0.0–1.0) en ambos ejes (X e Y), no solo en el eje horizontal — permite variedad de composición visual, no un layout en fila. Vive en el catálogo (`content.WorldDiscoveryElement`), es estable por elemento.
2. **Límite de mundo (`worldWidth`):** por **host** (`content.WorldHost`), no por elemento ni por sesión — un mismo host/bioma comparte un único ancho de mundo.
3. **Selección de los 2-3 elementos visibles simultáneamente:** rotativa, con patrón anti-repetición dentro de la misma sesión activa (no persiste entre sesiones, ver exclusión §7). No es aleatoriedad pura ni un orden fijo.
4. **Cierre amable ante `WORLD_STATE_SYNC.status` `INACTIVE_CLOSED`/`NO_WORLD_STATE`:** lo dispara el frontend al recibir ese estado, no un mecanismo del servidor. World ya emite el status correcto; queda como handoff hacia un sprint de frontend (fuera del alcance de World).
5. **Evolución de contrato:** los campos nuevos (`positionX`, `positionY`, `worldWidth`) son aditivos y `nullable` — sin romper compatibilidad con el cliente ya desplegado, sin versión nueva de payload.

## 9. Sprints de implementación (fase 2)

- **SPRINT-089** — Bioma MEADOW confinado y selección rotativa con anti-repetición de elementos visibles.
  - Archivo: `docs/sprints/backend/SPRINT-089-meadow-confinado-seleccion-rotativa-elementos.md`
- **SPRINT-090** — Posición estable por elemento y ancho de mundo por host en el contrato `WORLD_STATE_SYNC`.
  - Archivo: `docs/sprints/backend/SPRINT-090-posicion-worldwidth-contrato-world-state-sync.md`
