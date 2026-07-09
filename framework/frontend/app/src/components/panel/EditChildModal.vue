<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/ui/Modal.vue'
import AvatarPicker from '@/components/shared/AvatarPicker.vue'
import type { ChildProfileResponse, UpdateChildProfileRequest, ColorVisionMode } from '@/shared/types/api'
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
const colorVisionMode = ref<ColorVisionMode>('NONE')
const nameError = ref('')
const birthdayError = ref('')
const serverError = ref('')
const submitting = ref(false)
const deleteConfirmOpen = ref(false)

const colorVisionModes: Array<{
  value: ColorVisionMode
  shape: 'circle' | 'triangle' | 'square' | 'diamond' | 'gray'
  color: string
  labelKey: string
  subtitleKey: string
}> = [
  {
    value: 'PROTANOPIA',
    shape: 'circle',
    color: '#CC4444',
    labelKey: 'panel.children.editModal.colorVisionProtanopiaLabel',
    subtitleKey: 'panel.children.editModal.colorVisionProtanopiaSubtitle'
  },
  {
    value: 'DEUTERANOMALY',
    shape: 'triangle',
    color: '#44AA44',
    labelKey: 'panel.children.editModal.colorVisionDeuteranomalyLabel',
    subtitleKey: 'panel.children.editModal.colorVisionDeuteranomalySubtitle'
  },
  {
    value: 'DEUTERANOPIA',
    shape: 'square',
    color: '#888800',
    labelKey: 'panel.children.editModal.colorVisionDeuteranopiaLabel',
    subtitleKey: 'panel.children.editModal.colorVisionDeuteranopiaSubtitle'
  },
  {
    value: 'TRITANOPIA',
    shape: 'diamond',
    color: '#4488CC',
    labelKey: 'panel.children.editModal.colorVisionTritanopiaLabel',
    subtitleKey: 'panel.children.editModal.colorVisionTritanopiaSubtitle'
  },
  {
    value: 'ACHROMATOPSIA',
    shape: 'gray',
    color: '#888888',
    labelKey: 'panel.children.editModal.colorVisionAchromatopsiaLabel',
    subtitleKey: 'panel.children.editModal.colorVisionAchromatopsiaSubtitle'
  }
]

const colorPreviewSamples = [
  { identity: 'RED', shape: 'circle', labelKey: 'panel.children.editModal.colorVisionRedLabel' },
  { identity: 'BLUE', shape: 'square', labelKey: 'panel.children.editModal.colorVisionBlueLabel' },
  { identity: 'GREEN', shape: 'triangle', labelKey: 'panel.children.editModal.colorVisionGreenLabel' },
  { identity: 'YELLOW', shape: 'diamond', labelKey: 'panel.children.editModal.colorVisionYellowLabel' }
]

const colorPreviewFallback: Record<string, Record<ColorVisionMode, string>> = {
  RED: { NONE: '#FF0000', PROTANOPIA: '#808000', DEUTERANOMALY: '#FF8000', DEUTERANOPIA: '#808000', TRITANOPIA: '#FF0080', ACHROMATOPSIA: '#888888' },
  BLUE: { NONE: '#0000FF', PROTANOPIA: '#008080', DEUTERANOMALY: '#0080FF', DEUTERANOPIA: '#008080', TRITANOPIA: '#FF8000', ACHROMATOPSIA: '#888888' },
  GREEN: { NONE: '#00FF00', PROTANOPIA: '#00FF80', DEUTERANOMALY: '#80FF00', DEUTERANOPIA: '#808000', TRITANOPIA: '#00FF80', ACHROMATOPSIA: '#888888' },
  YELLOW: { NONE: '#FFFF00', PROTANOPIA: '#FFFF00', DEUTERANOMALY: '#FFFF80', DEUTERANOPIA: '#FFFF00', TRITANOPIA: '#00FFFF', ACHROMATOPSIA: '#888888' }
}

function getPreviewColor(identity: string): string {
  return colorPreviewFallback[identity]?.[colorVisionMode.value] ?? '#888888'
}

watch(() => props.open, (isOpen) => {
  if (isOpen) {
    name.value = props.child.name
    birthday.value = props.child.birthday
    selectedAvatar.value = props.child.avatar
    ttsEnabled.value = props.child.ttsEnabled
    agentEnabled.value = props.child.agentEnabled
    colorVisionMode.value = props.child.colorVisionMode
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
    colorVisionMode.value = newChild.colorVisionMode
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
    agentEnabled: agentEnabled.value,
    colorVisionMode: colorVisionMode.value
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

      <div class="color-vision-field">
        <div class="toggle-row">
          <span class="field-label">{{ t('panel.children.editModal.colorVisionLabel') }}</span>
          <button
            id="color-vision-toggle"
            type="button"
            class="toggle-btn"
            :class="{ 'toggle-btn--on': colorVisionMode !== 'NONE' }"
            :disabled="submitting"
            :aria-pressed="colorVisionMode !== 'NONE'"
            @click="colorVisionMode = colorVisionMode === 'NONE' ? 'DEUTERANOMALY' : 'NONE'"
          >
            <span class="toggle-knob" />
          </button>
        </div>

        <p v-if="colorVisionMode !== 'NONE'" class="color-vision-helper">
          {{ t('panel.children.editModal.colorVisionHelper') }}
        </p>

        <div v-if="colorVisionMode !== 'NONE'" class="color-vision-selector" role="radiogroup">
          <label
            v-for="mode in colorVisionModes"
            :key="mode.value"
            class="color-vision-option"
            :class="{ 'color-vision-option--selected': colorVisionMode === mode.value }"
          >
            <input
              type="radio"
              :value="mode.value"
              v-model="colorVisionMode"
              class="color-vision-radio"
            />
            <span class="color-vision-shape" :class="`color-vision-shape--${mode.shape}`" :style="{ '--shape-color': mode.color }" aria-hidden="true" />
            <span class="color-vision-content">
              <span class="color-vision-label">{{ t(mode.labelKey) }}</span>
              <span class="color-vision-subtitle">{{ t(mode.subtitleKey) }}</span>
            </span>
          </label>

          <button
            type="button"
            class="not-sure-btn"
            @click="colorVisionMode = 'DEUTERANOMALY'"
          >
            {{ t('panel.children.editModal.colorVisionNotSure') }}
          </button>

          <div class="color-vision-preview">
            <p class="color-vision-preview-title">
              {{ t('panel.children.editModal.colorVisionPreviewTitle') }}
            </p>
            <div class="color-vision-preview-samples">
              <div
                v-for="sample in colorPreviewSamples"
                :key="sample.identity"
                class="color-vision-sample"
              >
                <span
                  class="color-vision-sample-swatch"
                  :class="`color-vision-sample-swatch--${sample.shape}`"
                  :style="{ '--sample-color': getPreviewColor(sample.identity) }"
                  :aria-label="t(sample.labelKey)"
                />
                <span class="color-vision-sample-label">{{ t(sample.labelKey) }}</span>
              </div>
            </div>
          </div>
        </div>
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

.color-vision-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.color-vision-helper {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin: 0;
  padding-left: var(--space-xs);
}

.color-vision-selector {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  padding: var(--space-sm);
  background-color: var(--color-neutral-lightest);
  border-radius: var(--radius-md);
}

.color-vision-option {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm);
  background-color: white;
  border: 2px solid var(--color-neutral);
  border-radius: var(--radius-md);
  cursor: pointer;
  min-height: var(--touch-target-min);
  transition: border-color var(--transition-base), background-color var(--transition-base);
}

.color-vision-option:hover {
  border-color: var(--color-primary);
}

.color-vision-option--selected {
  border-color: var(--color-primary);
  background-color: var(--color-primary-lightest);
}

.color-vision-radio {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.color-vision-shape {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.color-vision-shape--circle {
  border-radius: 50%;
  background-color: var(--shape-color);
}

.color-vision-shape--square {
  border-radius: 3px;
  background-color: var(--shape-color);
}

.color-vision-shape--triangle {
  width: 0;
  height: 0;
  border-left: 12px solid transparent;
  border-right: 12px solid transparent;
  border-bottom: 20px solid var(--shape-color);
  background: transparent;
}

.color-vision-shape--diamond {
  width: 18px;
  height: 18px;
  background-color: var(--shape-color);
  transform: rotate(45deg);
}

.color-vision-shape--gray {
  border-radius: 50%;
  background: repeating-conic-gradient(#888 0% 25%, #aaa 0% 50%) 50% / 8px 8px;
}

.color-vision-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.color-vision-label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.color-vision-subtitle {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.not-sure-btn {
  margin-top: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  border: 2px dashed var(--color-neutral);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-family: var(--font-family-base);
  font-weight: 600;
  cursor: pointer;
  transition: border-color var(--transition-base), color var(--transition-base);
}

.not-sure-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.not-sure-btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.color-vision-preview {
  margin-top: var(--space-sm);
  padding: var(--space-sm);
  background-color: white;
  border-radius: var(--radius-md);
  border: 1px dashed var(--color-neutral);
}

.color-vision-preview-title {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  margin: 0 0 var(--space-xs) 0;
  text-align: center;
}

.color-vision-preview-samples {
  display: flex;
  justify-content: space-around;
  gap: var(--space-sm);
}

.color-vision-sample {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.color-vision-sample-swatch {
  width: 32px;
  height: 32px;
  background-color: var(--sample-color);
}

.color-vision-sample-swatch--circle {
  border-radius: 50%;
}

.color-vision-sample-swatch--square {
  border-radius: 3px;
}

.color-vision-sample-swatch--triangle {
  width: 0;
  height: 0;
  border-left: 16px solid transparent;
  border-right: 16px solid transparent;
  border-bottom: 28px solid var(--sample-color);
  background: transparent !important;
}

.color-vision-sample-swatch--diamond {
  width: 22px;
  height: 22px;
  background-color: var(--sample-color);
  transform: rotate(45deg);
}

.color-vision-sample-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}
</style>