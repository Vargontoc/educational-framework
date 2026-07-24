import { ref, computed } from 'vue'

export interface Toast {
  id: string
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

/**
 * Store reactivo global para los toasts
 * Se comparte entre el composable y el componente de presentación
 */
const toasts = ref<Toast[]>([])
let nextId = 0

function generateId(): string {
  return `toast-${++nextId}`
}

/**
 * useToast - Composable para gestionar notificaciones toast
 * 
 * Características:
 * - Sistema de cola para múltiples notificaciones
 * - Duración configurable (3-5 segundos por defecto)
 * - Tipos: success, error, warning, info
 * - Eliminación manual o automática
 */
export function useToast() {
  /**
   * Muestra un nuevo toast
   */
  function show(message: string, type: Toast['type'] = 'info', duration = 4000): string {
    const id = generateId()
    const toast: Toast = { id, message, type, duration }
    
    toasts.value.push(toast)
    
    if (duration > 0) {
      setTimeout(() => {
        remove(id)
      }, duration)
    }
    
    return id
  }
  
  function success(message: string, duration?: number): string {
    return show(message, 'success', duration)
  }
  
  function error(message: string, duration?: number): string {
    return show(message, 'error', duration ?? 5000)
  }
  
  function warning(message: string, duration?: number): string {
    return show(message, 'warning', duration)
  }
  
  function info(message: string, duration?: number): string {
    return show(message, 'info', duration)
  }
  
  function remove(id: string) {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index !== -1) {
      toasts.value.splice(index, 1)
    }
  }
  
  function clearAll() {
    toasts.value = []
  }
  
  return {
    toasts: computed(() => toasts.value),
    show,
    success,
    error,
    warning,
    info,
    remove,
    clearAll
  }
}
