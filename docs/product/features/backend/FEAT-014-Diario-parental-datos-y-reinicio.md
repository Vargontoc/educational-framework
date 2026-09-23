# FEAT-014 — Diario parental: datos y reinicio por perfil

## Estado

- **Estado:** aceptada.
- **Responsable principal:** backend y datos.
- **Decisión de producto:** ADR-031.
- **Depende de:** FEAT-006 — Tracking Module; FEAT-013 frontend; acceso parental válido.

## 1. Objetivo y valor para la familia

Proporcionar al Diario solo los hechos parentales necesarios: tiempo jugado, actividades únicas completadas en un periodo, configuración actual de dificultad por actividad y señal contextual de abandono. Permitir al adulto reiniciar este historial y la adaptación sin borrar el perfil.

## 2. Actores y escenarios de uso

### Consulta parental por periodo

1. El adulto autorizado solicita el Diario de un perfil y periodo.
2. Recibe solo los datos de ese perfil: resumen, actividades únicas completadas y dificultad actual de cada actividad visible.
3. Si no hubo actividad en el periodo, recibe un estado neutro sin interpretación.

### Señal de abandono

1. Se consideran los seis intentos iniciales más recientes de una misma actividad.
2. Si cuatro o más terminan en abandono, el dato queda disponible como señal contextual parental.
3. La señal no cambia por sí misma la dificultad ni se traduce en una clasificación.

### Reinicio solicitado por adulto

1. El adulto confirma limpiar Diario/tracking del perfil.
2. Se eliminan los datos de actividad y adaptación de ese perfil.
3. Se conservan identidad del perfil, avatar, mes/año de nacimiento y preferencias; las actividades vuelven a comenzar en Fácil.

## 3. Requisitos funcionales y no funcionales

1. El acceso al Diario se limita a un adulto autorizado de la misma familia y al perfil solicitado.
2. El resumen por periodo incluye tiempo jugado y número de actividades únicas completadas, no número total de partidas ni repeticiones.
3. Una actividad cuenta una sola vez por periodo cuando se ha completado, aunque exista más de una finalización en ese periodo.
4. La dificultad disponible es la configuración actual individual de cada actividad, con los valores Fácil, Normal o Difícil; no es una dificultad global del juego.
5. Los cambios de filtro no convierten el nivel actual en histórico ni infieren una evolución.
6. La señal de abandono se hace disponible únicamente con 4 o más abandonos en los 6 intentos iniciales más recientes de esa actividad. Los valores son internos, no un ajuste parental.
7. La limpieza parental elimina Diario/tracking y reinicia adaptación del perfil, sin eliminar su perfil ni sus preferencias.
8. Nombre, mes/año de nacimiento y conversaciones parentales requieren protección reforzada y acceso exclusivamente parental. Mes/año se usan solo para filtrar actividades conjuntas.
9. No se exponen porcentajes de capacidad, rendimiento, métricas de velocidad, clasificación, diagnóstico ni datos de otro perfil.

## 4. Criterios de aceptación verificables

1. Un adulto no autorizado, una familia distinta o un perfil distinto no pueden consultar datos del Diario del perfil solicitado.
2. Una actividad completada varias veces en el mismo periodo cuenta una vez en el resumen de actividades únicas.
3. Una actividad no completada no se cuenta como actividad única completada del periodo.
4. Toda actividad devuelta incluye solo su nivel actual individual; no se devuelve nivel máximo histórico ni nivel global de juego.
5. Con tres o menos abandonos en los seis intentos iniciales recientes no hay señal parental de abandono; con cuatro o más, la señal queda disponible para la presentación aprobada.
6. El abandono no modifica por sí mismo la dificultad actual de una actividad.
7. Tras una limpieza confirmada, no quedan disponibles los datos de Diario/tracking previos del perfil y su siguiente actividad parte de Fácil; el perfil y preferencias continúan disponibles.
8. La edad calculada desde mes/año solo se proporciona para el filtrado de actividades conjuntas, nunca para adaptar el juego.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **Backend y datos:** aislamiento familiar/perfil, consistencia de periodos, actividad única, reinicio y ventana interna de abandonos.
- **Seguridad y privacidad:** minimización de fecha, protección reforzada de datos personales y conversaciones, y borrado completo de los datos seleccionados por el adulto.
- **Frontend:** estados disponibles de resumen, nivel actual, abandono y resultado de limpiar datos.
- **Agentes:** acceso solo a los hechos parentales autorizados por ADR-031.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- El Diario y sus datos se destinan exclusivamente a adultos autenticados.
- El reinicio no reutiliza ni conserva datos de tracking eliminados para clasificación, perfilado o recomendaciones.
- Los datos no se mezclan entre perfiles ni se usan para evaluar al menor.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Diseño de almacenamiento, cifrado, contratos, algoritmos de adaptación, retención, eventos o infraestructura.
- Gráficas, evolución temporal, porcentajes, rankings, diagnósticos y recomendaciones educativas.

### Riesgos

- Un reinicio ambiguo puede confundirse con eliminar el perfil; la confirmación adulta debe diferenciar ambas acciones.
- Exponer abandonos sin el umbral acordado puede inducir interpretaciones erróneas.

### Decisiones pendientes

- Ninguna de producto.

## Referencias

- ADR-031 — Diario parental por actividad.
- FEAT-006 — Tracking Module.
- FEAT-015 frontend — Diario parental por actividad.
