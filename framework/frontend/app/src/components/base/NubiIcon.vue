<template>
  <component 
    :is="iconComponent" 
    :size="size" 
    :color="color"
    :stroke-width="strokeWidth"
    class="nubi-icon"
  />
</template>

<script setup lang="ts">
/**
 * Componente wrapper para iconos
 * 
 * Según ADR-018 y SPRINT-018:
 * - Usa @lucide/vue con importación selectiva (solo los 35 iconos usados)
 * - Permite iconos custom almacenados en src/assets/icons/custom/
 * - Iconos custom cargados bajo demanda (lazy) con defineAsyncComponent
 * - Busca primero en custom, luego en Lucide como fallback
 * 
 * Uso:
 * <NubiIcon name="home" :size="24" color="currentColor" />
 * <NubiIcon name="reading" /> // icono custom
 */

import { computed, defineAsyncComponent, type Component } from 'vue'
import {
  Menu,
  X,
  LogOut,
  Lock,
  AlertCircle,
  HelpCircle,
  Settings,
  ArrowLeft,
  Construction,
  Plus,
  Edit,
  ChevronRight,
  ChevronLeft,
  Loader,
  Check,
  Minus,
  CheckCircle,
  Users,
  Clock,
  ChevronDown,
  Sun,
  Moon,
  AlertTriangle,
  Info,
  XCircle,
  MessageCircle,
  FileText,
  BookOpen,
  Wind,
  Book,
  User,
  Shield,
  Bell,
  Search,
  Home,
} from '@lucide/vue'

interface Props {
  /** Nombre del icono (de Lucide o custom) */
  name: string
  /** Tamaño en píxeles */
  size?: number
  /** Color del icono */
  color?: string
  /** Grosor del trazo */
  strokeWidth?: number
}

const props = withDefaults(defineProps<Props>(), {
  size: 24,
  color: 'currentColor',
  strokeWidth: 2,
})

/**
 * Mapa explícito de iconos Lucide usados en la aplicación
 * Solo se importan los 35 iconos identificados en el rastreo del codebase
 */
const lucideIconMap: Record<string, Component> = {
  'menu': Menu,
  'x': X,
  'log-out': LogOut,
  'lock': Lock,
  'alert-circle': AlertCircle,
  'help-circle': HelpCircle,
  'settings': Settings,
  'arrow-left': ArrowLeft,
  'construction': Construction,
  'plus': Plus,
  'edit': Edit,
  'chevron-right': ChevronRight,
  'chevron-left': ChevronLeft,
  'loader': Loader,
  'check': Check,
  'minus': Minus,
  'check-circle': CheckCircle,
  'users': Users,
  'clock': Clock,
  'chevron-down': ChevronDown,
  'sun': Sun,
  'moon': Moon,
  'alert-triangle': AlertTriangle,
  'info': Info,
  'x-circle': XCircle,
  'message-circle': MessageCircle,
  'file-text': FileText,
  'book-open': BookOpen,
  'wind': Wind,
  'book': Book,
  'user': User,
  'shield': Shield,
  'bell': Bell,
  'search': Search,
  'home': Home,
}

/**
 * Importa dinámicamente iconos custom como componentes Vue bajo demanda
 * Los archivos SVG en src/assets/icons/custom/ se cargan como componentes lazy
 */
const customIconsModules = import.meta.glob('../../assets/icons/custom/*.svg', { 
  eager: false,
  query: '?component'
})

/**
 * Busca el componente de icono
 * Primero en iconos custom (lazy), luego en Lucide
 */
const iconComponent = computed(() => {
  const customPath = `../../assets/icons/custom/${props.name}.svg`
  if (customIconsModules[customPath]) {
    return defineAsyncComponent(customIconsModules[customPath] as () => Promise<any>)
  }
  
  const lucideIcon = lucideIconMap[props.name]
  if (lucideIcon) {
    return lucideIcon
  }
  
  console.warn(`Icon "${props.name}" not found in custom icons or Lucide`)
  return HelpCircle
})
</script>

<style scoped>
.nubi-icon {
  display: inline-block;
  vertical-align: middle;
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}
</style>
