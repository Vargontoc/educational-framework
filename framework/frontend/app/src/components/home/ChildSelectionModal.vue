<template>
  <NubiInfoModal
    :model-value="modelValue"
    :title="modalTitle"
    :close-on-overlay="currentView === 'selection'"
    @update:model-value="handleModalUpdate"
    @close="handleModalClose"
  >
    <div class="child-selection-modal__content">
      <Transition name="nubi-fade" mode="out-in">
        <div v-if="currentView === 'selection'" key="selection">
          <Transition name="nubi-fade" mode="out-in">
            <div v-if="familyLoading || profilesLoading" key="loading" class="child-selection-modal__state">
              <NubiSpinner size="lg" :label="t('views.home.childSelection.loading')" show-label />
            </div>

            <div v-else-if="familyError || profilesError" key="error" class="child-selection-modal__state">
              <NubiErrorState
                :title="t('views.home.childSelection.errorTitle')"
                :message="familyError || profilesErrorMessage"
                :show-retry="true"
                :retry-label="t('common.retry')"
                @retry="loadData"
              />
            </div>

            <div v-else-if="profiles.length === 0" key="empty" class="child-selection-modal__state">
              <p class="child-selection-modal__empty-text">
                {{ t('views.home.childSelection.noProfiles') }}
              </p>
            </div>

            <div v-else key="content" class="child-selection-modal__grid-wrapper">
              <NubiGrid :items="profiles" :cols="3" :empty-text="t('views.home.childSelection.noProfiles')">
                <template #item="{ item }">
                  <ChildProfileCard
                    :profile="item"
                    :selected="selectedProfileId === item.id"
                    @select="handleSelectProfile(item)"
                  />
                </template>
              </NubiGrid>
            </div>
          </Transition>
        </div>

        <div v-else-if="currentView === 'pin-verification'" key="pin-verification" class="child-selection-modal__pin-view">
          <p class="child-selection-modal__description">
            {{ t('views.home.childSelection.pinVerification.description') }}
          </p>
          <NubiPinInput
            ref="pinInputRef"
            v-model="pin"
            :masked="true"
            :error="pinError"
            :disabled="pinVerifying"
            @complete="handleVerifyPin"
          />
          <div v-if="pinError" class="child-selection-modal__error-message" role="alert" aria-live="assertive">
            {{ pinError }}
          </div>
        </div>

        <div v-else-if="currentView === 'registration'" key="registration" class="child-selection-modal__registration-view">
          <NubiStepper
            :key="registrationStep"
            :steps="registrationStepLabels"
            :model-value="registrationStep"
            class="child-selection-modal__stepper"
          >
            <template #default>
              <div v-if="registrationStep === 0" class="child-selection-modal__step-content">
                <NubiTextInput
                  ref="nameInputRef"
                  v-model="childName"
                  :label="t('views.home.childSelection.registration.nameLabel')"
                  :placeholder="t('views.home.childSelection.registration.namePlaceholder')"
                  :required="true"
                  :error="nameError"
                />
              </div>
              <div v-else-if="registrationStep === 1" class="child-selection-modal__step-content">
                <div class="child-selection-modal__field">
                  <label class="child-selection-modal__field-label" for="child-birthday">
                    {{ t('views.home.childSelection.registration.birthdayLabel') }}
                    <span class="child-selection-modal__field-required" aria-hidden="true">*</span>
                  </label>
                  <input
                    id="child-birthday"
                    ref="dateInputRef"
                    v-model="childBirthday"
                    type="date"
                    class="child-selection-modal__date-input"
                    :max="todayDate"
                    :aria-invalid="!!birthdayError"
                    :aria-label="t('views.home.childSelection.registration.birthdayLabel')"
                  />
                  <span v-if="birthdayError" class="child-selection-modal__field-error" role="alert">
                    {{ birthdayError }}
                  </span>
                </div>
                <AvatarSelector v-model="childAvatar" />
              </div>
            </template>
          </NubiStepper>
          <div v-if="submitError" class="child-selection-modal__error-message" role="alert" aria-live="assertive">
            {{ submitError }}
          </div>
          <NubiSpinner v-if="submitting" size="sm" :label="t('views.home.childSelection.registration.submitting')" show-label />
        </div>
      </Transition>
    </div>

    <template #footer>
      <div v-if="currentView === 'selection'" class="child-selection-modal__footer">
        <NubiButton variant="secondary" @click="handleRegisterChild">
          {{ t('views.home.childSelection.registerChild') }}
        </NubiButton>
      </div>

      <div v-else-if="currentView === 'pin-verification'" class="child-selection-modal__footer child-selection-modal__footer--actions">
        <NubiButton variant="secondary" @click="handleCancelPin">
          {{ t('common.cancel') }}
        </NubiButton>
        <NubiButton
          variant="primary"
          :disabled="pin.length < 4"
          :loading="pinVerifying"
          @click="handleVerifyPin"
        >
          {{ t('views.home.childSelection.pinVerification.verify') }}
        </NubiButton>
      </div>

      <div v-else-if="currentView === 'registration'" class="child-selection-modal__footer child-selection-modal__footer--actions">
        <NubiButton variant="secondary" @click="handleCancelRegistration">
          {{ t('common.cancel') }}
        </NubiButton>
        <div class="child-selection-modal__footer-right">
          <NubiButton v-if="registrationStep === 1" variant="secondary" @click="handleBackStep">
            {{ t('common.back') }}
          </NubiButton>
          <NubiButton
            v-if="registrationStep === 0"
            variant="primary"
            :disabled="!childName.trim()"
            @click="handleNextStep"
          >
            {{ t('common.next') }}
          </NubiButton>
          <NubiButton
            v-if="registrationStep === 1"
            variant="primary"
            :disabled="!canConfirm"
            :loading="submitting"
            @click="handleConfirmRegistration"
          >
            {{ t('views.home.childSelection.registration.confirm') }}
          </NubiButton>
        </div>
      </div>
    </template>
  </NubiInfoModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import NubiInfoModal from '../base/NubiInfoModal.vue'
import NubiGrid from '../base/NubiGrid.vue'
import NubiButton from '../base/NubiButton.vue'
import NubiSpinner from '../base/NubiSpinner.vue'
import NubiErrorState from '../base/NubiErrorState.vue'
import NubiPinInput from '../base/NubiPinInput.vue'
import NubiTextInput from '../base/NubiTextInput.vue'
import NubiStepper from '../base/NubiStepper.vue'
import ChildProfileCard from './ChildProfileCard.vue'
import AvatarSelector from './AvatarSelector.vue'
import { getFamily, verifyPin, createChild, type ChildProfile } from '../../services/familyService'
import { useChildProfiles } from '../../composables/useChildProfiles'
import { useSessionStore } from '../../stores/session'
import type { ApiError } from '../../services/api'

type ModalView = 'selection' | 'pin-verification' | 'registration'

interface Props {
  modelValue: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

const { t } = useI18n()
const router = useRouter()
const sessionStore = useSessionStore()
const { profiles, loading: profilesLoading, error: profilesError, errorMessage: profilesErrorMessage, fetchProfiles } = useChildProfiles()

const familyName = ref('')
const familyLoading = ref(false)
const familyError = ref('')
const selectedProfileId = ref<number | null>(null)

const currentView = ref<ModalView>('selection')

const pin = ref('')
const pinError = ref('')
const pinVerifying = ref(false)
const pinInputRef = ref<InstanceType<typeof NubiPinInput> | null>(null)

const registrationStep = ref(0)
const childName = ref('')
const childBirthday = ref('')
const childAvatar = ref('avatar-1')
const submitting = ref(false)
const submitError = ref('')
const nameInputRef = ref<InstanceType<typeof NubiTextInput> | null>(null)
const dateInputRef = ref<HTMLInputElement | null>(null)

const registrationStepLabels = computed(() => [
  t('views.home.childSelection.registration.step1Label'),
  t('views.home.childSelection.registration.step2Label')
])

const modalTitle = computed(() => {
  if (currentView.value === 'pin-verification') {
    return t('views.home.childSelection.pinVerification.title')
  }
  if (currentView.value === 'registration') {
    return t('views.home.childSelection.registration.title')
  }
  if (familyName.value) {
    return t('views.home.childSelection.familyTitle', { name: familyName.value })
  }
  return t('views.home.childSelection.familyTitleDefault')
})

const todayDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const birthdayError = computed(() => {
  if (!childBirthday.value) return ''
  const selected = new Date(childBirthday.value + 'T00:00:00')
  const today = new Date()
  today.setHours(23, 59, 59, 999)
  if (selected > today) {
    return t('views.home.childSelection.registration.birthdayFuture')
  }
  return ''
})

const nameError = computed(() => {
  return ''
})

const canConfirm = computed(() => {
  return childName.value.trim() !== '' &&
         childBirthday.value !== '' &&
         !birthdayError.value
})

function resetFormState(): void {
  pin.value = ''
  pinError.value = ''
  pinVerifying.value = false
  registrationStep.value = 0
  childName.value = ''
  childBirthday.value = ''
  childAvatar.value = 'avatar-1'
  submitting.value = false
  submitError.value = ''
}

function resetToSelection(): void {
  currentView.value = 'selection'
  resetFormState()
}

async function loadData(): Promise<void> {
  familyLoading.value = true
  familyError.value = ''

  try {
    const family = await getFamily()
    if (family) {
      familyName.value = family.name
    }
  } catch (err) {
    const apiError = err as ApiError
    familyError.value = apiError.message || 'Error al obtener la familia'
  } finally {
    familyLoading.value = false
  }

  await fetchProfiles()
}

function handleSelectProfile(profile: ChildProfile): void {
  selectedProfileId.value = profile.id
  sessionStore.selectChild(String(profile.id))
  router.replace({ name: 'GameView', params: { childId: String(profile.id) } })
}

function handleRegisterChild(): void {
  currentView.value = 'pin-verification'
}

function handleModalUpdate(value: boolean): void {
  if (!value && currentView.value !== 'selection') {
    resetToSelection()
    return
  }
  emit('update:modelValue', value)
}

function handleModalClose(): void {
  if (currentView.value === 'selection') {
    emit('close')
  }
}

async function handleVerifyPin(): Promise<void> {
  if (pin.value.length < 4) return

  pinVerifying.value = true
  pinError.value = ''

  try {
    const result = await verifyPin(pin.value)
    if (result.success) {
      pin.value = ''
      pinError.value = ''
      currentView.value = 'registration'
    } else {
      if (result.errorKey === 'invalid-pin') {
        pinError.value = t('views.home.childSelection.pinVerification.errorInvalid')
      } else if (result.errorKey === 'connection') {
        pinError.value = t('views.home.childSelection.pinVerification.errorConnection')
      } else {
        pinError.value = t('views.home.childSelection.pinVerification.errorServer')
      }
      pinInputRef.value?.clear()
    }
  } catch {
    pinError.value = t('views.home.childSelection.pinVerification.errorServer')
    pinInputRef.value?.clear()
  } finally {
    pinVerifying.value = false
  }
}

function handleCancelPin(): void {
  resetToSelection()
}

function handleNextStep(): void {
  if (childName.value.trim()) {
    registrationStep.value = 1
  }
}

function handleBackStep(): void {
  registrationStep.value = 0
}

function handleCancelRegistration(): void {
  resetToSelection()
}

async function handleConfirmRegistration(): Promise<void> {
  if (!canConfirm.value) return

  submitting.value = true
  submitError.value = ''

  try {
    const result = await createChild({
      name: childName.value.trim(),
      birthday: childBirthday.value,
      avatar: childAvatar.value,
      ttsEnabled: true,
      agentEnabled: true,
      colorVisionMode: null
    })

    if (result.success) {
      await fetchProfiles()
      resetToSelection()
    } else {
      if (result.errorKey === 'validation') {
        submitError.value = result.errorMessage || t('views.home.childSelection.registration.errorValidation')
      } else if (result.errorKey === 'conflict') {
        submitError.value = t('views.home.childSelection.registration.errorConflict')
      } else if (result.errorKey === 'connection') {
        submitError.value = t('views.home.childSelection.registration.errorConnection')
      } else {
        submitError.value = t('views.home.childSelection.registration.errorServer')
      }
    }
  } catch {
    submitError.value = t('views.home.childSelection.registration.errorServer')
  } finally {
    submitting.value = false
  }
}

watch(currentView, async () => {
  await nextTick()
  if (currentView.value === 'pin-verification') {
    pinInputRef.value?.focus()
  } else if (currentView.value === 'registration') {
    focusCurrentStep()
  }
})

watch(registrationStep, async () => {
  await nextTick()
  focusCurrentStep()
})

function focusCurrentStep(): void {
  if (registrationStep.value === 0) {
    nameInputRef.value?.focus()
  } else if (registrationStep.value === 1) {
    dateInputRef.value?.focus()
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.child-selection-modal__content {
  min-height: 200px;
}

.child-selection-modal__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--nubi-spacing-xl) 0;
}

.child-selection-modal__empty-text {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  text-align: center;
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.child-selection-modal__grid-wrapper {
  padding: var(--nubi-spacing-sm) 0;
}

.child-selection-modal__pin-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-md) 0;
}

.child-selection-modal__description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  text-align: center;
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.child-selection-modal__registration-view {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.child-selection-modal__stepper {
  width: 100%;
}

:deep(.nubi-stepper__footer) {
  display: none;
}

.child-selection-modal__step-content {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-lg);
  padding: var(--nubi-spacing-md) 0;
}

.child-selection-modal__field {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.child-selection-modal__field-label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.child-selection-modal__field-required {
  color: var(--nubi-color-error);
  margin-left: var(--nubi-spacing-xs);
}

.child-selection-modal__date-input {
  width: 100%;
  min-height: 48px;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  border: var(--nubi-border-width-thick) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  background-color: var(--nubi-bg-surface);
  font-size: var(--nubi-font-size-base);
  font-family: var(--nubi-font-family-base);
  color: var(--nubi-text-primary);
  outline: none;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.child-selection-modal__date-input:focus {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.child-selection-modal__date-input[aria-invalid="true"] {
  border-color: var(--nubi-color-error);
}

.child-selection-modal__field-error {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-error);
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
}

.child-selection-modal__error-message {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-error);
  text-align: center;
  padding: var(--nubi-spacing-sm) 0;
}

.child-selection-modal__footer {
  display: flex;
  justify-content: center;
}

.child-selection-modal__footer--actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--nubi-spacing-sm);
}

.child-selection-modal__footer-right {
  display: flex;
  gap: var(--nubi-spacing-sm);
}

.nubi-fade-enter-active {
  transition: opacity var(--nubi-duration-normal) var(--nubi-ease-out);
}

.nubi-fade-leave-active {
  transition: opacity var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-fade-enter-from,
.nubi-fade-leave-to {
  opacity: 0;
}

@media (max-width: 480px) {
  .child-selection-modal__footer--actions {
    flex-direction: column;
    gap: var(--nubi-spacing-sm);
  }

  .child-selection-modal__footer-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
