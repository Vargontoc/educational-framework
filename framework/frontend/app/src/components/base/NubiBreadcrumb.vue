<template>
  <nav :aria-label="t('components.breadcrumb.label')" class="nubi-breadcrumb">
    <ol class="nubi-breadcrumb__list">
      <li
        v-for="(item, index) in items"
        :key="index"
        class="nubi-breadcrumb__item"
      >
        <span v-if="index > 0" class="nubi-breadcrumb__separator" aria-hidden="true">
          <NubiIcon name="chevron-right" :size="14" />
        </span>
        
        <a
          v-if="item.to && index < items.length - 1"
          :href="item.to"
          class="nubi-breadcrumb__link"
          @click.prevent="handleClick(item)"
        >
          {{ item.label }}
        </a>
        
        <span
          v-else
          class="nubi-breadcrumb__current"
          :aria-current="index === items.length - 1 ? 'page' : undefined"
        >
          {{ item.label }}
        </span>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
/**
 * NubiBreadcrumb - Migas de pan para navegación
 * 
 * Características:
 * - Muestra ruta de navegación
 * - Enlaces a niveles anteriores clicables
 * - Último elemento marcado como página actual
 * - Accesibilidad con aria-label y aria-current
 * - Integración con Vue Router
 */

import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import NubiIcon from './NubiIcon.vue'

export interface BreadcrumbItem {
  label: string
  to?: string
}

interface Props {
  /** Items de la ruta de navegación */
  items: BreadcrumbItem[]
}

defineProps<Props>()

const emit = defineEmits<{
  navigate: [item: BreadcrumbItem]
}>()

const { t } = useI18n()
const router = useRouter()

function handleClick(item: BreadcrumbItem) {
  emit('navigate', item)
  
  if (item.to) {
    router.push(item.to)
  }
}
</script>

<style scoped>
.nubi-breadcrumb {
  display: flex;
  align-items: center;
}

.nubi-breadcrumb__list {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  list-style: none;
  margin: 0;
  padding: 0;
  flex-wrap: wrap;
}

.nubi-breadcrumb__item {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
}

.nubi-breadcrumb__separator {
  display: flex;
  align-items: center;
  color: var(--nubi-text-tertiary);
}

.nubi-breadcrumb__link {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-link);
  text-decoration: none;
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-xs);
  border-radius: var(--nubi-radius-sm);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  min-height: 32px;
  display: flex;
  align-items: center;
}

.nubi-breadcrumb__link:hover {
  color: var(--nubi-text-link-hover);
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-breadcrumb__link:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

.nubi-breadcrumb__current {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  font-weight: var(--nubi-font-weight-medium);
  padding: var(--nubi-spacing-xs) var(--nubi-spacing-xs);
  min-height: 32px;
  display: flex;
  align-items: center;
}
</style>
