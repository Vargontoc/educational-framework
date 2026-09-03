# ADR-025 — Alcance de Relajación Familiar: solo ejercicios cotidianos de respiración guiada

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-09-01
- **Sustituida por:** —

## 1. Contexto y problema

El panel parental prevé una sección «Relajación familiar» (README, FEAT-004) aún sin definir. La familia quiere ejercicios de relajación para hacer con el niño, con música relajante.

Durante la discusión de esta decisión se planteó también un apartado para momentos de alta intensidad o rabieta. La aplicación se define como un acompañamiento no profesional y nunca evaluativo (README), y el chatbot parental ya establece que ante asuntos sanitarios o psicológicos siempre deriva a un profesional adecuado y nunca diagnostica ni trata (ADR-003). Un apartado de «crisis» sin acotar podría sugerir que la aplicación gestiona o resuelve una crisis conductual infantil, lo cual excede su alcance declarado.

Tras valorar esa tensión, se confirma que **esta versión no incluye ningún apartado para momentos difíciles ni «modo crisis»**. Relajación familiar se limita a rutinas de relajación cotidianas.

## 2. Necesidad de la familia y usuarios afectados

- **Adulto:** quiere ejercicios sencillos de relajación en familia para el día a día, disponibles en cualquier momento, sin que la aplicación se presente como una herramienta clínica.
- **Niño:** se beneficia de un momento de calma compartido, siempre guiado por el adulto, sin exposición a lenguaje evaluativo, diagnóstico o alarmante sobre su propia conducta.

## 3. Alternativas de producto consideradas y compromisos

### A. Solo ejercicios cotidianos, sin apartado para momentos difíciles (elegida)

**Valor:** alcance mínimo, riesgo bajo, coherente sin matices con el carácter no profesional del producto. Permite entregar valor real a la familia sin abrir una superficie sensible que requiera guardarraíles de contenido adicionales.

**Inconveniente:** no cubre, en esta versión, la necesidad de tener algo a mano en momentos difíciles. Puede revisarse más adelante como una decisión de producto independiente.

### B. Apartado para momentos difíciles con framing no clínico

**Valor:** cubriría la necesidad de la familia sin prometer gestión de crisis, reutilizando el mismo ejercicio de respiración guiada, solo cambiando la puerta de entrada y el acompañamiento textual.

**Motivo por el que no se adopta en esta versión:** aun con un framing cuidadoso, introduce una superficie de contenido sensible (redacción de derivación, distinción visual sin generar alarma) que la familia ha decidido no abordar todavía. Queda como opción para una futura decisión de producto si la necesidad persiste.

### C. Apartado de crisis con contenido y técnicas específicas de manejo conductual

**Valor aparente:** parece más específico para el momento de mayor necesidad.

**Riesgo:** se acerca a orientación terapéutica o conductual especializada, contradice el carácter no profesional del producto y puede generar expectativas de eficacia clínica que la aplicación no puede sostener.

**Decisión:** descartada.

## 4. Decisión confirmada y justificación

Se confirma que Relajación familiar incluye, en esta versión, **una única área de rutinas**: ejercicios de relajación cotidianos, disponibles para cualquier momento del día, sin distinción de intensidad ni framing de crisis.

El único tipo de ejercicio es la **respiración guiada**, con apoyo visual del ritmo y **música relajante integrada como acompañamiento del propio ejercicio** (no un reproductor de música independiente). El adulto controla el inicio, la pausa y la repetición del ejercicio; el niño no maneja los controles, siguiendo el mismo modelo de control adulto ya confirmado para Lectura familiar.

No se incluye ningún apartado, entrada, texto ni nota de derivación relacionados con momentos difíciles, rabietas o crisis en esta versión.

## 5. Impacto

### Experiencia infantil

- El niño participa en un momento de calma compartido y guiado por el adulto; no ve lenguaje sobre «crisis», «rabieta» ni evaluación de su comportamiento.

### Experiencia parental

- El adulto encuentra ejercicios sencillos para el día a día, sin ninguna superficie que sugiera que la aplicación gestiona situaciones difíciles.

### Accesibilidad

- El apoyo visual y sonoro de la respiración guiada debe ser comprensible y utilizable en móvil y tableta, y debe funcionar incluso con el sonido desactivado.

### Seguridad infantil y privacidad

- No se registra ni analiza ninguna información sobre el estado emocional o conductual del niño; la sección no recoge datos del niño en absoluto.
- Al no existir apartado de momentos difíciles, se elimina el riesgo de que la aplicación parezca prometer gestión de crisis.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- Cualquier apartado, entrada o contenido dirigido específicamente a momentos difíciles, rabietas o crisis.
- Diagnóstico, terapia, tratamiento o técnicas de intervención conductual especializada.
- Registro o seguimiento de episodios, frecuencia o intensidad de rabietas del niño.
- Ejercicios de movimiento o estiramiento y un reproductor de música independiente: quedan fuera de esta primera versión.

### Preguntas abiertas para los responsables técnicos

- **Contenido:** redactar el texto de introducción y las instrucciones de respiración guiada, revisados para edad 3-4 años.
- **Backend/infraestructura/contenido:** definir el origen del recurso de música relajante.

### Reconsideración futura

- Un apartado para momentos difíciles (alternativa B) puede revisarse en una futura decisión de producto independiente si la familia confirma esa necesidad; este documento conserva el análisis de riesgo ya realizado como punto de partida.

## Referencias

- README.md
- ADR-003 — Chatbot parental conversacional de Nubi (precedente de derivación profesional)
- FEAT-004 — Estructura visual y navegación del panel parental
- FEAT-009 — Relajación familiar: ejercicios cotidianos de respiración guiada
