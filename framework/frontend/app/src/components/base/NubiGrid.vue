<template>
  <div :class="['nubi-grid', `nubi-grid--cols-${cols}`]">
    <div
      v-for="(item, index) in items"
      :key="index"
      class="nubi-grid__item"
    >
      <slot name="item" :item="item" :index="index">
        {{ item }}
      </slot>
    </div>
    
    <div v-if="items.length === 0" class="nubi-grid__empty">
      <slot name="empty">
        {{ emptyText }}
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiGrid - Cuadrícula responsive configurable
 * 
 * Características:
 * - 1 columna en móvil (< 768px)
 * - 2 columnas en tablet (768px - 1024px)
 * - 3 columnas en desktop (> 1024px)
 * - Columnas configurables
 * - Slot personalizado por item
 * - Estado vacío personalizable
 */

import { useI18n } from 'vue-i18n'

interface Props {
  /** Elementos de la grid */
  items?: any[]
  /** Número de columnas en desktop */
  cols?: 1 | 2 | 3 | 4
  /** Texto cuando no hay elementos */
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  cols: 3,
  emptyText: ''
})

const { t } = useI18n()

const emptyText = props.emptyText || t('components.grid.empty')
</script>

<style scoped>
.nubi-grid {
  display: grid;
  gap: var(--nubi-spacing-md);
  grid-template-columns: 1fr;
}

@media (min-width: 768px) {
  .nubi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .nubi-grid--cols-1 {
    grid-template-columns: 1fr;
  }
  
  .nubi-grid--cols-2 {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .nubi-grid--cols-3 {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .nubi-grid--cols-4 {
    grid-template-columns: repeat(4, 1fr);
  }
}

.nubi-grid__item {
  min-width: 0;
}

.nubi-grid__empty {
  grid-column: 1 / -1;
  padding: var(--nubi-spacing-xl);
  text-align: center;
  color: var(--nubi-text-tertiary);
  font-size: var(--nubi-font-size-sm);
}
</style>
