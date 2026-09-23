# FEAT-015 — Diario parental por actividad

## Estado

- **Estado:** aceptada.
- **Responsable principal:** frontend.
- **Decisión de producto:** ADR-031.
- **Depende de:** FEAT-006; FEAT-013 frontend; acceso parental válido.

## 1. Objetivo y valor para la familia

Permitir al adulto consultar un Diario compacto y no evaluativo del perfil seleccionado: actividad familiar realizada, tiempo de juego y configuración actual de dificultad por actividad. El Diario reemplaza el destino «Dashboard» y permanece separado de la experiencia infantil.

## 2. Actores y escenarios de uso

### Adulto que consulta el Diario

1. Accede mediante **Niños → [Nombre] → Diario** con acceso parental válido.
2. Ve por defecto la semana, el tiempo jugado y las actividades únicas completadas.
3. Consulta bloques en orden fijo: Reconocimiento, Comparación y Memoria.
4. En Reconocimiento ve Letras, Formas, Números, Colores y Animales cuando se hayan completado en el periodo.

### Adulto que cambia el periodo

1. Elige Hoy, Semana, Mes o Total.
2. El resumen y las actividades visibles cambian según las completadas en ese periodo.
3. El nivel de cada actividad visible continúa mostrando su configuración actual, no un nivel histórico del periodo.

### Adulto que limpia el historial de juego

1. Elige la acción diferenciada para limpiar datos del Diario.
2. Recibe una confirmación adulta que explica que se reinician historial y adaptación, pero se conserva el perfil y sus preferencias.
3. Tras confirmarlo, el Diario muestra el estado sin actividad y el juego comienza de nuevo su adaptación por actividad.

## 3. Requisitos funcionales y no funcionales

1. El Diario solo está disponible para adultos con acceso parental válido y solo para el perfil seleccionado.
2. La cabecera identifica el perfil y permite seleccionar otro perfil autorizado sin mezclar sus datos.
3. Semana es el periodo inicial; las opciones son Hoy, Semana, Mes y Total.
4. El resumen muestra exclusivamente tiempo jugado y número de actividades únicas completadas en el periodo.
5. Los bloques se presentan en orden fijo: Reconocimiento, Comparación y Memoria.
6. Reconocimiento contempla Letras, Formas, Números, Colores y Animales.
7. Cada actividad muestra: **«Nivel actual de esta actividad: [Fácil | Normal | Difícil]. El juego lo ajusta automáticamente para que pueda jugar a gusto.»**
8. El nivel se representa mediante tres paradas discretas con texto visible; no depende exclusivamente de color ni expresa capacidad, máximo histórico o tendencia.
9. Una actividad nueva aparece inicialmente en Fácil sin celebración ni marca de logro.
10. Cuando se cumpla la regla de 4 abandonos en los 6 intentos iniciales más recientes de esa actividad, el Diario puede mostrar esa señal contextual con lenguaje neutro.
11. Sin actividad en el periodo, se muestra un mensaje neutro, por ejemplo: «Hoy todavía no ha jugado».
12. Debe existir una acción separada para limpiar Diario/tracking y reiniciar adaptación; exige confirmación adulta y no elimina perfil ni preferencias.
13. No se muestran gráficas, comparativas, rankings, rachas, objetivos, puntuaciones, porcentajes de capacidad ni diagnósticos.

## 4. Criterios de aceptación verificables

1. Una persona sin acceso parental válido no puede abrir ni consultar el Diario.
2. El Diario muestra Semana por defecto y permite elegir los cuatro periodos confirmados.
3. El contador de actividades del periodo no duplica una actividad aunque haya sido completada más de una vez en ese periodo.
4. El orden visible de bloques no cambia según tiempo, dificultad, abandonos ni actividad reciente.
5. Una actividad visible muestra su nivel actual individual, incluso al consultar un periodo pasado.
6. El texto de nivel explica el ajuste automático y no presenta el nivel como logro, capacidad o comparación.
7. Colores aparece como subcategoría de Reconocimiento junto a Letras, Formas, Números y Animales cuando corresponda.
8. El abandono no aparece hasta cumplir 4 de los 6 intentos iniciales más recientes de la misma actividad y, cuando aparece, no usa lenguaje de fallo o capacidad.
9. Al confirmar limpiar datos, el perfil, avatar, mes/año de nacimiento y preferencias se conservan; el Diario queda sin actividad y se reinicia la adaptación.
10. El contenido es legible y utilizable en móvil y tableta, sin que nivel, estado vacío o abandonos dependan solo de color.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **Frontend y accesibilidad:** presentación compacta, claridad de filtros, nivel actual, estados vacíos, selector de perfil y confirmación de limpieza.
- **Backend y datos:** resumen por periodo, actividades únicas, nivel actual por actividad, abandono contextual y limpieza aislada del historial/adaptación.
- **Contenido:** vocabulario cálido, no evaluativo y comprensible para adultos.
- **Privacidad y seguridad:** acceso parental, separación por perfil y familia, y explicación clara del efecto de limpiar datos.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- El niño no puede ver ni acceder al Diario.
- No se exponen datos entre perfiles o familias y no se usan para perfilado comercial, diagnóstico o evaluación.
- El nivel siempre describe la configuración actual de una actividad, no al menor.
- La limpieza debe ser deliberada y reversible solo mediante nueva actividad; no se confunde con eliminar el perfil.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Chatbot, catálogo de actividades conjuntas, diseño técnico de datos o mecanismos de protección.
- Gráficas, tendencias, niveles máximos, recomendaciones de intervención, comparativas y gamificación.

### Riesgos

- El adulto podría leer «Difícil» como capacidad; el texto obligatorio y la ausencia de histórico deben reducir esa interpretación.
- Mostrar abandonos fuera de contexto puede generar atribuciones incorrectas; se limita a la ventana confirmada y a lenguaje descriptivo.

### Decisiones pendientes

- Ninguna de producto.

## Referencias

- ADR-031 — Diario parental por actividad.
- FEAT-006 — Gestión parental de perfiles infantiles.
- FEAT-013 frontend — Minijuegos: interacción visual básica y cierre sin fricción.
