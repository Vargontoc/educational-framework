# FEAT-011 — RecognitionEngine: reglas de dificultad y registro parental

## Estado

- **Estado:** aceptada parcialmente; contiene decisiones pendientes explícitas.
- **Responsable principal:** backend.
- **Decisión de producto:** ADR-028 — Reconocimiento visual sin fricción.
- **Depende de:** FEAT-009 — Recognition Engine Module; FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción; FEAT-014 — RecognitionEngine: experiencia visual y dificultad sin fricción.

## 1. Objetivo y valor para la familia

Aplicar de forma consistente las reglas funcionales acordadas para la dificultad y el registro de `RecognitionEngine`, de modo que el niño siempre pueda terminar una ronda sin presión y la familia solo disponga de información orientativa tras completar el minijuego.

Esta especificación complementa FEAT-009. No modifica ni sustituye sus decisiones técnicas previas; fija las necesidades funcionales posteriores que su responsable principal debe traducir a diseño técnico.

## 2. Actores y escenarios de uso

### Niño o niña que reintenta una ronda

1. Recibe una ronda de la dificultad vigente con el estímulo visual siempre presente.
2. Hace una selección que no corresponde a la opción objetivo.
3. La ronda permanece abierta, con el mismo objetivo y opciones, y puede reintentar sin límite.
4. Al tocar la opción objetivo, la ronda se resuelve inmediatamente.

### Niño o niña que inicia una ronda por dificultad

1. En EASY recibe 2 opciones, distractores semánticamente lejanos, cromo guía y una espera corta antes de habilitar el toque.
2. En MEDIUM recibe 3 opciones, distractores de la misma categoría temática, sin cromo y una espera media.
3. En HARD recibe 3–4 opciones, distractores similares por contorno o grafía, sin cromo y sin espera o con una espera mínima.

### Adulto autenticado que consulta progreso futuro

1. El niño completa el minijuego.
2. Se hacen disponibles al adulto las señales de aciertos y selecciones no acertadas de la actividad completada, con la única finalidad de información familiar orientativa.
3. Un dashboard futuro puede expresar la dificultad alcanzada con lenguaje amigable; no muestra capacidad, diagnóstico ni clasificación.

## 3. Requisitos funcionales y no funcionales

1. El estímulo a reconocer debe permanecer disponible en las rondas EASY, MEDIUM y HARD. Ocultarlo queda excluido de este motor.
2. La selección de una opción no acertada no puede cerrar, perder, reemplazar ni bloquear la ronda; el niño puede repetirla sin límite.
3. No existe límite de tiempo funcional que pueda provocar fallo o pérdida. La espera de EASY y MEDIUM precede solo a la habilitación inicial del toque.
4. La dificultad debe respetar el número de opciones, tipo de distractor, cromo guía y espera confirmados en ADR-028.
5. Letras y números se tratan como reconocimiento de grafía, no como lectura, fonética, cantidad o conteo.
6. Cuando el perfil infantil tenga una preferencia visual de color configurada, una ronda de color debe incluir una clave adicional al matiz para los distractores relevantes.
7. Los eventos o datos destinados a la experiencia infantil no deben incluir contadores de fallos, resultados evaluativos, presión temporal, diagnóstico ni clasificación.
8. Los aciertos y selecciones no acertadas de un intento inicial se registran solo cuando el minijuego se completa, conforme a FEAT-013. Los datos parciales de un abandono no se consolidan como progreso.
9. El acceso a datos de interacción y dificultad queda limitado al adulto autorizado de la familia y a la finalidad de información parental no evaluativa.
10. La ausencia de audio, la ausencia de Nubi o el estado dormido de Nubi no pueden impedir la resolución de una ronda ni alterar estas reglas.

## 4. Criterios de aceptación verificables

1. Una ronda de cualquiera de las tres dificultades mantiene visible el estímulo hasta resolverse.
2. Tras cualquier número de selecciones no acertadas, el niño puede seleccionar la opción objetivo y resolver la misma ronda.
3. Una selección no acertada no genera un estado infantil de pérdida, temporizador, bloqueo ni contador visible de fallos.
4. Las rondas EASY, MEDIUM y HARD respetan la ladder de opciones, distractores, cromo y espera aprobada.
5. La espera inicial no cambia una respuesta posterior en fallo ni cierra la ronda sin una selección del niño.
6. Una ronda de letra o número no exige demostrar lectura, fonética, cantidad ni conteo para resolverse.
7. Con una preferencia de visión de color configurada, las rondas de color proporcionan una clave visual adicional al matiz.
8. Si un minijuego se abandona antes de completarse, sus aciertos y selecciones no acertadas parciales no están disponibles como progreso parental.
9. Si se completa, los datos de interacción solo están disponibles para el adulto autorizado, no para el niño ni para otros perfiles.
10. Desactivar audio o Nubi, o mostrar Nubi dormido, no modifica la disponibilidad de la ronda ni la posibilidad de completarla.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **Backend y datos:** registro exclusivamente al completar, aislamiento de datos por perfil/familia, aplicación consistente de la ladder y no exposición infantil de métricas.
- **Frontend y accesibilidad:** presentación comprensible del estímulo, cromo, espera y feedback neutro; equivalencia sin audio y sin Nubi.
- **Contenido:** disponibilidad de distractores adecuados a cada nivel y de claves no cromáticas que preserven la actividad de color.
- **Agentes:** Nubi como refuerzo opcional y no como fuente del objetivo ni condición de juego.
- **Privacidad y seguridad infantil:** control parental del acceso y lenguaje no evaluativo de cualquier uso posterior de los datos.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

- Se conservan solo los datos mínimos de actividades completadas necesarios para la finalidad parental aprobada.
- El progreso es orientativo; no habilita diagnóstico, evaluación, perfilado comercial ni decisiones automatizadas sobre el niño.
- Los datos no se exponen entre perfiles ni fuera de la familia autorizada.
- El juego sigue siendo utilizable sin audio, sin lectura, sin color como canal único y sin intervención activa de Nubi.
- Nubi se limita al contexto de juego; no solicita datos personales ni formula valoraciones sobre el niño.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Diseño técnico, contratos, persistencia, algoritmos, temporización, integraciones y detalles de implementación.
- Diseño, contenido y visualización del dashboard parental.
- Puntuaciones, diagnósticos, clasificación o recomendaciones educativas y clínicas.
- Decidir memoria, fonética, lectura, conteo o valor numérico como variantes de este motor.

### Riesgos

- Distractores mal graduados pueden crear ambigüedad o frustración.
- La espera previa al toque puede interpretarse como bloqueo si no resulta breve y comprensible.
- Un acceso o texto parental inadecuado podría convertir señales orientativas en una evaluación del menor.

### Supuestos

- FEAT-009 continúa siendo la referencia de sus decisiones técnicas y de alcance ya aprobadas.
- La presentación infantil correspondiente se concreta en FEAT-014.

### Decisiones pendientes

1. Confirmar si los patrones o texturas de color se muestran siempre o solo con una preferencia visual configurada.
2. Confirmar el tamaño táctil estándar común después de validarlo en dispositivos reales.
3. Confirmar el tratamiento de FORMAS, incluida en documentación anterior pero no concretada en ADR-028.

## Referencias

- README.md.
- ADR-028 — Reconocimiento visual sin fricción.
- FEAT-009 — Recognition Engine Module.
- FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción.
- FEAT-014 — RecognitionEngine: experiencia visual y dificultad sin fricción.
