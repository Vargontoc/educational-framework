<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import Modal from '@/components/ui/Modal.vue'

const { t } = useI18n()

export type ConfirmType = 'block' | 'unblock' | 'delete' | 'close'

const props = withDefaults(defineProps<{
  open: boolean
  type: ConfirmType
  title: string
  message: string
  confirmLabel?: string
  loading?: boolean
}>(), {
  loading: false
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const TITLE_ID = 'confirm-modal-title'

const buttonClass = {
  block: 'confirm-btn confirm-btn--primary',
  unblock: 'confirm-btn confirm-btn--primary',
  delete: 'confirm-btn confirm-btn--destructive',
  close: 'confirm-btn confirm-btn--primary'
}

function handleConfirm() {
  if (!props.loading) emit('confirm')
}

function handleCancel() {
  if (!props.loading) emit('cancel')
}
</script>

<template>
  <Modal
    :open="open"
    :title-id="TITLE_ID"
    @close="handleCancel"
  >
    <div class="confirm-modal">
      <div class="confirm-icon" :class="`confirm-icon--${type}`" aria-hidden="true">
        <svg v-if="type === 'delete'" width="32" height="32" viewBox="0 0 32 32" fill="none">
          <path d="M6 8h20M12 8V6a2 2 0 012-2h4a2 2 0 012 2v2M13 14v10M19 14v10M8 8l1 16a2 2 0 002 2h10a2 2 0 002-2l1-16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <svg v-else-if="type === 'block'" width="32" height="32" viewBox="0 0 32 32" fill="none">
          <circle cx="16" cy="16" r="12" stroke="currentColor" stroke-width="2"/>
          <path d="M10 10l12 12M22 10L10 22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <svg v-else-if="type === 'unblock'" width="32" height="32" viewBox="0 0 32 32" fill="none">
          <circle cx="16" cy="16" r="12" stroke="currentColor" stroke-width="2"/>
          <path d="M10 16h12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <svg v-else-if="type === 'close'" width="32" height="32" viewBox="0 0 32 32" fill="none">
          <path d="M8 24L16 8l8 16H8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M12 20h8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>

      <h2 :id="TITLE_ID" class="confirm-title">{{ title }}</h2>
      <p class="confirm-message">{{ message }}</p>

      <div class="confirm-actions">
        <button
          type="button"
          class="cancel-btn"
          :disabled="loading"
          @click="handleCancel"
        >
          {{ t('panel.children.confirm.cancel') }}
        </button>
        <button
          type="button"
          :class="buttonClass[type]"
          :disabled="loading"
          @click="handleConfirm"
        >
          {{ loading ? t('panel.children.confirm.loading') : (confirmLabel || t('panel.children.confirm.confirm')) }}
        </button>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.confirm-modal {
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

.confirm-icon--block,
.confirm-icon--unblock,
.confirm-icon--close {
  background-color: color-mix(in srgb, var(--color-primary) 10%, transparent);
  color: var(--color-primary);
}

.confirm-icon--delete {
  background-color: color-mix(in srgb, #e53935 10%, transparent);
  color: #e53935;
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
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base), transform var(--transition-base);
}

.confirm-btn--primary {
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  box-shadow: 0 4px 0 var(--color-primary-dark), 0 18px 28px rgba(43, 91, 224, 0.22);
}

.confirm-btn--primary:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.confirm-btn--primary:active:not(:disabled) {
  transform: translateY(2px);
  box-shadow: 0 2px 0 var(--color-primary-dark);
}

.confirm-btn--destructive {
  background-color: #e53935;
  color: white;
  box-shadow: 0 4px 0 #b71c1c, 0 18px 28px rgba(229, 57, 53, 0.22);
}

.confirm-btn--destructive:hover:not(:disabled) {
  background-color: #b71c1c;
}

.confirm-btn--destructive:active:not(:disabled) {
  transform: translateY(2px);
  box-shadow: 0 2px 0 #b71c1c;
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}
</style>