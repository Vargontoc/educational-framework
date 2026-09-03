# FEAT-009 — Relajación familiar: ejercicios cotidianos de respiración guiada

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-09-01
- **Historia de usuario:** Como adulto de la familia, quiero abrir un ejercicio de respiración guiada con música relajante para compartir un momento de calma cotidiano con el niño.
- **Depende de:** ADR-025 — Alcance de Relajación Familiar; FEAT-004 — Estructura visual y navegación del panel parental; ADR-020.

## 1. Objetivo y valor para la familia

Ofrecer a la familia un recurso sencillo y tranquilizador: un ejercicio de respiración guiada con música relajante que el adulto puede iniciar en cualquier momento del día junto al niño.

Esta funcionalidad cubre la sección «Relajación familiar» ya prevista como acceso en FEAT-004, hasta ahora sin desarrollar. Según ADR-025, esta versión se limita a rutinas cotidianas; no incluye ningún apartado para momentos difíciles ni «modo crisis».

## 2. Actores y escenarios de uso

### Adulto que abre la sección

1. Desde «Experiencias» en el panel parental entra a Relajación familiar.
2. Ve un fondo visual propio de la sección, ajustado correctamente tanto en vertical como en horizontal.
3. Ve el acceso al ejercicio de respiración guiada.

### Adulto que hace el ejercicio con el niño

1. Inicia el ejercicio de respiración guiada junto al niño.
2. Ve un apoyo visual del ritmo de respiración y escucha música relajante de acompañamiento.
3. Puede pausar o repetir el ejercicio cuando quiera.
4. Termina el ejercicio y puede volver a iniciarlo o salir de la sección.

## 3. Requisitos funcionales y no funcionales de producto

1. La sección debe mostrar un fondo visual propio, correctamente ajustado tanto en vertical como en horizontal, en móvil y tableta.
2. El único tipo de ejercicio de esta versión es la respiración guiada, con apoyo visual del ritmo y música relajante de acompañamiento integrada en el propio ejercicio.
3. El adulto controla el inicio, la pausa y la repetición del ejercicio; el niño no necesita manejar ningún control para participar.
4. El ejercicio debe seguir siendo comprensible y utilizable sin música, mediante el apoyo visual del ritmo de respiración.
5. El estilo visual debe ser tranquilo, sobrio y coherente con el resto del producto.
6. La sección no debe mostrar ningún apartado, entrada, texto o distinción relacionados con momentos difíciles, rabietas o crisis.
7. La sección debe ser usable en móvil portrait y tableta landscape, con objetivos táctiles amplios.

## 4. Criterios de aceptación verificables

1. El fondo de la sección se muestra correctamente ajustado tanto en vertical como en horizontal, sin recortes ni deformaciones.
2. El adulto puede iniciar, pausar y repetir el ejercicio de respiración guiada.
3. Durante el ejercicio se muestra un apoyo visual del ritmo de respiración y se reproduce música relajante de acompañamiento.
4. El ejercicio sigue siendo comprensible y utilizable sin sonido.
5. Ningún control de la actividad requiere que el niño interactúe directamente; el adulto puede completar el ejercicio sin que el niño toque la pantalla.
6. La sección no muestra ningún apartado, texto o distinción relacionados con momentos difíciles, rabietas o crisis.
7. La sección no muestra datos infantiles, progreso, comparativas ni registros de episodios de conducta.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Ajuste correcto del fondo en las distintas orientaciones y tamaños de pantalla.
- Diseño del ejercicio y del apoyo visual del ritmo de respiración.
- Reproducción de la música relajante de acompañamiento, incluida su ausencia sin bloquear el ejercicio.

### Contenido

- Redacción del texto de introducción y de las instrucciones de respiración guiada, revisados para edad 3-4 años.

### Backend/infraestructura

- Origen y disponibilidad del recurso de música relajante.

### Dependencias de producto conocidas

- Acceso a la sección mediante el panel parental autenticado por PIN (FEAT-004).
- ADR-025 — Alcance de Relajación Familiar.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- La sección se accede únicamente tras validar el PIN parental, dentro de «Experiencias».
- No se recoge ni registra ninguna información sobre el estado emocional o conductual del niño.
- El adulto controla en todo momento el ejercicio; el niño no dispone de controles propios en esta vista.

### Accesibilidad y experiencia

- El apoyo visual del ritmo de respiración debe ser comprensible sin depender únicamente del sonido.
- Fondo, textos y controles deben ser legibles y usables en móvil portrait y tableta landscape.
- Objetivos táctiles amplios, adecuados para uso compartido adulto-niño.

### Límites de IA

- Esta funcionalidad no usa IA para generar contenido, evaluar al niño ni personalizar el ejercicio según su conducta.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Cualquier apartado, entrada o contenido dirigido específicamente a momentos difíciles, rabietas o crisis (ADR-025).
- Diagnóstico, terapia, tratamiento o técnicas de intervención conductual especializada.
- Registro o seguimiento de episodios, frecuencia o intensidad de rabietas del niño.
- Ejercicios de movimiento o estiramiento en esta primera versión.
- Un reproductor de música independiente fuera del ejercicio de respiración.

### Riesgos

- Ninguno significativo identificado para esta versión acotada; el riesgo relevante (framing de un apartado de crisis) se elimina al retirarlo del alcance.

### Supuestos

- La música relajante es un recurso de acompañamiento del ejercicio, no una biblioteca de música independiente.

### Decisiones pendientes

- Origen técnico del recurso de música relajante.
- Redacción definitiva del texto de introducción y de las instrucciones de respiración guiada.
- Diseño visual definitivo del fondo adaptado y del apoyo visual de respiración.
- Un apartado para momentos difíciles queda fuera de esta versión; su posible incorporación requerirá una futura decisión de producto independiente (ver ADR-025).
