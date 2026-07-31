# SKILL — defect-reporting

## Objetivo
Crear incidencias accionables para que el developer pueda corregir sin reinterpretar el problema.

## Campos obligatorios
- `id`
- `severity`: critical | major | minor | observation
- `type`: completeness | functional | contract | security | regression | quality | environment
- `task`
- `description`
- `expected`
- `observed`
- `evidence`
- `reproduction`
- `required_action`
- `status`: open | fixed | verified | waived

## Escalado
Los defectos tecnicos vuelven al developer. Usa `USER_DECISION_REQUIRED` solo ante ambiguedad o contradiccion de producto/arquitectura.
