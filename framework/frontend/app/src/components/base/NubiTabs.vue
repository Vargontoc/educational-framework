<template>
  <div class="nubi-tabs">
    <div
      class="nubi-tabs__list"
      role="tablist"
      :aria-label="ariaLabel"
      @keydown="handleKeydown"
    >
      <button
        v-for="(tab, index) in tabs"
        :id="`tab-${uniqueId}-${index}`"
        :key="tab.value"
        role="tab"
        :class="[
          'nubi-tabs__tab',
          { 'nubi-tabs__tab--active': modelValue === tab.value }
        ]"
        :aria-selected="modelValue === tab.value"
        :aria-controls="`tabpanel-${uniqueId}-${index}`"
        :tabindex="modelValue === tab.value ? 0 : -1"
        @click="selectTab(tab.value)"
      >
        <NubiIcon v-if="tab.icon" :name="tab.icon" :size="18" class="nubi-tabs__tab-icon" />
        <span class="nubi-tabs__tab-label">{{ tab.label }}</span>
      </button>
    </div>
    
    <div
      v-for="(tab, index) in tabs"
      :id="`tabpanel-${uniqueId}-${index}`"
      :key="tab.value"
      role="tabpanel"
      :aria-labelledby="`tab-${uniqueId}-${index}`"
      :hidden="modelValue !== tab.value"
      class="nubi-tabs__panel"
    >
      <slot :name="tab.value" />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiTabs - Navegación entre subsecciones
 * 
 * Características:
 * - Indicador visual de tab activa
 * - Accesible por teclado (flechas, Home, End)
 * - Soporte iconos por tab
 * - ARIA pattern completo (role="tablist", "tab", "tabpanel")
 * - Objetivo táctil mínimo 48x48dp
 */

import { useId } from 'vue'
import NubiIcon from './NubiIcon.vue'

export interface TabItem {
  value: string
  label: string
  icon?: string
}

interface Props {
  /** Tabs disponibles */
  tabs: TabItem[]
  /** Tab activa (v-model) */
  modelValue: string
  /** Aria-label para el tablist */
  ariaLabel?: string
}

const props = withDefaults(defineProps<Props>(), {
  ariaLabel: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const uniqueId = useId()

function selectTab(value: string) {
  emit('update:modelValue', value)
}

function handleKeydown(event: KeyboardEvent) {
  const currentIndex = props.tabs.findIndex(tab => tab.value === props.modelValue)
  let newIndex = currentIndex
  
  switch (event.key) {
    case 'ArrowRight':
      event.preventDefault()
      newIndex = (currentIndex + 1) % props.tabs.length
      break
    case 'ArrowLeft':
      event.preventDefault()
      newIndex = (currentIndex - 1 + props.tabs.length) % props.tabs.length
      break
    case 'Home':
      event.preventDefault()
      newIndex = 0
      break
    case 'End':
      event.preventDefault()
      newIndex = props.tabs.length - 1
      break
    default:
      return
  }
  
  const newTab = props.tabs[newIndex]
  selectTab(newTab.value)
  
  // Focus the new tab button
  const tabButton = document.getElementById(`tab-${uniqueId}-${newIndex}`)
  tabButton?.focus()
}
</script>

<style scoped>
.nubi-tabs {
  display: flex;
  flex-direction: column;
}

.nubi-tabs__list {
  display: flex;
  border-bottom: var(--nubi-border-width) solid var(--nubi-border-default);
  gap: var(--nubi-spacing-xs);
  overflow-x: auto;
  scrollbar-width: none;
}

.nubi-tabs__list::-webkit-scrollbar {
  display: none;
}

.nubi-tabs__tab {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  min-height: 48px;
  border: none;
  background: none;
  cursor: pointer;
  font-family: var(--nubi-font-family-base);
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-secondary);
  white-space: nowrap;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-tabs__tab:hover:not(.nubi-tabs__tab--active) {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-tabs__tab--active {
  color: var(--nubi-color-primary);
  border-bottom-color: var(--nubi-color-primary);
}

.nubi-tabs__tab:focus-visible {
  outline: none;
  box-shadow: inset 0 0 0 2px var(--nubi-color-focus);
  border-radius: var(--nubi-radius-sm) var(--nubi-radius-sm) 0 0;
}

.nubi-tabs__tab-icon {
  flex-shrink: 0;
}

.nubi-tabs__tab-label {
  line-height: 1;
}

.nubi-tabs__panel {
  padding: var(--nubi-spacing-md) 0;
}

.nubi-tabs__panel[hidden] {
  display: none;
}
</style>
