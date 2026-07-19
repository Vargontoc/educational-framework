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
- `framework/infrastructure/`  -> docker compose, levantar aplicación
- `framework/agents/`          -> agentes de la aplicación
- `framework/backend/`         -> Spring Boot API, business logic
- `framework/frontend/`        -> Vue3 SPS, UI components, API conssumption

## Contratos

Fuente de verdad transversal a todas las capas, ninguna capa duplica ficheros.

- `docs/contracts/schemas`    -> descansa los distintos esquemas compartidos entre capas, tanto entrada como salida. En formato .yaml
- `docs/contracts/endpoints`  -> habita los distintos endpoints relacionando esquema
en formato .yaml
- `docs/contracts/ddl`        -> distintos esquemas de la base de datos



