<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useFamilyStore } from '@/stores/useFamilyStore'
import Modal from '@/components/ui/Modal.vue'

const { t } = useI18n()
const familyStore = useFamilyStore()

const TITLE_ID = 'family-registration-title'

const step = ref<1 | 2>(1)
const name = ref('')
const pinFirst = ref('')
const pinConfirm = ref('')
const submitting = ref(false)
const nameError = ref('')
const pinError = ref('')
const serverError = ref('')
const shaking = ref(false)
const firstPinInputRef = ref<HTMLInputElement | null>(null)
const confirmPinInputRef = ref<HTMLInputElement | null>(null)

const isOpen = computed(() => familyStore.activeModal === 'familyRegistration')
const pinFirstLength = computed(() => pinFirst.value.length)
const pinConfirmLength = computed(() => pinConfirm.value.length)
const submittingState = computed(() => submitting.value)

const pinDotsFirst = computed(() => pinFirst.value.split('').concat(Array(4 - pinFirst.value.length).fill('empty')).slice(0, 4))
const pinDotsConfirm = computed(() => pinConfirm.value.split('').concat(Array(4 - pinConfirm.value.length).fill('empty')).slice(0, 4))

watch(isOpen, async (open) => {
  if (open) {
    step.value = 1
    name.value = ''
    pinFirst.value = ''
    pinConfirm.value = ''
    nameError.value = ''
    pinError.value = ''
    serverError.value = ''
    shaking.value = false
    submitting.value = false
    await nextTick()
    const firstInput = document.getElementById('family-name')
    firstInput?.focus()
  }
})

function validateName(): boolean {
  if (!name.value.trim()) {
    nameError.value = t('modal.registerFamily.nameRequired')
    return false
  }
  nameError.value = ''
  return true
}

function goToStep2() {
  if (!validateName()) return
  step.value = 2
  pinFirst.value = ''
  pinConfirm.value = ''
  pinError.value = ''
  nextTick(() => firstPinInputRef.value?.focus())
}

function goToStep1() {
  step.value = 1
  pinFirst.value = ''
  pinConfirm.value = ''
  pinError.value = ''
  nextTick(() => {
    const el = document.getElementById('family-name')
    el?.focus()
  })
}

function appendDigit(digit: string) {
  if (submitting.value) return
  serverError.value = ''
  pinError.value = ''

  if (step.value === 2 && pinFirst.value.length < 4) {
    pinFirst.value += digit
    if (pinFirst.value.length === 4) {
      nextTick(() => confirmPinInputRef.value?.focus())
    }
  } else if (step.value === 2 && pinConfirm.value.length < 4) {
    pinConfirm.value += digit
  }
}

function deleteDigit() {
  if (submitting.value) return
  if (step.value === 2 && pinConfirm.value.length > 0) {
    pinConfirm.value = pinConfirm.value.slice(0, -1)
    pinError.value = ''
  } else if (step.value === 2 && pinFirst.value.length > 0) {
    pinFirst.value = pinFirst.value.slice(0, -1)
  }
}

function clearAll() {
  if (step.value === 2) {
    pinFirst.value = ''
    pinConfirm.value = ''
    pinError.value = ''
    nextTick(() => firstPinInputRef.value?.focus())
  }
}

function triggerMismatch() {
  pinError.value = t('modal.registerFamily.pinMismatch')
  shaking.value = true
  pinConfirm.value = ''
  setTimeout(() => {
    shaking.value = false
    nextTick(() => firstPinInputRef.value?.focus())
  }, 500)
}

async function handleSubmit() {
  if (submitting.value) return
  if (pinFirst.value.length < 4) return
  if (pinConfirm.value.length < 4) return
  if (pinConfirm.value !== pinFirst.value) {
    triggerMismatch()
    return
  }

  submitting.value = true
  serverError.value = ''
  pinError.value = ''

  try {
    await familyStore.registerFamily({
      name: name.value.trim(),
      pin: pinFirst.value,
      ttsEnabled: true,
      agentEnabled: true
    })
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number; data?: { errors?: string[]; message?: string } } }
    const status = axiosError.response?.status
    if (status === 409) {
      await familyStore.fetchFamily()
      familyStore.setActiveModal(null)
      return
    }
    if (status === 400) {
      const errors = axiosError.response?.data?.errors
      if (errors?.length) {
        serverError.value = errors[0]
      } else {
        serverError.value = t('modal.registerFamily.errorBadRequest')
      }
    } else {
      serverError.value = t('modal.registerFamily.errorServer')
    }
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  if (submitting.value) return
  familyStore.setActiveModal(null)
}
</script>

<template>
  <Modal
    :open="isOpen"
    :title-id="TITLE_ID"
    @close="handleClose"
  >
    <div class="modal-inner">
      <h2 :id="TITLE_ID" class="modal-title">{{ t('modal.registerFamily.title') }}</h2>

      <div v-if="serverError" class="server-error" role="alert">
        <p>{{ serverError }}</p>
        <button
          v-if="step === 1"
          class="retry-btn"
          type="button"
          @click="validateName()"
        >
          {{ t('common.retry') }}
        </button>
      </div>

      <div v-if="step === 1" class="step-container">
        <p class="step-label">{{ t('modal.registerFamily.step1Label') }}</p>
        <div class="form-field">
          <label for="family-name" class="sr-only">
            {{ t('modal.registerFamily.namePlaceholder') }}
          </label>
          <input
            id="family-name"
            v-model="name"
            type="text"
            class="name-input"
            :class="{ 'name-input--error': nameError }"
            :placeholder="t('modal.registerFamily.namePlaceholder')"
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
          :disabled="!name.trim()"
          @click="goToStep2"
        >
          {{ t('modal.registerFamily.continue') }}
        </button>
      </div>

      <div v-else class="step-container">
        <button
          class="back-btn"
          type="button"
          :aria-label="t('modal.registerFamily.backAriaLabel')"
          @click="goToStep1"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path d="M10 12L6 8L10 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ t('modal.registerFamily.back') }}
        </button>
        <p class="step-label">
          {{ pinConfirm.length === 0
            ? t('modal.registerFamily.step2LabelCreate')
            : t('modal.registerFamily.step2LabelConfirm') }}
        </p>

        <div
          class="pin-dots"
          :class="{ 'pin-dots--shake': shaking, 'pin-dots--error': pinError }"
          role="status"
          aria-live="polite"
        >
          <span
            v-for="(dot, i) in (pinConfirm.length > 0 ? pinDotsConfirm : pinDotsFirst)"
            :key="i"
            class="pin-dot"
            :class="{
              'pin-dot--filled': dot !== 'empty',
              'pin-dot--error': pinError && pinConfirm.length > 0
            }"
            :aria-label="dot !== 'empty' ? t('modal.registerFamily.digitEntered') : undefined"
            aria-hidden="true"
          />
        </div>

        <p v-if="pinError" class="field-error pin-error" role="alert">
          {{ pinError }}
        </p>

        <div
          class="keypad"
          role="group"
          :aria-label="t('modal.registerFamily.keypadAriaLabel')"
        >
          <div class="keypad-row">
            <button
              v-for="d in ['1','2','3']"
              :key="d"
              class="keypad-btn"
              type="button"
              :aria-label="t('modal.registerFamily.digitAria', { digit: d })"
              :disabled="submitting"
              @click="appendDigit(d)"
            >
              {{ d }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              v-for="d in ['4','5','6']"
              :key="d"
              class="keypad-btn"
              type="button"
              :aria-label="t('modal.registerFamily.digitAria', { digit: d })"
              :disabled="submitting"
              @click="appendDigit(d)"
            >
              {{ d }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              v-for="d in ['7','8','9']"
              :key="d"
              class="keypad-btn"
              type="button"
              :aria-label="t('modal.registerFamily.digitAria', { digit: d })"
              :disabled="submitting"
              @click="appendDigit(d)"
            >
              {{ d }}
            </button>
          </div>
          <div class="keypad-row keypad-row--actions">
            <button
              class="keypad-btn keypad-btn--action"
              type="button"
              :aria-label="t('modal.registerFamily.clearAriaLabel')"
              :disabled="submitting"
              @click="clearAll"
            >
              {{ t('modal.registerFamily.clear') }}
            </button>
            <button
              class="keypad-btn"
              type="button"
              :aria-label="t('modal.registerFamily.digitAria', { digit: '0' })"
              :disabled="submitting"
              @click="appendDigit('0')"
            >
              0
            </button>
            <button
              class="keypad-btn keypad-btn--action keypad-btn--delete"
              type="button"
              :aria-label="t('modal.registerFamily.deleteAriaLabel')"
              :disabled="submitting"
              @click="deleteDigit"
            >
              {{ t('modal.registerFamily.delete') }}
            </button>
          </div>
        </div>

        <div class="action-row">
          <button
            class="action-btn"
            type="button"
            :disabled="submittingState || pinFirstLength < 4 || pinConfirmLength < 4"
            @click="handleSubmit"
          >
            {{ t('modal.registerFamily.confirm') }}
          </button>
        </div>

        <input
          ref="firstPinInputRef"
          class="sr-only"
          aria-hidden="true"
          tabindex="-1"
          @keydown="(e) => { if (/^\d$/.test(e.key)) appendDigit(e.key); if (e.key === 'Backspace') deleteDigit(); }"
        />
        <input
          ref="confirmPinInputRef"
          class="sr-only"
          aria-hidden="true"
          tabindex="-1"
          @keydown="(e) => { if (/^\d$/.test(e.key)) appendDigit(e.key); if (e.key === 'Backspace') deleteDigit(); }"
        />
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
  margin: 0 0 var(--space-sm);
}

.retry-btn {
  min-height: 36px;
  padding: var(--space-xs) var(--space-md);
  border: none;
  border-radius: var(--radius-pill);
  background-color: #e53935;
  color: #fff;
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
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

.name-input {
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

.name-input:focus {
  border-color: var(--color-primary);
}

.name-input--error {
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

.pin-dots {
  display: flex;
  gap: 10px;
  padding: var(--space-sm) 0;
}

.pin-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid var(--color-neutral);
  background-color: transparent;
  transition: background-color 0.15s, border-color 0.15s;
}

.pin-dot--filled {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
}

.pin-dot--error {
  border-color: #e53935;
  background-color: transparent;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-6px); }
  40% { transform: translateX(6px); }
  60% { transform: translateX(-4px); }
  80% { transform: translateX(4px); }
}

.pin-dots--shake {
  animation: shake 0.4s ease-in-out;
}

.pin-dots--error .pin-dot:not(.pin-dot--filled) {
  border-color: #e53935;
}

.pin-error {
  margin: 0;
}

.keypad {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
  max-width: 260px;
}

.keypad-row {
  display: flex;
  justify-content: center;
  gap: 6px;
}

.keypad-row--actions {
  gap: 6px;
}

.keypad-btn {
  width: 64px;
  height: 64px;
  border: none;
  border-radius: var(--radius-md);
  background-color: var(--color-surface);
  color: #374151;
  font-size: var(--font-size-lg);
  font-family: var(--font-family-base);
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition-base), transform var(--transition-base);
  box-shadow: 0 3px 0 #d1d5db, 0 1px 4px rgba(0,0,0,0.08);
}

.keypad-btn:hover:not(:disabled) {
  background-color: #f3f4f6;
}

.keypad-btn:active:not(:disabled) {
  transform: translateY(2px);
  box-shadow: 0 1px 0 #d1d5db;
}

.keypad-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.keypad-btn--action {
  background-color: #f3f4f6;
  font-size: var(--font-size-sm);
  font-weight: 600;
  box-shadow: 0 2px 0 #d1d5db, 0 1px 2px rgba(0,0,0,0.06);
}

.keypad-btn--action:hover:not(:disabled) {
  background-color: #e5e7eb;
}

.keypad-btn--action:active:not(:disabled) {
  transform: translateY(1px);
  box-shadow: 0 1px 0 #d1d5db;
}

.keypad-btn--delete {
  color: #e53935;
}

.keypad-btn--delete:hover:not(:disabled) {
  background-color: #fef2f2;
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
  .keypad-btn {
    width: 58px;
    height: 58px;
  }

  .keypad {
    max-width: 240px;
  }
}
</style>