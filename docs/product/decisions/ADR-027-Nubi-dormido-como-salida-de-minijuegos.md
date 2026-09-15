# ADR-027 — Nubi dormido como salida de minijuegos

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-09-14
- **Modifica de forma acotada:** ADR-021 — Configuración global de audio, NPC y PIN.

## 1. Contexto y problema

La familia quiere que el niño pueda abandonar un minijuego en cualquier momento con una acción corta y reconocible. La experiencia debe seguir siendo utilizable cuando la preferencia parental del NPC esté desactivada.

ADR-021 establece que, al desactivar el NPC, Nubi no aparece ni se anima, salvo una excepción visual de despedida ante pérdida de conexión. Sin una excepción adicional, usar a Nubi como control de salida bloquearía o haría inconsistente la salida de un minijuego cuando el NPC está desactivado.

## 2. Necesidad de la familia y usuarios afectados

- **Niño o niña de 3–4 años:** necesita una salida inmediata, predecible y sin tener que leer, navegar menús ni confirmar decisiones.
- **Familia:** necesita mantener la posibilidad de jugar sin el NPC activo, sin impedir la autonomía del menor ni activar acompañamiento verbal no deseado.

## 3. Alternativas de producto consideradas y compromisos

### No iniciar minijuegos sin NPC

- **Ventaja:** mantiene literalmente la ausencia total de Nubi.
- **Inconveniente:** la preferencia parental de desactivar el NPC bloquea contenido infantil y reduce la autonomía.

### Usar un control de salida ajeno a Nubi

- **Ventaja:** mantiene la ausencia total de Nubi.
- **Inconveniente:** introduce un segundo patrón de salida que el niño debe aprender y no conserva la referencia visual acordada para los minijuegos.

### Mostrar a Nubi dormido solo como salida visual

- **Ventaja:** conserva una salida uniforme, inmediata y comprensible, sin reactivar al NPC como acompañante.
- **Compromiso:** constituye una excepción visual adicional y limitada a la preferencia parental de NPC desactivado.

## 4. Decisión confirmada y justificación

Se confirma que, en un minijuego con el NPC desactivado, Nubi se muestra únicamente en estado de dormido, acompañado de una nube de diálogo en estado de sueño. Este estado permite abandonar el minijuego mediante dos toques consecutivos sobre Nubi.

El segundo toque abandona inmediatamente el minijuego; no se solicita confirmación al niño. Un abandono accidental se acepta como un coste menor que añadir fricción a una interacción dirigida a 3–4 años.

Esta excepción no reactiva la presencia, movimiento, animación expresiva, voz ni intervenciones contextuales de Nubi. Su único propósito es ofrecer la salida visual del minijuego. El contenido concreto de la nube de diálogo de sueño no debe exigir lectura ni audio para comprender la salida.

## 5. Impacto

### Experiencia infantil

- El niño puede dejar una actividad sin esperar, leer ni justificar su decisión.
- La salida no genera castigo, presión, reproche ni necesidad de repetir.
- El estado dormido diferencia visualmente a Nubi de su papel habitual de acompañante.

### Experiencia parental

- Desactivar el NPC no impide el acceso ni la salida de los minijuegos.
- La excepción queda limitada y no habilita mensajes, voz o comportamiento activo de Nubi.

### Accesibilidad

- La salida debe ser reconocible mediante apoyo visual y objetivo táctil amplio, sin depender de texto, color o sonido.
- El doble toque debe ser consecutivo y comprensible para evitar una acción compleja de confirmación.

### Seguridad infantil y privacidad

- Nubi no solicita, expone ni infiere datos personales.
- La excepción no habilita conversación libre, generación de contenido ni decisiones de IA.
- La salida voluntaria no se presenta como incapacidad o conducta negativa.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- No se extiende la presencia de Nubi dormido a WorldMap, lectura, relajación ni otras pantallas.
- No modifica las preferencias de audio o voz ni crea una excepción de voz.
- No define mecanismos de detección del doble toque, contratos, persistencia, integración ni animaciones concretas.

### Cuestiones para validar

- **Frontend y accesibilidad:** que el estado dormido, la nube de sueño y la acción de dos toques sean distinguibles, accesibles y no provoquen salidas involuntarias sistemáticas en móvil y tableta.
- **Contenido:** adecuación por edad y ausencia de texto obligatorio en la nube de sueño.
- **Agentes:** que la excepción no produzca intervención del agente ni contenido fuera del contexto acordado.
- **Backend y privacidad:** que el abandono se trate solo conforme a las reglas de tracking que se confirmen para los minijuegos.

## Referencias

- ADR-021 — Configuración global de audio, NPC y PIN.
- FEAT-002 agentes — Acompañante de Juego Nubi.
- FEAT-013 frontend — Minijuegos: interacción visual básica y cierre sin fricción.
