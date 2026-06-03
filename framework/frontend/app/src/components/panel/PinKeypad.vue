<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const PIN_LENGTH = 4

defineProps<{
  pin: string
  error?: boolean
}>()

const emit = defineEmits<{
  digit: [digit: string]
  delete: []
  clear: []
}>()

const KEYBOARD = ['1', '2', '3', '4', '5', '6', '7', '8', '9', 'clear', '0', 'delete']

function handleKey(key: string) {
  if (key === 'clear') {
    emit('clear')
  } else if (key === 'delete') {
    emit('delete')
  } else {
    emit('digit', key)
  }
}

function getKeyLabel(key: string, index: number): string {
  if (key === 'clear') return t('panel.settings.pin.clearAriaLabel')
  if (key === 'delete') return t('panel.settings.pin.deleteAriaLabel')
  return t('panel.settings.pin.digitAria', { index: parseInt(key) })
}
</script>

<template>
  <div class="pin-keypad" role="group" :aria-label="t('panel.settings.pin.keypadAriaLabel')">
    <button
      v-for="(key, i) in KEYBOARD"
      :key="i"
      type="button"
      class="keypad-btn"
      :class="{ 'keypad-btn--action': key === 'clear' || key === 'delete', 'keypad-btn--error': error && key !== 'clear' && key !== 'delete' }"
      :aria-label="getKeyLabel(key, i)"
      @click="handleKey(key)"
    >
      <span v-if="key === 'clear'" class="keypad-btn__text" aria-hidden="true">
        {{ t('panel.settings.pin.clearAriaLabel').split(' ')[0] }}
      </span>
      <span v-else-if="key === 'delete'" class="keypad-btn__text" aria-hidden="true">⌫</span>
      <span v-else>{{ key }}</span>
    </button>
  </div>
</template>

<style scoped>
.pin-keypad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-sm);
  width: 100%;
  max-width: 280px;
}

.keypad-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 56px;
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  background-color: var(--color-surface);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: border-color var(--transition-base), background-color var(--transition-base), transform var(--transition-base);
}

.keypad-btn:hover {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 5%, transparent);
}

.keypad-btn:active {
  transform: scale(0.97);
}

.keypad-btn--action {
  background-color: var(--color-neutral);
  color: var(--color-text-secondary);
  font-size: var(--font-size-button);
}

.keypad-btn--action:hover {
  background-color: color-mix(in srgb, var(--color-neutral) 80%, black);
  border-color: var(--color-neutral);
}

.keypad-btn--error {
  border-color: var(--color-error-adult);
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-6px); }
  40% { transform: translateX(6px); }
  60% { transform: translateX(-4px); }
  80% { transform: translateX(4px); }
}

.keypad-btn__text {
  text-transform: uppercase;
  font-size: var(--font-size-caption);
}
</style>