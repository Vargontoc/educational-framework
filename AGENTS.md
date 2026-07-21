# Reglas comunes del proyecto

## Producto y alcance

- My Friend Nubi es una aplicación monofamiliar para niños de 3-4 años, diseñada primero para tabletas y móviles, con una concurrencia aproximada de 5-6 usuarios.
- Es un acompañamiento de refuerzo, no una herramienta educativa profesional, diagnóstica ni evaluativa.
- El progreso es orientativo para la familia: nunca debe expresar capacidad, clasificación o diagnóstico del menor.
- Consulta `README.md` antes de trabajar en un área del producto.

## Protección infantil y privacidad

- La protección del menor prevalece sobre la conveniencia, la analítica y los objetivos de producto.
- Recoge y conserva exclusivamente los datos necesarios para la funcionalidad acordada y bajo control parental.
- No incluir publicidad, perfilado comercial, compartición de datos de menores ni persuasión dirigida a niños.
- Mantener separadas la experiencia infantil y las opciones, datos y controles parentales.
- `npc-game` se limita al contexto de juego, con contenido seguro y adecuado por edad; no solicita datos personales ni induce decisiones sensibles.
- `chatbot` atiende solo a adultos, distingue hechos de inferencias y deriva consultas sanitarias, psicológicas, educativas profesionales o de seguridad.

## Convenciones compartidas

- Mantener las decisiones y especificaciones existentes salvo que una decisión confirmada requiera actualizarlas.
- La documentación de producto se redacta en español. El código sigue las convenciones de su capa.
- Los agentes especializados determinan los detalles de implementación dentro de su responsabilidad, respetando los ADR y FEAT aprobados.

## Layers

- `docs/`                      -> decisiones de arquitectura, features, documentacion, contratos
- `framework/tts/`             -> minimal api which is consumed for backend, integrate Chatterbox comunication 
- `framework/agents/`          -> agentes de la aplicación
- `framework/backend/`         -> Spring Boot API, business logic
- `framework/frontend/`        -> Vue3 SPS, UI components, API conssumption

## Contratos

Fuente de verdad transversal a todas las capas, ninguna capa duplica ficheros.

- `docs/contracts/schemas`    -> descansa los distintos esquemas compartidos entre capas, tanto entrada como salida. En formato .yaml
- `docs/contracts/endpoints`  -> habita los distintos endpoints relacionando esquema
en formato .yaml
- `docs/contracts/ddl`        -> distintos esquemas de la base de datos

## Flujo de trabajo entre agentes

1. El usuario expone una necesidad, modificación o problema
2. El Product Owner analiza la necesidad y genera o actualiza un FEAT o un ADR de producto
3. El usuario confirma el requisito funcional cuando sea necesaria una decisión de producto
4. El analista técnico correspondiente traduce el requisito aprobado a diseño técnico, contratos, dependencias y sprints
5. El desarrollador implementa exclusivamente un sprint aprobado
6. El desarrollador actualiza las tareas implementadas y registra las pruebas y evidencias correspondientes
7. El reviewer ejecuta las pruebas y comprueba:
    - la completitud del sprint
    - el cumplemiento del FEAT y los ADR
    - la conformidad con los contratos
    - la ausencia de regresiones
    - la calidad técnica de la implementacion
8. Si existen defectos corregibles, el reviewer devuelve un informe al desarrollador
9. Si falta una decisión funcional o arquitectónica se escala al usuario
10. Solo el reviwer puede dar declarar el sprint vverificado

## Reglas de transición

- El Product Owner no diseña la implementación técnica
- El analista no modifica código de producción
- El desarrollador no modifica el alcance funcional de un sprint
- El desarrollador marca tareas como implementadas, no como verificadas
- El reviewer no modifica código de producción
- Un sprint rechazado vuelve al desarrollador con incidencias concretas
- NO se solicita intervención del usuario para fallos técnicos ordinarios.
- El usuario interviene cuando existe una decisión, cambio de alcance, contradicción o bloqueo no resoluble por los agentes

### Routers

- Analisis: `agents/analysis/analyser.md`
- Desarrollo: `agents/develop/developer.md`
- Revison: `agents/review/reviewer.md`

