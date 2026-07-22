# ADR-017 — Componentes globales del panel parental, modo oscuro y catálogo de desarrollo

## Status
status:        accepted
date:          2026-07-22
superseded_by: —

## Context

My Friend Nubi es una aplicación monofamiliar para niños de 3-4 años con un panel parental separado que requiere estética y usabilidad adulta, pero manteniendo coherencia visual con la experiencia infantil. El frontend está en fase inicial y necesita definir un conjunto de componentes UI globales reutilizables para construir el panel parental de forma consistente.

Fuerzas y restricciones:
- La experiencia infantil y parental deben estar claramente separadas.
- El panel parental es usado exclusivamente por adultos en dispositivos móviles y tabletas Android.
- La aplicación debe mantener un tono visual amigable sin ser infantil en exceso en el panel adulto.
- Se requiere accesibilidad táctil (objetivos amplios) y legibilidad en distintas condiciones de luz.
- El desarrollo necesita una forma de visualizar y validar los componentes de forma aislada.

## Decision

Se confirman las siguientes decisiones de producto para el panel parental:

### 1. Componentes UI globales

Se define un conjunto de componentes reutilizables organizados en ocho categorías:

- **Acción**: botón primario, secundario, icono, destructivo.
- **Entrada**: input texto, numérico, PIN (4 dígitos numéricos estilo móvil), checkbox, toggle/switch, selector/dropdown, radio buttons.
- **Navegación**: sidebar colapsable (menú lateral izquierdo), tabs, breadcrumb, botón atrás.
- **Feedback**: modal de confirmación, modal informativo, toast/notificación, alerta/banner, tooltip.
- **Estado**: loading/spinner, skeleton loading, empty state, error state.
- **Contenido**: card/tarjeta, avatar, badge/indicador, lista, grid.
- **Progreso y datos**: barra de progreso, stepper, contador.
- **Sesión y seguridad**: pantalla de autenticación PIN, indicador de sesión, overlay de inactividad.

### 2. Navegación del panel parental

- Menú lateral izquierdo colapsable (sidebar).
- Selector de contexto (global vs. niño) por sesión, no persistente en cabecera.

### 3. Gestión de niños y familia

- Sin límite de niños por familia.
- Confirmación obligatoria para eliminar un niño.
- Ajustes de accesibilidad por niño: solo modo de visión de colores (daltonismo). No se contemplan otros ajustes de accesibilidad en esta versión.

### 4. Seguridad y sesión

- PIN numérico de 4 dígitos, estilo teclado móvil.
- Opción de recuperar/recrear el PIN si se olvida (seguridad básica aceptada para enfoque monofamiliar).
- Logout automático tras 5 minutos de inactividad.

### 5. Contenido

- Catálogo inicial de cuentos y ejercicios de relajación: 10-15 elementos aproximadamente.
- Documentación estática, sin búsqueda.

### 6. Modo oscuro

- Modo oscuro disponible exclusivamente para el panel parental.
- La experiencia infantil mantiene siempre modo claro.
- El adulto puede alternar entre modo claro y oscuro desde la configuración del panel.

### 7. Catálogo de componentes de desarrollo

- URL accesible solo en entorno de desarrollo para visualizar los componentes globales de forma aislada.
- No disponible en producción.
- Sirve como referencia visual y herramienta de validación durante el desarrollo.

## Consequences

positive:
- Consistencia visual y funcional en todo el panel parental.
- Desarrollo más rápido al reutilizar componentes probados.
- Mejor experiencia para el adulto con modo oscuro en condiciones de poca luz.
- Validación visual aislada de componentes durante el desarrollo.
- Separación clara entre experiencia infantil (siempre clara) y parental (configurable).

negative:
- Esfuerzo inicial para diseñar e implementar el sistema de componentes.
- Mantenimiento de dos temas (claro y oscuro) en el panel parental.
- Dependencia de una herramienta de catálogo de componentes en desarrollo (riesgo bajo al ser solo desarrollo).

neutral:
- El modo oscuro no afecta a la experiencia infantil.
- El catálogo de componentes es invisible para el usuario final.
- El PIN de 4 dígitos es suficiente para el contexto monofamiliar pero no ofrece seguridad real.

## Alternatives considered

alternative: Modo oscuro también en experiencia infantil
reason_rejected: La experiencia infantil debe ser predecible y consistente. El modo oscuro podría alterar contrastes y afectar la accesibilidad para niños de 3-4 años. Se mantiene solo en panel parental.

alternative: PIN alfanumérico o de mayor longitud
reason_rejected: Para un contexto monofamiliar donde la seguridad no es crítica, un PIN de 4 dígitos numéricos es más simple y rápido de usar. La opción de recuperación elimina el riesgo de bloqueo permanente.

alternative: Selector de contexto persistente en cabecera
reason_rejected: Puede generar confusión si el usuario cambia de contexto sin darse cuenta. Un selector por sesión es más explícito y reduce errores.

alternative: Notificaciones push externas
reason_rejected: Fuera del alcance de esta versión. Las notificaciones solo dentro de la app son suficientes para el contexto monofamiliar.

alternative: Dashboard con métricas detalladas desde el inicio
reason_rejected: No hay definición clara de métricas en esta fase. Se deja como trabajo futuro cuando se concrete el valor para la familia.

## Impacto

### Experiencia infantil
- No se ve afectada. Los componentes globales son exclusivos del panel parental.
- La experiencia infantil mantiene modo claro permanente.

### Experiencia parental
- Panel consistente, accesible y usable en distintas condiciones de luz.
- Navegación clara con sidebar colapsable.
- Gestión de niños flexible sin límite.
- Seguridad básica adecuada al contexto monofamiliar.

### Accesibilidad
- Objetivos táctiles amplios (mínimo 48x48dp).
- Modo oscuro para mejorar legibilidad en poca luz.
- Ajustes de visión de colores por niño.
- Tipografía legible y espaciado generoso.

### Seguridad infantil
- Separación clara entre experiencia infantil y panel parental.
- PIN requerido para acceder al panel.
- Logout automático tras inactividad.
- No se exponen datos de niños sin autenticación.

### Privacidad
- PIN de 4 dígitos es seguridad básica aceptada para contexto monofamiliar.
- Opción de recuperación de PIN sin verificación adicional (aceptado para monofamiliar).
- No se recopilan datos biométricos ni de seguridad avanzados.

## Límites y exclusiones

### Fuera de alcance en esta versión
- Dashboard con métricas detalladas (pendiente de definición).
- Chatbot con historial persistente y comandos específicos (pendiente de definición).
- Notificaciones push externas.
- Ajustes de accesibilidad adicionales más allá de daltonismo.
- Modo oscuro en experiencia infantil.
- Catálogo de componentes en producción.

### Cuestiones pendientes para responsables técnicos
- Elección de librería de iconos (Material, FontAwesome, personalizado).
- Implementación técnica del sistema de temas (claro/oscuro).
- Herramienta específica para catálogo de componentes (Storybook, Histoire, etc.).
- Estrategia de animaciones y transiciones.
- Breakpoints responsive específicos para móvil y tablet.
- Persistencia de preferencia de tema (localStorage, sesión, etc.).

## References

- README.md
- ADR-010 Frontend Layer Architecture
- SPRINT-002 Shell, rutas, orientación y PWA
- compatibility-matrix.md
