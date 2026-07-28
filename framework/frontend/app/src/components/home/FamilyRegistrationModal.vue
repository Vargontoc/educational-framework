<template>
  <NubiInfoModal
    :model-value="modelValue"
    :title="t('views.home.familyRegistration.title')"
    :close-on-overlay="false"
    @update:model-value="handleOverlayUpdate"
    @close="handleCancel"
  >
    <div class="family-registration">
      <div class="family-registration__stepper" role="group" :aria-label="t('views.home.familyRegistration.stepperLabel')">
        <div
          :class="['family-registration__step', { 'family-registration__step--active': step === 1, 'family-registration__step--completed': step === 2 }]"
          :aria-current="step === 1 ? 'step' : undefined"
        >
          <NubiIcon :name="step === 2 ? 'check-circle' : 'users'" :size="20" />
          <span class="family-registration__step-label">{{ t('views.home.familyRegistration.step1Label') }}</span>
        </div>
        <div class="family-registration__step-connector" aria-hidden="true">
          <div :class="['family-registration__step-line', { 'family-registration__step-line--active': step === 2 }]" />
        </div>
        <div
          :class="['family-registration__step', { 'family-registration__step--active': step === 2 }]"
          :aria-current="step === 2 ? 'step' : undefined"
        >
          <NubiIcon name="lock" :size="20" />
          <span class="family-registration__step-label">{{ t('views.home.familyRegistration.step2Label') }}</span>
        </div>
      </div>

      <div class="family-registration__body">
        <Transition name="family-registration-fade" mode="out-in">
          <div v-if="step === 1" key="step1" class="family-registration__panel">
            <p class="family-registration__description" id="step1-desc">
              {{ t('views.home.familyRegistration.step1Description') }}
            </p>
            <NubiTextInput
              ref="nameInputRef"
              :model-value="familyName"
              :label="t('views.home.familyRegistration.familyNameLabel')"
              :placeholder="t('views.home.familyRegistration.familyNamePlaceholder')"
              :error="nameError"
              :disabled="isSubmitting"
              :maxlength="200"
              aria-describedby="step1-desc"
              @update:model-value="setFamilyName"
            />
          </div>

          <div v-else key="step2" class="family-registration__panel">
            <p class="family-registration__description" id="step2-desc">
              {{ t('views.home.familyRegistration.step2Description') }}
            </p>

            <NubiPinInput
              ref="pinInputRef"
              :model-value="pin"
              :label="t('views.home.familyRegistration.createPinLabel')"
              :masked="true"
              :show-clear="false"
              :disabled="isSubmitting"
              :error="pinInputError"
              @update:model-value="setPin"
            />

            <NubiPinInput
              ref="confirmPinInputRef"
              :model-value="confirmPin"
              :label="t('views.home.familyRegistration.confirmPinLabel')"
              :masked="true"
              :show-clear="false"
              :disabled="isSubmitting"
              :error="confirmPinError"
              @update:model-value="setConfirmPin"
            />

            <div
              v-if="!pinsMatch && confirmPin.length === 4"
              class="family-registration__pin-error"
              role="alert"
              aria-live="assertive"
            >
              <NubiIcon name="alert-circle" :size="16" color="var(--nubi-color-error)" />
              <span>{{ t('views.home.familyRegistration.pinMismatch') }}</span>
            </div>

            <div
              v-if="errorKey && errorKey !== 'pin-mismatch'"
              class="family-registration__server-error"
              role="alert"
              aria-live="assertive"
            >
              <NubiIcon name="alert-circle" :size="16" color="var(--nubi-color-error)" />
              <span>{{ serverErrorMessage }}</span>
            </div>
          </div>
        </Transition>
      </div>

      <div class="family-registration__actions">
        <template v-if="step === 1">
          <NubiButton variant="secondary" :disabled="isSubmitting" @click="handleCancel">
            {{ t('common.cancel') }}
          </NubiButton>
          <NubiButton
            variant="primary"
            :disabled="!canContinueStep1"
            :loading="false"
            @click="handleContinue"
          >
            {{ t('common.next') }}
          </NubiButton>
        </template>

        <template v-else>
          <NubiButton variant="secondary" :disabled="isSubmitting" @click="handleBack">
            {{ t('common.back') }}
          </NubiButton>
          <div class="family-registration__actions-right">
            <NubiButton variant="secondary" :disabled="isSubmitting" @click="handleCancel">
              {{ t('common.cancel') }}
            </NubiButton>
            <NubiButton
              variant="primary"
              :disabled="!canSubmit"
              :loading="isSubmitting"
              @click="handleSubmit"
            >
              {{ t('views.home.familyRegistration.createFamily') }}
            </NubiButton>
          </div>
        </template>
      </div>
    </div>
  </NubiInfoModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiInfoModal from '../base/NubiInfoModal.vue'
import NubiTextInput from '../base/NubiTextInput.vue'
import NubiPinInput from '../base/NubiPinInput.vue'
import NubiButton from '../base/NubiButton.vue'
import NubiIcon from '../base/NubiIcon.vue'
import { useFamilyRegistration } from '../../composables/useFamilyRegistration'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
  'family-created': []
}>()

const { t } = useI18n()

const {
  step,
  familyName,
  pin,
  confirmPin,
  errorKey,
  isNameValid,
  pinsMatch,
  canContinueStep1,
  canSubmit,
  isSubmitting,
  setFamilyName,
  setPin,
  setConfirmPin,
  goToStep2,
  goToStep1,
  reset,
  submit
} = useFamilyRegistration()

const nameInputRef = ref<InstanceType<typeof NubiTextInput> | null>(null)
const pinInputRef = ref<InstanceType<typeof NubiPinInput> | null>(null)
const confirmPinInputRef = ref<InstanceType<typeof NubiPinInput> | null>(null)
const nameTouched = ref(false)

const nameError = computed(() => {
  if (!nameTouched.value) return ''
  if (!isNameValid.value) return t('views.home.familyRegistration.nameRequired')
  return ''
})

const pinInputError = computed(() => {
  if (pin.value.length > 0 && pin.value.length < 4) return t('views.home.familyRegistration.pinIncomplete')
  return ''
})

const confirmPinError = computed(() => {
  if (!pinsMatch.value && confirmPin.value.length === 4) return t('views.home.familyRegistration.pinMismatch')
  return ''
})

const serverErrorMessage = computed(() => {
  switch (errorKey.value) {
    case 'validation': return t('views.home.familyRegistration.errorValidation')
    case 'conflict': return t('views.home.familyRegistration.errorConflict')
    case 'server': return t('views.home.familyRegistration.errorServer')
    case 'connection': return t('views.home.familyRegistration.errorConnection')
    default: return t('views.home.familyRegistration.errorGeneric')
  }
})

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    reset()
    nameTouched.value = false
    nextTick(() => {
      nameInputRef.value?.focus()
    })
  }
})

watch(step, (newStep) => {
  nextTick(() => {
    if (newStep === 1) {
      nameInputRef.value?.focus()
    } else if (newStep === 2) {
      pinInputRef.value?.focus()
    }
  })
})

function handleContinue() {
  nameTouched.value = true
  if (!canContinueStep1.value) return
  goToStep2()
}

function handleBack() {
  goToStep1()
}

function handleCancel() {
  emit('update:modelValue', false)
  emit('close')
}

function handleOverlayUpdate(value: boolean) {
  if (!value) {
    handleCancel()
  }
}

async function handleSubmit() {
  const result = await submit()
  if (result.success) {
    emit('family-created')
    emit('update:modelValue', false)
    emit('close')
  }
}
</script>

<style scoped>
.family-registration {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
  max-width: 500px;
}

.family-registration__stepper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-sm) 0;
}

.family-registration__step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  padding: var(--nubi-spacing-sm);
  border-radius: var(--nubi-radius-md);
  color: var(--nubi-text-tertiary);
  transition: color 0.3s ease, background-color 0.3s ease;
}

.family-registration__step--active {
  color: var(--nubi-color-primary);
  background-color: var(--nubi-color-primary-light);
}

.family-registration__step--completed {
  color: var(--nubi-color-success);
}

.family-registration__step-label {
  font-size: var(--nubi-font-size-xs);
  font-weight: var(--nubi-font-weight-medium);
  white-space: nowrap;
}

.family-registration__step-connector {
  flex: 0 0 auto;
  width: 40px;
  display: flex;
  align-items: center;
  padding-bottom: var(--nubi-spacing-lg);
}

.family-registration__step-line {
  width: 100%;
  height: 2px;
  background-color: var(--nubi-border-default);
  border-radius: 1px;
  transition: background-color 0.3s ease;
}

.family-registration__step-line--active {
  background-color: var(--nubi-color-success);
}

.family-registration__panel {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.family-registration__description {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  line-height: var(--nubi-line-height-relaxed);
  margin: 0;
}

.family-registration__pin-error,
.family-registration__server-error {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-error);
  padding: var(--nubi-spacing-sm);
  border-radius: var(--nubi-radius-md);
  background-color: rgba(239, 68, 68, 0.08);
}

.family-registration__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding-top: var(--nubi-spacing-sm);
}

.family-registration__actions-right {
  display: flex;
  gap: var(--nubi-spacing-sm);
}

.family-registration-fade-enter-active,
.family-registration-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.family-registration-fade-enter-from {
  opacity: 0;
  transform: translateX(12px);
}

.family-registration-fade-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}

@media (orientation: portrait) {
  .family-registration {
    max-width: 100%;
  }

  .family-registration__actions {
    flex-direction: column;
    gap: var(--nubi-spacing-sm);
  }

  .family-registration__actions > *,
  .family-registration__actions-right {
    width: 100%;
  }

  .family-registration__actions-right {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .family-registration__step-connector {
    width: 24px;
  }
}

@media (max-height: 500px) {
  .family-registration {
    gap: var(--nubi-spacing-sm);
  }

  .family-registration__stepper {
    gap: var(--nubi-spacing-xs);
    padding: var(--nubi-spacing-xs) 0;
  }

  .family-registration__step {
    flex-direction: row;
    gap: var(--nubi-spacing-xs);
    padding: var(--nubi-spacing-xs) var(--nubi-spacing-sm);
  }

  .family-registration__step-connector {
    width: 24px;
    padding-bottom: 0;
  }

  .family-registration__panel {
    gap: var(--nubi-spacing-sm);
  }

  .family-registration__description {
    font-size: var(--nubi-font-size-xs);
    line-height: var(--nubi-line-height-normal);
  }

  .family-registration__actions {
    padding-top: var(--nubi-spacing-xs);
    gap: var(--nubi-spacing-xs);
  }
}
</style>
