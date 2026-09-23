# ADR-031 — Diario parental por actividad

## Estado

- **Estado:** aceptada.
- **Fecha:** 2026-09-23.
- **Modifica de forma acotada:** ADR-022 y FEAT-006, sustituyendo el destino placeholder «Dashboard» por «Diario»; FEAT-003 agentes, para permitir consultas parentales acotadas sobre el Diario.

## 1. Contexto y problema

La familia necesita una vista parental para conocer, sin convertirla en evaluación, qué actividades ha completado el niño y cuál es la configuración actual de dificultad de cada actividad. El actual destino «Dashboard» es un placeholder y el término puede sugerir métricas o juicio de rendimiento.

## 2. Necesidad de la familia y usuarios afectados

- **Adulto autenticado:** necesita consultar un resumen limitado, descriptivo y por actividad del perfil seleccionado, y puede limpiar el historial de juego sin eliminar el perfil.
- **Niño o niña:** no accede al Diario, no ve sus datos ni recibe mensajes sobre su nivel o abandonos.
- **Familia:** necesita que la adaptación siga siendo un apoyo del juego, no una medida de capacidad, y que los datos personales se minimicen y permanezcan protegidos.

## 3. Alternativas de producto consideradas y compromisos

### A. Mantener un dashboard con tendencias y métricas de rendimiento

- **Valor aparente:** aporta más datos al adulto.
- **Inconveniente:** favorece comparaciones, lectura evaluativa y supervisión excesiva; no se acepta.

### B. Diario compacto, descriptivo y por actividad

- **Valor:** informa sin rankings, gráficas, rachas ni comparaciones entre perfiles o periodos.
- **Compromiso:** limita el detalle disponible y exige lenguaje y jerarquía visual cuidadosamente no evaluativos.

### C. Mostrar solo un nivel máximo histórico

- **Valor aparente:** parece resumir progresión.
- **Inconveniente:** se interpreta como logro o capacidad y deja de describir la experiencia presente; no se acepta.

## 4. Decisión confirmada y justificación

Se sustituye «Dashboard» por **«Diario»** en la navegación **Niños → [Nombre] → Diario**. Es una vista parental, de solo lectura respecto de los datos mostrados y sin tendencias, rankings, rachas, objetivos, semáforos ni comparativas.

El Diario muestra por defecto la semana y permite Hoy, Semana, Mes y Total. Incluye tiempo jugado y el número de **actividades únicas completadas** en el periodo. Los bloques tienen orden fijo: Reconocimiento, Comparación y Memoria. Reconocimiento incluye Letras, Formas, Números, Colores y Animales.

La dificultad es siempre **por actividad**, no por motor ni por juego en general. Se muestra siempre el **nivel actual de esa actividad**, incluso si el filtro representa actividad pasada. El texto confirmado es:

> **Nivel actual de esta actividad: [Fácil | Normal | Difícil]. El juego lo ajusta automáticamente para que pueda jugar a gusto.**

Las tres paradas discreta Fácil, Normal y Difícil permanecen visibles, con texto que no depende del color. El indicador describe la configuración actual del juego, nunca una capacidad, nivel alcanzado, progreso histórico o clasificación del niño.

El Diario puede hacer disponible una señal de abandonos únicamente cuando haya **4 o más abandonos entre los 6 intentos iniciales más recientes de la misma actividad**. Esta ventana protege frente a toques accidentales; sus valores son configuración interna en esta versión y no un control parental. El abandono se presenta como hecho contextual, no como desempeño, conducta o motivo de cambio de dificultad.

El adulto dispone de una acción separada para **limpiar los datos de Diario/tracking y reiniciar la adaptación** del perfil. Conserva nombre, avatar, mes y año de nacimiento y preferencias; eliminar el perfil continúa siendo la única acción que elimina toda su información.

Se minimiza la fecha de nacimiento a mes y año. La edad calculada se usa solo para filtrar el catálogo de actividades conjuntas; no ajusta la experiencia de juego. Nombre, mes/año de nacimiento y contenido de conversaciones parentales requieren protección reforzada bajo control parental; la definición de los medios técnicos corresponde a seguridad, datos e infraestructura.

El chatbot parental puede consultar el Diario solo como adulto autenticado y con perfil explícitamente proporcionado por el contexto parental autorizado. Si no hay perfil, las propuestas de actividades conjuntas son genéricas. No infiere perfiles ni usa el Diario para recomendar necesidades, capacidad o intervención educativa.

## 5. Impacto

### Experiencia infantil

- No hay exposición al Diario ni cambio de las reglas de juego infantiles.
- El ajuste de dificultad continúa siendo invisible como evaluación y está asociado a cada actividad.

### Experiencia parental

- El adulto ve información limitada y ordenada, sin jerarquía entre actividades.
- Puede distinguir limpiar el historial/adaptación de eliminar definitivamente un perfil.

### Accesibilidad

- Nivel, filtro, estado vacío y abandonos se entienden sin depender solo de color.
- El Diario es compacto y utilizable en móvil y tableta por adultos.

### Seguridad infantil y privacidad

- Acceso exclusivamente parental y aislado por perfil y familia.
- Datos minimizados; no hay compartición, perfilado comercial ni reutilización fuera de las finalidades confirmadas.
- El chatbot distingue hechos de síntesis y no infiere información sobre un menor.

## 6. Límites, exclusiones y cuestiones para los responsables técnicos

### Límites y exclusiones

- Diagnóstico, evaluación, clasificación, comparación, recomendación educativa profesional o interpretación de conducta.
- Gráficas de tendencia, niveles máximos históricos, porcentajes de capacidad, logros, rachas o puntuaciones.
- Personalización de actividades conjuntas a partir del desempeño del Diario.
- Definición técnica de eventos, almacenamiento, cifrado, borrado, herramientas, contratos, acceso o infraestructura.

### Ámbitos que deben validar los responsables

- **Frontend y accesibilidad:** comprensión de nivel actual por actividad, filtros, estados vacíos, abandonos y acción destructiva separada.
- **Backend y datos:** unicidad de actividades completadas por periodo, aislamiento de datos, reinicio completo de tracking/adaptación y ventana de abandonos.
- **Agentes:** uso de información autorizada, selección explícita de perfil y respuestas sin inferencias o consejos profesionales.
- **Contenido:** textos cálidos y neutros, y catálogo de actividades conjuntas apropiado por edad.
- **Seguridad, privacidad e infraestructura:** protección reforzada de datos personales y conversaciones, consentimiento/control parental y eliminación conforme a la decisión.

### Preguntas abiertas

- Ninguna de producto dentro del alcance confirmado. Los responsables técnicos deben validar viabilidad y garantías sin alterar estas decisiones.

## Referencias

- README.md.
- diario-feature.md.
- ADR-022 — Gestión parental de perfiles infantiles.
- FEAT-006 — Gestión parental de perfiles infantiles.
- FEAT-013 frontend — Minijuegos: interacción visual básica y cierre sin fricción.
- FEAT-003 agentes — Chatbot parental conversacional de Nubi.
