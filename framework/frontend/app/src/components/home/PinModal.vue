<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import { useSessionStore } from '@/stores/useSessionStore'
import * as authService from '@/services/authService'
import Modal from '@/components/ui/Modal.vue'
import Button from '@/components/ui/Button.vue'

const { t } = useI18n()
const familyStore = useFamilyStore()
const sessionStore = useSessionStore()

const pin = ref('')
const errorMsg = ref<string | null>(null)
const submitting = ref(false)

const TITLE_ID = 'pin-title'

const numericPin = computed({
  get: () => pin.value,
  set: (value: string) => {
    pin.value = value.replace(/\D/g, '').slice(0, 6)
  }
})

function blockNonDigit(event: KeyboardEvent) {
  if (event.ctrlKey || event.metaKey || event.altKey) return
  if (event.key.length === 1 && !/\d/.test(event.key)) {
    event.preventDefault()
  }
}

async function handleSubmit() {
  errorMsg.value = null
  submitting.value = true
  try {
    const response = await authService.login(pin.value)
    sessionStore.token = response.token
    sessionStore.familyId = response.familyId
    sessionStore.isAuthenticated = true
    familyStore.setActiveModal(null)
  } catch (error: unknown) {
    const status = (error as { response?: { status?: number } })?.response?.status
    if (status === 401) {
      errorMsg.value = t('modal.pin.error401')
    } else {
      errorMsg.value = (error as Error).message ?? 'Error'
    }
  } finally {
    submitting.value = false
  }
}

function handleClose() {
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
      <div class="form-field">
        <label for="pin-input" class="sr-only">{{ t('modal.pin.placeholder') }}</label>
        <input
          id="pin-input"
          v-model="numericPin"
          type="text"
          inputmode="numeric"
          pattern="[0-9]*"
          maxlength="6"
          autocomplete="one-time-code"
          :placeholder="t('modal.pin.placeholder')"
          required
          class="form-input"
          @keydown="blockNonDigit"
        />
      </div>

      <p v-if="errorMsg" class="form-error" role="alert">{{ errorMsg }}</p>

      <Button type="submit" :disabled="submitting || !pin">
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
  gap: var(--space-md);
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-input {
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-family: var(--font-family-base);
  outline: none;
  transition: border-color var(--transition-base);
}

.form-input:focus {
  border-color: var(--color-primary);
}

.form-error {
  font-size: var(--font-size-sm);
  color: #ef4444;
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
