<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/ui/Modal.vue'
import AvatarPicker from '@/components/shared/AvatarPicker.vue'
import type { ChildProfileResponse, UpdateChildProfileRequest } from '@/shared/types/api'
import { updateChild, deleteChild } from '@/services/childService'

const { t } = useI18n()

const props = defineProps<{
  open: boolean
  child: ChildProfileResponse
  familyTtsEnabled: boolean
  familyAgentEnabled: boolean
}>()

const emit = defineEmits<{
  close: []
  updated: [child: ChildProfileResponse]
  deleted: [childId: number]
}>()

const TITLE_ID = 'edit-child-title'

const name = ref('')
const birthday = ref('')
const selectedAvatar = ref<string | null>(null)
const ttsEnabled = ref(true)
const agentEnabled = ref(true)
const nameError = ref('')
const birthdayError = ref('')
const serverError = ref('')
const submitting = ref(false)
const deleteConfirmOpen = ref(false)

watch(() => props.open, (isOpen) => {
  if (isOpen) {
    name.value = props.child.name
    birthday.value = props.child.birthday
    selectedAvatar.value = props.child.avatar
    ttsEnabled.value = props.child.ttsEnabled
    agentEnabled.value = props.child.agentEnabled
    nameError.value = ''
    birthdayError.value = ''
    serverError.value = ''
    submitting.value = false
  }
})

watch(() => props.child, (newChild) => {
  if (props.open && newChild) {
    name.value = newChild.name
    birthday.value = newChild.birthday
    selectedAvatar.value = newChild.avatar
    ttsEnabled.value = newChild.ttsEnabled
    agentEnabled.value = newChild.agentEnabled
  }
})

function validateName(): boolean {
  if (!name.value.trim()) {
    nameError.value = t('panel.children.editModal.nameRequired')
    return false
  }
  nameError.value = ''
  return true
}

function validateBirthday(): boolean {
  if (!birthday.value) {
    birthdayError.value = t('panel.children.editModal.birthdayRequired')
    return false
  }
  const date = new Date(birthday.value)
  if (isNaN(date.getTime())) {
    birthdayError.value = t('panel.children.editModal.birthdayInvalid')
    return false
  }
  birthdayError.value = ''
  return true
}

async function handleSubmit() {
  if (!validateName() || !validateBirthday()) return
  if (submitting.value) return

  submitting.value = true
  serverError.value = ''

  const payload: UpdateChildProfileRequest = {
    name: name.value.trim(),
    birthday: birthday.value,
    avatar: selectedAvatar.value,
    ttsEnabled: ttsEnabled.value,
    agentEnabled: agentEnabled.value
  }

  try {
    const updated = await updateChild(props.child.id, payload)
    emit('updated', updated)
    emit('close')
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number; data?: { errors?: string[]; message?: string } } }
    const status = axiosError.response?.status
    if (status === 400) {
      const errors = axiosError.response?.data?.errors
      if (errors?.length) {
        serverError.value = errors[0]
      } else {
        serverError.value = t('panel.children.editModal.errorBadRequest')
      }
    } else if (status === 404) {
      serverError.value = t('panel.children.editModal.errorNotFound')
      emit('close')
    } else {
      serverError.value = t('panel.children.editModal.errorServer')
    }
  } finally {
    submitting.value = false
  }
}

async function handleDelete() {
  if (submitting.value) return
  submitting.value = true
  serverError.value = ''
  deleteConfirmOpen.value = false

  try {
    await deleteChild(props.child.id)
    emit('deleted', props.child.id)
    emit('close')
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number } }
    const status = axiosError.response?.status
    if (status === 404) {
      emit('close')
    } else {
      serverError.value = t('panel.children.editModal.errorServer')
    }
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  if (submitting.value) return
  emit('close')
}
</script>

<template>
  <Modal
    :open="open"
    :title-id="TITLE_ID"
    @close="handleClose"
  >
    <div class="edit-modal">
      <h2 :id="TITLE_ID" class="modal-title">{{ t('panel.children.editModal.title') }}</h2>

      <div v-if="serverError" class="server-error">
        <p>{{ serverError }}</p>
      </div>

      <div class="form-field">
        <label for="child-name" class="field-label">
          {{ t('panel.children.editModal.nameLabel') }}
        </label>
        <input
          id="child-name"
          v-model="name"
          type="text"
          class="name-input"
          :class="{ 'name-input--error': nameError }"
          :placeholder="t('panel.children.editModal.namePlaceholder')"
          :disabled="submitting"
          @blur="validateName"
        />
        <p v-if="nameError" class="field-error">{{ nameError }}</p>
      </div>

      <div class="form-field">
        <label for="child-birthday" class="field-label">
          {{ t('panel.children.editModal.birthdayLabel') }}
        </label>
        <input
          id="child-birthday"
          v-model="birthday"
          type="date"
          class="date-input"
          :class="{ 'date-input--error': birthdayError }"
          :disabled="submitting"
          @blur="validateBirthday"
        />
        <p v-if="birthdayError" class="field-error">{{ birthdayError }}</p>
      </div>

      <div class="form-field">
        <span class="field-label">{{ t('panel.children.editModal.avatarLabel') }}</span>
        <AvatarPicker v-model="selectedAvatar" :disabled="submitting" />
      </div>

      <div class="toggle-field">
        <div class="toggle-row">
          <label for="tts-toggle" class="toggle-label">{{ t('panel.children.editModal.ttsLabel') }}</label>
          <button
            id="tts-toggle"
            type="button"
            class="toggle-btn"
            :class="{ 'toggle-btn--on': ttsEnabled, 'toggle-btn--disabled': !familyTtsEnabled }"
            :disabled="submitting || !familyTtsEnabled"
            :aria-pressed="ttsEnabled"
            @click="ttsEnabled = !ttsEnabled"
          >
            <span class="toggle-knob" />
          </button>
        </div>
        <p v-if="!familyTtsEnabled" class="toggle-reason">
          {{ t('panel.children.editModal.ttsDisabledReason') }}
        </p>
      </div>

      <div class="toggle-field">
        <div class="toggle-row">
          <label for="agent-toggle" class="toggle-label">{{ t('panel.children.editModal.agentLabel') }}</label>
          <button
            id="agent-toggle"
            type="button"
            class="toggle-btn"
            :class="{ 'toggle-btn--on': agentEnabled, 'toggle-btn--disabled': !familyAgentEnabled }"
            :disabled="submitting || !familyAgentEnabled"
            :aria-pressed="agentEnabled"
            @click="agentEnabled = !agentEnabled"
          >
            <span class="toggle-knob" />
          </button>
        </div>
        <p v-if="!familyAgentEnabled" class="toggle-reason">
          {{ t('panel.children.editModal.agentDisabledReason') }}
        </p>
      </div>

      <div class="action-row">
        <button
          type="button"
          class="submit-btn"
          :disabled="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? t('panel.children.editModal.submitting') : t('panel.children.editModal.submit') }}
        </button>
      </div>

      <div class="delete-row">
        <button
          type="button"
          class="delete-btn"
          :disabled="submitting"
          @click="deleteConfirmOpen = true"
        >
          {{ t('panel.children.editModal.delete') }}
        </button>
      </div>
    </div>

    <ConfirmModal
      :open="deleteConfirmOpen"
      type="delete"
      :title="t('panel.children.editModal.deleteConfirmTitle')"
      :message="t('panel.children.editModal.deleteConfirmMessage')"
      :loading="submitting"
      @confirm="handleDelete"
      @cancel="deleteConfirmOpen = false"
    />
  </Modal>
</template>

<script lang="ts">
import ConfirmModal from '@/components/shared/ConfirmModal.vue'
export default { components: { ConfirmModal } }
</script>

<style scoped>
.edit-modal {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: var(--space-lg) var(--space-md);
}

.modal-title {
  margin: 0 0 var(--space-xs);
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-text-primary);
  text-align: center;
}

.server-error {
  background-color: #fef2f2;
  border: 2px solid #e53935;
  border-radius: var(--radius-md);
  padding: var(--space-sm) var(--space-md);
  text-align: center;
}

.server-error p {
  color: #e53935;
  font-size: var(--font-size-sm);
  margin: 0;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
}

.name-input,
.date-input {
  width: 100%;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-family: var(--font-family-base);
  outline: none;
  transition: border-color var(--transition-base);
  text-align: center;
}

.name-input:focus,
.date-input:focus {
  border-color: var(--color-primary);
}

.name-input--error,
.date-input--error {
  border-color: #e53935;
}

.field-error {
  color: #e53935;
  font-size: var(--font-size-sm);
  margin: 0;
  text-align: center;
}

.toggle-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toggle-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: var(--touch-target-min);
}

.toggle-label {
  font-size: var(--font-size-body);
  font-weight: 600;
  color: var(--color-text-primary);
}

.toggle-btn {
  position: relative;
  width: 52px;
  height: 28px;
  border: 2px solid var(--color-neutral);
  border-radius: 14px;
  background: transparent;
  cursor: pointer;
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.toggle-btn--on {
  border-color: var(--color-primary);
  background-color: var(--color-primary);
}

.toggle-btn--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toggle-knob {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 1px 4px rgba(0,0,0,0.15);
  transition: transform var(--transition-base);
}

.toggle-btn--on .toggle-knob {
  transform: translateX(24px);
}

.toggle-reason {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin: 0;
  text-align: right;
}

.action-row {
  margin-top: var(--space-xs);
}

.submit-btn {
  width: 100%;
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
  transition: background-color var(--transition-base), transform var(--transition-base);
  box-shadow: 0 4px 0 var(--color-primary-dark), 0 18px 28px rgba(43, 91, 224, 0.22);
}

.submit-btn:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(2px);
  box-shadow: 0 2px 0 var(--color-primary-dark);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.delete-row {
  display: flex;
  justify-content: center;
  margin-top: var(--space-xs);
}

.delete-btn {
  padding: var(--space-xs) var(--space-md);
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: #e53935;
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
  transition: color var(--transition-base);
}

.delete-btn:hover:not(:disabled) {
  color: #b71c1c;
}

.delete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>