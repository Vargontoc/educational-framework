<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import Modal from '@/components/ui/Modal.vue'
import AvatarPicker from '@/components/shared/AvatarPicker.vue'

const { t } = useI18n()
const familyStore = useFamilyStore()

const TITLE_ID = 'add-child-title'

const step = ref<1 | 2 | 3>(1)
const name = ref('')
const birthday = ref('')
const selectedAvatar = ref<string | null>(null)
const nameError = ref('')
const birthdayError = ref('')
const serverError = ref('')
const submitting = ref(false)

const addButtonRef = ref<HTMLButtonElement | null>(null)

const isOpen = computed(() => familyStore.activeModal === 'addChild')
const submittingState = computed(() => submitting.value)
const canGoToStep2 = computed(() => name.value.trim().length > 0)
const canSubmit = computed(() => selectedAvatar.value !== null)

function validateName(): boolean {
  if (!name.value.trim()) {
    nameError.value = t('modal.addChild.nameWhitespace')
    return false
  }
  nameError.value = ''
  return true
}

function validateBirthday(): boolean {
  if (!birthday.value) {
    birthdayError.value = t('modal.addChild.birthdayRequired')
    return false
  }
  const date = new Date(birthday.value)
  if (isNaN(date.getTime())) {
    birthdayError.value = t('modal.addChild.birthdayInvalid')
    return false
  }
  const today = new Date()
  const age = today.getFullYear() - date.getFullYear()
  if (age < 3 || age > 8) {
    birthdayError.value = t('modal.addChild.birthdayOutOfRange')
    return false
  }
  birthdayError.value = ''
  return true
}

function goToStep2() {
  if (!validateName()) return
  step.value = 2
  nextTick(() => {
    const input = document.getElementById('child-birthday')
    input?.focus()
  })
}

function goToStep1() {
  step.value = 1
  nextTick(() => {
    const input = document.getElementById('child-name')
    input?.focus()
  })
}

function goToStep3() {
  if (!validateBirthday()) return
  step.value = 3
}

async function handleSubmit() {
  if (submitting.value || !selectedAvatar.value) return

  submitting.value = true
  serverError.value = ''

  try {
    await familyStore.addChild({
      name: name.value.trim(),
      birthday: birthday.value,
      avatar: selectedAvatar.value,
      ttsEnabled: true,
      agentEnabled: true
    })
    resetAndClose()
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number; data?: { errors?: string[]; message?: string } } }
    const status = axiosError.response?.status
    if (status === 400) {
      const errors = axiosError.response?.data?.errors
      if (errors?.length) {
        serverError.value = errors[0]
      } else {
        serverError.value = t('modal.addChild.errorBadRequest')
      }
    } else if (status === 404) {
      serverError.value = t('modal.addChild.errorNotFound')
      await familyStore.fetchFamily()
    } else {
      serverError.value = t('modal.addChild.errorServer')
    }
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  if (submitting.value) return
  resetStepper()
  familyStore.setActiveModal(null)
}

function resetAndClose() {
  resetStepper()
  familyStore.setActiveModal('childSelector')
  nextTick(() => {
    addButtonRef.value?.focus()
  })
}

function resetStepper() {
  step.value = 1
  name.value = ''
  birthday.value = ''
  selectedAvatar.value = null
  nameError.value = ''
  birthdayError.value = ''
  serverError.value = ''
  submitting.value = false
}
</script>

<template>
  <Modal
    :open="isOpen"
    :title-id="TITLE_ID"
    @close="handleClose"
  >
    <div class="modal-inner">
      <h2 :id="TITLE_ID" class="modal-title">{{ t('modal.addChild.title') }}</h2>

      <div v-if="serverError" class="server-error" role="alert">
        <p>{{ serverError }}</p>
      </div>

      <div v-if="step === 1" class="step-container">
        <p class="step-label">{{ t('modal.addChild.step1Label') }}</p>
        <div class="form-field">
          <label for="child-name" class="sr-only">
            {{ t('modal.addChild.namePlaceholder') }}
          </label>
          <input
            id="child-name"
            v-model="name"
            type="text"
            class="name-input"
            :class="{ 'name-input--error': nameError }"
            :placeholder="t('modal.addChild.namePlaceholder')"
            :aria-describedby="nameError ? 'name-error' : undefined"
            autocomplete="off"
            @keydown.enter="goToStep2"
          />
          <p v-if="nameError" id="name-error" class="field-error" role="alert">
            {{ nameError }}
          </p>
        </div>
        <button
          class="action-btn"
          type="button"
          :disabled="!canGoToStep2"
          @click="goToStep2"
        >
          {{ t('modal.addChild.continue') }}
        </button>
      </div>

      <div v-else-if="step === 2" class="step-container">
        <button
          class="back-btn"
          type="button"
          :aria-label="t('modal.addChild.backAriaLabel')"
          @click="goToStep1"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path d="M10 12L6 8L10 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ t('modal.addChild.back') }}
        </button>
        <p class="step-label">{{ t('modal.addChild.step2Label') }}</p>
        <div class="form-field">
          <label for="child-birthday" class="sr-only">
            {{ t('modal.addChild.birthdayPlaceholder') }}
          </label>
          <input
            id="child-birthday"
            v-model="birthday"
            type="date"
            class="date-input"
            :class="{ 'date-input--error': birthdayError }"
            :placeholder="t('modal.addChild.birthdayPlaceholder')"
            :aria-describedby="birthdayError ? 'birthday-error' : undefined"
            @keydown.enter="goToStep3"
          />
          <p v-if="birthdayError" id="birthday-error" class="field-error" role="alert">
            {{ birthdayError }}
          </p>
        </div>
        <button
          class="action-btn"
          type="button"
          :disabled="!birthday"
          @click="goToStep3"
        >
          {{ t('modal.addChild.continue') }}
        </button>
      </div>

      <div v-else-if="step === 3" class="step-container">
        <button
          class="back-btn"
          type="button"
          :aria-label="t('modal.addChild.backAriaLabel')"
          @click="goToStep2"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path d="M10 12L6 8L10 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ t('modal.addChild.back') }}
        </button>
        <p class="step-label">{{ t('modal.addChild.step3Label') }}</p>

        <AvatarPicker v-model="selectedAvatar" />

        <div class="action-row">
          <button
            class="action-btn"
            type="button"
            :disabled="submittingState || !canSubmit"
            @click="handleSubmit"
          >
            {{ submittingState ? t('modal.addChild.submitting') : t('modal.addChild.confirm') }}
          </button>
        </div>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.modal-inner {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.modal-title {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: #1f2937;
  margin: 0;
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

.step-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
}

.step-label {
  font-size: var(--font-size-body);
  color: #6b7280;
  margin: 0;
  text-align: center;
}

.form-field {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
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

.action-btn {
  min-width: 200px;
  min-height: var(--touch-target-min);
  padding: var(--space-sm) var(--space-lg);
  border: none;
  border-radius: var(--radius-pill);
  background-color: var(--color-primary);
  color: var(--color-text-on-primary);
  font-size: var(--font-size-button);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--transition-base), transform var(--transition-base);
  box-shadow: 0 4px 0 var(--color-primary-dark), 0 18px 28px rgba(43, 91, 224, 0.22);
}

.action-btn:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.action-btn:active:not(:disabled) {
  transform: translateY(2px);
  box-shadow: 0 2px 0 var(--color-primary-dark);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.back-btn {
  align-self: flex-start;
  min-height: 36px;
  padding: var(--space-xs) var(--space-md);
  border: none;
  border-radius: var(--radius-pill);
  background-color: transparent;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: inset 0 0 0 2px color-mix(in srgb, var(--color-primary) 18%, transparent);
  transition: background-color var(--transition-base), box-shadow var(--transition-base);
}

.back-btn:hover {
  background-color: color-mix(in srgb, var(--color-primary) 8%, transparent);
  box-shadow: inset 0 0 0 2px color-mix(in srgb, var(--color-primary) 30%, transparent);
}

.back-btn:active {
  background-color: color-mix(in srgb, var(--color-primary) 12%, transparent);
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-sm);
  width: 100%;
  max-width: 320px;
}

.avatar-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--space-sm);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  outline: none;
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.avatar-option:focus-visible {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 6%, transparent);
}

.avatar-option {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--space-sm);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  outline: none;
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.avatar-option:focus-visible {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 6%, transparent);
}

.avatar-option--selected {
  border-color: var(--color-primary);
  background-color: color-mix(in srgb, var(--color-primary) 8%, transparent);
}

.avatar-svg {
  display: block;
  filter: drop-shadow(0 3px 0 rgba(0,0,0,0.12)) drop-shadow(0 1px 4px rgba(0,0,0,0.08));
}

.avatar-check {
  position: absolute;
  bottom: -2px;
  right: -2px;
}

.action-row {
  margin-top: var(--space-xs);
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

@media (max-width: 360px) {
  .avatar-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }

  .avatar-svg {
    width: 56px;
    height: 56px;
  }
}
</style>