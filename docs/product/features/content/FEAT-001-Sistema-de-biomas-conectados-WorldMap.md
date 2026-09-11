# FEAT-001 — Sistema de biomas conectados de WorldMap

## Estado

- **Estado:** aceptada parcialmente — fase 3; intervención concreta de Nubi en transiciones pendiente de validación de preferencias.
- **Responsable principal:** contenido.
- **Decisión confirmada:** 2026-09-09.
- **Depende de:** ADR-026; FEAT-010 backend — Descripción de WorldMap para la experiencia visual; FEAT-011 frontend — WorldMap: paseo visual básico.

## 1. Objetivo y valor para la familia

Definir paisajes conectados, reconocibles y apropiados para 3–4 años, para que el niño explore un mundo continuo sin selector en la entrada, bloqueos ni objetivos. Cada bioma incorpora elementos decorativos propios que sostienen su identidad y preparan coherencia temática futura, sin convertir esta fase en actividad educativa o evaluativa.

## 2. Actores y escenarios de uso

### Niño que explora un bioma

1. Explora una zona de Pradera, Granja, Bosque encantado, Playa, Espacio o Prehistoria cuando esté disponible.
2. Encuentra elementos decorativos propios del entorno.
3. Puede tocarlos o ignorarlos; no recibe evaluación ni obligación.

### Niño que pasa a un bioma conectado

1. Se aproxima libremente a la continuidad del paisaje.
2. Percibe una transición suave a otro bioma.
3. Si las preferencias parentales vigentes permiten presencia y voz de Nubi, la transición podrá incorporar una intervención breve de contexto de paseo. En cualquier otro caso, continúa siendo comprensible de forma visual y sin voz.

### Niño que vuelve en otra sesión

1. Finaliza una sesión durante la exploración.
2. En la siguiente sesión, retoma el mismo estado de exploración.
3. No recibe mensajes de rachas, progreso, contenidos completados ni obligación de continuar.

## 3. Requisitos funcionales y no funcionales

1. El catálogo previsto de biomas es Pradera (MEADOW), Granja, Bosque encantado (WOODS), Playa, Espacio y Prehistoria (dinosaurios). Pradera y Bosque encantado deben diferenciarse visualmente de forma clara.
2. Los biomas deben formar zonas conectadas de un único paseo. No hay selector infantil en la entrada inicial, pero junto al punto de inicio de cada bioma hay un transporte temático que permite elegir libremente un destino.
3. Todo bioma disponible debe ser accesible sin niveles, desbloqueos, candados, resultados ni requisitos de uso.
4. Cada bioma debe disponer de elementos decorativos visualmente coherentes y diferenciables de los demás.
5. Los elementos decorativos permiten reacciones básicas, pero no inician actividades en esta fase.
6. El niño debe retomar el mismo estado de exploración entre sesiones.
7. La transición entre biomas debe ser tranquila, comprensible sin lectura, color o audio, y no debe transmitir una meta, pantalla superada o ruta obligatoria.
8. Cualquier intervención de Nubi en transición debe respetar estrictamente las preferencias parentales vigentes de NPC y voz del NPC; no se aprueba una excepción nueva.
9. La futura asociación temática —por ejemplo, animales y hábitat— es una posibilidad de coherencia de contenido para minijuegos posteriores; no aparece como enseñanza, pregunta ni medición en WorldMap básico.
10. El gestor de contenido debe proporcionar, cuando proceda, frases breves de transición referidas exclusivamente al siguiente bioma. Solo se presentan si las preferencias parentales vigentes permiten la presencia y voz de Nubi.
11. Los placeholders confirmados son:

| Momento o transición | Placeholder |
|---|---|
| Entrada inicial a Pradera | «Hola, paseemos por la pradera.» |
| Pradera → Granja | «¡Vamos a visitar mi granja!» |
| Granja → Bosque encantado | «Ahora vamos al bosque encantado.» |
| Bosque encantado → Playa | «El camino sigue hasta la playa.» |
| Playa → Espacio | «¡Vamos a viajar al espacio!» |
| Espacio → Prehistoria | «¡Vamos a visitar a los dinosaurios!» |

## 4. Criterios de aceptación verificables

1. El catálogo puede distinguir los seis biomas previstos y sus elementos decorativos propios, incluida la diferencia entre Pradera y Bosque encantado.
2. La experiencia no ofrece selector infantil en la entrada inicial, candado, insignia, nivel ni condición de acceso. El transporte temático situado al inicio de cada bioma permite elegir visualmente cualquiera de los seis destinos disponibles.
3. Un elemento decorativo no abre minijuego ni muestra acierto, error o progreso.
4. El paso entre biomas disponibles se entiende visualmente sin texto ni audio y no exige avanzar.
5. Después de volver a entrar en otra sesión, el niño puede continuar desde el mismo estado de exploración sin ver métricas ni mensajes evaluativos.
6. Con NPC o voz del NPC desactivados, la transición sigue siendo completa y no incorpora intervención de Nubi no permitida.
7. Los elementos y transiciones no dependen únicamente del color para comunicar bioma o continuidad.
8. Cuando la voz de Nubi está permitida, una frase de transición se refiere al siguiente bioma y no contiene preguntas personales, instrucciones obligatorias, promesas de recompensa ni mensajes evaluativos.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Contenido

- Adecuación por edad, seguridad y variedad de elementos por bioma.
- Coherencia temática futura sin introducir contenidos educativos, morales, diagnósticos ni evaluativos en el paseo básico.

### Backend / World

- Continuidad funcional entre biomas y estado mínimo necesario para retomar exploración, aplicando minimización de datos.
- Expresión de disponibilidad de biomas y transiciones, sin que este FEAT determine contratos o persistencia.

### Frontend

- Comprensión en móvil y tableta de transiciones conectadas, diferencias de bioma y retorno de exploración.

### Agentes, voz y preferencias familiares

- Presencia de Nubi y cualquier frase de transición frente a las preferencias vigentes de NPC, voz del NPC y audio general.
- Mensajes breves, seguros, apropiados por edad y limitados al paseo; nunca solicitan datos personales.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA

- Solo se conserva la información necesaria para retomar exploración; las interacciones decorativas no se usan para perfilado, diagnóstico ni dashboard.
- No se muestra ni solicita información personal, progreso, dificultad o comportamiento del niño.
- No hay presión temporal, competición, castigos, persuasión ni mecánicas adictivas.
- Se usan apoyos visuales adicionales al color, objetivos táctiles amplios y comprensión sin audio.
- Cualquier voz de Nubi es opcional, contextual y subordinada a controles parentales; no hay conversación ni contenido abierto de IA.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Minijuegos de asociación, preguntas sobre hábitats, avance, dificultad, tracking, dashboard y recompensas.
- Aleatoriedad de bioma y configuración parental de biomas.
- Texto concreto, duración y recursos concretos de la intervención de Nubi.
- Contratos, mecanismos de persistencia, estructuras de datos, integraciones y detalles de implementación.

### Riesgos

- Diferencias visuales insuficientes pueden confundir biomas; diferencias excesivas pueden sobreestimular.
- Una transición narrada con NPC desactivado vulneraría el control parental confirmado.
- Conservar más información de la necesaria para retomar el paseo incumpliría minimización de datos.

### Decisiones pendientes

- Ninguna de producto para esta fase. Contenido debe validar la adecuación por edad de los placeholders confirmados antes de ponerlos a disposición del niño.
