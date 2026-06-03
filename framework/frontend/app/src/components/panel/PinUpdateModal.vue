<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/useSessionStore'
import { useFamilyStore } from '@/stores/useFamilyStore'
import * as familyService from '@/services/familyService'
import Modal from '@/components/ui/Modal.vue'
import PinKeypad from './PinKeypad.vue'

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()
const familyStore = useFamilyStore()

const PIN_LENGTH = 4

const step = ref<'new' | 'confirm'>('new')
const newPin = ref('')
const confirmPin = ref('')
const errorMsg = ref<string | null>(null)
const submitting = ref(false)

const TITLE_ID = 'pin-update-title'

const pinDots = computed(() => {
  return Array.from({ length: PIN_LENGTH }, (_, i) => {
    if (step.value === 'new') {
      return i < newPin.value.length
    }
    return i < confirmPin.value.length
  })
})

const activePin = computed(() => step.value === 'new' ? newPin.value : confirmPin.value)

const showMismatchError = ref(false)
const shakeDots = ref(false)

watch(step, () => {
  errorMsg.value = null
  showMismatchError.value = false
})

function handleDigit(digit: string) {
  errorMsg.value = null
  if (step.value === 'new') {
    if (newPin.value.length < PIN_LENGTH) {
      newPin.value += digit
      if (newPin.value.length === PIN_LENGTH) {
        step.value = 'confirm'
      }
    }
  } else {
    if (confirmPin.value.length < PIN_LENGTH) {
      confirmPin.value += digit
      if (confirmPin.value.length === PIN_LENGTH) {
        validateAndSubmit()
      }
    }
  }
}

function handleDelete() {
  if (step.value === 'new') {
    newPin.value = newPin.value.slice(0, -1)
  } else {
    confirmPin.value = confirmPin.value.slice(0, -1)
  }
  errorMsg.value = null
}

function handleClear() {
  newPin.value = ''
  confirmPin.value = ''
  step.value = 'new'
  errorMsg.value = null
  showMismatchError.value = false
  shakeDots.value = false
}

function validateAndSubmit() {
  if (confirmPin.value !== newPin.value) {
    showMismatchError.value = true
    shakeDots.value = true
    errorMsg.value = t('panel.settings.pin.mismatchError')
    setTimeout(() => {
      confirmPin.value = ''
      shakeDots.value = false
    }, 400)
    return
  }
  submitNewPin()
}

async function submitNewPin() {
  if (submitting.value) return
  submitting.value = true
  errorMsg.value = null

  try {
    await familyService.updateFamily({
      name: familyStore.family?.name ?? '',
      pin: newPin.value,
      ttsEnabled: familyStore.family?.ttsEnabled ?? true,
      agentEnabled: familyStore.family?.agentEnabled ?? true
    })

    await sessionStore.logout()
    router.replace('/')
  } catch {
    errorMsg.value = t('panel.settings.pin.updateError')
    submitting.value = false
  }
}

function handleClose() {
  if (submitting.value) return
  handleClear()
  emit('close')
}

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <Modal :open="true" :title-id="TITLE_ID" @close="handleClose">
    <div class="pin-update-modal">
      <h2 :id="TITLE_ID" class="modal-title">
        {{ step === 'new' ? t('panel.settings.pin.newPinLabel') : t('panel.settings.pin.confirmPinLabel') }}
      </h2>

      <div class="pin-display" :class="{ 'pin-display--error': showMismatchError || shakeDots }" aria-hidden="true">
        <span
          v-for="(filled, i) in pinDots"
          :key="i"
          class="pin-dot"
          :class="{
            'pin-dot--filled': filled,
            'pin-dot--error': showMismatchError || shakeDots
          }"
        >
          <span v-if="filled" class="pin-dot__dot" aria-hidden="true" />
        </span>
      </div>

      <p v-if="errorMsg" class="form-error" role="alert">{{ errorMsg }}</p>
      <p v-else-if="step === 'confirm'" class="form-hint">{{ t('panel.settings.pin.mismatchHint') }}</p>

      <PinKeypad
        :pin="activePin"
        :error="showMismatchError || shakeDots"
        @digit="handleDigit"
        @delete="handleDelete"
        @clear="handleClear"
      />

      <div class="modal-actions">
        <button
          type="button"
          class="cancel-btn"
          :disabled="submitting"
          @click="handleClose"
        >
          {{ t('panel.settings.pin.cancel') }}
        </button>
        <button
          v-if="step === 'confirm'"
          type="button"
          class="back-btn"
          :disabled="submitting"
          @click="step = 'new'; confirmPin = ''"
        >
          {{ t('modal.registerFamily.back') }}
        </button>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.pin-update-modal {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg) var(--space-md);
}

.modal-title {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.pin-display {
  display: flex;
  gap: var(--space-sm);
  margin-bottom: var(--space-xs);
}

.pin-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.pin-dot--filled {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.pin-dot--error {
  border-color: var(--color-error-adult);
}

.pin-dot__dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: var(--color-primary);
}

.pin-display--error .pin-dot {
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-6px); }
  40% { transform: translateX(6px); }
  60% { transform: translateX(-4px); }
  80% { transform: translateX(4px); }
}

.form-error {
  font-size: var(--font-size-sm);
  color: var(--color-error-adult);
  margin: 0;
  text-align: center;
}

.form-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin: 0;
  text-align: center;
}

.modal-actions {
  display: flex;
  gap: var(--space-sm);
  width: 100%;
  max-width: 280px;
  margin-top: var(--space-sm);
}

.cancel-btn,
.back-btn {
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

.cancel-btn:hover:not(:disabled),
.back-btn:hover:not(:disabled) {
  border-color: var(--color-text-secondary);
  color: var(--color-text-primary);
}

.cancel-btn:disabled,
.back-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>