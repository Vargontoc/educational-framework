# ADR-028 — Reconocimiento visual sin fricción

## Estado

- **Estado:** aceptada parcialmente.
- **Fecha:** 2026-09-15.
- **Alcance:** decisiones de producto para `RecognitionEngine`; no define su realización técnica.

## 1. Contexto y problema

`RecognitionEngine` debe proponer minijuegos de reconocimiento adecuados para niños y niñas de 3–4 años. El producto debe preservar una experiencia autónoma, breve y sin presión, que siga siendo comprensible cuando el audio o el acompañamiento de Nubi no estén disponibles.

Ocultar el elemento a buscar convertiría la actividad en un ejercicio de recuerdo. Eso no responde al propósito de reconocimiento visual y añade una fricción cognitiva innecesaria.

## 2. Necesidad de la familia y usuarios afectados

- **Niño o niña:** necesita identificar y tocar una opción con una regla estable, información visual suficiente y reintentos ilimitados, sin perder una ronda ni esperar a una persona adulta.
- **Adulto autenticado:** necesita poder consultar posteriormente señales orientativas de la actividad, sin diagnósticos, clasificaciones ni exposición de intentos al niño.
- **Familia:** necesita que el juego funcione con o sin audio, con Nubi activo o dormido, y que respete las preferencias de accesibilidad visual configuradas bajo control parental.

## 3. Alternativas de producto consideradas y compromisos

### A. Ocultar el objetivo tras una exposición inicial

- **Valor aparente:** añade un reto de recuerdo.
- **Inconveniente:** cambia la naturaleza de la actividad a memoria; no se acepta para este motor.

### B. Mantener visible el objetivo y graduar los distractores y las ayudas

- **Valor:** conserva el reconocimiento visual, permite una progresión predecible y evita presión temporal.
- **Compromiso:** exige que contenido y experiencia visual diseñen distractores apropiados y no ambiguos.

### C. Hacer el audio o la burbuja de Nubi la fuente del objetivo

- **Valor aparente:** puede añadir narración.
- **Inconveniente:** excluiría el juego sin audio y duplicaría o confundiría la referencia visual; no se acepta.

## 4. Decisión confirmada y justificación

### Reglas transversales

1. Una selección que no corresponde a la opción objetivo recibe solo una señal neutra y breve —por ejemplo, un vaivén leve—. No se muestran rojo, lenguaje de error, sonido negativo ni contador de fallos al niño.
2. La misma ronda permanece disponible hasta que el niño acierte. No existe derrota de ronda, límite de reintentos ni dependencia de ayuda adulta para continuar.
3. Un toque válido sobre una opción resuelve la ronda de inmediato; no hay paso infantil de confirmación.
4. El juego debe seguir siendo plenamente jugable sin audio. El audio, si está disponible y autorizado, es un refuerzo aditivo.
5. Nubi no es una fuente crítica del estímulo. Su presencia es decorativa o motivacional; dormido no habla ni se anima y su nube pasa a ser una nube de sueño, sin alterar la jugabilidad.
6. Se propone como punto de partida una zona táctil de aproximadamente 88–96 px de lado, equivalente de forma orientativa a 15–16 mm físicos. No queda fijada como estándar definitivo hasta validarla en dispositivos reales.
7. Los aciertos y selecciones no acertadas se registran al completar el minijuego únicamente para la información parental autorizada. Su presentación en dashboard está fuera de este alcance y debe ser descriptiva —por ejemplo, dificultad alcanzada—, nunca una evaluación de capacidad.

### Principio y ladder de dificultad

El estímulo a reconocer se muestra visualmente y permanece visible en **EASY**, **MEDIUM** y **HARD**. La dificultad se gradúa sin ocultarlo:

| Eje | EASY | MEDIUM | HARD |
|---|---|---|---|
| Opciones | 2 | 3 | 3–4 |
| Distractores | Semánticamente lejanos | De la misma categoría temática | De contorno o grafía similar |
| Cromo guía visual | Sí | No | No |
| Espera antes de habilitar el toque | Corta | Media | Ninguna o mínima |
| Estímulo visual | Siempre presente | Siempre presente | Siempre presente |
| Audio | Opcional y aditivo | Opcional y aditivo | Opcional y aditivo |

La espera inicial sirve para invitar a mirar antes de tocar; no es un límite de tiempo ni puede hacer que el niño falle o pierda la ronda. En letras y números, el modelo debe poder mostrarse con estilo o tamaño diferente de las opciones para favorecer el reconocimiento de la forma frente a una coincidencia visual exacta.

### Reglas por contenido abordado

- **Letras:** reconocimiento de grafía o forma visual; no se exige fonética ni lectura.
- **Números:** reconocimiento de grafía; no se exige valor numérico ni conteo.
- **Colores:** cuando exista una preferencia visual de color configurada para el perfil, los distractores necesitan una clave adicional al matiz —como patrón o textura—. Esta decisión no diagnostica ni infiere una condición visual.
- **Animales:** reconocimiento directo del elemento visual, sin reglas especiales adicionales.

El estímulo se sitúa en una tarjeta o zona visual central propia, separada de la burbuja de Nubi. Si Nubi está despierto puede narrar opcionalmente el estímulo y mostrar en su burbuja un icono neutro genérico; no muestra el estímulo ni se convierte en una segunda fuente visual de verdad.

## 5. Impacto

### Experiencia infantil

- La regla de juego es constante: mirar el objetivo visible y tocar una opción.
- Se favorecen exploración, autonomía acompañada y reintento voluntario, sin presión, castigo ni competición.
- La interacción es de toque directo, con o sin sonido.

### Experiencia parental

- La familia conserva control sobre audio, Nubi y preferencias visuales desde sus controles separados.
- Los datos posteriores se limitan a orientar conversaciones familiares; no describen inteligencia, capacidad ni diagnóstico.

### Accesibilidad

- Ninguna instrucción crítica depende solo de audio, texto, color o Nubi.
- El objetivo táctil orientativo es amplio y debe validarse en móvil y tableta reales.
- En actividades de color, los diferenciadores no cromáticos son obligatorios cuando se usa una preferencia de visión de color.

### Seguridad infantil y privacidad

- Nubi permanece limitado al contexto de juego, no pide datos personales ni induce decisiones sensibles.
- Los datos de interacción son mínimos, se conservan para la finalidad parental confirmada y no se muestran entre perfiles ni a menores.
- Cualquier dashboard futuro debe estar tras acceso parental y evitar lenguaje evaluativo.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- No se decide el diseño, texto ni implementación del dashboard parental.
- No se define una prueba, recomendación o diagnóstico de visión de color.
- No se decide contenido, mecánica o dificultad de motores de memoria, conteo o lectura.
- No se prescribe contrato, persistencia, modelo de datos, algoritmos de selección, temporización, renderizado ni integración de audio/Nubi.

### Cuestiones pendientes de decisión de producto

1. **Color y preferencia visual:** decidir si el patrón o textura adicional se muestra siempre en las rondas de color o solo cuando el adulto haya configurado una preferencia visual para ese perfil.
2. **Zona táctil estándar:** confirmar el tamaño común definitivo tras validación en dispositivos reales.
3. **Catálogo de formas:** la documentación anterior de `RecognitionEngine` incluye FORMAS; este alcance concreta letras, números, colores y animales, pero no confirma si FORMAS continúa con esta ladder, se adapta después o queda fuera. No debe asumirse una retirada del catálogo sin confirmación expresa.

### Ámbitos que deben validar los responsables

- **Frontend y accesibilidad:** comprensión sin audio, claridad del cromo guía y del feedback neutro, tamaño y separación de objetivos táctiles, y comportamiento equivalente en móvil y tableta.
- **Contenido:** adecuación por edad, ausencia de ambigüedad en modelos/distractores, similitud progresiva y claves no cromáticas apropiadas.
- **Backend y datos:** registro mínimo solo al completar, aislamiento por perfil y familia, y disponibilidad de la dificultad orientativa exclusivamente para acceso parental.
- **Agentes y seguridad infantil:** que las intervenciones de Nubi sean opcionales, contextuales y nunca revelen, sustituyan ni dupliquen el estímulo visual.
- **Privacidad:** control parental de acceso a los datos y ausencia de reutilización fuera de la finalidad acordada.

## Referencias

- README.md.
- ADR-023 — Selector visual de accesibilidad cromática.
- ADR-027 — Nubi dormido como salida de minijuegos.
- FEAT-009 — Recognition Engine Module.
- FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción.
