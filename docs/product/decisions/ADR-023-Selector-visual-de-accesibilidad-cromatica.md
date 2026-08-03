# ADR-023 — Selector visual de accesibilidad cromática

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-08-02
- **Supersede:** —

## 1. Contexto y problema

ADR-022 confirmó que un adulto puede seleccionar manualmente un modo de visualización de colores para un perfil infantil. La presentación vigente, basada en términos médicos y ejemplos geométricos simples, no permite a la familia comprender con facilidad el efecto práctico de cada modo durante el juego.

La familia necesita comparar las opciones de forma comprensible, sin que la aplicación transforme esta configuración en una prueba visual, diagnóstico o recomendación clínica sobre el menor.

## 2. Necesidad de la familia y usuarios afectados

- **Adultos autenticados:** necesitan elegir una preferencia visual de juego individual mediante una comparación clara, reversible y expresada en lenguaje cotidiano.
- **Niños de 3-4 años:** se benefician indirectamente de minijuegos de color que ofrecen claves visuales adicionales y no dependen solo del matiz cromático. No ven ni manejan este selector.

La decisión se mantiene dentro del alcance monofamiliar: no crea perfiles sanitarios, no recoge información médica y no comparte la preferencia visual fuera de la familia autorizada.

## 3. Alternativas de producto consideradas y compromisos

### A. Mantener el menú textual y ejemplos geométricos

**Valor:** ocupa poco espacio y conserva una interacción conocida.

**Inconveniente:** exige comprender términos médicos y no muestra con claridad la consecuencia de la elección en elementos similares a los del juego.

### B. Usar tarjetas visuales con muestra comparativa y explicaciones cotidianas

**Valor:** permite comparar las opciones de forma inmediata, relacionarlas con el juego y reducir la dependencia de terminología clínica.

**Compromiso:** requiere más espacio en la pantalla parental y textos cuidadosamente delimitados para evitar una interpretación diagnóstica.

### C. Recomendar automáticamente un modo a partir de la muestra

**Valor aparente:** reduciría la decisión del adulto.

**Inconveniente:** implicaría inferir una condición visual del menor y es incompatible con el carácter no clínico ni evaluativo del producto.

## 4. Decisión confirmada y justificación

Se confirma la alternativa B. En la configuración individual del niño, el menú textual de modos de visión de color se reemplaza por un **selector de tarjetas visuales**. Se conservan el estado **«Sin ajuste»** y los ocho modos ya aprobados en ADR-022.

Cada tarjeta muestra el término del modo y una explicación cotidiana de la confusión de colores más habitual asociada. La terminología médica se conserva solo como nombre de la opción; la explicación cotidiana debe ser la información principal para decidir.

Los textos funcionales aprobados son:

| Tarjeta | Explicación cotidiana |
|---|---|
| Sin ajuste | Sin ajuste de visualización. |
| DEUTERENOPIA | Rojo y verde pueden verse muy parecidos. |
| DEUTERANOMALY | Rojo y verde pueden costar más de distinguir. |
| PROTANOPIA | Algunos rojos y verdes pueden confundirse. |
| PROTANOMALY | Algunos rojos y verdes pueden parecerse. |
| TRITANOPIA | Azul y amarillo pueden verse parecidos. |
| TRITANOMALY | Azul y amarillo pueden costar más de distinguir. |
| ACHROMATOMALY | Los colores pueden verse menos intensos o apagados. |
| ACHROMATOPSIA | Los colores pueden verse en tonos grises. |

Todas las tarjetas usan la misma muestra breve basada en tres globos del juego —rojo, verde y azul—. Los globos muestran patrones o símbolos claros además de su color. Al explorar una tarjeta con puntero, foco o selección, la muestra presenta una simulación visual del modo correspondiente. En dispositivos táctiles, la selección ofrece la misma previsualización sin requerir una acción de pasar el cursor.

La muestra sirve exclusivamente para comparar cómo se distinguen los elementos del juego. Debe comunicar de forma visible que no identifica la visión del niño, no determina cuál es el modo correcto y no sustituye la orientación de un especialista ante dudas.

## 5. Impacto

### Experiencia infantil

- El niño no accede al selector ni recibe etiquetas relacionadas con su visión.
- Los minijuegos de color conservan patrones o símbolos como claves no cromáticas, de modo que el color no sea el único dato para jugar.
- No se introducen pruebas, tiempos, comparativas ni mensajes sobre la capacidad del niño.

### Experiencia parental

- Las tarjetas muestran un efecto comprensible y comparable antes de guardar la preferencia.
- La selección sigue siendo manual, deliberada y reversible mediante «Sin ajuste».
- El adulto recibe explicaciones cotidianas sin tener que interpretar teoría cromática.

### Accesibilidad

- Las tarjetas y la previsualización deben poder explorarse mediante toque, puntero y foco, sin depender exclusivamente del hover ni del color.
- Los patrones o símbolos deben distinguir los globos incluso cuando no se perciban sus colores.
- Los textos y controles deben mantenerse legibles y utilizables en móvil y tableta.

### Seguridad infantil y privacidad

- La preferencia permanece bajo control de un adulto autenticado y vinculada solo al perfil de su familia.
- No se solicitan, almacenan ni infieren datos médicos o visuales del menor.
- No se exponen ni reutilizan los datos del perfil para fines distintos de adaptar la experiencia de juego autorizada.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- Diagnóstico, cribado, evaluación o recomendación de un modo para el niño.
- Recogida de información sanitaria, visual o clínica del menor.
- Cambio de los ocho modos y del estado sin ajuste confirmados en ADR-022.
- Dependencia exclusiva de la simulación de la tarjeta: el juego debe conservar diferenciadores no cromáticos.
- Decisiones sobre el mecanismo técnico usado para crear la simulación visual, almacenamiento de preferencias o construcción de controles.

### Preguntas abiertas para los responsables técnicos

- **Frontend y accesibilidad:** validar que la previsualización responda de forma equivalente a toque, puntero y foco, que el tamaño y orden de las tarjetas funcione en móvil y tableta, y que los patrones se distingan sin color.
- **Contenido y accesibilidad:** validar la claridad no clínica de las explicaciones cotidianas, del aviso a especialista y de los símbolos empleados en los globos.
- **Backend, datos y seguridad/privacidad:** validar que la preferencia siga siendo individual, accesible solo por la familia autorizada y que no habilite inferencias ni registros médicos.
- **Responsable técnico correspondiente:** validar que la simulación solicitada refleje la opción seleccionada de manera coherente, sin alterar el alcance funcional ni las garantías de accesibilidad.

## Referencias

- README.md
- ADR-022 — Gestión parental de perfiles infantiles.
- FEAT-006 — Gestión parental de perfiles infantiles.
- FEAT-009 — Recognition Engine Module.
