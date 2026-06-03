<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/ui/Modal.vue'

const { t } = useI18n()

export type ToggleType = 'tts' | 'agent' | 'pin'

const props = withDefaults(defineProps<{
  open: boolean
  type: ToggleType
  currentValue: boolean
  loading?: boolean
}>(), {
  loading: false
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const modalTitle = computed(() => {
  if (props.type === 'tts') {
    return props.currentValue
      ? t('panel.settings.tts.confirmDisableTitle')
      : t('panel.settings.tts.confirmEnableTitle')
  }
  if (props.type === 'agent') {
    return props.currentValue
      ? t('panel.settings.agent.confirmDisableTitle')
      : t('panel.settings.agent.confirmEnableTitle')
  }
  return ''
})

const modalMessage = computed(() => {
  if (props.type === 'tts') {
    return props.currentValue
      ? t('panel.settings.tts.confirmDisableMessage')
      : t('panel.settings.tts.confirmEnableMessage')
  }
  if (props.type === 'agent') {
    return props.currentValue
      ? t('panel.settings.agent.confirmDisableMessage')
      : t('panel.settings.agent.confirmEnableMessage')
  }
  return ''
})

function handleConfirm() {
  if (!props.loading) emit('confirm')
}

function handleCancel() {
  if (!props.loading) emit('cancel')
}
</script>

<template>
  <Modal :open="open" @close="handleCancel">
    <div class="toggle-confirm-modal">
      <div class="confirm-icon" :class="`confirm-icon--${type}`" aria-hidden="true">
        <svg v-if="type === 'tts'" width="32" height="32" viewBox="0 0 32 32" fill="none">
          <path d="M6 16a8 8 0 010 0" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M10 12a4 4 0 000 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M14 9a2 2 0 000 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M19 6a1 1 0 000 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <svg v-else-if="type === 'agent'" width="32" height="32" viewBox="0 0 32 32" fill="none">
          <circle cx="16" cy="14" r="6" stroke="currentColor" stroke-width="2"/>
          <path d="M8 26c0-4.42 3.58-8 8-8s8 3.58 8 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>

      <h2 class="confirm-title">{{ modalTitle }}</h2>
      <p class="confirm-message">{{ modalMessage }}</p>

      <div class="confirm-actions">
        <button
          type="button"
          class="cancel-btn"
          :disabled="loading"
          @click="handleCancel"
        >
          {{ type === 'tts' ? t('panel.settings.tts.cancel') : t('panel.settings.agent.cancel') }}
        </button>
        <button
          type="button"
          class="confirm-btn"
          :disabled="loading"
          @click="handleConfirm"
        >
          {{ loading
            ? (type === 'tts' ? t('panel.settings.tts.updating') : t('panel.settings.agent.updating'))
            : (type === 'tts' ? t('panel.settings.tts.confirm') : t('panel.settings.agent.confirm'))
          }}
        </button>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.toggle-confirm-modal {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg) var(--space-md);
  text-align: center;
}

.confirm-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
}

.confirm-icon--tts {
  background-color: color-mix(in srgb, var(--color-primary) 10%, transparent);
  color: var(--color-primary);
}

.confirm-icon--agent {
  background-color: color-mix(in srgb, var(--color-celebration) 10%, transparent);
  color: var(--color-celebration-dark);
}

.confirm-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-text-primary);
}

.confirm-message {
  margin: 0;
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
  max-width: 320px;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  gap: var(--space-sm);
  width: 100%;
  max-width: 320px;
}

.cancel-btn {
  flex: 1;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  transition: border-color var(--transition-base), color var(--transition-base);
}

.cancel-btn:hover:not(:disabled) {
  border-color: var(--color-text-secondary);
  color: var(--color-text-primary);
}

.cancel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.confirm-btn {
  flex: 1;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: none;
  border-radius: var(--radius-md);
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
}

.confirm-btn:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>