# Feat-001 - Family Module

## Status

state: accepted
user_history: Interfaz visual básica para la aplicación
depends_on:
owned_by: frontend
scope: frontend-only (styles & UI components). No hay integración backend en esta feature.
test: URL accesible solo en perfil de desarrollo para visualizar componentes customizados

## Description

El objetivo de esta feature es establecer la base visual de la aplicación: variables de diseño, estilos globales y los primeros componentes UI (botones, tarjetas, tipografía, paleta de colores, fichas interactuables). Público objetivo: niños de 3–8 años. Debe ser colorida, accesible y fácil de usar.

La demo será una ruta/colección de vistas incluida solamente en builds de desarrollo para revisar y ajustar componentes; no debe enviar datos reales ni integrar APIs.

## Alcance (aclaración)

- Solo estilos y componentes visuales (CSS/SCSS, variables, componentes Vue).
- Mock data local para la demo; no crear o modificar lógica backend.
- Incluir controles de accesibilidad y ajustes para targets táctiles.

## Riesgos

- Exposición accidental de la demo en producción (URL/asset leak).
- Contenido o vocabulario no apropiado para la edad.
- Problemas de accesibilidad (contraste, tamaños táctiles insuficientes).
- Rendimiento en dispositivos modestos debido a imágenes/animaciones pesadas.

## Mitigaciones (por capa)

- Backend: N/A para esta feature (no hay cambios ni dependencias). Si en el futuro se añade integración, aplicar restricciones de entorno para endpoints de demo.
- Frontend:
 	- Incluir la demo solo cuando `import.meta.env.MODE === 'development'`.
 	- Mantener todos los datos de demo en un módulo `mock` (no llamadas a red).
 	- Definir variables CSS para colores, tamaños y espaciados; usar SVGs optimizados y `loading=lazy` donde proceda.
 	- Aplicar buenas prácticas WCAG básicas: contraste >= 4.5:1 para texto, targets táctiles >= 44x44px.
 	- Evitar animaciones largas; ofrecer opción de reducir movimiento.
- Agents (si los usan en el futuro): restringir vocabulario, aplicar filtros y usar respuestas preaprobadas para demo; no permitir generación libre de texto para usuarios menores.

## Criterios de aceptación

- Variables CSS definidas: paleta primaria/secundaria, tipografías, espacios, radios.
- Componentes: `Button`, `Card`, `Badge` (estados primario/secondary/disabled) con ejemplos en la demo.
- Demo accesible sólo en ambiente de desarrollo y documentada en este feature.
- Mock data centralizada en `mock` y sin llamadas externas.
- Checklist de accesibilidad simple incluida (contraste, tamaños, labels).

## Mejoras recomendadas (sin sobre-ingeniería)

- Añadir un pequeño README en la carpeta frontend describiendo cómo abrir la demo y qué archivos editar.
- Empezar con un set reducido de variables CSS (4 colores, 3 tamaños de texto, 3 spacings).
- Validar componentes frente a un checklist manual de accesibilidad y realizar pequeñas pruebas en dispositivos móviles.

## Notas

Mantener este feature ligero: es una base visual inicial para iterar con feedback de diseño y pruebas de usuarios. No añadir integración backend ni persistencia en esta etapa.
