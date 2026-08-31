# ADR-024 — Origen de los cuentos de Lectura Familiar: recurso externo subido a backend

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-08-26
- **Supersede:** —

## 1. Contexto y problema

FEAT-003 (Backend: Content Module) había previsto que los cuentos de Lectura Familiar (`Story`/`StoryPage`) fueran un catálogo administrado internamente, con altas y ediciones mediante APIs CRUD como el resto del contenido educativo.

Se confirma que esto ya no refleja cómo se producen los cuentos: existe una herramienta llamada **Agente Cuenta cuentos** que genera cada cuento como un recurso compuesto por un directorio con un fichero de datos (título y textos de cada página) y los archivos de imagen y audio correspondientes a cada página. Esta herramienta es **externa a esta aplicación**: no forma parte de sus capas (backend, agents, TTS o frontend) ni de su arquitectura. La aplicación no necesita conocer su funcionamiento interno; su única relación con ella es recibir el recurso que produce mediante una subida al backend.

Este cambio afecta a cómo se puebla el catálogo de cuentos (backend/contenido) y a si la narración se sintetiza o se reproduce ya generada (TTS). La responsabilidad de que cada cuento cumpla la regla de contenido aséptico del producto (sin refranes ni moralejas) corresponde a quien produce y sube el recurso, no a esta aplicación.

## 2. Necesidad de la familia y usuarios afectados

- **Familia (adulto y niño):** sigue necesitando un catálogo de cuentos asépticos y listos para leer; no necesita saber ni participar en cómo se generan o se incorporan al sistema.
- **Persona responsable de contenido/administración:** necesita un proceso claro, aunque técnico, para incorporar al catálogo visible los cuentos ya generados por el Agente Cuenta cuentos, sin exponer ningún flujo de carga a la familia.

## 3. Alternativas de producto consideradas y compromisos

### A. Recepción del recurso mediante subida a backend, sin integración con la herramienta externa

**Valor:** evita depender de conectividad en tiempo real durante la lectura familiar; separa con claridad la generación de contenido (ajena a esta aplicación) del consumo (familia); la aplicación no necesita conocer ni integrar el funcionamiento del Agente Cuenta cuentos, solo recibir su resultado.

**Compromiso:** cada cuento nuevo requiere una acción de subida antes de estar disponible; el catálogo no se actualiza automáticamente.

### B. Integración/sincronización en vivo con el Agente Cuenta cuentos

**Valor aparente:** catálogo siempre actualizado sin pasos manuales.

**Inconveniente:** introduce una dependencia en tiempo real con un sistema externo durante el uso familiar y una integración innecesaria con una herramienta que no forma parte de esta aplicación.

### C. Mantener la generación y administración de cuentos dentro del módulo de contenido, como preveía FEAT-003

**Valor aparente:** un único sistema de administración para todo el contenido.

**Inconveniente:** ya no refleja la realidad del producto; el Agente Cuenta cuentos es quien produce el contenido, y su formato (directorio con datos y medios por página) no encaja con una administración CRUD campo a campo.

## 4. Decisión confirmada y justificación

Se confirma que los cuentos de Lectura Familiar se originan como recursos producidos por el **Agente Cuenta cuentos**, una herramienta externa que no forma parte de esta aplicación ni de ninguna de sus capas. El recurso de cada cuento es un directorio compuesto por: un fichero con el título y los textos de cada página, y los recursos de imagen y audio correspondientes a cada página. Este recurso llega al sistema mediante una **subida al backend**; más allá de recibirlo, la aplicación no integra, consulta ni depende del funcionamiento interno del Agente Cuenta cuentos, y no establece con él ninguna conexión en tiempo real mientras la familia consulta el catálogo o lee un cuento.

El audio incluido en el recurso de cada página **sustituye** a la narración sintetizada por TTS para esta sección: Lectura Familiar reproduce el audio ya recibido y no invoca el TTS narrativo (tonos definidos en TTS FEAT-002) para narrar cuentos del catálogo.

La incorporación, actualización o retirada de estos recursos en el catálogo es una tarea técnica/administrativa. La familia no dispone de ninguna función para añadir, editar o eliminar cuentos desde la aplicación.

## 5. Impacto

### Experiencia infantil

- Sin cambios directos: el niño sigue viendo un catálogo de cuentos listos para leer junto al adulto.

### Experiencia parental

- El adulto consulta un catálogo ya disponible; no participa en cómo se generó ni se incorporó cada cuento al sistema.

### Accesibilidad

- El audio pregrabado por página debe mantenerse comprensible y de calidad consistente entre cuentos.
- La sección debe seguir siendo utilizable solo con texto e imagen si el audio de un cuento concreto no está disponible.

### Seguridad infantil y privacidad

- La garantía de que cada cuento es aséptico (sin refranes ni moralejas) es responsabilidad de quien produce y sube el recurso, ajena a esta aplicación; ningún cuento debe quedar visible a la familia sin haber pasado por ese origen.
- No se introduce ninguna conexión en tiempo real ni intercambio de datos familiares o infantiles con el Agente Cuenta cuentos: la relación se limita a recibir el recurso que sube al backend.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- Cualquier interfaz para que la familia añada, edite o elimine cuentos.
- Cualquier integración, consulta o conexión en tiempo real entre esta aplicación y el Agente Cuenta cuentos.
- Uso del TTS narrativo (tonos de TTS FEAT-002) para narrar cuentos de este catálogo.
- El diseño, comportamiento y garantías del Agente Cuenta cuentos son ajenos a esta aplicación: no se documentan ni se especifican en ninguna de sus capas.

### Preguntas abiertas para los responsables técnicos

- **Backend/contenido:** definir cómo se reconcilia esta decisión con el alcance de `Story`/`StoryPage` descrito en FEAT-003 (administración CRUD, ahora eliminada, frente a recepción del recurso por subida), y cómo se valida la estructura del recurso (datos + imágenes + audio por página) al recibirlo.
- **Backend:** definir el mecanismo de subida del recurso al backend y quién la realiza.
- **Frontend:** confirmar que el consumo del catálogo y de cada cuento no depende de ningún campo o endpoint que asumiera generación o administración interna de texto, ya no vigente con este nuevo origen.

## Referencias

- README.md
- FEAT-003 — Backend: Content Module (alcance de `Story`/`StoryPage` a reconciliar)
- FEAT-008 — Lectura familiar: catálogo y lectura de cuentos
- TTS FEAT-002 — Tonos narrativos y simplificación del contrato TTS
