# ADR-019 — Rediseño portrait real para evitar CSS problemático

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-07-26
- **Responsable principal:** frontend
- **Supersede:** ADR-010 (parcialmente, solo para vistas con diseño portrait)

---

## 1. Contexto y problema

### Problema detectado

La implementación de horizontal forzado mediante `rotate(90deg) scale()` no funciona correctamente en dispositivos reales:

- En orientación vertical, el contenido se ajusta al ancho pero no a la altura, dejando espacios vacíos
- El cálculo del escalado con `Math.max()` causa recortes en uno de los ejes
- El centrado con `transform-origin: center center` no se coordina correctamente con la rotación
- La interactividad táctil puede verse afectada por las transformaciones CSS complejas

### Intentos de solución

El SPRINT-009 realizó 4 iteraciones para resolver el problema:

1. **Escalado proporcional:** Cortaba contenido en horizontal
2. **Dimensiones dinámicas por orientación:** Dejaba bordes negros en vertical
3. **Rotación 90° con escalado:** No llenaba la altura correctamente
4. **Wrapper adicional con flexbox:** Mejora parcial pero persisten problemas de centrado

### Conclusión técnica

Las transformaciones CSS complejas (`rotate() + scale()`) no son robustas para simular landscape forzado en dispositivos con aspect ratios variados. La solución requiere rediseño real de las vistas para orientación portrait.

---

## 2. Necesidad de la familia y usuarios afectados

### Usuarios afectados

- **Adultos (panel parental):** Necesitan una experiencia usable en móvil en orientación vertical (uso habitual)
- **Niños (experiencia de juego):** Requieren una experiencia estable y predecible, sin problemas de centrado ni interactividad

### Necesidad identificada

- Evitar que el usuario tenga que forzar manualmente la orientación horizontal
- Garantizar que el contenido sea visible y accesible en ambas orientaciones
- Mantener la experiencia infantil crítica (juego) estable y sin problemas técnicos

---

## 3. Alternativas de producto consideradas

### Alternativa A: Mantener horizontal forzado con mejor implementación CSS

**Descripción:** Continuar con `rotate(90deg) scale()` pero con cálculos más precisos y wrappers adicionales.

**Ventajas:**
- No requiere rediseño de vistas
- Mantiene la política actual de ADR-010

**Desventajas:**
- Implementación frágil y dependiente del aspect ratio del dispositivo
- Problemas persistentes de centrado y escalado
- Posible impacto en interactividad táctil
- No resuelve el problema de fondo

**Razón de descarte:** La implementación no es robusta y genera problemas recurrentes en dispositivos reales.

### Alternativa B: Horizontal forzado solo en minijuegos críticos

**Descripción:** Mantener landscape forzado solo en minijuegos específicos donde la experiencia del niño lo requiera, permitiendo portrait en el resto de la aplicación.

**Ventajas:**
- Protege la experiencia crítica del niño en minijuegos
- Permite flexibilidad en el resto de la aplicación

**Desventajas:**
- Complejidad adicional para gestionar dos modos de orientación
- Transiciones entre vistas pueden ser confusas para el niño

**Razón de descarte:** Añade complejidad innecesaria y puede confundir al niño con cambios de orientación.

### Alternativa C: Rediseño portrait real con estilos específicos por orientación (elegida)

**Descripción:** Diseñar vistas específicas para orientación portrait usando media queries CSS, sin rotaciones ni escalados problemáticos.

**Ventajas:**
- Solución robusta y estándar (media queries)
- No depende de aspect ratio del dispositivo
- Interactividad táctil preservada
- Experiencia consistente en ambas orientaciones
- Más fácil de mantener y extender

**Desventajas:**
- Requiere rediseño de vistas afectadas
- Mayor esfuerzo inicial de implementación

**Justificación de elección:** Es la solución más robusta y mantenible, aunque requiere esfuerzo inicial de rediseño.

---

## 4. Decisión confirmada

### Política nueva

**Rediseño portrait real con estilos específicos por orientación:**

- Las vistas afectadas tendrán estilos específicos para landscape y portrait usando media queries CSS
- No se usarán rotaciones CSS (`rotate()`) ni escalados complejos (`scale()`) para simular landscape
- El contenido se reacomodará naturalmente según la orientación del dispositivo

### Alcance inicial

**Vista prioritaria:** HomeView (pantalla principal)

**Vistas pendientes de análisis:**
- PanelControlView (panel parental)
- GameView (experiencia de juego)
- DocumentationView (documentación)

### Criterios de diseño

1. **Landscape:** Diseño optimizado para horizontal (actual)
2. **Portrait:** Diseño adaptado para vertical con reacomodo de contenido
3. **Transición:** Suave entre orientaciones sin saltos visuales
4. **Interactividad:** Preservada en ambas orientaciones (objetivos táctiles 48x48dp mínimo)

---

## 5. Impacto en experiencia infantil, parental, accesibilidad, seguridad infantil y privacidad

### Experiencia infantil

- **Positivo:** Elimina problemas de centrado y escalado que pueden confundir al niño
- **Positivo:** Interactividad táctil preservada sin transformaciones CSS problemáticas
- **Neutro:** La experiencia de juego (GameView) se analizará específicamente en futuras decisiones

### Experiencia parental

- **Positivo:** Panel parental usable en móvil en orientación vertical (uso habitual)
- **Positivo:** No requiere forzar manualmente la orientación horizontal
- **Positivo:** Contenido visible y accesible sin recortes ni espacios vacíos

### Accesibilidad

- **Positivo:** Objetivos táctiles mantienen tamaño mínimo 48x48dp en ambas orientaciones
- **Positivo:** Orden de tabulación preservado sin transformaciones CSS
- **Positivo:** Lectores de pantalla no afectados por rotaciones

### Seguridad infantil y privacidad

- **Sin impacto:** La decisión no afecta a la protección de datos de menores ni a los controles parentales
- **Sin impacto:** La separación entre experiencia infantil y controles parentales se mantiene

---

## 6. Límites, exclusiones y preguntas abiertas

### Límites

- Esta decisión solo aplica a las vistas que se rediseñen explícitamente
- GameView (experiencia de juego) requiere análisis específico por su criticidad
- Los minijuegos pueden requerir landscape forzado si la mecánica de juego lo exige

### Exclusiones

- No se modifica la política de PWA (`orientation: landscape` en manifest)
- No se modifica la política de Screen Orientation API
- No se implementa indicación visual para girar el dispositivo (ADR-010)

### Preguntas abiertas para responsables técnicos

1. **¿GameView requiere landscape forzado?** Analizar si los minijuegos específicos necesitan landscape para su mecánica de juego
2. **¿Cómo gestionar la transición entre vistas con diferente orientación?** Definir si algunas vistas requieren landscape y otras portrait, y cómo gestionar la transición
3. **¿Qué breakpoints usar para portrait?** Definir si se usan los breakpoints estándar de TailwindCSS o se requieren específicos para dispositivos target
4. **¿Cómo validar el diseño portrait en dispositivos reales?** Definir estrategia de pruebas en Samsung Galaxy A15 y tablets Android

---

## 7. Referencias

- ADR-010: Frontend Layer Architecture (política anterior de horizontal forzado)
- SPRINT-009: Mejora de escalado en orientación vertical (intentos de solución)
- FEAT-002: Pantalla principal y accesos iniciales
- ADR-018: Design System Foundation (breakpoints y estrategia responsive)

---

## 8. Consecuencias y implicaciones

### Para el equipo de frontend

- Rediseñar HomeView con estilos específicos para portrait
- Evaluar el impacto en otras vistas (PanelControl, GameView, Documentation)
- Validar en dispositivos target (Samsung Galaxy A15, tablets Android)
- Documentar patrones de diseño portrait para futuras vistas

### Para el equipo de producto

- Comunicar el cambio de política a los stakeholders
- Actualizar documentación de usuario si es necesario
- Validar la experiencia en dispositivos reales

### Para el equipo de diseño

- Proporcionar mockups de HomeView en orientación portrait
- Definir patrones de reacomodo de contenido (ej: avatar centrado, botones apilados)
- Validar la experiencia visual en ambas orientaciones

---

## 9. Riesgos y mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Rediseño portrait requiere más esfuerzo del esperado | Media | Medio | Empezar con HomeView como prueba, evaluar esfuerzo antes de extender a otras vistas |
| Diseño portrait no es óptimo para experiencia infantil | Baja | Alto | Validar con el equipo de producto y diseñadores antes de implementar |
| Transición entre orientaciones causa saltos visuales | Baja | Medio | Usar transiciones CSS suaves y validar en dispositivos reales |
| GameView requiere landscape forzado y no es compatible con portrait | Media | Alto | Analizar GameView específicamente en futura decisión de producto |
