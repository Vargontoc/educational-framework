<template>
  <div :class="['nubi-list', { 'nubi-list--bordered': bordered }]">
    <div
      v-for="(item, index) in items"
      :key="index"
      :class="['nubi-list__item', { 'nubi-list__item--clickable': clickable }]"
      @click="handleClick(item, index)"
    >
      <slot name="item" :item="item" :index="index">
        <div class="nubi-list__item-content">
          {{ item }}
        </div>
      </slot>
    </div>
    
    <div v-if="items.length === 0" class="nubi-list__empty">
      <slot name="empty">
        {{ emptyText }}
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiList - Lista de elementos apilados verticalmente
 * 
 * Características:
 * - Elementos con separadores sutiles
 * - Slot personalizado por item
 * - Variante bordered con borde exterior
 * - Items clickeables opcionales
 * - Estado vacío personalizable
 */

import { useI18n } from 'vue-i18n'

interface Props {
  /** Elementos de la lista */
  items?: any[]
  /** Mostrar borde exterior */
  bordered?: boolean
  /** Si los items son clickeables */
  clickable?: boolean
  /** Texto cuando no hay elementos */
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  bordered: false,
  clickable: false,
  emptyText: ''
})

const emit = defineEmits<{
  'item-click': [item: any, index: number]
}>()

const { t } = useI18n()

const emptyText = props.emptyText || t('components.list.empty')

function handleClick(item: any, index: number) {
  if (props.clickable) {
    emit('item-click', item, index)
  }
}
</script>

<style scoped>
.nubi-list {
  display: flex;
  flex-direction: column;
  background-color: var(--nubi-bg-surface);
}

.nubi-list--bordered {
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  overflow: hidden;
}

.nubi-list__item {
  padding: var(--nubi-spacing-md);
  border-bottom: 1px solid var(--nubi-border-default);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-list__item:last-child {
  border-bottom: none;
}

.nubi-list__item--clickable {
  cursor: pointer;
}

.nubi-list__item--clickable:hover {
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-list__item--clickable:focus-visible {
  outline: none;
  background-color: var(--nubi-bg-surface-secondary);
  box-shadow: inset 0 0 0 2px var(--nubi-color-focus);
}

.nubi-list__item-content {
  color: var(--nubi-text-primary);
  font-size: var(--nubi-font-size-base);
  line-height: var(--nubi-line-height-normal);
}

.nubi-list__empty {
  padding: var(--nubi-spacing-xl);
  text-align: center;
  color: var(--nubi-text-tertiary);
  font-size: var(--nubi-font-size-sm);
}
</style>
