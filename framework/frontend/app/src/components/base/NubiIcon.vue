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
 * Según ADR-018:
 * - Usa @lucide/vue como librería base
 * - Permite iconos custom almacenados en src/assets/icons/custom/
 * - Busca primero en custom, luego en Lucide como fallback
 * 
 * Uso:
 * <NubiIcon name="home" :size="24" color="currentColor" />
 * <NubiIcon name="reading" /> // icono custom
 */

import { computed, defineAsyncComponent } from 'vue'
import * as lucideIcons from '@lucide/vue'

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
 * Importa dinámicamente todos los iconos custom como componentes Vue
 * Los archivos SVG en src/assets/icons/custom/ se cargan como componentes
 */
const customIconsModules = import.meta.glob('../../assets/icons/custom/*.svg', { 
  eager: true,
  query: '?component'
})

/**
 * Mapa de iconos custom: nombre del archivo -> componente Vue
 */
const customIcons: Record<string, any> = {}
Object.entries(customIconsModules).forEach(([path, module]) => {
  const iconName = path.split('/').pop()?.replace('.svg', '') || ''
  customIcons[iconName] = module
})

/**
 * Busca el componente de icono
 * Primero en iconos custom, luego en Lucide
 */
const iconComponent = computed(() => {
  // 1. Buscar primero en iconos custom
  if (customIcons[props.name]) {
    return customIcons[props.name]
  }
  
  // 2. Convertir nombre a PascalCase para buscar en Lucide
  const pascalName = props.name
    .split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join('')
  
  // 3. Buscar en Lucide
  const lucideIcon = (lucideIcons as any)[pascalName]
  if (lucideIcon) {
    return lucideIcon
  }
  
  // 4. Fallback: icono de pregunta si no se encuentra
  console.warn(`Icon "${props.name}" not found in custom icons or Lucide`)
  return lucideIcons.HelpCircle
})
</script>

<style scoped>
.nubi-icon {
  display: inline-block;
  vertical-align: middle;
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}
</style>
