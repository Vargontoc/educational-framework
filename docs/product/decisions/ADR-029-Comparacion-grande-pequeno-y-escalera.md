# ADR-029 — Comparación grande/pequeño y escalera de dificultad

## Estado

- **Estado:** aceptada.
- **Fecha:** 2026-09-20.
- **Alcance:** primer eje del futuro motor de comparación; no define su realización técnica.

## 1. Contexto y problema

La familia incorpora un minijuego de comparación visual para niños y niñas de 3–4 años. El alcance confirmado se limita al contraste **grande/pequeño** y debe conservar las reglas infantiles transversales: interacción sin presión, jugable sin audio y sin confundir el atributo de tamaño con cambios de objeto.

## 2. Necesidad de la familia y usuarios afectados

- **Niño o niña:** necesita comparar visualmente tamaños mediante una consigna estable, breve y comprensible sin lectura ni audio.
- **Familia:** necesita una progresión suave que priorice exploración y no transforme el juego en una prueba de capacidad.
- **Adulto autenticado:** podrá acceder en el futuro solo a información orientativa autorizada; su visualización queda fuera de esta decisión.

## 3. Alternativas de producto consideradas y compromisos

### A. Cambiar el objeto entre opciones

- **Valor aparente:** permite mayor variedad visual.
- **Inconveniente:** puede desviar la atención del tamaño y añadir ruido a la comparación; no se acepta.

### B. Mantener el mismo objeto y graduar la diferencia de tamaño

- **Valor:** centra la actividad en el atributo grande/pequeño y crea una escalera predecible.
- **Compromiso:** requiere contenido que mantenga el objeto reconocible mientras cambia únicamente su tamaño relativo.

### C. Alternar entre «el más grande» y «el más pequeño»

- **Valor aparente:** amplía la variedad de consignas.
- **Inconveniente:** duplica la carga de comprensión y el lenguaje visual necesario; no se acepta en este alcance inicial.

## 4. Decisión confirmada y justificación

Se confirma un único eje de comparación: **grande/pequeño**. La consigna pide siempre identificar **«el más grande»**; «pequeño» actúa como contraste visual, no como una consigna alterna en esta primera versión.

El mismo sprite u objeto se mantiene idéntico entre opciones en EASY, MEDIUM y HARD. La dificultad se construye únicamente mediante una diferencia relativa de tamaño progresivamente menor:

| Nivel | Opciones | Diferencia de tamaño relativa | Regla de objeto |
|---|---:|---|---|
| EASY | 2 | Extrema: 100 % frente a 40 % | Mismo sprite en ambas opciones |
| MEDIUM | 2 | Moderada: 100 % frente a 65 % | Mismo sprite en ambas opciones |
| HARD | 3 | Tres niveles: 100 %, 75 % y 50 % | Mismo sprite en las tres opciones |

El porcentaje expresa una relación visual de referencia para la escalera de producto, no una instrucción de implementación. La consigna debe mantenerse comprensible sin texto ni audio como único canal y debe seguir las reglas transversales ya aprobadas de toque directo, reintento sin límite y feedback no evaluativo.

## 5. Impacto

### Experiencia infantil

- El niño aprende una única regla estable: encontrar el objeto más grande.
- El juego no exige contar, leer, recordar una imagen ni distinguir entre objetos distintos.
- La similitud entre tamaños aumenta gradualmente sin pérdida, castigo ni límite de tiempo.

### Experiencia parental

- El eje se presenta como juego de exploración visual, no como medición del desarrollo del menor.
- No se introducen controles parentales, diagnósticos ni interpretaciones nuevas.

### Accesibilidad

- El significado de la consigna no depende exclusivamente de audio, lectura o color.
- Mantener el mismo objeto entre opciones reduce carga visual y cognitiva.
- Las opciones deben respetar los criterios transversales de objetivos táctiles amplios, pendientes de su validación en dispositivo real.

### Seguridad infantil y privacidad

- No se solicitan datos personales ni se amplía la intervención de Nubi.
- Cualquier señal de interacción futura se limita a la finalidad parental autorizada y no expresa capacidad, diagnóstico ni clasificación.

## 6. Límites, exclusiones y cuestiones para los responsables técnicos

### Límites y exclusiones

- Alto/bajo, largo/corto, lleno/vacío y muchos/pocos quedan fuera de este requisito.
- No se incorpora conteo, cantidad exacta, aritmética temprana, lectura, memoria ni fonética.
- No se alterna la consigna hacia «el más pequeño».
- No se definen contratos, persistencia, algoritmos, assets, temporización, integración, renderizado ni mecanismos técnicos.
- No se diseña dashboard, tracking detallado, puntuación, evaluación o adaptación posterior.

### Ámbitos que deben validar los responsables

- **Contenido:** que los sprites sean apropiados por edad, se mantengan idénticos entre opciones y permitan percibir las proporciones sin ambigüedad.
- **Frontend y accesibilidad:** comprensión visual de la consigna, separación y tamaño de objetivos táctiles, y equivalencia con audio desactivado.
- **Backend y datos:** respeto de las reglas transversales de actividad completada y acceso parental exclusivo a cualquier dato que se acuerde registrar.
- **Agentes y seguridad infantil:** que Nubi sea opcional y no sustituya la fuente visual de la consigna.

### Preguntas abiertas

- Ninguna dentro del alcance confirmado de grande/pequeño y su escalera. Los demás ejes requerirán una decisión de producto independiente.

## Referencias

- README.md.
- comparacion-decisiones.md (fuente de la sesión de debate; alcance parcial confirmado).
- ADR-028 — Reconocimiento visual sin fricción (reglas transversales de minijuego).
- FEAT-013 — Minijuegos: interacción visual básica y cierre sin fricción.
