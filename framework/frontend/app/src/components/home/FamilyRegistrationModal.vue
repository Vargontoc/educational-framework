<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import Modal from '@/components/ui/Modal.vue'
import Button from '@/components/ui/Button.vue'

const { t } = useI18n()
const familyStore = useFamilyStore()

const name = ref('')
const pin = ref('')
const fieldErrors = ref<Record<string, string>>({})
const submitting = ref(false)

const TITLE_ID = 'family-registration-title'

async function handleSubmit() {
  fieldErrors.value = {}
  submitting.value = true
  try {
    await familyStore.registerFamily({
      name: name.value,
      pin: pin.value,
      ttsEnabled: true,
      agentEnabled: true
    })
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number; data?: { errors?: string[] } } }
    if (axiosError.response?.status === 409) {
      await familyStore.fetchFamily()
      familyStore.setActiveModal(null)
      return
    }
    if (axiosError.response?.status === 400 && axiosError.response.data?.errors) {
      for (const msg of axiosError.response.data.errors) {
        const match = msg.match(/^(\w+):\s*(.+)$/)
        if (match) {
          fieldErrors.value[match[1]] = match[2]
        }
      }
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
    :open="familyStore.activeModal === 'familyRegistration'"
    :title-id="TITLE_ID"
    @close="handleClose"
  >
    <h2 :id="TITLE_ID" class="modal-title">{{ t('modal.registerFamily.title') }}</h2>

    <form class="modal-form" @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="family-name" class="sr-only">{{ t('modal.registerFamily.namePlaceholder') }}</label>
        <input
          id="family-name"
          v-model="name"
          type="text"
          :placeholder="t('modal.registerFamily.namePlaceholder')"
          required
          class="form-input"
        />
        <span v-if="fieldErrors.name" class="form-error">{{ fieldErrors.name }}</span>
      </div>

      <div class="form-field">
        <label for="family-pin" class="sr-only">{{ t('modal.registerFamily.pinPlaceholder') }}</label>
        <input
          id="family-pin"
          v-model="pin"
          type="password"
          :placeholder="t('modal.registerFamily.pinPlaceholder')"
          required
          class="form-input"
        />
        <span v-if="fieldErrors.pin" class="form-error">{{ fieldErrors.pin }}</span>
      </div>

      <Button type="submit" :disabled="submitting || !name || !pin">
        {{ t('modal.registerFamily.submit') }}
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
