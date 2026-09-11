# FEAT-012 — WorldMap: continuidad de zonas y límites del paseo

## Estado

- **Estado:** aceptada — fase 4A.
- **Responsable principal:** frontend.
- **Decisiones confirmadas:** 2026-09-09.
- **Depende de:** ADR-026; FEAT-011 — WorldMap: paseo visual básico; FEAT-001 contenido — Sistema de biomas conectados.

## 1. Objetivo y valor para la familia

Hacer comprensible el comienzo, la continuidad y los límites de las zonas de WorldMap sin presentarlos como pantallas que se superan. El niño conserva libertad para explorar, detenerse o volver atrás, mientras la familia recibe una experiencia no gamificada y sin presión.

## 2. Actores y escenarios de uso

### Niño que comienza o retoma el paseo

1. En una sesión que continúa una exploración previa, vuelve al estado acordado sin ver indicadores de avance, recuperación o contenido completado.
2. En una entrada sin estado previo, el paseo empieza en Pradera (MEADOW).

### Niño que usa el transporte de un bioma

1. Junto al punto de inicio de Nubi en cada bioma, ve un elemento de transporte propio de ese paisaje.
2. Lo toca para abrir una selección visual de destino.
3. Puede elegir libremente cualquiera de los seis biomas disponibles, sin candados, condiciones, resultados ni indicación de cuál debe escoger.
4. Tras elegir un destino, llega a su punto de inicio mediante una transición amable y comprensible.

### Niño que se aproxima a otra zona

1. Antes de llegar a la continuidad del paisaje, percibe una señal ambiental visual suave que invita, pero no obliga, a seguir explorando.
2. Puede continuar, volver hacia atrás o permanecer donde está.
3. Si continúa, observa una pausa visual breve de llegada antes de explorar el nuevo bioma conectado.

### Niño que alcanza el extremo del mundo disponible

1. Llega al final del paisaje actualmente disponible.
2. Encuentra un entorno tranquilo y estable, sin mensaje de finalización, recompensa, reinicio ni salida obligatoria.
3. Puede regresar libremente hacia zonas anteriores.

## 3. Requisitos funcionales y no funcionales

1. Los biomas conectados deben organizarse como un paseo lineal con posibilidad de volver libremente a zonas anteriores, en este orden: Pradera → Granja → Bosque encantado → Playa → Espacio → Prehistoria.
1.1. Junto al punto de inicio de cada bioma debe existir un transporte temático que permita al niño seleccionar visualmente cualquiera de los seis biomas disponibles.
2. La conexión entre biomas debe preservar la autonomía infantil: no hay paso forzado, ruta obligatoria, flecha imperativa ni selección de nivel.
3. Antes de una transición debe mostrarse una señal visual suave de continuidad del paisaje, visible con cierta anticipación y comprensible sin texto, audio o color exclusivamente.
4. Al entrar en un nuevo bioma debe existir una pausa visual breve de llegada, sin temporizador visible, recompensa ni indicación de contenido completado.
5. Las transiciones entre biomas visualmente distantes —por ejemplo, Bosque encantado y Espacio— deben contar con una transición de contenido que resulte comprensible y no abrupta para el niño.
6. Si las preferencias parentales vigentes permiten presencia y voz del NPC, la pausa de llegada puede incluir una intervención breve de Nubi contextual al paseo. En caso contrario, la transición debe conservar sentido completo de forma visual y sin voz.
7. El extremo del mundo disponible debe comunicar calma y posibilidad de volver, no final de juego, bloqueo o necesidad de reiniciar.
8. El retorno a una sesión anterior debe restablecer el estado de exploración acordado, sin mostrar al niño la información conservada ni interpretar ese estado como progreso o capacidad.
9. En una primera entrada sin estado previo, el paseo debe empezar en Pradera (MEADOW).
10. El transporte debe ser reconocible como elemento de viaje sin depender exclusivamente de texto, color o sonido.
11. La selección de destino debe ofrecer los seis biomas disponibles por igual, sin candados, requisitos, recomendaciones, orden de preferencia ni datos de progreso o visitas previas.
12. Elegir un destino desde el transporte debe llevar al punto de inicio de ese bioma mediante una transición tranquila, sin presentarlo como salto de nivel o premio.
13. Al final de cada tramo, el camino debe desembocar en una salida natural relacionada con su bioma; al cruzarla, se aplica la transición suave al siguiente bioma del orden lineal.

## 4. Criterios de aceptación verificables

1. El niño puede recorrer biomas conectados en una dirección y volver a los anteriores sin bloqueo.
2. Antes de cada transición, existe una señal ambiental suave, visible sin requerir acción inmediata y comprensible sin texto, color o audio exclusivamente.
3. El niño puede ignorar la señal y continuar observando o volver atrás sin feedback negativo.
4. La llegada a otro bioma incluye una pausa visual breve sin porcentajes, temporizador, felicitación, nivel ni mensaje de superación.
5. Una transición entre biomas de identidad visual muy distinta no parece un cambio brusco o un error de la experiencia.
6. Con NPC o voz del NPC desactivados, no hay presencia, animación o voz de Nubi no permitida y la transición continúa siendo comprensible.
7. Al alcanzar el extremo del contenido disponible, no se muestran mensajes de final, recompensas, reinicio automático ni rutas obligatorias.
8. Al volver en una nueva sesión con estado previo, el niño puede continuar desde la zona que dejó sin ver métricas, rachas, progreso ni contenido completado.
9. En una primera entrada sin estado previo, el niño comienza la exploración en Pradera (MEADOW).
10. En el punto de inicio de cada bioma, el niño ve un transporte temático y puede usarlo para elegir visualmente cualquiera de los seis destinos.
11. La selección de destinos no contiene candados, requisitos, puntuaciones, recomendaciones de ruta ni datos de visitas previas.
12. Al elegir un destino, el niño llega a ese bioma con una transición tranquila, sin mensaje de nivel, premio o superación.
13. Al cruzar la salida natural al final de un tramo, el niño entra en la transición suave hacia el siguiente bioma lineal, sin bloquear el posterior retorno o uso del transporte.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Comprensión infantil de señal anticipada, pausa de llegada, vuelta libre, extremo tranquilo y transporte temático en móvil y tableta.
- Ausencia de elementos visuales que se interpreten como meta, nivel, bloqueo o instrucción imperativa, incluido el selector visual de destino.

### Contenido

- Hitos y transiciones visuales entre biomas, especialmente entre paisajes muy diferentes.
- Adecuación por edad de cualquier frase breve de Nubi y continuidad visual cuando la voz no esté permitida.

### Backend / World y privacidad

- Estado mínimo necesario para retomar la exploración, sin incorporar datos de progreso, interacción decorativa o perfilado no necesarios.
- Regla de inicio en Pradera, orden lineal confirmado de los biomas y disponibilidad igualitaria de todos los destinos desde cada transporte.

### Agentes y preferencias familiares

- Respeto estricto de las preferencias parentales de NPC, voz del NPC y audio general ante cualquier intervención de Nubi.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA

- Retomar una zona no constituye una métrica, un logro ni una inferencia sobre el niño.
- No se recogen datos adicionales de navegación más allá de los estrictamente necesarios para la continuidad aprobada.
- La experiencia evita competición, presión temporal, castigos, persuasión y contenido inapropiado por edad.
- Las señales y pausas usan apoyos visuales y no dependen exclusivamente de lectura, color o sonido.
- Una intervención opcional de Nubi se limita al contexto de paseo, no pide información personal y no habilita conversación o contenido abierto de IA.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Minijuegos, avance, desbloqueos, recompensas, tracking y dashboard.
- Definición de animaciones particulares de elementos decorativos (fase 4B).
- Contratos, persistencia, mecanismos de desplazamiento, formatos, integración y detalles técnicos.

### Riesgos

- Una señal demasiado visible puede interpretarse como obligación; una demasiado débil puede pasar desapercibida.
- Una transición entre paisajes muy distintos puede desorientar si el contenido no construye continuidad suficiente.
- Una intervención de Nubi incompatible con preferencias parentales reduciría el control adulto confirmado.
- Una selección de seis destinos visualmente ambigua o demasiado cargada puede desorientar a un niño de 3–4 años; frontend y contenido deben validarla sin convertirla en un menú adulto.

### Decisiones pendientes

- Ninguna de producto para esta fase. Contenido debe validar la adecuación por edad de los placeholders confirmados en FEAT-001 antes de exponerlos al niño.
