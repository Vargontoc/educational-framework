# Feat-006 - Agent Child: Motivation scope for childs

## Status

state: accepted
user_history: El ámbito del agente es acompañar y motivar
owned_by: agents
depends_on: FEAT-001-Agent-Child-Modelfile.md
test: El agente motiva al usuario sin caer en la sobre estimulación

## Description

El objetivo de este feature es que el agente pueda motivar a los niños de una manera efectiva y segura. Esto incluye proporcionar recompensas, reconocimientos y feedback positivo para fomentar su interés y participación en las actividades. Nunca debe comportaser como un tutor o padre estricto, sino como un aliado que apoya y anima.

Intentará siempre mantener el equilibrio entre motivación y desafío, asegurándose de que los niños no se sientan presionados ni juzgados. El agente debe ser capaz de adaptarse a las necesidades individuales de cada niño y proporcionar un entorno seguro y estimulante. 

Nunca hacer que el el aprendizaje dependa de la aplicación, intentar en la medida de lo posible despertar la curiosidad y que haga preguntas a los adultos.

## Análisis

Resumen: el agente debe motivar y acompañar, evitando sobreestimulación y dependencia. La motivación será una mezcla de feedback positivo, micro-recompensas internas (badges/estrellas) y recomendaciones de acciones fuera de la app (preguntas a adultos, actividades físicas). El backend es el autoritativo para reglas de motivación (qué dar, cuándo y a quién), mientras que el agente aplica estilos y frases adaptadas al rango de edad.

Requerimientos clave:
- Equilibrio: evitar reforzar únicamente motivación extrínseca (recompensas) sobre motivación intrínseca (curiosidad).
- Seguridad: no inducir comportamientos riesgosos ni dependencia de la app para el aprendizaje.
- Personalización limitada: adaptar feedback por edad y por señales de progreso, sin almacenar PII innecesaria.
- Controles parentales: intensidad de motivación (baja/media/alta), bloquear recompensas, y límites de sesión.

## Patrón y relación con otras features

- FEAT-001: Modelfile debe incluir constraints de tono y longitud para mensajes motivacionales.
- FEAT-004 (Curiosidades): usar curiosidades como estímulo ocasional para motivación intrínseca.
- FEAT-005 (Character): muletillas de personaje pueden reforzar motivación si están contextualizadas.

## Esquema de datos (motivation_action)

Cada acción de motivación enviada al agent/backend puede tener este esquema mínimo:

{
	"action_id": "string",
	"type": "praise|reward|suggestion|challenge",
	"text": "string",
	"age_min": integer,
	"age_max": integer,
	"intensity": "low|medium|high",
	"external_suggestion": {"label":"string","hint":"string"},
	"curiosity_id": "string (optional)",
	"metadata": { }
}

Notas:
- `type`: define si es feedback (praise), una recompensa virtual (reward), una sugerencia para la vida real (suggestion) o un pequeño reto (challenge).
- `external_suggestion`: usado para incentivar actividades fuera de la app (ej. "pregunta a un adulto sobre X").

## Flujo recomendado

1. Juego genera evento de progreso o fallo.
2. Backend evalúa estado parental, intensidad configurada y perfil del niño; selecciona candidate `motivation_action` (puede usar pool rotatorio y heurísticas de diversidad).
3. Si se permite paraphrase/variedad (v. FEAT-005 option B), backend puede enviar plantilla con `allow_paraphrase` y `paraphrase_constraints` para obtener una versión más natural del agent.
4. Agent devuelve la respuesta final; backend aplica validaciones (si hubo paraphrase) y decide mostrar TTS o UI.
5. Backend registra métricas (engagement, repeats, parental feedback).

## Riesgos y mitigaciones

Riesgo: Dependencia/extrinsic motivation (niños motivados solo por recompensas virtuales).
- Mitigación: Priorizar acciones que fomenten curiosidad y acciones fuera de la app; limitar frecuencia de rewards; diseñar transiciones hacia metas intrínsecas.

Riesgo: Sobreestimulación (demasiadas notificaciones, elogios repetitivos).
- Mitigación: Rate limiting, intensidad configurable por padres, heurística de cooldown por sesión.

Riesgo: Mensajes inapropiados o culturalmente insensibles.
- Mitigación: Curación y revisión humana de templates; safety_filters y listas de bloqueo por locale.

Riesgo: Recompensas que fomenten comportamiento de riesgo o competición nociva.
- Mitigación: No ofrecer recomendaciones que impliquen riesgo físico; evitar comparaciones directas entre niños; diseñar rewards orientadas a progreso personal.

## Requerimientos por capas

- Backend: reglas de selección, pool rotatorio, validación de paraphrase (si aplica), auditoría, telemetría de engagement; endpoints para configuración parental.
- Agent/Modelfile: aplicar constraints de tono, longitud y lenguaje positivo; si se paraphrasea, seguir validación de FEAT-005.
- Frontend: controles parentales, UI de reward (no intrusiva), opción para mostrar sugerencias externas y marcar completadas.
- Ops: pruebas A/B para calibrar intensidad y latencia de validación; backups y rollback para cambios en templates.

## Testing y criterios de aceptación

- Unit tests: validación de esquema `motivation_action`.
- Integration: flujo evento→selección backend→(paraphrase opcional)→agent→frontend; medir que restricciones (edad, intensidad) se respetan.
- UX testing: con padres/educadores para validar que la motivación no resulta en sobreestimulación ni dependencia.

## Métricas y auditoría

- Engagement: tasa de respuesta tras acción motivacional.
- Repetición: frecuencia de muletillas/rewards por sesión.
- Feedback parental: porcentaje de desactivaciones o ajustes de intensidad.
- Safety: número de paraphrases rechazadas por validación.

## Notas futuras

- Diseñar plantillas que favorezcan preguntas abiertas para adultos (refuerzo de aprendizaje fuera de la app).
- Evaluar experimentos de transición de extrinsic→intrinsic motivation (micro-habits) con cohorts.
