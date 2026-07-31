# FEAT-005 — Configuración global de audio, NPC y PIN

## Estado

- **Estado:** aceptada
- **Responsable principal:** frontend
- **Decisión confirmada:** 2026-07-28
- **Historia de usuario:** Como adulto autenticado, quiero configurar globalmente el audio, el NPC, las voces y el PIN de la familia para adaptar la aplicación a nuestro contexto de uso.
- **Depende de:** FEAT-004 — Estructura visual y navegación del panel parental; ADR-021; acceso parental mediante PIN.

## 1. Objetivo y valor para la familia

Dar a la familia control global, claro y reversible sobre el audio y la presencia del NPC, sin bloquear la experiencia infantil. Permitir cambiar el PIN familiar usando el formato conocido de creación y finalizar la sesión cuando ese cambio se confirma.

## 2. Actores y escenarios de uso

### Adulto que ajusta el sonido y el NPC

1. Entra en Configuración desde el panel parental autenticado.
2. Consulta secciones separadas para audio general, NPC, voz del NPC y voz narrativa.
3. Activa, desactiva o ajusta porcentajes según sus necesidades.
4. Usa el valor 0 como acceso directo para apagar una opción con porcentaje, si lo desea.
5. Pulsa «Guardar cambios» para aplicar el conjunto de modificaciones.

### Adulto que cambia el PIN

1. En la sección de PIN introduce un PIN nuevo de cuatro dígitos numéricos.
2. Introduce el mismo PIN para confirmarlo; puede coincidir con el PIN previo.
3. Pulsa «Guardar cambios».
4. Si la confirmación es correcta, la sesión parental se cierra y vuelve a Home.

## 3. Requisitos funcionales y no funcionales de producto

1. La vista de Configuración solo está disponible para un adulto con acceso parental válido.
2. Debe presentar en una misma vista secciones visualmente diferenciadas de: Audio general, NPC, Voz del NPC, Voz narrativa y PIN familiar.
3. Audio general debe disponer de interruptor on/off y valor porcentual. Su estado no debe modificar automáticamente los controles de voz NPC ni narrativa.
4. NPC debe disponer de interruptor on/off. Desactivarlo elimina su presencia, movimiento, animaciones e intervenciones de voz en el juego, sin desactivar la voz narrativa.
5. Voz del NPC debe disponer de interruptor on/off y valor porcentual. Desactivarla mantiene al NPC visualmente presente e interactivo cuando el NPC esté activado, pero sin intervenciones habladas.
6. Voz narrativa debe disponer de interruptor on/off y valor porcentual. Es independiente del NPC y de la voz del NPC.
7. Los controles con porcentaje deben permitir ajustar el valor de forma gradual y establecerlo directamente en 0.
8. Establecer directamente un porcentaje en 0 debe apagar el interruptor asociado como acción rápida.
9. Al apagar Audio general, Voz del NPC o Voz narrativa, debe conservarse el último valor porcentual distinto de cero para recuperarlo al reactivar el control.
10. Al apagar NPC, debe conservarse la configuración de Voz del NPC para recuperarla al reactivar el NPC.
11. La sección PIN familiar debe solicitar un nuevo PIN de exactamente cuatro dígitos numéricos y una segunda introducción de confirmación.
12. El PIN nuevo puede ser igual al PIN actual.
13. No debe confirmarse un cambio de PIN si los dos valores introducidos no coinciden; debe mostrarse un aviso claro y respetuoso.
14. Debe existir una única acción final denominada exactamente **«Guardar cambios»** para aplicar los ajustes modificados de la vista.
15. Si se confirma un cambio correcto de PIN dentro del conjunto de ajustes guardados, la sesión parental debe cerrarse y la persona debe volver a Home.
16. La vista no debe mostrar datos infantiles, progreso, comparativas ni clasificaciones.
17. Los controles deben ser comprensibles para adultos, táctiles, distinguibles sin depender solo del color y utilizables en móvil y tableta.

## 4. Criterios de aceptación verificables

1. Una persona sin acceso parental válido no puede ver ni modificar los ajustes globales.
2. La vista muestra cinco secciones diferenciadas: Audio general, NPC, Voz del NPC, Voz narrativa y PIN familiar.
3. Audio general puede activarse, desactivarse y recibir un valor porcentual sin cambiar automáticamente las voces NPC o narrativa.
4. Con NPC desactivado, el juego no muestra ni anima al NPC y este no realiza intervenciones de voz; la voz narrativa puede mantenerse activa.
5. Con NPC activo y Voz del NPC desactivada, el NPC permanece visible e interactivo sin hablar.
6. Voz narrativa se puede activar, desactivar y ajustar independientemente del NPC.
7. Cada control porcentual permite establecer el valor directamente en 0; al hacerlo, su interruptor queda apagado.
8. Tras apagar y volver a activar un control porcentual, se recupera su último valor distinto de cero.
9. Tras apagar y volver a activar el NPC, se conserva su configuración previa de Voz del NPC.
10. «Guardar cambios» es la única acción final para aplicar las modificaciones de la vista.
11. Un PIN nuevo solo se acepta con dos valores coincidentes de cuatro dígitos numéricos, incluido el caso en que sea igual al PIN anterior.
12. Tras guardar correctamente un cambio de PIN, la sesión parental se cierra y se muestra Home.
13. La vista no presenta datos de niños, progreso ni contenido evaluativo.

## 5. Ámbitos que deben validar los responsables y dependencias de producto conocidas

### Frontend

- Comprensión y disposición de las cinco secciones en móvil y tableta.
- Claridad del valor 0, del estado apagado, de los valores conservados y de «Guardar cambios».
- Mensajes comprensibles para PIN no coincidente, cancelación, error y cierre posterior al cambio de PIN.

### Backend, agentes y TTS

- Respeto de los controles globales en la experiencia infantil sin impedir el juego.
- Independencia entre audio general, NPC, voz NPC y voz narrativa conforme a los comportamientos definidos.

### Seguridad y privacidad

- Acceso exclusivo de adultos autenticados a la configuración.
- Tratamiento protegido del PIN, validación del cambio y cierre de sesión posterior.

### Dependencias de producto conocidas

- Acceso parental mediante PIN y acción «Salir» definidos en FEAT-004.
- Configuración global familiar ya contemplada por el producto.
- Experiencias de juego, lectura familiar y sus voces respectivas.

## 6. Privacidad, seguridad infantil, accesibilidad y límites de IA aplicables

### Privacidad y seguridad infantil

- No se recogen datos nuevos de menores ni se presentan datos infantiles en esta vista.
- El PIN no debe mostrarse como texto legible al introducirlo ni después de guardarlo.
- El cambio de PIN cierra la sesión parental para reducir la exposición de controles adultos.
- Desactivar voces o NPC no debe activar conversaciones, pedir datos personales ni inducir decisiones sensibles al niño.

### Accesibilidad y experiencia infantil

- La configuración está dirigida a adultos y no forma parte de la experiencia infantil.
- Los ajustes pueden adaptarse a necesidades sensoriales o contextos silenciosos sin penalizar ni bloquear al niño.
- Los controles tienen etiquetas visibles, objetivos táctiles amplios y estados comprensibles sin depender solo del color.

### Límites de IA

- La opción NPC solo afecta a su presencia e intervenciones en contexto de juego.
- La voz narrativa se limita a la experiencia de lectura familiar; esta configuración no activa un agente conversacional.

## 7. Exclusiones, riesgos, supuestos y decisiones pendientes

### Exclusiones

- Ajustes individuales por niño, incluido daltonismo.
- Recuperación de PIN, autenticación adicional, cambios de inactividad o configuración de sesión.
- Ajustes de contenido, cuentos, relajación, dashboard, progreso o chatbot.
- Detalles de implementación de audio, generación de voz, persistencia, comunicación entre capas o controles visuales concretos.

### Riesgos

- La coexistencia de audio general y voces independientes puede resultar confusa. Las etiquetas y la breve ayuda de cada sección deben aclarar su alcance sin lenguaje técnico.
- Al guardar varios cambios junto con un PIN nuevo, el cierre de sesión puede sorprender al adulto. Debe comunicarse claramente antes de la confirmación.
- Un estado apagado que conserva un volumen previo debe ser reconocible para evitar expectativas erróneas al reactivarlo.

### Supuestos

- El valor 0 se interpreta como un acceso rápido para apagar el control y conservar el último valor distinto de cero para su posterior recuperación.
- «Guardar cambios» aplica el conjunto de modificaciones de la vista, incluido un posible cambio de PIN.

### Decisiones pendientes

- El texto de ayuda breve para diferenciar Audio general, Voz del NPC y Voz narrativa debe ser validado por contenido.
- Los responsables técnicos deben definir la aplicación efectiva de los cambios, el tratamiento seguro del PIN y los estados de error sin alterar el comportamiento funcional confirmado.
