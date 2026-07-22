# Feat-001 - Chatterbox como único proveedor TTS

## Status

state: accepted
user_history: Decisión confirmada en ADR-013-Chatterbox-unico-proveedor-TTS el 2026-07-18.
depends_on: ADR-013-Chatterbox-unico-proveedor-TTS; contrato `docs/contracts/api/openapi_tts.json`.
owned_by: tts
test: Validación del servicio TTS, de su contrato y de la continuidad funcional sin audio por los responsables de capa.

## Description

### Objetivo y valor para la familia

Ofrecer una síntesis de voz simple y mantenible para el personaje del juego y la narración familiar, usando exclusivamente Chatterbox. La familia no debe elegir, configurar ni comprender proveedores de voz alternativos.

La aplicación seguirá siendo utilizable cuando el audio no esté disponible: la ausencia de TTS no debe impedir jugar ni acceder a experiencias familiares.

### Actores y escenarios de uso

- **Niño o niña de 3-4 años:** recibe mensajes cortos y apropiados de Nubi y, cuando corresponda, narración de cuentos. Si el audio falla, continúa la actividad sin castigos ni mensajes técnicos.
- **Madre, padre o adulto responsable:** activa o desactiva audio según las opciones aprobadas del producto, sin seleccionar proveedor ni gestionar fallbacks técnicos.
- **Backend:** solicita síntesis al servicio TTS para contenido ya autorizado y resuelve la continuidad cuando el servicio no está disponible.
- **Servicio TTS:** sintetiza mediante Chatterbox con los perfiles de voz y tonos aprobados.

### Requisitos funcionales

1. Chatterbox es el único proveedor de síntesis disponible para el servicio TTS.
2. El servicio admite los perfiles de voz aprobados por el contrato: `npc` y `storyteller`.
3. El servicio admite los tonos semánticos aprobados en el contrato y los aplica exclusivamente mediante Chatterbox.
4. El servicio entrega audio en el formato definido por `docs/contracts/api/openapi_tts.json`.
5. El servicio expone el estado de disponibilidad conforme a su contrato, identificando Chatterbox como proveedor activo.
6. Si Chatterbox no está disponible, supera el tiempo de espera o devuelve un error, el servicio informa del error conforme al contrato. No intenta otro proveedor automáticamente.
7. La validación de longitud de texto y de locales permitidos corresponde al backend antes de solicitar síntesis; no forma parte de este servicio.
8. No se ofrecen opciones parentales, infantiles o administrativas para elegir proveedor ni configurar fallback entre proveedores.

### Requisitos no funcionales

- La configuración operativa del servicio solo contempla Chatterbox como proveedor de voz.
- No debe permanecer una dependencia funcional de XTTS, Coqui ni de un proveedor de respaldo.
- Los errores del proveedor deben distinguirse de una respuesta de audio válida y conservar el formato de error acordado en el contrato.
- El contrato público del servicio se mantiene compatible con `docs/contracts/api/openapi_tts.json`, salvo que una modificación futura sea expresamente aprobada.

### Criterios de aceptación verificables

1. Una solicitud válida con perfil `npc` devuelve audio conforme a `openapi_tts.json` cuando Chatterbox está disponible.
2. Una solicitud válida con perfil `storyteller` devuelve audio conforme a `openapi_tts.json` cuando Chatterbox está disponible.
3. Una solicitud con un tono no admitido devuelve el error contractual correspondiente y no intenta usar otro proveedor.
4. Ante indisponibilidad o tiempo de espera de Chatterbox, el servicio devuelve el error contractual correspondiente y no realiza una segunda síntesis con XTTS, Coqui u otro proveedor.
5. El estado del servicio informa de Chatterbox como único proveedor activo.
6. No existen configuraciones operativas ni rutas funcionales para seleccionar XTTS, Coqui o fallback de proveedor.
7. La documentación del servicio no presenta XTTS/Coqui como proveedor disponible o fallback activo.
8. La aplicación puede continuar el juego o la experiencia de lectura sin audio cuando backend informa que el audio no está disponible; esta continuidad requiere validación de backend y frontend.

### Privacidad, seguridad infantil, accesibilidad y límites de IA

- La síntesis se limita a texto autorizado por el backend para las finalidades de juego y lectura familiar aprobadas.
- El servicio no solicita información personal al menor ni genera interacción abierta con el niño.
- No se envían textos sintetizados a proveedores externos: Chatterbox opera en el entorno previsto por el producto.
- No se almacenan textos sintetizados, conversaciones ni progreso infantil para una finalidad distinta de la petición autorizada.
- La ausencia de audio debe tener alternativa visual o de continuidad definida por frontend/contenido, sin penalización para el menor.
- El contenido asociado a voz NPC debe ser corto, seguro, apropiado para la edad y limitado al contexto de juego.

### Exclusiones

- XTTS, Coqui y cualquier fallback automático entre proveedores.
- Selección de proveedor por parte de familias, menores o interfaz administrativa.
- Validación de longitud de texto y de locales dentro del servicio TTS.
- Generación de contenido textual, razonamiento conversacional o toma de decisiones del NPC: corresponde a los agentes y al backend.
- Diseño de la experiencia visual, reproducción cliente o continuidad del juego sin audio: corresponden a frontend y backend.

### Supuestos y decisiones pendientes

- Se asume que Chatterbox dispone de las voces configuradas para `npc` y `storyteller`.
- La familia acepta que una caída de Chatterbox no tendrá conmutación automática a otro proveedor.
- Debe confirmarse por infraestructura la retirada de cualquier servicio Coqui/XTTS de la operación del proyecto.
- Debe confirmarse por TTS si los artefactos de pruebas de XTTS se eliminan o se archivan como referencia no ejecutable.
- Debe revisarse la documentación general para eliminar referencias obsoletas a XTTS/Coqui.

### Ámbitos que deben validar los responsables y dependencias de producto conocidas

- **TTS:** exclusividad de Chatterbox, perfiles, tonos, formato de audio, errores y retirada de dependencias XTTS/Coqui.
- **Backend:** validación de texto/locales, mediación de solicitudes TTS y comportamiento de continuidad cuando no hay audio.
- **Frontend:** continuidad comprensible sin audio, sin mensajes técnicos ni penalización infantil; no consumo directo de TTS.
- **Contenido/IA:** seguridad y adecuación por edad del texto destinado a `npc` y `storyteller`.
- **Infraestructura:** operación del contenedor Chatterbox, disponibilidad interna y retirada/archivo de componentes Coqui/XTTS.

### Risks

- La indisponibilidad de Chatterbox deja la aplicación sin audio hasta que el adulto reintente o el servicio se recupere.
- Una calidad insuficiente de voz en español requeriría una decisión futura para incorporar otro proveedor, no un fallback automático implícito.
- Referencias residuales a XTTS/Coqui en configuración, pruebas o documentación pueden inducir configuraciones erróneas.

### Riesgos (detalle) (por capa)

- **TTS:** configuración o adaptadores residuales de XTTS/Coqui pueden mantener complejidad o rutas no soportadas.
- **Backend:** solicitar síntesis sin validar texto/locales puede trasladar errores evitables al proveedor.
- **Frontend:** tratar una caída de audio como error de juego puede frustrar al menor.
- **Infraestructura:** mantener servicios o recursos Coqui/XTTS no usados aumenta coste y superficie operativa.
- **Contenido/IA:** texto no revisado para niños podría producir audio inadecuado aunque el proveedor funcione correctamente.

## Mitigaciones (por capa)

- **TTS:** conservar una única configuración de proveedor y verificar errores/estado contra el contrato.
- **Backend:** validar texto y locales antes de solicitar audio; informar a frontend de una degradación que permita continuar.
- **Frontend:** mantener feedback visual y actividades funcionales sin audio/NPC, sin detalle técnico para el niño.
- **Infraestructura:** validar retirada o archivo de servicios/configuración no usados y conservar solo la operación necesaria de Chatterbox.
- **Contenido/IA:** limitar textos a contenido aprobado, seguro y adecuado para 3-4 años.
