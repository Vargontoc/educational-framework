# SPRINT-086: Diario Parental - Acción de Reinicio

## Objetivo
Implementar la acción de "limpiar datos" en el diario parental que permite al adulto reiniciar el historial de tracking y la adaptación de dificultad de un perfil infantil, con doble confirmación de seguridad.

## Contexto
El diario parental incluye una acción destructiva que:
- Elimina todos los datos de tracking del perfil
- Reinicia la adaptación de dificultad a EASY
- Conserva el perfil y sus preferencias
- Requiere doble confirmación de seguridad para evitar accidentes

## Requisitos

### Acción de Reinicio
- **Ubicación**: botón "Limpiar datos del diario" en la vista del diario
- **Acceso**: solo adultos autorizados de la misma familia
- **Confirmación**: doble verificación con modal de confirmación

### Flujo de Confirmación
1. Usuario hace clic en "Limpiar datos del diario"
2. Se muestra modal de confirmación con:
   - Advertencia clara: "Esta acción eliminará todo el historial de juego y reiniciará la adaptación de dificultad"
   - Aclaración: "El perfil, avatar y preferencias del niño se conservarán"
   - Botones: "Cancelar" y "Confirmar limpieza"
3. Usuario hace clic en "Confirmar limpieza"
4. Se muestra segundo modal de confirmación con:
   - Advertencia final: "¿Estás seguro? Esta acción no se puede deshacer"
   - Campo de texto: escribir "CONFIRMAR" para habilitar el botón
   - Botones: "Cancelar" y "Eliminar datos" (deshabilitado hasta escribir "CONFIRMAR")
5. Usuario escribe "CONFIRMAR" y hace clic en "Eliminar datos"
6. Se llama al endpoint `POST /api/v1/diary/children/{childProfileId}/reset`
7. Se muestra mensaje de éxito: "Los datos del diario han sido reiniciados"
8. Se actualiza la vista del diario (estado vacío)

### Estados
- **Carga**: mostrar spinner mientras se procesa la solicitud
- **Éxito**: mostrar mensaje de éxito y actualizar vista
- **Error**: mostrar mensaje de error y permitir reintentar

### Accesibilidad
- Modales accesibles con teclado (Tab, Escape)
- Textos claros y no evaluativos
- Confirmación explícita para evitar accidentes

## Tareas

### Componentes Vue
- [ ] Crear `DiaryResetButton.vue` con botón de acción de reinicio
- [ ] Crear `DiaryResetConfirmationModal.vue` con primer modal de confirmación
- [ ] Crear `DiaryResetFinalConfirmationModal.vue` con segundo modal de confirmación
- [ ] Crear `DiaryResetSuccessMessage.vue` con mensaje de éxito
- [ ] Crear `DiaryResetErrorMessage.vue` con mensaje de error

### Servicios API
- [ ] Añadir método en `diaryService.ts`: `resetDiary(childProfileId)` → `DiaryResetResponse`
- [ ] Implementar manejo de errores y estados de carga

### Tipos TypeScript
- [ ] Crear `DiaryResetResponse` con campos: childProfileId, deletedAttempts, deletedSummaries, deletedAchievements, resetAt

### Integración
- [ ] Añadir botón de reinicio en `DiaryView.vue`
- [ ] Implementar flujo de doble confirmación con modales
- [ ] Implementar campo de texto "CONFIRMAR" para habilitar botón final
- [ ] Llamar al endpoint de reinicio tras confirmación final
- [ ] Manejar estados de carga, éxito y error
- [ ] Actualizar vista del diario tras reinicio exitoso

### Estilos y Accesibilidad
- [ ] Implementar diseño de modales accesibles
- [ ] Implementar navegación por teclado (Tab, Escape)
- [ ] Implementar textos claros y no evaluativos
- [ ] Implementar confirmación explícita con campo de texto
- [ ] Verificar accesibilidad en móvil y tableta

### Tests
- [ ] Test de componente: `DiaryResetButton` abre primer modal
- [ ] Test de componente: `DiaryResetConfirmationModal` abre segundo modal
- [ ] Test de componente: `DiaryResetFinalConfirmationModal` habilita botón al escribir "CONFIRMAR"
- [ ] Test de componente: flujo completo de doble confirmación
- [ ] Test de componente: manejo de estado de carga
- [ ] Test de componente: manejo de estado de éxito
- [ ] Test de componente: manejo de estado de error
- [ ] Test de integración: flujo completo de reinicio de diario
- [ ] Test de accesibilidad: navegación por teclado en modales

## Criterios de Aceptación

1. El botón de reinicio solo es visible para adultos autorizados
2. Se muestra primer modal de confirmación con advertencia clara
3. Se muestra segundo modal de confirmación con campo de texto "CONFIRMAR"
4. El botón final solo se habilita al escribir "CONFIRMAR" exactamente
5. Se llama al endpoint de reinicio tras confirmación final
6. Se muestra mensaje de éxito tras reinicio exitoso
7. Se actualiza la vista del diario (estado vacío) tras reinicio
8. Se muestra mensaje de error si falla el reinicio
9. Los modales son accesibles con teclado (Tab, Escape)
10. Los textos son claros y no evaluativos
11. La confirmación es explícita para evitar accidentes
12. Los tests de componente y de integración pasan

## Notas Técnicas

- **Doble confirmación**: implementar dos modales secuenciales para mayor seguridad
- **Campo de texto**: requerir escribir "CONFIRMAR" exactamente (case-sensitive)
- **Estados**: manejar carga, éxito y error con feedback visual claro
- **Accesibilidad**: modales deben ser navegables con teclado y screen readers
- **Texto**: usar mensajes claros y no evaluativos (no "has borrado todo", sino "los datos han sido reiniciados")

## Dependencias

- SPRINT-113 completado (endpoint de reinicio de historial)
- SPRINT-085 completado (vista del diario)
- FEAT-015 (frontend) aceptado

## Estimación

- **Tamaño:** M (Medium)
- **Complejidad:** Media (doble confirmación, accesibilidad, manejo de estados)
- **Riesgo:** Medio (acción destructiva, accesibilidad, confirmación explícita)
