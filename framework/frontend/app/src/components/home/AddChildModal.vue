<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import Modal from '@/components/ui/Modal.vue'
import Button from '@/components/ui/Button.vue'

const { t } = useI18n()
const familyStore = useFamilyStore()

const name = ref('')
const birthday = ref('')
const avatar = ref('')
const fieldErrors = ref<Record<string, string>>({})
const submitting = ref(false)

const TITLE_ID = 'add-child-title'

async function handleSubmit() {
  fieldErrors.value = {}
  submitting.value = true
  try {
    await familyStore.addChild({
      name: name.value,
      birthday: birthday.value,
      avatar: avatar.value || null,
      ttsEnabled: true,
      agentEnabled: true
    })
    name.value = ''
    birthday.value = ''
    avatar.value = ''
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number; data?: { errors?: string[] } } }
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
    :open="familyStore.activeModal === 'addChild'"
    :title-id="TITLE_ID"
    @close="handleClose"
  >
    <h2 :id="TITLE_ID" class="modal-title">{{ t('modal.addChild.title') }}</h2>

    <form class="modal-form" @submit.prevent="handleSubmit">
      <div class="form-field">
        <label for="child-name" class="sr-only">{{ t('modal.addChild.namePlaceholder') }}</label>
        <input
          id="child-name"
          v-model="name"
          type="text"
          :placeholder="t('modal.addChild.namePlaceholder')"
          required
          class="form-input"
        />
        <span v-if="fieldErrors.name" class="form-error">{{ fieldErrors.name }}</span>
      </div>

      <div class="form-field">
        <label for="child-birthday" class="sr-only">{{ t('modal.addChild.birthdayPlaceholder') }}</label>
        <input
          id="child-birthday"
          v-model="birthday"
          type="date"
          :placeholder="t('modal.addChild.birthdayPlaceholder')"
          required
          class="form-input"
        />
        <span v-if="fieldErrors.birthday" class="form-error">{{ fieldErrors.birthday }}</span>
      </div>

      <div class="form-field">
        <label for="child-avatar" class="sr-only">{{ t('modal.addChild.avatarPlaceholder') }}</label>
        <input
          id="child-avatar"
          v-model="avatar"
          type="text"
          :placeholder="t('modal.addChild.avatarPlaceholder')"
          class="form-input"
        />
      </div>

      <Button type="submit" :disabled="submitting || !name || !birthday">
        {{ t('modal.addChild.submit') }}
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
