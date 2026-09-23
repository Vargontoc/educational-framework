# FEAT-004 — Chatbot parental: Diario y actividades conjuntas

## Estado

- **Estado:** aceptada.
- **Responsable principal:** agents.
- **Decisión de producto:** ADR-031.
- **Depende de:** FEAT-003 — Chatbot parental conversacional de Nubi; FEAT-014 backend — Diario parental: datos y reinicio por perfil.

## 1. Objetivo y valor para la familia

Permitir que el chatbot parental explique hechos autorizados del Diario con vocabulario no evaluativo y proponga actividades conjuntas procedentes de un catálogo curado. No diagnostica, no califica al niño ni personaliza propuestas según desempeño.

## 2. Actores y escenarios de uso

### Adulto que consulta un perfil

1. Accede al chatbot como adulto autenticado.
2. Selecciona explícitamente un perfil, salvo que exista uno único.
3. Consulta resumen, nivel actual por actividad o detalle de actividad.
4. Recibe hechos disponibles y la explicación: el juego ajusta cada actividad para que pueda jugar a gusto.

### Adulto que solicita una actividad conjunta

1. Si el contexto parental autorizado ya contiene un perfil seleccionado, la propuesta se filtra por su edad calculada desde mes/año.
2. Si no hay perfil seleccionado, recibe una propuesta genérica del catálogo curado.
3. La propuesta no se basa en aciertos, abandonos, dificultad ni necesidad inferida.

## 3. Requisitos funcionales y no funcionales

1. El chatbot permanece disponible exclusivamente a adultos autenticados.
2. Puede presentar resumen de periodo, nivel actual por actividad y detalle de actividad solo del perfil explícitamente seleccionado o del único perfil disponible.
3. Toda respuesta sobre nivel debe decir que es el nivel actual de esa actividad y que el juego lo ajusta automáticamente para que pueda jugar a gusto.
4. El chatbot no presenta el nivel como nivel alcanzado, capacidad, rendimiento, logro ni recomendación.
5. Puede proponer actividades conjuntas solo desde catálogo curado; con perfil seleccionado puede aplicar exclusivamente el filtro de edad confirmado, y sin perfil responde de forma genérica.
6. No infiere qué perfil usar, no mezcla perfiles y no utiliza Diario/tracking para personalizar actividades conjuntas.
7. Puede abrir la sección de Relajación en familia como navegación, sin registrar ni modificar datos infantiles.
8. Distingue hechos disponibles de explicaciones y mantiene las exclusiones y derivaciones de FEAT-003.

## 4. Criterios de aceptación verificables

1. Una consulta individual con varios perfiles sin selección explícita no muestra datos y solicita seleccionar un perfil.
2. Una respuesta de nivel identifica la actividad concreta y su nivel actual, sin comparativas, porcentaje ni interpretación sobre el niño.
3. Una propuesta conjunta con perfil seleccionado usa únicamente el filtro de edad autorizado y nunca menciona desempeño, abandono o dificultad como motivo.
4. Una propuesta conjunta sin perfil seleccionado es genérica y no contiene datos de ningún niño.
5. Ninguna respuesta mezcla datos de perfiles, solicita datos personales adicionales ni formula diagnóstico o consejo profesional.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **Agentes:** límites de respuesta, distinción hecho/síntesis, ausencia de inferencias y derivación de asuntos profesionales.
- **Backend y datos:** disponibilidad exclusiva de hechos autorizados, perfil seleccionado y catálogo curado por edad.
- **Contenido:** adecuación familiar de actividades conjuntas y textos no evaluativos.
- **Privacidad y seguridad:** acceso parental, aislamiento por perfil y protección de conversaciones.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- No se ofrece chatbot al niño ni conversación libre infantil.
- No se reutilizan conversaciones ni datos de Diario para fines distintos de la consulta adulta autorizada.
- No se infieren emociones, capacidad, necesidades educativas o sanitarias.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Recomendaciones educativas, sanitarias, psicológicas o de seguridad; diagnóstico; personalización por desempeño; escritura o modificación de datos.

### Riesgos

- El adulto puede interpretar una propuesta conjunta como intervención recomendada; el lenguaje debe describirla como idea genérica y curada para compartir.

### Decisiones pendientes

- Ninguna de producto.

## Referencias

- ADR-031 — Diario parental por actividad.
- FEAT-003 — Chatbot parental conversacional de Nubi.
- FEAT-014 backend — Diario parental: datos y reinicio por perfil.
