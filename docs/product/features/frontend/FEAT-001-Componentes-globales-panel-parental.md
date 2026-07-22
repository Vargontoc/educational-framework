# FEAT-001 — Componentes globales del panel parental, modo oscuro y catálogo de desarrollo

## Status

state: accepted
user_history:
depends_on: ADR-017
owned_by: frontend
test:

## Descripción

### Objetivo y valor para la familia

Proveer al panel parental de un conjunto de componentes UI globales reutilizables que garanticen consistencia visual, accesibilidad táctil y usabilidad para adultos, manteniendo coherencia con la identidad visual de My Friend Nubi. Incluir modo oscuro exclusivo para el panel parental y un catálogo de componentes accesible solo en desarrollo para validación visual.

### Actores y escenarios de uso

**Adulto (padre/madre)**
- Accede al panel parental mediante PIN de 4 dígitos.
- Navega por las secciones del panel usando un sidebar colapsable.
- Gestiona niños registrados (alta, edición, eliminación con confirmación).
- Configura ajustes globales y por niño (audio, NPC, daltonismo, PIN).
- Alterna entre modo claro y oscuro según preferencia o condiciones de luz.
- Visualiza contenido (cuentos, ejercicios, documentación).
- Recibe notificaciones internas de la app.
- Es expulsado automáticamente tras 5 minutos de inactividad.

**Desarrollador**
- Accede a una URL exclusiva de desarrollo para visualizar los componentes globales de forma aislada.
- Valida aspectos visuales, estados y variantes de cada componente.
- Usa el catálogo como referencia durante la implementación.

### Requisitos funcionales

#### Componentes de acción

1. **Botón primario**: acción principal, color destacado, objetivo táctil mínimo 48x48dp, estados normal/hover/pressed/disabled.
2. **Botón secundario**: acción complementaria, menos prominente visualmente.
3. **Botón de icono**: acción rápida con icono, tooltip al hover.
4. **Botón destructivo**: acción irreversible, color distintivo (rojo suave).

#### Componentes de entrada

5. **Input de texto**: label visible, placeholder, validación en tiempo real.
6. **Input numérico**: teclado numérico en móvil, incrementos/decrementos con botones.
7. **Input PIN**: 4 dígitos numéricos estilo teclado móvil, oculta dígitos, feedback visual de completado.
8. **Checkbox**: opción binaria con label claro.
9. **Toggle/switch**: alternativa on/off visual para configuraciones frecuentes.
10. **Selector/dropdown**: selección única entre múltiples opciones.
11. **Radio buttons**: selección única entre opciones mutuamente excluyentes.

#### Componentes de navegación

12. **Sidebar colapsable**: menú lateral izquierdo, iconos + texto expandido, solo iconos colapsado, animación suave.
13. **Tabs**: navegación entre subsecciones, indicador de tab activa.
14. **Breadcrumb**: muestra ruta de navegación, permite volver a niveles anteriores.
15. **Botón atrás**: flecha + texto opcional, posición consistente (izquierda superior).

#### Componentes de feedback

16. **Modal de confirmación**: diálogo centrado para acciones críticas, overlay oscuro, título/mensaje/botones.
17. **Modal informativo**: muestra detalles o ayuda, solo botón de cierre.
18. **Toast/notificación**: mensaje temporal no intrusivo, desaparece en 3-5 segundos.
19. **Alerta/banner**: mensaje persistente hasta cierre manual.
20. **Tooltip**: información contextual al hover, no usar como única forma de comunicación.

#### Componentes de estado

21. **Loading/spinner**: indicador de carga, overlay parcial o total.
22. **Skeleton loading**: placeholder animado mientras cargan datos.
23. **Empty state**: vista cuando no hay datos, mensaje amigable + acción sugerida.
24. **Error state**: vista cuando algo falla, mensaje claro + opción de reintentar.

#### Componentes de contenido

25. **Card/tarjeta**: contenedor para elementos (niño, cuento, ejercicio), imagen + título + descripción + acciones.
26. **Avatar**: imagen circular/redondeada, tamaños pequeño/mediano/grande, fallback con iniciales.
27. **Badge/indicador**: pequeño círculo o etiqueta para estados.
28. **Lista**: elementos apilados verticalmente, separadores sutiles.
29. **Grid**: elementos en cuadrícula responsive (1 columna móvil, 2-3 tablet).

#### Componentes de progreso y datos

30. **Barra de progreso**: indicador visual de avance, animación suave.
31. **Stepper**: formularios multi-paso, muestra paso actual y total.
32. **Contador**: muestra cantidad, puede ser estático o animado.

#### Componentes de sesión y seguridad

33. **Pantalla de autenticación**: vista completa para entrada de PIN, teclado numérico, mensaje de error tras fallo.
34. **Indicador de sesión**: muestra tiempo restante antes de logout automático, aviso cuando queda poco tiempo.
35. **Overlay de inactividad**: aparece antes del logout automático, permite extender sesión o cerrar.

#### Modo oscuro

36. **Tema oscuro**: disponible exclusivamente para el panel parental.
37. **Alternancia de tema**: el adulto puede cambiar entre modo claro y oscuro desde la configuración.
38. **Persistencia de preferencia**: la preferencia de tema se mantiene entre sesiones.
39. **Experiencia infantil**: mantiene siempre modo claro, no se ve afectada por la preferencia del adulto.

#### Catálogo de componentes

40. **URL de desarrollo**: ruta accesible solo en entorno de desarrollo para visualizar componentes.
41. **Visualización aislada**: cada componente se muestra con sus variantes, estados y tamaños.
42. **No disponible en producción**: la URL no existe o redirige en entorno productivo.

### Requisitos no funcionales

- **Accesibilidad táctil**: todos los elementos interactivos deben tener objetivo táctil mínimo de 48x48dp.
- **Legibilidad**: tipografía clara con tamaños jerárquicos y contraste adecuado en ambos temas.
- **Rendimiento**: los componentes no deben introducir latencia perceptible en la navegación.
- **Consistencia**: todos los componentes deben seguir la misma estética (bordes redondeados, sombras suaves, animaciones de 200-300ms).
- **Responsive**: componentes adaptables a móvil y tablet Android.
- **Internacionalización**: todos los textos deben pasar por i18n, no se permiten literales en templates.

### Criterios de aceptación

1. Todos los componentes listados están implementados y son reutilizables.
2. Los componentes siguen la estética definida (colores suaves, bordes redondeados, animaciones funcionales).
3. El modo oscuro es aplicable exclusivamente al panel parental.
4. El adulto puede alternar entre modo claro y oscuro desde la configuración.
5. La preferencia de tema persiste entre sesiones.
6. La experiencia infantil mantiene siempre modo claro.
7. Existe una URL accesible solo en desarrollo para visualizar los componentes.
8. El catálogo muestra todos los componentes con sus variantes y estados.
9. La URL del catálogo no está disponible en producción.
10. Todos los componentes son accesibles en móvil y tablet Android.
11. Los objetivos táctiles cumplen el mínimo de 48x48dp.
12. Todos los textos visibles están internacionalizados.
13. Los componentes de sesión gestionan el logout automático tras 5 minutos de inactividad.
14. El input PIN acepta exactamente 4 dígitos numéricos.
15. El sidebar es colapsable con animación suave.

### Ámbitos que deben validar los responsables

**Frontend**
- Implementación técnica del sistema de temas (claro/oscuro).
- Elección de librería de iconos.
- Estrategia de animaciones y transiciones.
- Breakpoints responsive específicos.
- Persistencia de preferencia de tema.
- Herramienta específica para catálogo de componentes.

**Contenido**
- Definir paleta de colores exacta para ambos temas.
- Definir tipografía y tamaños jerárquicos.
- Definir iconografía (personalizada o librería estándar).
- Validar que la estética mantiene equilibrio adulto-infantil.

**Infraestructura**
- Configuración de variable de entorno para detectar modo desarrollo.
- Asegurar que la URL del catálogo no sea accesible en producción.

### Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

**Privacidad**
- La preferencia de tema es local al dispositivo, no se envía al backend.
- No se recopilan datos adicionales por usar modo oscuro.

**Seguridad infantil**
- El modo oscuro y los componentes globales no afectan a la experiencia infantil.
- La separación entre panel parental y experiencia infantil se mantiene.
- El PIN de 4 dígitos es seguridad básica aceptada para contexto monofamiliar.

**Accesibilidad**
- Objetivos táctiles mínimos de 48x48dp en todos los elementos interactivos.
- Contraste adecuado en ambos temas (claro y oscuro).
- Tipografía legible con tamaños jerárquicos claros.
- El modo oscuro mejora la legibilidad en condiciones de poca luz.
- No depender exclusivamente de color para transmitir información.

**Límites de IA**
- No aplica. Los componentes globales no involucran IA.

### Exclusiones, riesgos, supuestos y decisiones pendientes

**Exclusiones**
- Dashboard con métricas detalladas (pendiente de definición futura).
- Chatbot con historial persistente y comandos específicos (pendiente de definición futura).
- Notificaciones push externas.
- Ajustes de accesibilidad adicionales más allá de daltonismo.
- Modo oscuro en experiencia infantil.
- Catálogo de componentes en producción.

**Riesgos**
- Esfuerzo inicial elevado para implementar el sistema completo de componentes.
- Mantenimiento de dos temas (claro y oscuro) puede incrementar complejidad.
- La URL del catálogo en desarrollo podría accidentalmente quedar expuesta en producción si no se configura correctamente.

**Supuestos**
- Los adultos que usan el panel parental tienen capacidad visual y motora estándar.
- El dispositivo tiene capacidad para renderizar animaciones CSS sin problemas de rendimiento.
- El entorno de desarrollo es claramente distinguishible del productivo por los usuarios.

**Decisiones pendientes**
- Paleta de colores exacta para ambos temas.
- Tipografía específica.
- Librería de iconos.
- Herramienta de catálogo de componentes.
- Breakpoints responsive específicos.
- Estrategia de animaciones (solo funcionales o también decorativas).

### Decisiones técnicas confirmadas

Las siguientes decisiones técnicas han sido confirmadas para la implementación del sistema de diseño:

| # | Decisión | Opción | Detalles |
|---|----------|--------|----------|
| 1 | Paleta de colores | Híbrida (TailwindCSS + personalizado) | Tokens de TailwindCSS para neutros/estados + colores de marca personalizados |
| 2 | Tipografía | Nunito (Google Fonts) | Preload + font-display: swap, variable font |
| 3 | Librería de iconos | Lucide con wrapper NubiIcon | 1,400+ iconos SVG + iconos custom (lectura, relajación) |
| 4 | Herramienta de catálogo | Histoire | URL `/dev/components` protegida con `VITE_ENV=development` |
| 5 | Breakpoints responsive | TailwindCSS mobile-first | sm:640px, md:768px, lg:1024px, xl:1280px |
| 6 | Estrategia de animaciones | Funcionales + decorativas sutiles | 200ms micro-interacciones, 300ms transiciones, respetar prefers-reduced-motion |

**Condición transversal:** Todas las decisiones son fácilmente modificables cuando entre un diseñador experto. Ver `docs/design/` para guías de modificación.

**Referencia arquitectónica:** `docs/product/decisions/ADR-018-Design-System-Foundation.md`

## Mitigaciones (por capa)

**Frontend**
- Implementar componentes de forma incremental, priorizando los más usados.
- Usar variables CSS para el sistema de temas, facilitando mantenimiento.
- Validar que la URL del catálogo esté condicionada a variable de entorno de desarrollo.
- Pruebas en dispositivo físico Samsung Galaxy A15 como criterio de aceptación obligatorio.

**Contenido**
- Definir guía de estilo visual antes de implementar componentes.
- Validar contraste WCAG AA en ambos temas.
- Revisar que la estética mantenga equilibrio adulto-infantil.

**Infraestructura**
- Documentar variable de entorno para modo desarrollo.
- Validar en despliegue que la URL del catálogo no sea accesible en producción.
