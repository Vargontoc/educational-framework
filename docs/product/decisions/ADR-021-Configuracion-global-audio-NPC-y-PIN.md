# ADR-021 — Configuración global de audio, NPC y PIN

## Estado

- **Estado:** aceptada
- **Fecha:** 2026-07-28
- **Supersede:** —

## 1. Contexto y problema

La sección Configuración del panel parental necesita ofrecer controles globales sobre el audio general, el NPC, las voces y el PIN familiar. La familia requiere poder adaptar la experiencia al contexto de uso sin interferir con el juego ni perder sus preferencias al desactivar temporalmente una opción.

## 2. Necesidad de la familia y usuarios afectados

Los adultos autenticados necesitan controlar de forma comprensible el sonido y la presencia del NPC para adaptar el uso de la aplicación a cada momento. También necesitan cambiar el PIN familiar desde el panel.

Los niños se ven afectados indirectamente: las opciones deben permitir una experiencia silenciosa o sin NPC sin castigos, bloqueos ni interrupción del juego.

## 3. Alternativas de producto consideradas y compromisos

### Aplicar cada cambio inmediatamente

**Ventaja:** efecto inmediato y menos pasos.

**Inconveniente:** facilita cambios accidentales al recorrer una vista con varias configuraciones globales.

### Confirmar todos los cambios con una acción única

**Ventaja:** permite revisar los ajustes antes de aplicarlos y reduce modificaciones involuntarias.

**Compromiso:** exige una acción final adicional.

### Restablecer el porcentaje al apagar una opción

**Ventaja:** representa de manera literal el estado apagado.

**Inconveniente:** obliga a la familia a ajustar de nuevo el volumen al reactivar una opción.

### Conservar el último porcentaje al apagar una opción

**Ventaja:** hace los ajustes reversibles y adecuados para cambios temporales de contexto.

**Compromiso:** debe quedar claro que una opción apagada conserva una preferencia que no está activa.

## 4. Decisión confirmada y justificación

Se confirma una única vista de Configuración con secciones visualmente separadas y un botón final **«Guardar cambios»** que aplica el conjunto de ajustes modificados.

La vista incluye:

- **Audio general:** interruptor on/off y valor porcentual. Este ajuste solo controla el audio general; no altera por sí mismo la voz del NPC ni la voz narrativa.
- **NPC:** interruptor on/off para la presencia e interacción visual del NPC en el juego. Al desactivarlo, no aparece, se mueve ni anima, y no produce intervenciones de voz. La voz narrativa sigue siendo independiente.
- **Voz del NPC:** interruptor on/off y valor porcentual. Si solo se desactiva esta voz, el NPC permanece presente e interactivo, pero no habla.
- **Voz narrativa:** interruptor on/off y valor porcentual, independiente del NPC.
- **PIN familiar:** creación de un PIN nuevo de cuatro dígitos numéricos y confirmación mediante una segunda introducción. Se permite repetir el PIN actual. Tras guardar un cambio de PIN correcto, la sesión parental se cierra y la persona vuelve a Home.

Al apagar un control que tiene porcentaje se conserva su último valor distinto de cero para recuperarlo al reactivarlo. Establecer el valor directamente en 0 actúa como una acción rápida de apagado del control asociado; no obliga a desplazar el ajuste de forma gradual.

Esta decisión favorece controles reversibles, distingue el NPC de sus voces y evita que el silencio o la ausencia del NPC bloqueen la experiencia infantil.

## 5. Impacto

### Experiencia infantil

- El juego puede continuar sin audio, voz o NPC, sin penalizaciones ni mensajes negativos.
- La desactivación de voz del NPC no elimina sus animaciones e interacción visual.
- La voz narrativa sigue disponible aunque el NPC esté desactivado.

### Experiencia parental

- Los ajustes se agrupan por propósito y se guardan de manera deliberada mediante una única acción.
- Las preferencias de volumen se recuperan tras una desactivación temporal.
- El cambio de PIN es coherente con el formato usado al crear la familia.

### Accesibilidad

- Las secciones, interruptores, porcentajes y acción de guardado requieren etiquetas claras y controles táctiles amplios.
- El valor 0 ofrece una alternativa directa al ajuste gradual.
- El estado activo o inactivo no depende exclusivamente de color.

### Seguridad infantil y privacidad

- La configuración global y el cambio de PIN permanecen limitados a adultos autenticados.
- El cambio de PIN termina la sesión parental para evitar que el acceso anterior continúe abierto.
- Esta funcionalidad no solicita nuevos datos de menores ni muestra progreso infantil.

## 6. Límites, exclusiones y preguntas abiertas para los responsables técnicos

### Límites y exclusiones

- Ajustes por perfil infantil, incluido el modo de visión de colores.
- Configuración de contenido, cuentos, relajación, progreso o chatbot.
- Recuperación de PIN, requisitos adicionales de autenticación y cambios en la política de inactividad.
- Definición técnica de reproducción, generación de voz, persistencia de preferencias, sesión o comunicación entre capas.

### Preguntas abiertas para los responsables técnicos

- **Frontend:** validar que la jerarquía de secciones, los controles porcentuales, el estado apagado y «Guardar cambios» sean comprensibles en móvil y tableta.
- **Backend, agentes y TTS:** validar que la configuración global se respete sin activar intervenciones de voz NPC cuando el NPC o su voz estén desactivados, y que la voz narrativa conserve su independencia funcional.
- **Backend y seguridad:** validar el cierre de sesión posterior a un cambio correcto de PIN y que no se exponga el PIN.

## Referencias

- README.md
- ADR-017 — Componentes globales del panel parental, modo oscuro y catálogo de desarrollo.
- ADR-020 — Estructura adaptable del panel parental.
- FEAT-005 — Configuración global de audio, NPC y PIN.
