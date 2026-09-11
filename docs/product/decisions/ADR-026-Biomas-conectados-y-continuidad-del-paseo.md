# ADR-026 — Biomas conectados y continuidad del paseo

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-09-09

## 1. Contexto y problema

WorldMap debe ampliar el paisaje inicial sin convertir su entrada en un selector infantil, una secuencia de niveles ni una experiencia gamificada. La interrupción de una sesión tampoco debe romper innecesariamente la continuidad percibida por el niño.

## 2. Necesidad de la familia y usuarios afectados

El niño de 3–4 años necesita explorar paisajes reconocibles con autonomía, sin menús infantiles ni contenidos aparentemente obligatorios. La familia necesita acceso completo al contenido disponible y continuidad del paseo entre sesiones, sin convertir la exploración en una métrica infantil.

## 3. Alternativas de producto consideradas y compromisos

- **Selector infantil de biomas:** ofrece elección explícita, pero añade carga de decisión y puede transformar la entrada en un menú.
- **Bioma independiente por sesión:** simplifica cada sesión, pero debilita la sensación de viaje y puede resultar arbitrario al cambiar de paisaje.
- **Biomas conectados en un paseo continuo:** refuerza la exploración y conexión narrativa; exige transiciones comprensibles que no parezcan niveles superados.

## 4. Decisión confirmada y justificación

- WorldMap se compone de biomas conectados dentro de un mismo paseo. No hay selector infantil al entrar inicialmente, pero cada bioma ofrece un transporte temático junto a su punto de inicio que permite al niño elegir libremente un destino.
- El catálogo previsto es: **Pradera (MEADOW), Granja, Bosque encantado (WOODS), Playa, Espacio y Prehistoria (dinosaurios)**.
- Pradera y Bosque encantado son biomas distintos: el segundo debe adoptar una identidad visual de bosque encantado que no se confunda con la pradera.
- En una primera entrada sin estado previo, el paseo empieza siempre en Pradera.
- El orden de conexión es: **Pradera → Granja → Bosque encantado → Playa → Espacio → Prehistoria**. El niño puede volver libremente por ese recorrido.
- Todo bioma disponible es accesible sin desbloqueos, candados, resultados ni requisitos de uso.
- El transporte de cada punto de inicio permite elegir cualquiera de los seis biomas disponibles, sin bloquear ni distinguir destinos por progreso, visitas o resultados.
- La siguiente sesión retoma el estado de exploración anterior.
- Cada bioma cuenta con elementos decorativos propios para conservar identidad y coherencia infantil.
- La futura asociación temática entre elementos y biomas es solo una oportunidad de coherencia para minijuegos posteriores; no implica enseñanza, actividad ni avance en WorldMap básico.
- Las frases de transición, si están permitidas por las preferencias parentales de NPC y voz, las proporciona el gestor de contenido y se refieren únicamente al siguiente bioma; por ejemplo: «Vamos a visitar mi granja».
- Los placeholders confirmados para las transiciones son: «Hola, paseemos por la pradera»; «¡Vamos a visitar mi granja!»; «Ahora vamos al bosque encantado»; «El camino sigue hasta la playa»; «¡Vamos a viajar al espacio!»; y «¡Vamos a visitar a los dinosaurios!».

La decisión conserva un paseo narrativo, libre y no evaluativo, evitando mecanismos de gamificación.

## 5. Impacto

### Experiencia infantil

- El cambio de bioma se percibe como continuidad del paisaje, no como pantalla superada.
- Puede continuar explorando o permanecer donde está sin presión.
- Los elementos propios favorecen reconocimiento y repetición voluntaria.

### Experiencia parental

- No se exponen controles ni elecciones adultas al niño.
- El acceso no depende de logros, resultados ni tiempo de uso.

### Accesibilidad

- Cada bioma debe diferenciarse mediante más de una señal visual; no exclusivamente por color.
- Las transiciones y elementos deben ser comprensibles sin texto ni audio.

### Seguridad infantil y privacidad

- La continuidad no debe transformarse en métricas, perfilado ni inferencias sobre el niño.
- Solo se conserva la información estrictamente necesaria para retomar el paseo.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- Sin selector infantil al entrar inicialmente, niveles, desbloqueos, recompensas, puntuaciones ni clasificación.
- Sin minijuegos, asociaciones, progreso pedagógico, tracking ni dashboard en esta fase.
- Esta decisión no define contratos, persistencia, integración, algoritmos ni representación técnica.

### Preguntas abiertas

- Una intervención de Nubi en la transición solo puede producirse cuando las preferencias parentales vigentes permiten la presencia y voz del NPC. No hay una nueva excepción aprobada al control parental.
- Debe validarse qué estado mínimo permite retomar el paseo sin conservar datos innecesarios del menor.
- Debe validarse la adecuación y disponibilidad de contenido de cada bioma antes de incorporarlo.

## Referencias

- FEAT-005 — Configuración global de audio, NPC y PIN.
- FEAT-010 frontend — Entrada a GameView y carga inicial.
- FEAT-010 backend — Descripción de WorldMap para la experiencia visual.
- FEAT-011 frontend — WorldMap: paseo visual básico.
