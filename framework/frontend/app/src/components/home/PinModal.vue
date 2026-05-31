<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useFamilyStore } from '@/stores/useFamilyStore'
import { useSessionStore } from '@/stores/useSessionStore'
import * as authService from '@/services/authService'
import Modal from '@/components/ui/Modal.vue'
import Button from '@/components/ui/Button.vue'

const { t } = useI18n()
const router = useRouter()
const familyStore = useFamilyStore()
const sessionStore = useSessionStore()

const pin = ref('')
const errorMsg = ref<string | null>(null)
const submitting = ref(false)

const TITLE_ID = 'pin-title'
const PIN_LENGTH = 4

const pinDots = computed(() => {
  return Array.from({ length: PIN_LENGTH }, (_, i) => i < pin.value.length)
})

function blockNonDigit(event: KeyboardEvent) {
  if (event.ctrlKey || event.metaKey || event.altKey) return
  if (event.key.length === 1 && !/\d/.test(event.key)) {
    event.preventDefault()
  }
}

async function handleSubmit() {
  if (pin.value.length !== PIN_LENGTH) return
  errorMsg.value = null
  submitting.value = true
  try {
    const response = await authService.login(pin.value)
    sessionStore.token = response.token
    sessionStore.familyId = response.familyId
    familyStore.setActiveModal(null)
    router.replace('/panel')
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number } }
    const status = axiosError.response?.status
    if (status === 401) {
      errorMsg.value = t('modal.pin.error401')
    } else {
      errorMsg.value = t('modal.pin.errorServer')
    }
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  pin.value = ''
  errorMsg.value = null
  if (submitting.value) return
  familyStore.setActiveModal(null)
}
</script>

<template>
  <Modal
    :open="familyStore.activeModal === 'pin'"
    :title-id="TITLE_ID"
    @close="handleClose"
  >
    <h2 :id="TITLE_ID" class="modal-title">{{ t('modal.pin.title') }}</h2>

    <form class="modal-form" @submit.prevent="handleSubmit">
      <div class="pin-display" aria-hidden="true">
        <span
          v-for="(filled, i) in pinDots"
          :key="i"
          class="pin-dot"
          :class="{ 'pin-dot--filled': filled }"
        >
          <span v-if="filled" class="pin-dot__dot" aria-hidden="true" />
        </span>
      </div>

      <p v-if="errorMsg" class="form-error" role="alert">{{ errorMsg }}</p>

      <input
        id="pin-input"
        v-model="pin"
        type="hidden"
        autocomplete="one-time-code"
        @keydown="blockNonDigit"
      />

      <div class="pin-keypad" role="group" :aria-label="t('modal.registerFamily.keypadAriaLabel')">
        <button
          v-for="digit in [1, 2, 3, 4, 5, 6, 7, 8, 9]"
          :key="digit"
          type="button"
          class="keypad-btn"
          :aria-label="t('modal.registerFamily.digitAria', { digit })"
          @click="pin = (pin + digit).slice(0, PIN_LENGTH)"
        >
          {{ digit }}
        </button>
        <button
          type="button"
          class="keypad-btn keypad-btn--action"
          :aria-label="t('modal.registerFamily.clearAriaLabel')"
          @click="pin = ''"
        >
          {{ t('modal.registerFamily.clear') }}
        </button>
        <button
          type="button"
          class="keypad-btn"
          :aria-label="t('modal.registerFamily.digitAria', { digit: 0 })"
          @click="pin = (pin + '0').slice(0, PIN_LENGTH)"
        >
          0
        </button>
        <button
          type="button"
          class="keypad-btn keypad-btn--action"
          :aria-label="t('modal.registerFamily.deleteAriaLabel')"
          @click="pin = pin.slice(0, -1)"
        >
          {{ t('modal.registerFamily.delete') }}
        </button>
      </div>

      <Button
        type="submit"
        :disabled="submitting || pin.length !== PIN_LENGTH"
      >
        {{ t('modal.pin.submit') }}
      </Button>
    </form>
  </Modal>
</template>

<style scoped>
.modal-title {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 var(--space-md);
}

.modal-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
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
  width: 40px;
  height: 40px;
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.pin-dot--filled {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.pin-dot__dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: var(--color-primary);
}

.form-error {
  font-size: var(--font-size-sm);
  color: #e53935;
  margin: 0;
  text-align: center;
}

.pin-keypad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-sm);
  width: 100%;
  max-width: 260px;
}

.keypad-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 52px;
  border: none;
  border-radius: var(--radius-md);
  background-color: var(--color-neutral);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base);
}

.keypad-btn:hover {
  background-color: color-mix(in srgb, var(--color-neutral) 80%, black);
}

.keypad-btn:active {
  background-color: color-mix(in srgb, var(--color-neutral) 60%, black);
}

.keypad-btn--action {
  background-color: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.keypad-btn--action:hover {
  background-color: color-mix(in srgb, var(--color-neutral) 70%, transparent);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
